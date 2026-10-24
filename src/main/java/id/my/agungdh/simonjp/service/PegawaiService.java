package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.domain.Bidang;
import id.my.agungdh.simonjp.domain.PangkatGolongan;
import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.domain.UserAccount;
import id.my.agungdh.simonjp.dto.CursorPageDTO;
import id.my.agungdh.simonjp.dto.PegawaiRequest;
import id.my.agungdh.simonjp.dto.PegawaiResponse;
import id.my.agungdh.simonjp.mapper.PegawaiMapper;
import id.my.agungdh.simonjp.repository.BidangRepository;
import id.my.agungdh.simonjp.repository.PangkatGolonganRepository;
import id.my.agungdh.simonjp.repository.PegawaiRepository;
import id.my.agungdh.simonjp.repository.UserAccountRepository;
import id.my.agungdh.simonjp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * CRUD pegawai sekaligus akun login-nya. Satu pegawai selalu punya satu akun, username akun
 * selalu sama dengan NIP, dan role disimpan di akun.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PegawaiService {

    private static final int PAGE_SIZE = 20;

    private final PegawaiRepository repository;
    private final PegawaiMapper mapper;
    private final BidangRepository bidangRepository;
    private final PangkatGolonganRepository pangkatGolonganRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserAccountService userAccountService;

    public PegawaiResponse create(PegawaiRequest request) {
        validateNipAvailable(request.nip(), null);

        Pegawai entity = mapper.toEntity(request);
        entity.setBidang(resolveBidang(request.bidangUuid()));
        entity.setPangkatGolongan(resolvePangkatGolongan(request.pangkatGolonganUuid()));
        repository.save(entity);

        String rawPassword = request.password() != null ? request.password() : request.nip();
        userAccountService.createFor(entity, request.role(), rawPassword);
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public PegawaiResponse get(UUID uuid) {
        Pegawai entity = repository.findWithRelationsByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pegawai tidak ditemukan"));
        return mapper.toResponse(entity);
    }

    public PegawaiResponse update(UUID uuid, PegawaiRequest request) {
        Pegawai entity = repository.findWithRelationsByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pegawai tidak ditemukan"));
        validateNipAvailable(request.nip(), uuid);

        UserAccount account = entity.getAccount();
        guardSelfRoleChange(account, request.role());

        mapper.updateEntity(request, entity);
        entity.setBidang(resolveBidang(request.bidangUuid()));
        entity.setPangkatGolongan(resolvePangkatGolongan(request.pangkatGolonganUuid()));
        repository.save(entity);

        if (account == null) {
            String rawPassword = request.password() != null ? request.password() : request.nip();
            userAccountService.createFor(entity, request.role(), rawPassword);
        } else {
            userAccountService.updateFor(account, request.nip(), request.role(), request.password());
        }
        return mapper.toResponse(entity);
    }

    public void delete(UUID uuid) {
        Pegawai entity = repository.findWithRelationsByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pegawai tidak ditemukan"));
        guardSelfAccount(entity.getAccount());

        // Akun login selalu menempel ke pegawai, jadi ikut dihapus saat pegawai dihapus.
        userAccountService.softDeleteByPegawai(entity);
        entity.setDeletedAt(OffsetDateTime.now());
        CurrentUser.id().ifPresent(entity::setDeletedBy);
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public CursorPageDTO<PegawaiResponse> list(UUID cursor) {
        int size = PAGE_SIZE;
        Long cursorId = null;
        if (cursor != null) {
            Pegawai cursorEntity = repository.findByUuid(cursor)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cursor tidak valid"));
            cursorId = cursorEntity.getId();
        }
        List<Pegawai> rows = repository.findPage(cursorId, PageRequest.of(0, size + 1, Sort.by(Sort.Direction.DESC, "id")));
        boolean hasMore = rows.size() > size;
        List<Pegawai> items = hasMore ? rows.subList(0, size) : rows;
        UUID nextCursor = (hasMore && !items.isEmpty()) ? items.get(items.size() - 1).getUuid() : null;
        List<PegawaiResponse> responses = items.stream().map(mapper::toResponse).toList();
        return new CursorPageDTO<>(responses, nextCursor, hasMore);
    }

    private void validateNipAvailable(String nip, UUID selfUuid) {
        repository.findByNipIgnoreCase(nip)
                .filter(existing -> selfUuid == null || !existing.getUuid().equals(selfUuid))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NIP sudah digunakan");
                });

        userAccountRepository.findByUsernameIgnoreCase(nip)
                .filter(existing -> existing.getPegawai() == null
                        || selfUuid == null
                        || !existing.getPegawai().getUuid().equals(selfUuid))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NIP sudah digunakan");
                });
    }

    /**
     * Admin tidak boleh menurunkan role akunnya sendiri, supaya tidak langsung kehilangan akses.
     */
    private void guardSelfRoleChange(UserAccount account, Role requestedRole) {
        Long currentUserId = CurrentUser.id().orElse(null);
        if (account != null && currentUserId != null && currentUserId.equals(account.getId())
                && requestedRole != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tidak dapat menurunkan role akun sendiri");
        }
    }

    private void guardSelfAccount(UserAccount account) {
        Long currentUserId = CurrentUser.id().orElse(null);
        if (account != null && currentUserId != null && currentUserId.equals(account.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tidak dapat menghapus akun sendiri");
        }
    }

    private Bidang resolveBidang(UUID bidangUuid) {
        if (bidangUuid == null) {
            return null;
        }
        return bidangRepository.findByUuid(bidangUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bidang tidak ditemukan"));
    }

    private PangkatGolongan resolvePangkatGolongan(UUID pangkatGolonganUuid) {
        if (pangkatGolonganUuid == null) {
            return null;
        }
        return pangkatGolonganRepository.findByUuid(pangkatGolonganUuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Pangkat/golongan tidak ditemukan"));
    }
}
