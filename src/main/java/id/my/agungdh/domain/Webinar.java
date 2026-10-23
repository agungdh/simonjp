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
@Table(name = "webinar")
public class Webinar extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pegawai_id", nullable = false)
    private Pegawai pegawai;

    @Column(name = "materi_pengembangan", nullable = false, length = 255)
    private String materiPengembangan;

    @Column(name = "tanggal_pelaksanaan", nullable = false)
    private LocalDate tanggalPelaksanaan;

    @Column(name = "jumlah_jam", nullable = false)
    private Integer jumlahJam;

    @Column(name = "filename", length = 255)
    private String filename;
}
