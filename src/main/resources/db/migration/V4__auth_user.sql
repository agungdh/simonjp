CREATE TABLE user_account (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid          UUID NOT NULL,
    username      VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nama          VARCHAR(255) NOT NULL,
    role          VARCHAR(20) NOT NULL,
    enabled       BOOLEAN NOT NULL DEFAULT TRUE,

    created_at    TIMESTAMPTZ,
    created_by    BIGINT,
    updated_at    TIMESTAMPTZ,
    updated_by    BIGINT,
    deleted_at    TIMESTAMPTZ,
    deleted_by    BIGINT,

    CONSTRAINT ck_user_account_role CHECK (role IN ('ADMIN', 'OPERATOR', 'VIEWER'))
);

CREATE INDEX idx_user_account_uuid ON user_account USING hash (uuid);

CREATE UNIQUE INDEX ux_user_account_username_active ON user_account (lower(username)) WHERE deleted_at IS NULL;
