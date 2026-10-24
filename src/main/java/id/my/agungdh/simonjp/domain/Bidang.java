package id.my.agungdh.simonjp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bidang")
@Getter
@Setter
public class Bidang extends BaseEntity {

    @Column(name = "nama", nullable = false, length = 255)
    private String nama;

    @Column(name = "deskripsi", columnDefinition = "TEXT")
    private String deskripsi;
}