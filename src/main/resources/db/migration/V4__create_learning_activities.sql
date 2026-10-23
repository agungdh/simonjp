CREATE TABLE seminar (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    pegawai_id  BIGINT NOT NULL REFERENCES pegawai(id),
    materi_pengembangan VARCHAR(255) NOT NULL,
    tanggal_pelaksanaan DATE NOT NULL,
    jumlah_jam  INTEGER NOT NULL,
    filename    VARCHAR(255),

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);
CREATE INDEX idx_seminar_uuid ON seminar USING hash (uuid);
CREATE INDEX idx_seminar_pegawai_id ON seminar (pegawai_id);
CREATE INDEX idx_seminar_tanggal_pelaksanaan ON seminar (tanggal_pelaksanaan);

CREATE TABLE webinar (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    pegawai_id  BIGINT NOT NULL REFERENCES pegawai(id),
    materi_pengembangan VARCHAR(255) NOT NULL,
    tanggal_pelaksanaan DATE NOT NULL,
    jumlah_jam  INTEGER NOT NULL,
    filename    VARCHAR(255),

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);
CREATE INDEX idx_webinar_uuid ON webinar USING hash (uuid);
CREATE INDEX idx_webinar_pegawai_id ON webinar (pegawai_id);
CREATE INDEX idx_webinar_tanggal_pelaksanaan ON webinar (tanggal_pelaksanaan);

CREATE TABLE lc (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    pegawai_id  BIGINT NOT NULL REFERENCES pegawai(id),
    materi_pengembangan VARCHAR(255) NOT NULL,
    tanggal_pelaksanaan DATE NOT NULL,
    jumlah_jam  INTEGER NOT NULL,
    filename    VARCHAR(255),

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);
CREATE INDEX idx_lc_uuid ON lc USING hash (uuid);
CREATE INDEX idx_lc_pegawai_id ON lc (pegawai_id);
CREATE INDEX idx_lc_tanggal_pelaksanaan ON lc (tanggal_pelaksanaan);

CREATE TABLE belajar_mandiri (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    pegawai_id  BIGINT NOT NULL REFERENCES pegawai(id),
    materi_pengembangan VARCHAR(255) NOT NULL,
    tanggal_pelaksanaan DATE NOT NULL,
    jumlah_jam  INTEGER NOT NULL,
    filename    VARCHAR(255),

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);
CREATE INDEX idx_belajar_mandiri_uuid ON belajar_mandiri USING hash (uuid);
CREATE INDEX idx_belajar_mandiri_pegawai_id ON belajar_mandiri (pegawai_id);
CREATE INDEX idx_belajar_mandiri_tanggal_pelaksanaan ON belajar_mandiri (tanggal_pelaksanaan);

CREATE TABLE mentoring (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    pegawai_id  BIGINT NOT NULL REFERENCES pegawai(id),
    materi_pengembangan VARCHAR(255) NOT NULL,
    tanggal_pelaksanaan DATE NOT NULL,
    jumlah_jam  INTEGER NOT NULL,
    filename    VARCHAR(255),

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);
CREATE INDEX idx_mentoring_uuid ON mentoring USING hash (uuid);
CREATE INDEX idx_mentoring_pegawai_id ON mentoring (pegawai_id);
CREATE INDEX idx_mentoring_tanggal_pelaksanaan ON mentoring (tanggal_pelaksanaan);

CREATE TABLE coaching (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    pegawai_id  BIGINT NOT NULL REFERENCES pegawai(id),
    materi_pengembangan VARCHAR(255) NOT NULL,
    tanggal_pelaksanaan DATE NOT NULL,
    jumlah_jam  INTEGER NOT NULL,
    filename    VARCHAR(255),

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);
CREATE INDEX idx_coaching_uuid ON coaching USING hash (uuid);
CREATE INDEX idx_coaching_pegawai_id ON coaching (pegawai_id);
CREATE INDEX idx_coaching_tanggal_pelaksanaan ON coaching (tanggal_pelaksanaan);

CREATE TABLE workshop (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    pegawai_id  BIGINT NOT NULL REFERENCES pegawai(id),
    materi_pengembangan VARCHAR(255) NOT NULL,
    tanggal_pelaksanaan DATE NOT NULL,
    jumlah_jam  INTEGER NOT NULL,
    filename    VARCHAR(255),

    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);
CREATE INDEX idx_workshop_uuid ON workshop USING hash (uuid);
CREATE INDEX idx_workshop_pegawai_id ON workshop (pegawai_id);
CREATE INDEX idx_workshop_tanggal_pelaksanaan ON workshop (tanggal_pelaksanaan);
