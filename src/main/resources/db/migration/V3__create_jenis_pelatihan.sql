CREATE TABLE jenis_pelatihan (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    jenis_pelatihan VARCHAR(255) NOT NULL,

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);

CREATE INDEX idx_jenis_pelatihan_uuid ON jenis_pelatihan USING hash (uuid);
CREATE UNIQUE INDEX ux_jenis_pelatihan_nama_active ON jenis_pelatihan (lower(jenis_pelatihan)) WHERE deleted_at IS NULL;
