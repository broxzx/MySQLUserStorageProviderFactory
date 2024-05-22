drop table users;
drop table companies;


create table companies
(
    _id                 binary(16) PRIMARY KEY,
    name                varchar(255) not null unique,
    company_code        varchar(255) not null unique,
    company_description text,
    amount_of_users     int,
    company_logo        varchar(255),
    lat                 varchar(255),
    lng                 varchar(255),
    pre_paid            timestamp
);

create table users
(
    _id           binary(16) PRIMARY KEY,
    first_name    varchar(32)  not null,
    last_name     varchar(64)  not null,
    email         varchar(128) not null,
    company_id    binary(16),
    pin_code      varchar(255),
    phone_number  varchar(20),
    avatar        varchar(255),
    user_role     varchar(64),
    whats_app_url varchar(255),
    telegram_url  varchar(255),
    instagram_url varchar(255)
);



delete
from users
where true;
delete
from companies
where true;

INSERT INTO companies (_id, name, company_code, company_description, amount_of_users, company_logo, lat, lng, pre_paid)
VALUES (UNHEX('25850d433f704e5a9b76bbbfce7a64c7'), 'Cabrera, Page and Saunders', 'ik78985', 'User-centric leadingedge concept', 608,
        'https://placekitten.com/577/334', '10.1097735', '105.286252', '2024-01-09 15:13:30'),
       (UNHEX('4c951afdbc074d3ca93058df86af94f3'), 'Kim, Saunders and Rowland', 'ue17346', 'Persevering context-sensitive array', 277,
        'https://placekitten.com/532/532', '-5.928941', '-35.673291', '2024-05-04 20:12:36');

INSERT INTO users (_id, first_name, last_name, email, company_id, pin_code, phone_number, avatar, user_role, whats_app_url, telegram_url, instagram_url)
VALUES (UNHEX('c0b516bc67cc4aef8bcb1877c96438e2'), 'Dawn', 'Gonzalez', 'dawn.gonzalez@example.com', UNHEX('25850d433f704e5a9b76bbbfce7a64c7'), '4faw4w',
        '001-306-882-4922', 'https://dummyimage.com/72x592', 'manager', 'http://example.com/whatsapp', 'http://example.com/telegram',
        'http://example.com/instagram'),
       (UNHEX('d7c9cb7f8eb04e1cbe7a64a2f9f8943a'), 'Adam', 'Nichols', 'adam.nichols@example.com', UNHEX('4c951afdbc074d3ca93058df86af94f3'), 'z7u7hq',
        '4977524201', 'https://placeimg.com/895/815/any', 'manager', 'http://example.com/whatsapp', 'http://example.com/telegram',
        'http://example.com/instagram');
