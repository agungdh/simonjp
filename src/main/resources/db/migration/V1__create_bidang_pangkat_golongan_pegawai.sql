-- Tabel master bidang dan pangkat golongan, serta pegawai.
-- Semua tabel memakai audit standar BaseEntity: id, uuid, *_at, *_by, deleted_*.
-- Kolom audit bersifat opsional/informasional (untuk penelusuran langsung di database) dan tidak
-- punya default: baris hasil impor aplikasi lama boleh NULL, artinya "tidak diketahui".

CREATE TABLE bidang (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    bidang      VARCHAR(255) NOT NULL,

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);

CREATE INDEX idx_bidang_uuid ON bidang USING hash (uuid);
CREATE UNIQUE INDEX ux_bidang_bidang_active ON bidang (lower(bidang)) WHERE deleted_at IS NULL;

CREATE TABLE pangkat_golongan (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    jenjang     VARCHAR(50) NOT NULL,
    pangkat     VARCHAR(100) NOT NULL,
    golongan    VARCHAR(10) NOT NULL,
    ruang       VARCHAR(10) NOT NULL,

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);

CREATE INDEX idx_pangkat_golongan_uuid ON pangkat_golongan USING hash (uuid);
CREATE UNIQUE INDEX ux_pangkat_golongan_kode_active
    ON pangkat_golongan (golongan, ruang) WHERE deleted_at IS NULL;

CREATE TABLE pegawai (
    id                                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid                                UUID NOT NULL,
    nip                                 VARCHAR(30) NOT NULL,
    nama                                VARCHAR(255) NOT NULL,
    tipe                                VARCHAR(20) NOT NULL DEFAULT 'PEGAWAI',
    status                              VARCHAR(20) NOT NULL DEFAULT 'AKTIF',
    jabatan                             VARCHAR(255),
    peran                               VARCHAR(255),
    kategori_jabatan                    VARCHAR(30),
    kategori_kebutuhan_jam_pelatihan     VARCHAR(20),
    bidang_id                           BIGINT NOT NULL REFERENCES bidang (id),
    pangkat_golongan_id                 BIGINT REFERENCES pangkat_golongan (id),

    created_at                          TIMESTAMPTZ,
    created_by                          BIGINT,
    updated_at                          TIMESTAMPTZ,
    updated_by                          BIGINT,
    deleted_at                          TIMESTAMPTZ,
    deleted_by                          BIGINT,

    CONSTRAINT ck_pegawai_tipe CHECK (tipe IN ('PEGAWAI', 'ADMIN')),
    CONSTRAINT ck_pegawai_status CHECK (status IN ('AKTIF', 'NON_AKTIF')),
    CONSTRAINT ck_pegawai_kategori_jabatan CHECK (
        kategori_jabatan IN ('STRUKTURAL', 'FUNGSIONAL_AUDITOR', 'FUNGSIONAL_TERTENTU')
    ),
    CONSTRAINT ck_pegawai_kategori_kebutuhan_jam_pelatihan CHECK (
        kategori_kebutuhan_jam_pelatihan IN ('ADMIN', 'PEJABAT', 'AUDITOR')
    )
);

CREATE INDEX idx_pegawai_uuid ON pegawai USING hash (uuid);
CREATE INDEX idx_pegawai_bidang_id ON pegawai (bidang_id);
CREATE INDEX idx_pegawai_pangkat_golongan_id ON pegawai (pangkat_golongan_id);
CREATE UNIQUE INDEX ux_pegawai_nip_active ON pegawai (nip) WHERE deleted_at IS NULL;
