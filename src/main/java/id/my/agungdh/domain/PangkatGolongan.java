package id.my.agungdh.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pangkat_golongan")
public class PangkatGolongan extends BaseEntity {

    @Column(name = "jenjang", nullable = false, length = 50)
    private String jenjang;

    @Column(name = "pangkat", nullable = false, length = 100)
    private String pangkat;

    @Column(name = "golongan", nullable = false, length = 10)
    private String golongan;

    @Column(name = "ruang", nullable = false, length = 10)
    private String ruang;
}
