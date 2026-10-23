package id.my.agungdh.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bidang")
public class Bidang extends BaseEntity {

    @Column(name = "bidang", nullable = false, length = 255)
    private String bidang;
}
