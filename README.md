# Інформаційна система управління гуртожитком

Навчальний командний проєкт з дисципліни «Життєвий цикл розробки програмного забезпечення»
(НТУУ «КПІ ім. Ігоря Сікорського», ФІОТ, група ІО-44).
Цей репозиторій містить **базову структуру коду** (лабораторна робота № 4) на основі
специфікації вимог (ЛР 1), архітектури (ЛР 2) та специфікації БД.

## Архітектура коротко

Серверна частина — **модульний моноліт** на Java 21 + Spring Boot 3.1: модулі працюють в одному процесі
та спільній PostgreSQL, що дозволяє виконувати поселення в одній транзакції. Клієнт — JavaScript (ES-модулі) + Tailwind CSS,
спілкується із сервером через REST (`/api`, JSON, JWT). Схема БД змінюється лише міграціями Flyway.

Шари кожного модуля: `Controller` (HTTP, DTO) → `Service` (права, бізнес-правила, транзакції) → `Repository` (JPA).

## Структура репозиторію

```
.
├── backend/                         Spring Boot (Maven)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/java/ua/kpi/dormitory/
│       │   ├── common/              ApiException, ErrorResponse, GlobalExceptionHandler
│       │   ├── account/             Role, Account, UserSession, PasswordResetToken, AccountService
│       │   ├── security/            JWT, фільтр, SecurityConfig, AuthService, AccessPolicy
│       │   ├── profile/             ResidentProfile, DormitoryManager, ManagerAssignmentService
│       │   ├── inventory/           Dormitory, Room, Bed, генератори планування, RoomStatusCalculator
│       │   ├── settlement/          заяви, договори, SettlementService (поселення / виселення)
│       │   ├── payment/             Payment, FileStorage (абстракція), PaymentService
│       │   ├── repair/              RepairRequest, RepairStatusPolicy, RepairService
│       │   ├── announcement/        Announcement, AnnouncementService
│       │   └── report/              читання списків (проживаючі, вільні місця)
│       ├── main/resources/
│       │   ├── application.yml
│       │   └── db/migration/        V1__init_schema.sql, V2__seed_roles.sql
│       └── test/java/               модульні тести чистої логіки
├── frontend/                        SPA на ES-модулях (index.html, js/, views/)
├── nginx/default.conf               статика + проксі /api → backend
├── docker-compose.yml               db + backend + web
├── docs/                            документація (git-workflow, стан реалізації)
├── scripts/setup-branches.sh        створення робочих гілок команди
├── .env.example                     шаблон змінних середовища
└── .gitignore
```

## Запуск

Потрібні: Docker 24+ з Compose.

```bash
cp .env.example .env          # задайте POSTGRES_PASSWORD і JWT_SECRET (мінімум 32 символи)
docker compose up --build
```

- Вебінтерфейс: http://localhost
- Swagger UI: http://localhost/swagger-ui.html (документація API через Springdoc OpenAPI)
- Перший адміністратор у БД не створюється автоматично; створіть запис в `accounts` із роллю `ROLE_ADMIN` та BCrypt-хешем пароля (див. `docs/implementation-status.md`).

Локально без Docker: PostgreSQL 15 на `localhost:5432` (БД `dormitory`), далі

```bash
cd backend
mvn spring-boot:run
mvn test
```

## Ролі та права

| Роль | Основні дії в API |
| --- | --- |
| `ROLE_ADMIN` | створення гуртожитків, генерація кімнат, створення акаунтів, призначення завідуючих, оголошення |
| `ROLE_MANAGER` | поселення/виселення, обробка ремонтних заявок, перевірка оплат, оголошення, звіти — лише для закріплених гуртожитків |
| `ROLE_RESIDENT` | власні заяви, ремонтні заявки, квитанції, перегляд карти та оголошень свого корпусу |

## Модулі та вимоги

| Модуль | Вимоги | Стан у цій версії |
| --- | --- | --- |
| security / account | FR-19, FR-20, NFR-04 | вхід, JWT + перевірка `user_sessions`, вихід, створення акаунтів |
| inventory | FR-01, FR-02, FR-04, FR-05 | створення гуртожитку, генерація кімнат і місць, карта з кольоровим статусом |
| profile | FR-03 | призначення завідуючої; редагування профілю (FR-09) — не реалізовано |
| settlement | FR-06, FR-07, FR-08 | заява, поселення, виселення; переселення — заглушка (HTTP 501) |
| payment | FR-16 | подання квитанції, верифікація; завантаження файлу через `FileStorage` |
| repair | FR-13–FR-15 | створення, перегляд, зміна статусу за діаграмою станів |
| announcement | FR-11, FR-12 | публікація та перегляд актуальних |
| report | FR-18 (частково) | JSON-списки проживаючих і вільних місць; експорт Excel/PDF — не реалізовано |
| пошук | FR-17 | не реалізовано |

## Принципи SOLID у коді

- **S** — контролер лише приймає запит, сервіс виконує правила, репозиторій працює з БД; `RepairStatusPolicy` і `RoomStatusCalculator` винесено в окремі класи.
- **O** — новий тип планування = нова реалізація `RoomLayoutGenerator`, `RoomGenerationService` не змінюється.
- **L** — усі реалізації `RoomLayoutGenerator` та `FileStorage` взаємозамінні за контрактом інтерфейсу.
- **I** — вузькі інтерфейси: `RoomLayoutGenerator` (2 методи), `FileStorage` (2 методи), окремі репозиторії по таблицях.
- **D** — `RoomGenerationService` залежить від `RoomLayoutGenerator`, `PaymentService` — від `FileStorage`, а не від конкретних класів.

## Команда та ролі

| Учасник | Роль | Гілки |
| --- | --- | --- |
| Якубін Олександр | Java Backend Developer | `feature/security-accounts`, `feature/settlement` |
| Місюрко Мар'ян | DevOps / Fullstack | `feature/devops-db`, `feature/inventory`, `feature/repair-announcements` |
| Чорний Ігор | Frontend Developer | `feature/frontend` |
| Михаць Роман | QA Engineer / PM | `feature/tests-qa`, оновлення документації, код-рев'ю |

Розподіл гілок — пропозиція на основі ролей з ЛР 1; команда може його скоригувати.

## Робота з Git

Короткий опис — у [docs/git-workflow.md](docs/git-workflow.md): головна гілка `main` захищена, кожне завдання — у власній гілці
`feature/*`, злиття лише через Pull Request після код-рев'ю щонайменше одного іншого учасника.
