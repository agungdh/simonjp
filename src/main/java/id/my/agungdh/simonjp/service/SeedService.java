package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.domain.Bidang;
import id.my.agungdh.simonjp.domain.Kategori;
import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.domain.Product;
import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.repository.BidangRepository;
import id.my.agungdh.simonjp.repository.KategoriRepository;
import id.my.agungdh.simonjp.repository.PegawaiRepository;
import id.my.agungdh.simonjp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeedService {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String ADMIN_NAMA = "Administrator";

    private final BidangRepository bidangRepository;
    private final KategoriRepository kategoriRepository;
    private final ProductRepository productRepository;
    private final PegawaiRepository pegawaiRepository;
    private final UserAccountService userAccountService;

    @Transactional
    public void seed() {
        // Admin dibuat lebih dulu supaya tetap ada walau data contoh sudah terisi.
        userAccountService.ensureAdmin(ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_NAMA, Role.ADMIN);

        if (productRepository.count() >= 100) {
            System.out.println("Seed skipped: data already present");
            return;
        }

        List<Bidang> bidangs = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Bidang b = new Bidang();
            b.setUuid(UUID.randomUUID());
            b.setNama("Bidang " + i);
            b.setDeskripsi("Deskripsi bidang " + i);
            bidangs.add(b);
        }
        bidangRepository.saveAll(bidangs);

        List<Kategori> kategoris = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            Kategori k = new Kategori();
            k.setUuid(UUID.randomUUID());
            k.setNama("Kategori " + i);
            k.setDeskripsi("Deskripsi kategori " + i);
            kategoris.add(k);
        }
        kategoriRepository.saveAll(kategoris);

        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= 200; i++) {
            Product p = new Product();
            p.setUuid(UUID.randomUUID());
            p.setNama("Produk " + i);
            p.setDeskripsi("Deskripsi produk " + i);
            p.setHarga(new BigDecimal(10000 + (i * 100)));
            p.setStok(i % 50);
            p.setKategori(kategoris.get((i - 1) % kategoris.size()));
            products.add(p);
        }
        productRepository.saveAll(products);

        List<Pegawai> pegawais = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            Pegawai pg = new Pegawai();
            pg.setUuid(UUID.randomUUID());
            pg.setNama("Pegawai " + i);
            pg.setNip("NIP" + String.format("%05d", i));
            pg.setEmail("pegawai" + i + "@example.com");
            pg.setBidang(bidangs.get((i - 1) % bidangs.size()));
            pegawais.add(pg);
        }
        pegawaiRepository.saveAll(pegawais);

        System.out.println("Seeded: " + bidangs.size() + " bidang, " + kategoris.size()
                + " kategori, " + products.size() + " produk, " + pegawais.size() + " pegawai, admin "
                + ADMIN_USERNAME);
    }
}