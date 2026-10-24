package id.my.agungdh.simonjp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Getter
@Setter
public class Product extends BaseEntity {

    @Column(name = "nama", nullable = false, length = 255)
    private String nama;

    @Column(name = "deskripsi", columnDefinition = "TEXT")
    private String deskripsi;

    @Column(name = "harga", nullable = false, precision = 19, scale = 2)
    private BigDecimal harga;

    @Column(name = "stok", nullable = false)
    private Integer stok;

    @ManyToOne
    @JoinColumn(name = "kategori_id")
    private Kategori kategori;
}