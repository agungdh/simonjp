package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.domain.Bidang;
import id.my.agungdh.simonjp.domain.KategoriJabatan;
import id.my.agungdh.simonjp.domain.KategoriKebutuhanJamPelatihan;
import id.my.agungdh.simonjp.domain.PangkatGolongan;
import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.domain.PegawaiStatus;
import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.repository.BidangRepository;
import id.my.agungdh.simonjp.repository.PangkatGolonganRepository;
import id.my.agungdh.simonjp.repository.PegawaiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeedService {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String ADMIN_NAMA = "Administrator";
    private static final String SAMPLE_PEGAWAI_PASSWORD = "pegawai";

    private final BidangRepository bidangRepository;
    private final PangkatGolonganRepository pangkatGolonganRepository;
    private final PegawaiRepository pegawaiRepository;
    private final UserAccountService userAccountService;

    @Transactional
    public void seed() {
        // Admin dibuat lebih dulu supaya tetap ada walau data contoh sudah terisi.
        userAccountService.ensureAdmin(ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_NAMA, Role.ADMIN);

        if (pegawaiRepository.count() >= 100) {
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

        List<PangkatGolongan> pangkatGolongans = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            PangkatGolongan pg = new PangkatGolongan();
            pg.setUuid(UUID.randomUUID());
            pg.setJenjang("Jenjang " + i);
            pg.setPangkat("Pangkat " + i);
            pg.setGolongan("Golongan " + i);
            pg.setRuang(String.valueOf((char) ('a' + (i % 5))));
            pangkatGolongans.add(pg);
        }
        pangkatGolonganRepository.saveAll(pangkatGolongans);

        List<Pegawai> pegawais = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            Pegawai pg = new Pegawai();
            pg.setUuid(UUID.randomUUID());
            pg.setNama("Pegawai " + i);
            pg.setNip("NIP" + String.format("%05d", i));
            pg.setEmail("pegawai" + i + "@example.com");
            pg.setJabatan("Jabatan " + i);
            pg.setPeran("Peran " + i);
            pg.setStatus(PegawaiStatus.AKTIF);
            pg.setKategoriJabatan(KategoriJabatan.values()[i % KategoriJabatan.values().length]);
            pg.setKategoriKebutuhanJamPelatihan(
                    KategoriKebutuhanJamPelatihan.values()[i % KategoriKebutuhanJamPelatihan.values().length]);
            pg.setBidang(bidangs.get((i - 1) % bidangs.size()));
            pg.setPangkatGolongan(pangkatGolongans.get((i - 1) % pangkatGolongans.size()));
            pegawais.add(pg);
        }
        pegawaiRepository.saveAll(pegawais);

        // Username = NIP, password sama untuk semua data contoh supaya mudah dicoba.
        userAccountService.createForAll(pegawais, Role.PEGAWAI, SAMPLE_PEGAWAI_PASSWORD);

        System.out.println("Seeded: " + bidangs.size() + " bidang, " + pangkatGolongans.size()
                + " pangkat/golongan, " + pegawais.size() + " pegawai, admin " + ADMIN_USERNAME);
    }
}
