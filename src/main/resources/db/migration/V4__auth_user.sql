CREATE TABLE user_account (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid          UUID NOT NULL,
    username      VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20) NOT NULL,
    pegawai_id    BIGINT NOT NULL,

    created_at    TIMESTAMPTZ,
    created_by    BIGINT,
    updated_at    TIMESTAMPTZ,
    updated_by    BIGINT,
    deleted_at    TIMESTAMPTZ,
    deleted_by    BIGINT,

    CONSTRAINT fk_user_account_pegawai_id FOREIGN KEY (pegawai_id) REFERENCES pegawai(id),
    CONSTRAINT ck_user_account_role CHECK (role IN ('ADMIN', 'OPERATOR', 'VIEWER'))
);

CREATE INDEX idx_user_account_uuid ON user_account USING hash (uuid);

CREATE INDEX idx_user_account_pegawai_id ON user_account (pegawai_id);

CREATE UNIQUE INDEX ux_user_account_username_active ON user_account (lower(username)) WHERE deleted_at IS NULL;

-- Satu pegawai hanya boleh punya satu akun aktif; nilai bisa dipakai ulang setelah soft delete.
CREATE UNIQUE INDEX ux_user_account_pegawai_id_active ON user_account (pegawai_id) WHERE deleted_at IS NULL;
