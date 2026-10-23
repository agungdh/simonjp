CREATE TABLE app_user (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    username    VARCHAR(100) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20) NOT NULL,
    pegawai_id  BIGINT REFERENCES pegawai(id),

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT,

    CONSTRAINT ck_app_user_role CHECK (role IN ('ADMIN', 'PEGAWAI'))
);
CREATE INDEX idx_app_user_uuid ON app_user USING hash (uuid);
CREATE UNIQUE INDEX ux_app_user_username_active ON app_user (username) WHERE deleted_at IS NULL;
CREATE INDEX idx_app_user_pegawai_id ON app_user (pegawai_id);
