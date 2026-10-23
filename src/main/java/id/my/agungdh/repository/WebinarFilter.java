package id.my.agungdh.repository;

import java.time.LocalDate;
import java.util.UUID;

public record WebinarFilter(
        String search,
        UUID pegawaiUuid,
        LocalDate dariTanggal,
        LocalDate sampaiTanggal,
        Integer tahun) {
}
