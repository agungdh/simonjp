CREATE TABLE bidang (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    nama        VARCHAR(255) NOT NULL,
    deskripsi   TEXT,

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);

CREATE INDEX idx_bidang_uuid ON bidang USING hash (uuid);

CREATE UNIQUE INDEX ux_bidang_nama_active ON bidang (lower(nama)) WHERE deleted_at IS NULL;

CREATE TABLE pegawai (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    nama        VARCHAR(255) NOT NULL,
    nip         VARCHAR(50),
    email       VARCHAR(255),
    bidang_id   BIGINT REFERENCES bidang(id),

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);

CREATE INDEX idx_pegawai_uuid ON pegawai USING hash (uuid);

CREATE INDEX idx_pegawai_bidang_id ON pegawai (bidang_id);