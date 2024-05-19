drop table users;

CREATE TABLE users
(
    _id            CHAR(56) PRIMARY KEY,
    first_name    VARCHAR(255),
    last_name     VARCHAR(255),
    email         VARCHAR(255) UNIQUE,
    pin_code      VARCHAR(10),
    phone_number  VARCHAR(20),
    avatar        VARCHAR(255),
    user_role     ENUM('MANAGER', 'AGENT'),
    whats_app_url VARCHAR(255),
    telegram_url  VARCHAR(255),
    instagram_url VARCHAR(255)
);