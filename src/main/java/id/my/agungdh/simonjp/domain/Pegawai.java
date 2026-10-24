package id.my.agungdh.simonjp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @Column(name = "nip", length = 50)
    private String nip;

    @Column(name = "email", length = 255)
    private String email;

    @ManyToOne
    @JoinColumn(name = "bidang_id")
    private Bidang bidang;
}