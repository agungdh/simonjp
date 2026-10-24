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

CREATE TABLE pangkat_golongan (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    jenjang     VARCHAR(100) NOT NULL,
    pangkat     VARCHAR(100) NOT NULL,
    golongan    VARCHAR(20) NOT NULL,
    ruang       VARCHAR(20),

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);

CREATE INDEX idx_pangkat_golongan_uuid ON pangkat_golongan USING hash (uuid);

-- ruang boleh NULL, jadi dipakai coalesce supaya kombinasi tetap unik.
CREATE UNIQUE INDEX ux_pangkat_golongan_nama_active
    ON pangkat_golongan (lower(pangkat), lower(golongan), lower(coalesce(ruang, '')))
    WHERE deleted_at IS NULL;

CREATE TABLE pegawai (
    id                               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid                             UUID NOT NULL,
    nama                             VARCHAR(255) NOT NULL,
    nip                              VARCHAR(50) NOT NULL,
    email                            VARCHAR(255),
    bidang_id                        BIGINT REFERENCES bidang(id),
    pangkat_golongan_id              BIGINT REFERENCES pangkat_golongan(id),
    jabatan                          VARCHAR(255),
    peran                            VARCHAR(255),
    status                           VARCHAR(20) NOT NULL DEFAULT 'AKTIF',
    kategori_jabatan                 VARCHAR(50),
    kategori_kebutuhan_jam_pelatihan VARCHAR(20),

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT,

    CONSTRAINT ck_pegawai_status CHECK (status IN ('AKTIF', 'NON_AKTIF')),
    CONSTRAINT ck_pegawai_kategori_jabatan CHECK (
        kategori_jabatan IN ('STRUKTURAL', 'FUNGSIONAL_AUDITOR', 'FUNGSIONAL_TERTENTU')
    ),
    CONSTRAINT ck_pegawai_kategori_jam CHECK (
        kategori_kebutuhan_jam_pelatihan IN ('ADMIN', 'PEJABAT', 'AUDITOR')
    )
);

CREATE INDEX idx_pegawai_uuid ON pegawai USING hash (uuid);

CREATE INDEX idx_pegawai_bidang_id ON pegawai (bidang_id);

CREATE INDEX idx_pegawai_pangkat_golongan_id ON pegawai (pangkat_golongan_id);

-- NIP hanya unik untuk baris aktif, sehingga bisa dipakai ulang setelah soft delete.
CREATE UNIQUE INDEX ux_pegawai_nip_active ON pegawai (lower(nip)) WHERE deleted_at IS NULL;
