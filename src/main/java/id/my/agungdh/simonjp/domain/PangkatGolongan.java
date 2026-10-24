package id.my.agungdh.simonjp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pangkat_golongan")
@Getter
@Setter
public class PangkatGolongan extends BaseEntity {

    @Column(name = "jenjang", nullable = false, length = 100)
    private String jenjang;

    @Column(name = "pangkat", nullable = false, length = 100)
    private String pangkat;

    @Column(name = "golongan", nullable = false, length = 20)
    private String golongan;

    @Column(name = "ruang", length = 20)
    private String ruang;
}
