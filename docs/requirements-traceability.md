# Відповідність реалізації вимогам (ЛР 1)

Стовпець «Перевірка»: **IT** — перевіряється інтеграційним тестом `SettlementFlowIT` (потребує Docker, `mvn verify`);
**smoke** — `scripts/smoke_test.py` на запущеній системі; **ручна** — вручну в браузері / Postman.
Автоматичні тести написано, але в середовищі підготовки не запускалися (немає мережі й Docker): перший запуск виконує команда.

## Функціональні вимоги

| ID | Реалізація (REST / UI) | Стан | Перевірка |
| --- | --- | --- | --- |
| FR-01 | `POST /api/dormitories`; UI «Конфігурація» | реалізовано | IT, smoke |
| FR-02 | `POST /api/dormitories/{id}/rooms/generate` (стратегії CORRIDOR / BLOCK, місць 1–4) | реалізовано | IT, unit |
| FR-03 | `POST /api/dormitories/{id}/managers` | реалізовано | IT |
| FR-04 | `GET /api/dormitories/{id}/rooms?floor=` — вільні місця по поверхах | реалізовано | IT |
| FR-05 | сітка кімнат: FREE зелений, PARTIAL жовтий, FULL червоний, REPAIR сірий (+ текст) | реалізовано | IT (статуси), ручна (кольори) |
| FR-06 | `POST /api/settlement-applications`; кабінет мешканця | реалізовано | IT, smoke |
| FR-07 | `POST /api/settlements` (атомарно: заява + договір + зайнятість) | реалізовано | IT, smoke |
| FR-08 | `PATCH /api/settlements/{id}/move-out`, `PATCH /api/settlements/{id}/bed` | реалізовано | IT |
| FR-09 | `PUT /api/residents/{id}/profile`, `PATCH /api/settlements/{id}` (терміни); статус договору — виселенням | реалізовано | IT (профіль) |
| FR-10 | `GET /api/me/cabinet`: кімната, ліжко, договір, сусіди (ПІБ), сума підтверджених оплат | частково: «баланс» = сума VERIFIED, борг не обчислюється (немає тарифів у схемі) | IT |
| FR-11 | `POST /api/announcements` | реалізовано | IT |
| FR-12 | `GET /api/announcements?dormitoryId=` (актуальні, за спаданням дати) | реалізовано | IT |
| FR-13 | `POST /api/repair-requests` (лише на власну кімнату) | реалізовано | IT, smoke |
| FR-14 | `PATCH /api/repair-requests/{id}/status` за діаграмою станів, коментар | частково: окремого поля виконавця в схемі немає — ім'я майстра пишеться в коментар | IT |
| FR-15 | `GET /api/repair-requests/mine` | реалізовано | IT |
| FR-16 | `POST /api/settlements/{id}/payments` (multipart), `PATCH /api/payments/{id}/status`, `GET /api/payments/{id}/receipt` | реалізовано | IT |
| FR-17 | `GET /api/search?q=` (ПІБ, договір, кімната, гуртожиток) у межах дозволених корпусів | реалізовано | IT, smoke |
| FR-18 | `GET /api/reports/{residents\|free-beds}/export?format=xlsx\|pdf` | частково: список боржників недоступний (немає тарифів) | IT (формат файлів) |
| FR-19 | `POST /api/auth/login`, `/logout`; JWT із роллю + перевірка `user_sessions` | реалізовано | IT |
| FR-20 | `POST /api/accounts`; перший адміністратор — `AdminBootstrap` із ADMIN_USERNAME / ADMIN_PASSWORD | реалізовано | IT |

## Нефункціональні вимоги

| ID | Заходи | Стан | Як перевірити |
| --- | --- | --- | --- |
| NFR-01 | один агрегований запит для карти + індекси `V3__indexes.sql` | заходи впроваджено, час **не вимірювано** | виміряти карту на тестових даних (див. docs/performance-check.md) |
| NFR-02 | фільтрація на сервері, індекси, ліміт 50 для пошуку | частково: пагінації списків немає, час **не вимірювано** | замір читання до 1000 записів |
| NFR-03 | сервіс `backup` (pg_dump о 03:00 Europe/Kyiv, 30 копій, копія квитанцій), `restore.sh` | реалізовано, відновлення **не перевірено** | `docker compose exec backup /scripts/backup.sh now`, потім restore в ізольованому середовищі |
| NFR-04 | Spring Security, BCrypt, JWT + `user_sessions`, `AccessPolicy`, ролі на методах, сусіди лише ПІБ | реалізовано | IT (`accessControlByRoleAndOwnership`) |
| NFR-05 | однакова навігація, колір + текст статусу | реалізовано, час навчання **не вимірювано** | випробування з новою користувачкою |
| NFR-06 | Docker Compose, томи, healthcheck, `restart: unless-stopped` | реалізовано, uptime **не вимірювано** | моніторинг за погоджений період |
| NFR-07 | структура гуртожитку — дані (dormitories/rooms/beds), без змін коду | реалізовано | IT (створення BLOCK-корпусу через API) |
| NFR-08 | адаптивний Tailwind, мінімальна ширина 360 px | реалізовано, браузери **не перевірено** | ручна перевірка Chrome/Firefox/Edge/Safari |
| NFR-09 | модульна структура, DTO, Springdoc OpenAPI (`/swagger-ui.html`), ES-модулі | реалізовано | огляд коду, Swagger |
