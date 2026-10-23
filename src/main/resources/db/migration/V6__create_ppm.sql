CREATE TABLE ppm (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    pegawai_id  BIGINT NOT NULL REFERENCES pegawai(id),
    nomor_surat VARCHAR(255) NOT NULL,
    materi_pengembangan VARCHAR(255) NOT NULL,
    tanggal_pelaksanaan DATE NOT NULL,
    jumlah_jam_pelatihan INTEGER NOT NULL,

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);
CREATE INDEX idx_ppm_uuid ON ppm USING hash (uuid);
CREATE INDEX idx_ppm_pegawai_id ON ppm (pegawai_id);
CREATE INDEX idx_ppm_tanggal_pelaksanaan ON ppm (tanggal_pelaksanaan);
