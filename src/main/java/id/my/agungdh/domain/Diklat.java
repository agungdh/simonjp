package id.my.agungdh.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "diklat")
public class Diklat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pegawai_id", nullable = false)
    private Pegawai pegawai;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jenis_pelatihan_id", nullable = false)
    private JenisPelatihan jenisPelatihan;

    @Column(name = "nomor_surat", nullable = false, length = 255)
    private String nomorSurat;

    @Column(name = "materi_pengembangan", nullable = false, length = 255)
    private String materiPengembangan;

    @Column(name = "dari_tanggal_pelaksanaan", nullable = false)
    private LocalDate dariTanggalPelaksanaan;

    @Column(name = "sampai_tanggal_pelaksanaan", nullable = false)
    private LocalDate sampaiTanggalPelaksanaan;

    @Column(name = "jumlah_jam_pelatihan", nullable = false)
    private Integer jumlahJamPelatihan;

    @Column(name = "filename", length = 255)
    private String filename;
}
