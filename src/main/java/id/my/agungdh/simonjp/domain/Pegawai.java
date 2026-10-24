package id.my.agungdh.simonjp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pegawai")
@Getter
@Setter
public class Pegawai extends BaseEntity {

    @Column(name = "nama", nullable = false, length = 255)
    private String nama;

    @Column(name = "nip", nullable = false, length = 50)
    private String nip;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "jabatan", length = 255)
    private String jabatan;

    @Column(name = "peran", length = 255)
    private String peran;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PegawaiStatus status = PegawaiStatus.AKTIF;

    @Enumerated(EnumType.STRING)
    @Column(name = "kategori_jabatan", length = 50)
    private KategoriJabatan kategoriJabatan;

    @Enumerated(EnumType.STRING)
    @Column(name = "kategori_kebutuhan_jam_pelatihan", length = 20)
    private KategoriKebutuhanJamPelatihan kategoriKebutuhanJamPelatihan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidang_id")
    private Bidang bidang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pangkat_golongan_id")
    private PangkatGolongan pangkatGolongan;

    /**
     * Sisi balik relasi 1-1; FK tetap berada di {@code user_account.pegawai_id}.
     * Dipakai agar response pegawai bisa memuat username dan role akunnya.
     */
    @OneToOne(mappedBy = "pegawai", fetch = FetchType.LAZY)
    private UserAccount account;
}
