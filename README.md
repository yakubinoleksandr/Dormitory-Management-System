# Інформаційна система управління гуртожитком

Навчальний командний проєкт з дисципліни «Життєвий цикл розробки програмного забезпечення»
(НТУУ «КПІ ім. Ігоря Сікорського», ФІОТ, група ІО-44). Репозиторій містить інтегровану систему:
сервер (Java 21, Spring Boot), клієнт (JavaScript ES-модулі + Tailwind CSS), PostgreSQL, резервне копіювання
та конфігурацію запуску в Docker.

## Архітектура коротко

Серверна частина — **модульний моноліт**: модулі працюють в одному процесі та спільній PostgreSQL, тому поселення
виконується в одній транзакції. Клієнт спілкується із сервером через REST (`/api`, JSON, JWT). Схема БД змінюється
лише міграціями Flyway. Шари кожного модуля: `Controller` (HTTP, DTO) → `Service` (права, правила, транзакції) → `Repository` (JPA).

```
Браузер ──HTTPS──> nginx ──/api──> Spring Boot (модулі) ──JDBC──> PostgreSQL
                     │                    └── файли квитанцій (том receipts)
                     └── статичний клієнт (frontend/)        backup ──pg_dump──> ./backups
```

## Структура репозиторію

```
.
├── backend/                     Spring Boot (Maven)
│   ├── pom.xml                  залежності з версіями у <properties>
│   └── src/
│       ├── main/java/ua/kpi/dormitory/
│       │   ├── common/          ApiException, GlobalExceptionHandler
│       │   ├── account/         Role, Account, UserSession, AccountService, AdminBootstrap
│       │   ├── security/        JWT, фільтр, SecurityConfig, AuthService, AccessPolicy
│       │   ├── profile/         ResidentProfile, ProfileService, призначення завідуючих
│       │   ├── inventory/       Dormitory, Room, Bed, генератори планування, карта кімнат
│       │   ├── settlement/      заяви, договори, поселення / виселення / переселення
│       │   ├── cabinet/         особистий кабінет мешканця
│       │   ├── payment/         оплати, квитанції, FileStorage
│       │   ├── repair/          ремонтні заявки, RepairStatusPolicy
│       │   ├── announcement/    оголошення
│       │   ├── search/          глобальний пошук
│       │   └── report/          списки та експорт Excel / PDF
│       ├── main/resources/      application.yml, db/migration (V1–V3), fonts/
│       └── test/java/           unit-тести та інтеграційний SettlementFlowIT
├── frontend/                    SPA: index.html, js/ (api, auth, router, ui), js/views/
├── backup/                      backup.sh (щодня о 03:00), restore.sh
├── nginx/default.conf
├── docker-compose.yml           db + backend + backup + web
├── docs/                        git-workflow, requirements-traceability, performance-check, postman/
├── scripts/                     smoke_test.py, setup-branches.sh
├── .env.example
└── README.md
```

## Запуск

Потрібні Docker 24+ з Compose.

```bash
cp .env.example .env     # задайте POSTGRES_PASSWORD, JWT_SECRET (від 32 символів), ADMIN_PASSWORD
docker compose up --build
```

- Вебінтерфейс: http://localhost
- Swagger UI: http://localhost/swagger-ui.html
- Вхід: логін і пароль із `ADMIN_USERNAME` / `ADMIN_PASSWORD` у `.env`. Адміністратора створює `AdminBootstrap` при першому старті, якщо в БД ще немає жодного.
- БД створюється контейнером `db`, схему і ролі застосовує Flyway (`V1__init_schema`, `V2__seed_roles`, `V3__indexes`).

### Порядок першого налаштування в системі

1. Адміністратор: «Конфігурація» → створити гуртожиток → згенерувати кімнати → створити акаунт завідуючої → призначити її до гуртожитку → створити акаунти мешканців.
2. Мешканець: «Кабінет» → подати заяву на поселення.
3. Завідуюча: «Кімнати» → обрати кімнату й ліжко → обрати заяву → внести номер договору й дати → «Поселити».

### Локальний запуск без Docker

PostgreSQL 15 на `localhost:5432` (БД, користувач і пароль `dormitory`), далі `cd backend && mvn spring-boot:run`; клієнт можна віддавати будь-яким статичним сервером із проксі `/api` на `localhost:8080`.

## Тестування та перевірка інтеграції

| Що | Команда | Примітки |
| --- | --- | --- |
| Unit-тести | `cd backend && mvn test` | генератори кімнат, статуси кімнат, переходи ремонтних заявок |
| Інтеграційний сценарій | `cd backend && mvn verify` | `SettlementFlowIT` на PostgreSQL 15 через Testcontainers; потрібен Docker |
| Smoke-тест запущеної системи | `ADMIN_PASSWORD=... python3 scripts/smoke_test.py` | Python 3.9+, лише стандартна бібліотека |
| Postman | імпортуйте `docs/postman/dormitory.postman_collection.json` | запускайте Collection Runner, задайте `adminPassword` |

## Резервне копіювання (NFR-03)

Сервіс `backup` щодня о 03:00 (Europe/Kyiv) робить `pg_dump` у `./backups` і архів квитанцій, зберігає 30 останніх копій.
Негайна копія: `docker compose exec backup /scripts/backup.sh now`.
Відновлення: `docker compose exec backup /scripts/restore.sh /backups/db-<дата>.dump` (замінює дані в БД).

## Ролі та права

| Роль | Основні дії |
| --- | --- |
| `ROLE_ADMIN` | гуртожитки, генерація кімнат, акаунти, призначення завідуючих, оголошення, пошук |
| `ROLE_MANAGER` | поселення / виселення / переселення, заяви, ремонт, оплати, оголошення, пошук, звіти — лише для закріплених гуртожитків |
| `ROLE_RESIDENT` | кабінет, заява на поселення, ремонтні заявки, квитанції, перегляд карти й оголошень свого корпусу |

## Виконання вимог

Повна відповідність FR-01…FR-20 та NFR-01…NFR-09 зі станом і способом перевірки — у
[docs/requirements-traceability.md](docs/requirements-traceability.md). Коротко:

- **Функціональні:** усі 20 вимог мають реалізацію; три — частково через обмеження схеми БД: FR-10 (немає тарифів, показується сума підтверджених оплат), FR-14 (немає поля виконавця — ім'я майстра пишеться в коментар), FR-18 (список боржників неможливо побудувати без нарахувань).
- **Нефункціональні:** заходи для всіх впроваджено; час відповіді (NFR-01, NFR-02), uptime (NFR-06), відновлення з копії (NFR-03), юзабіліті (NFR-05) і браузерна сумісність (NFR-08) **ще не виміряні**.

## Керування залежностями

- Backend: `backend/pom.xml`; версії Spring Boot (3.1.12) і Flyway (9.x) успадковано від `spring-boot-starter-parent`, додаткові — у `<properties>` (jjwt, springdoc, Apache POI, OpenPDF, Testcontainers). Java 21.
- Клієнт: без збірки; Tailwind CSS 3.4.17 підключено зафіксованою версією з CDN.
- Образи: `postgres:15`, `nginx:1.27`, `maven:3.9-eclipse-temurin-21`, `eclipse-temurin:21-jre`.

## Принципи SOLID у коді

- **S** — контролер приймає запит, сервіс виконує правила, репозиторій працює з БД; правила винесено в `RoomStatusCalculator`, `RepairStatusPolicy`, `AccessPolicy`.
- **O** — новий тип планування = нова реалізація `RoomLayoutGenerator`; новий формат звіту = нова реалізація `ReportExporter`.
- **L** — реалізації `RoomLayoutGenerator`, `ReportExporter`, `FileStorage` взаємозамінні.
- **I** — вузькі інтерфейси (2–3 методи), окремий репозиторій на таблицю.
- **D** — сервіси залежать від абстракцій (`RoomLayoutGenerator`, `ReportExporter`, `FileStorage`), залежності впроваджуються через конструктор.

## Команда та ролі

| Учасник | Роль | Основні гілки |
| --- | --- | --- |
| Якубін Олександр | Java Backend Developer | `feature/security-accounts`, `feature/settlement` |
| Місюрко Мар'ян | DevOps / Fullstack | `feature/devops-db`, `feature/inventory`, `feature/repair-announcements` |
| Чорний Ігор | Frontend Developer | `feature/frontend` |
| Михаць Роман | QA Engineer / PM | `feature/tests-qa`, документація, код-рев'ю |

Робота з Git — [docs/git-workflow.md](docs/git-workflow.md): `main` захищена, зміни лише через Pull Request після код-рев'ю.
