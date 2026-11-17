-- Tasks belong to a user (owner_id). This is a many-to-one, like a Task belongsTo User.

CREATE TABLE tasks (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255)  NOT NULL,
    description     VARCHAR(2000),
    status          VARCHAR(32)   NOT NULL,
    owner_id        BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tasks_owner_id ON tasks (owner_id);
