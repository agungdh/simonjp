CREATE TABLE materi_ppm (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    nomor_surat VARCHAR(255),
    nama_pemateri VARCHAR(255),
    materi_pengembangan VARCHAR(255) NOT NULL,
    tanggal_pelaksanaan DATE NOT NULL,
    link_materi VARCHAR(500),
    link_dokumentasi VARCHAR(500),

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);
CREATE INDEX idx_materi_ppm_uuid ON materi_ppm USING hash (uuid);
CREATE INDEX idx_materi_ppm_tanggal_pelaksanaan ON materi_ppm (tanggal_pelaksanaan);
