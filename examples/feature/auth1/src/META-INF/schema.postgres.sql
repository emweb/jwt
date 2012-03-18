create table "abstract_user" (
  "id" integer primary key,
  "version" integer not null
);

create table "auth_info" (
  "id" integer primary key,
  "version" integer not null,
  "user_id" bigint,
  "password_hash" varchar(100) not null,
  "password_method" varchar(20) not null,
  "password_salt" varchar(50) not null,
  "status" integer not null,
  "failed_login_attempts" integer not null,
  "last_login_attempt" date,
  "email" varchar(256) not null,
  "unverified_email" varchar(256) not null,
  "email_token" varchar(64) not null,
  "email_token_expires" date,
  "email_token_role" integer not null,
  constraint "fk_auth_info_user" foreign key ("user_id") references "abstract_user" ("id")
);

create table "auth_token" (
  "id" integer primary key,
  "version" integer not null,
  "auth_info_id" bigint,
  "value" varchar(64) not null,
  "expiry_date" date,
  constraint "fk_auth_token_auth_info" foreign key ("auth_info_id") references "auth_info" ("id")
);

create table "auth_identity" (
  "id" integer primary key ,
  "version" integer not null,
  "auth_info_id" bigint,
  "provider" varchar(64) not null,
  "identity" varchar(512) not null,
  constraint "fk_auth_identity_auth_info" foreign key ("auth_info_id") references "auth_info" ("id")
);

create sequence hibernate_sequence;


