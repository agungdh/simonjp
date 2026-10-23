package id.my.agungdh.repository;

import java.util.UUID;

import id.my.agungdh.enums.KategoriJabatan;
import id.my.agungdh.enums.KategoriKebutuhanJamPelatihan;
import id.my.agungdh.enums.StatusPegawai;
import id.my.agungdh.enums.TipePegawai;

public record PegawaiFilter(
        String search,
        TipePegawai tipe,
        StatusPegawai status,
        UUID bidangUuid,
        UUID pangkatGolonganUuid,
        KategoriJabatan kategoriJabatan,
        KategoriKebutuhanJamPelatihan kategoriKebutuhanJamPelatihan) {
}
