-- Flyway migration, like a Laravel migration file in database/migrations.
-- Version prefix V1__ is required. Flyway applies pending files in order at startup.

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
