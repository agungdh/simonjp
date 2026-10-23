package id.my.agungdh.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "jenis_pelatihan")
public class JenisPelatihan extends BaseEntity {

    @Column(name = "jenis_pelatihan", nullable = false, length = 255)
    private String jenisPelatihan;
}
