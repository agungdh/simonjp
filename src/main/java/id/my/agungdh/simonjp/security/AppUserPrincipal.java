package id.my.agungdh.simonjp.security;

import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.dto.UserResponse;

import java.util.UUID;

/**
 * Principal yang dipakai saat request sudah terautentikasi. Tidak pernah membawa
 * password hash; data dibangun ulang dari database di setiap request.
 */
public record AppUserPrincipal(Long id, UUID uuid, String username, String nama, Role role) {

    public UserResponse toResponse() {
        return new UserResponse(uuid, username, nama, role);
    }
}
