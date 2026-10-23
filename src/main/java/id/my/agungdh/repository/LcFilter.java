package id.my.agungdh.repository;

import java.time.LocalDate;
import java.util.UUID;

public record LcFilter(
        String search,
        UUID pegawaiUuid,
        LocalDate dariTanggal,
        LocalDate sampaiTanggal,
        Integer tahun) {
}
