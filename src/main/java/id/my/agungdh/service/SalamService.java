package id.my.agungdh.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import id.my.agungdh.domain.Salam;
import id.my.agungdh.dto.SalamDTO;
import id.my.agungdh.dto.SalamRequest;
import id.my.agungdh.mapper.SalamMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

/**
 * Contoh service in-memory: entity disimpan di list, lalu dipetakan ke DTO saat keluar.
 */
@RequiredArgsConstructor
@ApplicationScoped
public class SalamService {

    private final SalamMapper mapper;
    private final List<Salam> data = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong();

    @PostConstruct
    void seedData() {
        seed("Agung", "Halo dari data contoh");
        seed("Budi", "DTO bikin bentuk response API tetap stabil");
        seed("Citra", "Field internal tidak ikut terkirim ke client");
    }

    public List<SalamDTO> semua() {
        return mapper.toDtoList(List.copyOf(data));
    }

    public Optional<SalamDTO> cari(Long id) {
        return data.stream()
                .filter(salam -> salam.getId().equals(id))
                .findFirst()
                .map(mapper::toDto);
    }

    public SalamDTO tambah(SalamRequest request) {
        Salam salam = mapper.toEntity(request);
        salam.setId(sequence.incrementAndGet());
        salam.setDibuatPada(OffsetDateTime.now());
        salam.setCatatanInternal("dibuat lewat POST /contoh/data");
        data.add(salam);
        return mapper.toDto(salam);
    }

    private void seed(String nama, String pesan) {
        data.add(Salam.builder()
                .id(sequence.incrementAndGet())
                .nama(nama)
                .pesan(pesan)
                .catatanInternal("seed: field internal, tidak ada di DTO")
                .dibuatPada(OffsetDateTime.now())
                .build());
    }
}
