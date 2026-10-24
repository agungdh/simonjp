package id.my.agungdh.simonjp.security;

import id.my.agungdh.simonjp.domain.Role;

import java.util.UUID;

/**
 * Principal yang dipakai saat request sudah terautentikasi. Tidak pernah membawa
 * password hash; data dibangun ulang dari database di setiap request. Data lengkap
 * (termasuk pegawai dan bidang) dibaca ulang oleh service saat dibutuhkan.
 */
public record AppUserPrincipal(Long id, UUID uuid, String username, Role role) {
}
