package id.my.agungdh.domain;

import id.my.agungdh.enums.KategoriJabatan;
import id.my.agungdh.enums.KategoriKebutuhanJamPelatihan;
import id.my.agungdh.enums.StatusPegawai;
import id.my.agungdh.enums.TipePegawai;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pegawai")
public class Pegawai extends BaseEntity {

    @Column(name = "nip", nullable = false, length = 30)
    private String nip;

    @Column(name = "nama", nullable = false, length = 255)
    private String nama;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipe", nullable = false, length = 20)
    private TipePegawai tipe;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusPegawai status;

    @Column(name = "jabatan", length = 255)
    private String jabatan;

    @Column(name = "peran", length = 255)
    private String peran;

    @Enumerated(EnumType.STRING)
    @Column(name = "kategori_jabatan", length = 30)
    private KategoriJabatan kategoriJabatan;

    @Enumerated(EnumType.STRING)
    @Column(name = "kategori_kebutuhan_jam_pelatihan", length = 20)
    private KategoriKebutuhanJamPelatihan kategoriKebutuhanJamPelatihan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bidang_id", nullable = false)
    private Bidang bidang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pangkat_golongan_id")
    private PangkatGolongan pangkatGolongan;
}
