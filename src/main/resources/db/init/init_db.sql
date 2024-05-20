drop table users;

CREATE TABLE users
(
    _id            CHAR(56) PRIMARY KEY,
    first_name    VARCHAR(255),
    last_name     VARCHAR(255),
    email         VARCHAR(255) UNIQUE,
    pin_code      VARCHAR(255),
    user_role     ENUM('MANAGER', 'AGENT')
);

ALTER TABLE users MODIFY pin_code VARCHAR(255);