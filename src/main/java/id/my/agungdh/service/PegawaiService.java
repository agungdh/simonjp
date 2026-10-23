package id.my.agungdh.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import id.my.agungdh.domain.Bidang;
import id.my.agungdh.domain.PangkatGolongan;
import id.my.agungdh.domain.Pegawai;
import id.my.agungdh.enums.KategoriJabatan;
import id.my.agungdh.enums.KategoriKebutuhanJamPelatihan;
import id.my.agungdh.enums.StatusPegawai;
import id.my.agungdh.enums.TipePegawai;
import id.my.agungdh.dto.CursorPageDTO;
import id.my.agungdh.dto.PegawaiRequest;
import id.my.agungdh.dto.PegawaiResponse;
import id.my.agungdh.mapper.PegawaiMapper;
import id.my.agungdh.repository.BidangRepository;
import id.my.agungdh.repository.PangkatGolonganRepository;
import id.my.agungdh.repository.PegawaiFilter;
import id.my.agungdh.repository.PegawaiRepository;
import id.my.agungdh.repository.QuerySupport;
import id.my.agungdh.service.exception.BadRequestException;
import id.my.agungdh.service.exception.ConflictException;
import id.my.agungdh.service.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PegawaiService {

    private final PegawaiRepository pegawaiRepository;
    private final BidangRepository bidangRepository;
    private final PangkatGolonganRepository pangkatGolonganRepository;
    private final PegawaiMapper mapper;

    @Transactional
    public CursorPageDTO<PegawaiResponse> list(
            String search,
            TipePegawai tipe,
            StatusPegawai status,
            UUID bidangUuid,
            UUID pangkatGolonganUuid,
            KategoriJabatan kategoriJabatan,
            KategoriKebutuhanJamPelatihan kategoriKebutuhanJamPelatihan,
            UUID cursor,
            int limit) {

        PegawaiFilter filter = new PegawaiFilter(
                CursorSupport.normalizeSearch(search),
                tipe,
                status,
                bidangUuid,
                pangkatGolonganUuid,
                kategoriJabatan,
                kategoriKebutuhanJamPelatihan);
        int size = CursorSupport.normalizeLimit(limit);

        Long cursorId = null;
        if (cursor != null) {
            cursorId = pegawaiRepository.findFilteredIdByUuid(cursor, filter)
                    .orElseThrow(() -> new BadRequestException("Cursor tidak valid."));
        }

        var rows = pegawaiRepository.list(filter, cursorId, size + 1);
        var page = CursorSupport.page(rows, size);

        return new CursorPageDTO<>(
                page.items().stream().map(mapper::toResponse).toList(),
                page.nextCursor(),
                page.hasMore());
    }

    @Transactional
    public PegawaiResponse get(UUID uuid) {
        return mapper.toResponse(find(uuid));
    }

    @Transactional
    public PegawaiResponse create(PegawaiRequest request) {
        String nip = requiredNip(request.nip());
        if (pegawaiRepository.existsByNip(nip, null)) {
            throw new ConflictException("NIP sudah digunakan.");
        }

        Pegawai entity = mapper.toEntity(request);
        apply(entity, request, nip);
        pegawaiRepository.persist(entity);

        return mapper.toResponse(entity);
    }

    @Transactional
    public PegawaiResponse update(UUID uuid, PegawaiRequest request) {
        Pegawai entity = find(uuid);
        String nip = requiredNip(request.nip());

        if (pegawaiRepository.existsByNip(nip, entity.getId())) {
            throw new ConflictException("NIP sudah digunakan.");
        }

        mapper.updateEntity(request, entity);
        apply(entity, request, nip);

        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(UUID uuid) {
        Pegawai entity = find(uuid);
        entity.setDeletedAt(OffsetDateTime.now());
        // deletedBy: null bila tidak ada principal
    }

    private Pegawai find(UUID uuid) {
        return pegawaiRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Pegawai tidak ditemukan."));
    }

    private void apply(Pegawai entity, PegawaiRequest request, String nip) {
        entity.setNip(nip);
        entity.setNama(required(request.nama(), "nama"));
        entity.setJabatan(QuerySupport.trimToNull(request.jabatan()));
        entity.setPeran(QuerySupport.trimToNull(request.peran()));
        entity.setBidang(resolveBidang(request.bidangUuid()));
        entity.setPangkatGolongan(resolvePangkatGolongan(request.pangkatGolonganUuid()));
    }

    private Bidang resolveBidang(UUID uuid) {
        return bidangRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new BadRequestException("Bidang tidak ditemukan."));
    }

    private PangkatGolongan resolvePangkatGolongan(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return pangkatGolonganRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new BadRequestException("Pangkat golongan tidak ditemukan."));
    }

    private String requiredNip(String value) {
        String nip = required(value, "nip");
        if (!nip.matches("^[0-9]+$")) {
            throw new BadRequestException("nip hanya boleh berisi angka.");
        }
        if (nip.length() > 30) {
            throw new BadRequestException("nip maksimal 30 karakter.");
        }
        return nip;
    }

    private String required(String value, String field) {
        String normalized = QuerySupport.trimToNull(value);
        if (normalized == null) {
            throw new BadRequestException(field + " wajib diisi.");
        }
        return normalized;
    }
}
