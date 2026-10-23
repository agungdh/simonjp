-- Seed data master yang dibutuhkan pegawai. Aksi sistem memakai audit 0.

INSERT INTO bidang (uuid, bidang, created_by, updated_by)
VALUES
    (gen_random_uuid(), 'KEPALA PERWAKILAN', 0, 0),
    (gen_random_uuid(), 'KEPALA BAGIAN UMUM', 0, 0),
    (gen_random_uuid(), 'SUBBAGIAN KEPEGAWAIAN', 0, 0),
    (gen_random_uuid(), 'SUBBAGIAN KEUANGAN', 0, 0),
    (gen_random_uuid(), 'SUBBAGIAN UMUM', 0, 0),
    (gen_random_uuid(), 'BIDANG PENGAWASAN INSTANSI PEMERINTAH PUSAT', 0, 0),
    (gen_random_uuid(), 'BIDANG AKUNTABILITAS PEMERINTAH DAERAH', 0, 0),
    (gen_random_uuid(), 'BIDANG AKUNTAN NEGARA', 0, 0),
    (gen_random_uuid(), 'BIDANG INVESTIGASI', 0, 0),
    (gen_random_uuid(), 'BIDANG PROGRAM DAN PELAPORAN SERTA PEMBINAAN JFA', 0, 0);

INSERT INTO pangkat_golongan (uuid, jenjang, pangkat, golongan, ruang, created_by, updated_by)
VALUES
    (gen_random_uuid(), 'Juru', 'Juru Muda', 'I', 'a', 0, 0),
    (gen_random_uuid(), 'Juru', 'Juru Muda Tingkat I', 'I', 'b', 0, 0),
    (gen_random_uuid(), 'Juru', 'Juru', 'I', 'c', 0, 0),
    (gen_random_uuid(), 'Juru', 'Juru Tingkat I', 'I', 'd', 0, 0),
    (gen_random_uuid(), 'Pengatur', 'Pengatur Muda', 'II', 'a', 0, 0),
    (gen_random_uuid(), 'Pengatur', 'Pengatur Muda Tingkat I', 'II', 'b', 0, 0),
    (gen_random_uuid(), 'Pengatur', 'Pengatur', 'II', 'c', 0, 0),
    (gen_random_uuid(), 'Pengatur', 'Pengatur Tingkat I', 'II', 'd', 0, 0),
    (gen_random_uuid(), 'Penata', 'Penata Muda', 'III', 'a', 0, 0),
    (gen_random_uuid(), 'Penata', 'Penata Muda Tingkat I', 'III', 'b', 0, 0),
    (gen_random_uuid(), 'Penata', 'Penata', 'III', 'c', 0, 0),
    (gen_random_uuid(), 'Penata', 'Penata Tingkat I', 'III', 'd', 0, 0),
    (gen_random_uuid(), 'Pembina', 'Pembina', 'IV', 'a', 0, 0),
    (gen_random_uuid(), 'Pembina', 'Pembina Tingkat I', 'IV', 'b', 0, 0),
    (gen_random_uuid(), 'Pembina Utama', 'Pembina Utama Muda', 'IV', 'c', 0, 0),
    (gen_random_uuid(), 'Pembina Utama', 'Pembina Utama Madya', 'IV', 'd', 0, 0),
    (gen_random_uuid(), 'Pembina Utama', 'Pembina Utama', 'IV', 'e', 0, 0);
