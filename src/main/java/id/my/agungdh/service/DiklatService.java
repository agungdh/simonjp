package id.my.agungdh.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import id.my.agungdh.domain.Diklat;
import id.my.agungdh.domain.JenisPelatihan;
import id.my.agungdh.domain.Pegawai;
import id.my.agungdh.dto.DiklatRequest;
import id.my.agungdh.dto.DiklatResponse;
import id.my.agungdh.dto.CursorPageDTO;
import id.my.agungdh.mapper.DiklatMapper;
import id.my.agungdh.repository.DiklatFilter;
import id.my.agungdh.repository.DiklatRepository;
import id.my.agungdh.repository.JenisPelatihanRepository;
import id.my.agungdh.repository.PegawaiRepository;
import id.my.agungdh.repository.QuerySupport;
import id.my.agungdh.service.exception.BadRequestException;
import id.my.agungdh.service.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DiklatService {

    private final DiklatRepository repository;
    private final PegawaiRepository pegawaiRepository;
    private final JenisPelatihanRepository jenisPelatihanRepository;
    private final DiklatMapper mapper;

    @Transactional
    public CursorPageDTO<DiklatResponse> list(
            String search,
            UUID pegawaiUuid,
            UUID jenisPelatihanUuid,
            java.time.LocalDate dariTanggal,
            java.time.LocalDate sampaiTanggal,
            Integer tahun,
            UUID cursor,
            int limit) {

        DiklatFilter filter = new DiklatFilter(
                CursorSupport.normalizeSearch(search),
                pegawaiUuid,
                jenisPelatihanUuid,
                dariTanggal,
                sampaiTanggal,
                tahun);
        int size = CursorSupport.normalizeLimit(limit);

        Long cursorId = null;
        if (cursor != null) {
            cursorId = repository.findFilteredIdByUuid(cursor, filter)
                    .orElseThrow(() -> new BadRequestException("Cursor tidak valid."));
        }

        var rows = repository.list(filter, cursorId, size + 1);
        var page = CursorSupport.page(rows, size);

        return new CursorPageDTO<>(
                page.items().stream().map(mapper::toResponse).toList(),
                page.nextCursor(),
                page.hasMore());
    }

    @Transactional
    public DiklatResponse get(UUID uuid) {
        return mapper.toResponse(find(uuid));
    }

    @Transactional
    public DiklatResponse create(DiklatRequest request) {
        Diklat entity = mapper.toEntity(request);
        entity.setPegawai(resolvePegawai(request.pegawaiUuid()));
        entity.setJenisPelatihan(resolveJenisPelatihan(request.jenisPelatihanUuid()));
        entity.setNomorSurat(require(request.nomorSurat(), "nomorSurat"));
        entity.setMateriPengembangan(require(request.materiPengembangan(), "materiPengembangan"));
        entity.setDariTanggalPelaksanaan(request.dariTanggalPelaksanaan());
        entity.setSampaiTanggalPelaksanaan(request.sampaiTanggalPelaksanaan());
        entity.setJumlahJamPelatihan(request.jumlahJamPelatihan());
        repository.persist(entity);

        return mapper.toResponse(entity);
    }

    @Transactional
    public DiklatResponse update(UUID uuid, DiklatRequest request) {
        Diklat entity = find(uuid);
        mapper.updateEntity(request, entity);
        entity.setPegawai(resolvePegawai(request.pegawaiUuid()));
        entity.setJenisPelatihan(resolveJenisPelatihan(request.jenisPelatihanUuid()));
        entity.setNomorSurat(require(request.nomorSurat(), "nomorSurat"));
        entity.setMateriPengembangan(require(request.materiPengembangan(), "materiPengembangan"));
        entity.setDariTanggalPelaksanaan(request.dariTanggalPelaksanaan());
        entity.setSampaiTanggalPelaksanaan(request.sampaiTanggalPelaksanaan());
        entity.setJumlahJamPelatihan(request.jumlahJamPelatihan());

        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(UUID uuid) {
        Diklat entity = find(uuid);
        entity.setDeletedAt(OffsetDateTime.now());
    }

    private Diklat find(UUID uuid) {
        return repository.findActiveByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Diklat tidak ditemukan."));
    }

    private Pegawai resolvePegawai(UUID uuid) {
        return pegawaiRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new BadRequestException("Pegawai tidak ditemukan."));
    }

    private JenisPelatihan resolveJenisPelatihan(UUID uuid) {
        return jenisPelatihanRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new BadRequestException("Jenis pelatihan tidak ditemukan."));
    }

    private String require(String value, String field) {
        String normalized = QuerySupport.trimToNull(value);
        if (normalized == null) {
            throw new BadRequestException(field + " wajib diisi.");
        }
        return normalized;
    }
}
