package id.my.agungdh.simonjp.security;

import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.domain.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * UserDetails yang hanya dipakai saat login (butuh password hash untuk verifikasi).
 * Request biasa memakai {@link AppUserPrincipal} yang tidak membawa password.
 */
@RequiredArgsConstructor
public class AppUserDetails implements UserDetails {

    private final UserAccount user;

    public Long getId() {
        return user.getId();
    }

    public UserAccount getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public Role getRole() {
        return user.getRole();
    }
}
