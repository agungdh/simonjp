package id.my.agungdh.simonjp.security;

import id.my.agungdh.simonjp.config.AuthProperties;
import id.my.agungdh.simonjp.domain.UserAccount;
import id.my.agungdh.simonjp.repository.AuthTokenRepository;
import id.my.agungdh.simonjp.repository.UserAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Autentikasi per request dari token opaque (cookie atau bearer). Data user selalu
 * dibaca ulang dari database, sehingga perubahan role / status enabled langsung berlaku.
 */
@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthTokenRepository tokenRepository;
    private final UserAccountRepository userRepository;
    private final TokenExtractor tokenExtractor;
    private final AuthCookieFactory cookieFactory;
    private final AuthProperties properties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(request, response);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> rawToken = tokenExtractor.extract(request);
        if (rawToken.isEmpty()) {
            return;
        }
        String token = rawToken.get();
        Duration ttl = tokenExtractor.isBearer(request) ? properties.mobileTokenTtl() : properties.webTokenTtl();

        tokenRepository.resolve(token)
                .flatMap(userRepository::findById)
                .filter(UserAccount::isEnabled)
                .ifPresent(user -> {
                    AppUserPrincipal principal = new AppUserPrincipal(
                            user.getId(), user.getUuid(), user.getUsername(), user.getNama(), user.getRole());
                    UsernamePasswordAuthenticationToken authentication =
                            UsernamePasswordAuthenticationToken.authenticated(principal, null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    boolean extended = tokenRepository.refresh(token, ttl);
                    if (extended && !tokenExtractor.isBearer(request)) {
                        // Max-Age cookie statis, jadi cookie harus dikirim ulang saat sliding refresh.
                        cookieFactory.addToken(response, token, ttl);
                    }
                });
    }
}
