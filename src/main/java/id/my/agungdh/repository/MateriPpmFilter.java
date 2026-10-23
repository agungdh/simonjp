package id.my.agungdh.repository;

import java.time.LocalDate;

public record MateriPpmFilter(
        String search,
        LocalDate dariTanggal,
        LocalDate sampaiTanggal,
        Integer tahun) {
}
