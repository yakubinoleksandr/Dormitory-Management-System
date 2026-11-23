#!/usr/bin/env python3
"""Smoke-тест інтегрованої системи через REST API (тільки стандартна бібліотека Python 3.9+).

Запуск після `docker compose up -d`:
    ADMIN_USERNAME=admin ADMIN_PASSWORD=<пароль з .env> python3 scripts/smoke_test.py
Змінні: BASE_URL (за замовчуванням http://localhost), ADMIN_USERNAME, ADMIN_PASSWORD.
Скрипт створює тестові дані з унікальним суфіксом і нічого не видаляє.
"""
import json
import os
import sys
import time
import urllib.error
import urllib.request

BASE = os.environ.get("BASE_URL", "http://localhost").rstrip("/")
ADMIN = (os.environ.get("ADMIN_USERNAME", "admin"), os.environ.get("ADMIN_PASSWORD", ""))
SUFFIX = str(int(time.time()))[-6:]

passed = failed = 0


def call(method, path, token=None, body=None, expect=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(f"{BASE}/api{path}", data=data, method=method)
    if data:
        req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            status, raw = resp.status, resp.read()
    except urllib.error.HTTPError as e:
        status, raw = e.code, e.read()
    parsed = json.loads(raw) if raw[:1] in (b"{", b"[") else raw
    if expect is not None:
        check(f"{method} {path} -> {expect}", status == expect, f"отримано {status}: {raw[:200]!r}")
    return status, parsed


def check(name, ok, detail=""):
    global passed, failed
    if ok:
        passed += 1
        print(f"  PASS  {name}")
    else:
        failed += 1
        print(f"  FAIL  {name}  {detail}")


def login(user, password):
    status, body = call("POST", "/auth/login", body={"username": user, "password": password})
    if status != 200:
        sys.exit(f"Не вдалося увійти як {user}: HTTP {status}. Перевірте ADMIN_USERNAME / ADMIN_PASSWORD.")
    return body["token"]


def main():
    if not ADMIN[1]:
        sys.exit("Задайте ADMIN_PASSWORD (див. .env)")
    print(f"Smoke-тест: {BASE}")

    print("1. Адміністратор: конфігурація")
    admin = login(*ADMIN)
    _, dorm = call("POST", "/dormitories", admin, {"nameOrNumber": f"Smoke-{SUFFIX}", "address": "Київ", "totalFloors": 3, "layoutType": "BLOCK"}, 201)
    d = dorm["id"]
    _, gen = call("POST", f"/dormitories/{d}/rooms/generate", admin, {"floors": 1, "blocksPerFloor": 1, "roomsPerUnit": 2, "capacity": 2}, 201)
    check("згенеровано 2 кімнати", gen.get("roomsCreated") == 2)
    mgr_user, res_user = f"mgr{SUFFIX}", f"res{SUFFIX}"
    _, mgr = call("POST", "/accounts", admin, {"username": mgr_user, "email": f"{mgr_user}@test.ua", "password": "Manager-pass-1", "role": "ROLE_MANAGER"}, 201)
    call("POST", f"/dormitories/{d}/managers", admin, {"managerAccountId": mgr["id"]}, 201)
    call("POST", "/accounts", admin, {"username": res_user, "email": f"{res_user}@test.ua", "password": "Resident-pass-1", "role": "ROLE_RESIDENT", "fullName": "Тестовий Мешканець"}, 201)

    print("2. Мешканець подає заяву, завідуюча поселяє")
    manager, resident = login(mgr_user, "Manager-pass-1"), login(res_user, "Resident-pass-1")
    _, app = call("POST", "/settlement-applications", resident, {"dormitoryId": d}, 201)
    _, rooms = call("GET", f"/dormitories/{d}/rooms", manager, expect=200)
    check("карта містить 2 кімнати зі статусом FREE", len(rooms) == 2 and rooms[0]["status"] == "FREE")
    _, beds = call("GET", f"/dormitories/{d}/rooms/{rooms[0]['id']}/beds", manager, expect=200)
    _, st = call("POST", "/settlements", manager, {"applicationId": app["id"], "bedId": beds[0]["id"], "contractNumber": f"S-{SUFFIX}", "startDate": "2026-09-01", "endDate": "2027-06-30"}, 201)
    _, rooms = call("GET", f"/dormitories/{d}/rooms", manager, expect=200)
    check("кімната стала PARTIAL", rooms[0]["status"] == "PARTIAL")
    call("POST", "/settlements", manager, {"applicationId": app["id"], "bedId": beds[1]["id"], "contractNumber": f"S2-{SUFFIX}", "startDate": "2026-09-01", "endDate": "2027-06-30"}, 409)

    print("3. Кабінет, права доступу")
    _, cab = call("GET", "/me/cabinet", resident, expect=200)
    check("кабінет показує договір", cab["placement"]["contractNumber"] == f"S-{SUFFIX}")
    call("GET", f"/reports/residents?dormitoryId={d}", resident, expect=403)
    call("GET", "/dormitories", expect=401)

    print("4. Ремонт, пошук, виселення")
    _, rep = call("POST", "/repair-requests", resident, {"roomId": rooms[0]["id"], "category": "ELECTRICITY", "description": "Не працює розетка"}, 201)
    call("PATCH", f"/repair-requests/{rep['id']}/status", manager, {"status": "COMPLETED"}, 409)
    call("PATCH", f"/repair-requests/{rep['id']}/status", manager, {"status": "IN_PROGRESS"}, 200)
    _, found = call("GET", f"/search?q=S-{SUFFIX}", manager, expect=200)
    check("пошук знаходить договір", len(found["residents"]) == 1)
    call("PATCH", f"/settlements/{st['id']}/move-out", manager, expect=200)
    _, rooms = call("GET", f"/dormitories/{d}/rooms", manager, expect=200)
    check("після виселення кімната FREE", rooms[0]["status"] == "FREE")

    print(f"\nРезультат: {passed} пройдено, {failed} провалено")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
