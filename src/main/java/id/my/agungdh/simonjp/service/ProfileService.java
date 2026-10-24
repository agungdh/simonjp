package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.domain.Bidang;
import id.my.agungdh.simonjp.domain.PangkatGolongan;
import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.domain.UserAccount;
import id.my.agungdh.simonjp.dto.ChangePasswordRequest;
import id.my.agungdh.simonjp.dto.PegawaiResponse;
import id.my.agungdh.simonjp.dto.ProfileUpdateRequest;
import id.my.agungdh.simonjp.mapper.PegawaiMapper;
import id.my.agungdh.simonjp.repository.BidangRepository;
import id.my.agungdh.simonjp.repository.PangkatGolonganRepository;
import id.my.agungdh.simonjp.repository.PegawaiRepository;
import id.my.agungdh.simonjp.repository.UserAccountRepository;
import id.my.agungdh.simonjp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Profil milik user yang sedang login. Role dan status sengaja tidak bisa diubah di sini.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProfileService {

    private final PegawaiRepository pegawaiRepository;
    private final UserAccountRepository userAccountRepository;
    private final BidangRepository bidangRepository;
    private final PangkatGolonganRepository pangkatGolonganRepository;
    private final UserAccountService userAccountService;
    private final PegawaiMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PegawaiResponse get() {
        return mapper.toResponse(currentPegawai());
    }

    public PegawaiResponse update(ProfileUpdateRequest request) {
        Pegawai entity = currentPegawai();
        validateNipAvailable(request.nip(), entity.getUuid());

        mapper.updateEntity(request, entity);
        entity.setBidang(resolveBidang(request.bidangUuid()));
        entity.setPangkatGolongan(resolvePangkatGolongan(request.pangkatGolonganUuid()));
        pegawaiRepository.save(entity);

        UserAccount account = entity.getAccount();
        if (account != null) {
            // Username selalu mengikuti NIP; password tidak berubah lewat endpoint profil.
            userAccountService.updateFor(account, request.nip(), account.getRole(), null);
        }
        return mapper.toResponse(entity);
    }

    /**
     * Ganti password sendiri. Password lama wajib cocok, dan semua sesi dicabut setelah
     * berhasil sehingga user harus login ulang.
     */
    public void changePassword(ChangePasswordRequest request) {
        Long userId = CurrentUser.require().id();
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Autentikasi diperlukan"));

        if (!passwordEncoder.matches(request.currentPassword(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password saat ini salah");
        }
        if (passwordEncoder.matches(request.newPassword(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password baru harus berbeda dari password saat ini");
        }
        userAccountService.changePassword(account, request.newPassword());
    }

    private Pegawai currentPegawai() {
        Long userId = CurrentUser.require().id();
        return pegawaiRepository.findByAccountId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Autentikasi diperlukan"));
    }

    private void validateNipAvailable(String nip, UUID selfUuid) {
        pegawaiRepository.findByNipIgnoreCase(nip)
                .filter(existing -> !existing.getUuid().equals(selfUuid))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NIP sudah digunakan");
                });

        userAccountRepository.findByUsernameIgnoreCase(nip)
                .filter(existing -> existing.getPegawai() == null
                        || !existing.getPegawai().getUuid().equals(selfUuid))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NIP sudah digunakan");
                });
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
