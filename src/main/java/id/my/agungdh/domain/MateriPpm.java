package id.my.agungdh.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "materi_ppm")
public class MateriPpm extends BaseEntity {

    @Column(name = "nomor_surat", length = 255)
    private String nomorSurat;

    @Column(name = "nama_pemateri", length = 255)
    private String namaPemateri;

    @Column(name = "materi_pengembangan", nullable = false, length = 255)
    private String materiPengembangan;

    @Column(name = "tanggal_pelaksanaan", nullable = false)
    private LocalDate tanggalPelaksanaan;

    @Column(name = "link_materi", length = 500)
    private String linkMateri;

    @Column(name = "link_dokumentasi", length = 500)
    private String linkDokumentasi;
}
