CREATE TABLE kategori (
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

CREATE INDEX idx_kategori_uuid ON kategori USING hash (uuid);

CREATE UNIQUE INDEX ux_kategori_nama_active ON kategori (lower(nama)) WHERE deleted_at IS NULL;

ALTER TABLE product
    ADD COLUMN kategori_id BIGINT REFERENCES kategori(id);

CREATE INDEX idx_product_kategori_id ON product (kategori_id);