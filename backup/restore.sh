#!/bin/sh
# Відновлення БД з копії. Запуск: docker compose exec backup /scripts/restore.sh /backups/db-YYYYMMDD-HHMMSS.dump
# УВАГА: наявні дані в БД буде замінено.
set -eu
[ $# -eq 1 ] || { echo "Використання: restore.sh <файл.dump>"; exit 1; }
export PGPASSWORD="${POSTGRES_PASSWORD}"
pg_restore -h db -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" --clean --if-exists --no-owner "$1"
echo "Відновлено з $1"
