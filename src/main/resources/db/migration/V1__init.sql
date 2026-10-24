CREATE TABLE product (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    nama        VARCHAR(255) NOT NULL,
    deskripsi   TEXT,
    harga       NUMERIC(19,2) NOT NULL,
    stok        INTEGER NOT NULL,

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);

CREATE INDEX idx_product_uuid ON product USING hash (uuid);

CREATE UNIQUE INDEX ux_product_nama_active ON product (nama) WHERE deleted_at IS NULL;