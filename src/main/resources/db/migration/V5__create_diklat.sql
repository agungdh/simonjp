CREATE TABLE diklat (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    pegawai_id  BIGINT NOT NULL REFERENCES pegawai(id),
    jenis_pelatihan_id BIGINT NOT NULL REFERENCES jenis_pelatihan(id),
    nomor_surat VARCHAR(255) NOT NULL,
    materi_pengembangan VARCHAR(255) NOT NULL,
    dari_tanggal_pelaksanaan DATE NOT NULL,
    sampai_tanggal_pelaksanaan DATE NOT NULL,
    jumlah_jam_pelatihan INTEGER NOT NULL,
    filename    VARCHAR(255),

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);
CREATE INDEX idx_diklat_uuid ON diklat USING hash (uuid);
CREATE INDEX idx_diklat_pegawai_id ON diklat (pegawai_id);
CREATE INDEX idx_diklat_jenis_pelatihan_id ON diklat (jenis_pelatihan_id);
CREATE INDEX idx_diklat_dari_tanggal_pelaksanaan ON diklat (dari_tanggal_pelaksanaan);
