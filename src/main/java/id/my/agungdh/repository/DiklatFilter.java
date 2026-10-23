package id.my.agungdh.repository;

import java.time.LocalDate;
import java.util.UUID;

public record DiklatFilter(
        String search,
        UUID pegawaiUuid,
        UUID jenisPelatihanUuid,
        LocalDate dariTanggal,
        LocalDate sampaiTanggal,
        Integer tahun) {
}
