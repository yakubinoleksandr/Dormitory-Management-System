-- Початкова схема за документом Dormitory_Database_Specification_v2 (14 таблиць, 18 зв'язків).
-- ENUM-поля реалізовано як VARCHAR + CHECK для сумісності з JPA (@Enumerated(STRING)).

CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE accounts (
    id            BIGSERIAL PRIMARY KEY,
    role_id       BIGINT       NOT NULL REFERENCES roles (id),
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL,
    last_login_at TIMESTAMP
);

CREATE TABLE password_reset_tokens (
    id         BIGSERIAL PRIMARY KEY,
    account_id BIGINT       NOT NULL REFERENCES accounts (id),
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP    NOT NULL,
    is_used    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL
);

CREATE TABLE user_sessions (
    id               BIGSERIAL PRIMARY KEY,
    account_id       BIGINT       NOT NULL REFERENCES accounts (id),
    session_token    VARCHAR(255) NOT NULL UNIQUE,
    last_activity_at TIMESTAMP    NOT NULL,
    expires_at       TIMESTAMP    NOT NULL,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE resident_profiles (
    id            BIGSERIAL PRIMARY KEY,
    account_id    BIGINT       NOT NULL UNIQUE REFERENCES accounts (id),
    full_name     VARCHAR(150) NOT NULL,
    course        INT,
    group_name    VARCHAR(20),
    phone         VARCHAR(20),
    benefits_info TEXT
);

CREATE TABLE dormitories (
    id             BIGSERIAL PRIMARY KEY,
    name_or_number VARCHAR(100) NOT NULL,
    address        VARCHAR(255),
    total_floors   INT,
    layout_type    VARCHAR(20)  NOT NULL CHECK (layout_type IN ('CORRIDOR', 'BLOCK'))
);

CREATE TABLE dormitory_managers (
    id           BIGSERIAL PRIMARY KEY,
    account_id   BIGINT NOT NULL REFERENCES accounts (id),
    dormitory_id BIGINT NOT NULL REFERENCES dormitories (id),
    assigned_at  TIMESTAMP
);

CREATE TABLE rooms (
    id           BIGSERIAL PRIMARY KEY,
    dormitory_id BIGINT      NOT NULL REFERENCES dormitories (id),
    floor        INT         NOT NULL,
    room_number  VARCHAR(20) NOT NULL,
    block_name   VARCHAR(20),
    capacity     INT         NOT NULL DEFAULT 4,
    status       VARCHAR(20) NOT NULL DEFAULT 'FREE' CHECK (status IN ('FREE', 'PARTIAL', 'FULL', 'REPAIR')),
    CONSTRAINT chk_rooms_capacity CHECK (capacity BETWEEN 1 AND 4)
);

CREATE TABLE beds (
    id          BIGSERIAL PRIMARY KEY,
    room_id     BIGINT  NOT NULL REFERENCES rooms (id),
    bed_number  INT     NOT NULL,
    is_occupied BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE settlement_applications (
    id                  BIGSERIAL PRIMARY KEY,
    resident_account_id BIGINT      NOT NULL REFERENCES accounts (id),
    dormitory_id        BIGINT      NOT NULL REFERENCES dormitories (id),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at          TIMESTAMP
);

CREATE TABLE settlements (
    id                        BIGSERIAL PRIMARY KEY,
    settlement_application_id BIGINT      NOT NULL UNIQUE REFERENCES settlement_applications (id),
    resident_account_id       BIGINT      NOT NULL REFERENCES accounts (id),
    bed_id                    BIGINT      NOT NULL REFERENCES beds (id),
    contract_number           VARCHAR(50) NOT NULL UNIQUE,
    start_date                DATE        NOT NULL,
    end_date                  DATE        NOT NULL,
    status                    VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'MOVED_OUT')),
    CONSTRAINT chk_settlements_dates CHECK (start_date <= end_date)
);

CREATE TABLE payments (
    id             BIGSERIAL PRIMARY KEY,
    settlement_id  BIGINT         NOT NULL REFERENCES settlements (id),
    amount         DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    payment_method VARCHAR(50),
    receipt_url    VARCHAR(255),
    paid_at        TIMESTAMP,
    status         VARCHAR(20)    NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    created_at     TIMESTAMP
);

CREATE TABLE repair_requests (
    id                  BIGSERIAL PRIMARY KEY,
    resident_account_id BIGINT      NOT NULL REFERENCES accounts (id),
    room_id             BIGINT      NOT NULL REFERENCES rooms (id),
    category            VARCHAR(20) NOT NULL CHECK (category IN ('PLUMBING', 'ELECTRICITY', 'FURNITURE')),
    description         TEXT        NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW', 'IN_PROGRESS', 'COMPLETED', 'REJECTED')),
    manager_comment     TEXT,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP
);

CREATE TABLE announcements (
    id                BIGSERIAL PRIMARY KEY,
    dormitory_id      BIGINT       NOT NULL REFERENCES dormitories (id),
    author_account_id BIGINT       NOT NULL REFERENCES accounts (id),
    title             VARCHAR(200) NOT NULL,
    content           TEXT         NOT NULL,
    expires_at        TIMESTAMP,
    created_at        TIMESTAMP
);
