-- Kolom audit bersifat informasional: hanya untuk penelusuran saat melihat langsung ke database.
-- Baris hasil impor dari aplikasi lama bisa tidak punya metadata audit, jadi semua kolom audit
-- dibuat nullable dan default database dilepas supaya NULL benar-benar berarti "tidak diketahui".
-- Aplikasi tetap mengisinya lewat AuditListener saat menulis data.

ALTER TABLE bidang
    ALTER COLUMN created_at DROP NOT NULL,
    ALTER COLUMN created_at DROP DEFAULT,
    ALTER COLUMN created_by DROP NOT NULL,
    ALTER COLUMN updated_at DROP NOT NULL,
    ALTER COLUMN updated_at DROP DEFAULT,
    ALTER COLUMN updated_by DROP NOT NULL;

ALTER TABLE pangkat_golongan
    ALTER COLUMN created_at DROP NOT NULL,
    ALTER COLUMN created_at DROP DEFAULT,
    ALTER COLUMN created_by DROP NOT NULL,
    ALTER COLUMN updated_at DROP NOT NULL,
    ALTER COLUMN updated_at DROP DEFAULT,
    ALTER COLUMN updated_by DROP NOT NULL;

ALTER TABLE pegawai
    ALTER COLUMN created_at DROP NOT NULL,
    ALTER COLUMN created_at DROP DEFAULT,
    ALTER COLUMN created_by DROP NOT NULL,
    ALTER COLUMN updated_at DROP NOT NULL,
    ALTER COLUMN updated_at DROP DEFAULT,
    ALTER COLUMN updated_by DROP NOT NULL;
