package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.config.AuthProperties;
import id.my.agungdh.simonjp.dto.LoginRequest;
import id.my.agungdh.simonjp.dto.UserResponse;
import id.my.agungdh.simonjp.mapper.UserMapper;
import id.my.agungdh.simonjp.repository.AuthTokenRepository;
import id.my.agungdh.simonjp.security.AppUserDetails;
import id.my.agungdh.simonjp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * Satu-satunya tempat kredensial diverifikasi dan token diterbitkan.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AuthTokenRepository tokenRepository;
    private final AuthProperties properties;
    private final UserMapper userMapper;

    /** Login web: token berumur pendek, dikirim lewat cookie. */
    @Transactional(readOnly = true)
    public AuthenticatedSession loginWeb(LoginRequest request, String previousToken) {
        return login(request, previousToken, properties.webTokenTtl());
    }

    /** Login mobile: token berumur panjang, dikirim lewat body sebagai bearer. */
    @Transactional(readOnly = true)
    public AuthenticatedSession loginMobile(LoginRequest request) {
        return login(request, null, properties.mobileTokenTtl());
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        return CurrentUser.require().toResponse();
    }

    public void logout(String token) {
        tokenRepository.revoke(token);
    }

    private AuthenticatedSession login(LoginRequest request, String previousToken, Duration ttl) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        AppUserDetails details = (AppUserDetails) authentication.getPrincipal();

        if (previousToken != null && !previousToken.isBlank()) {
            tokenRepository.revoke(previousToken);
        }
        String token = tokenRepository.issue(details.getId(), ttl);
        return new AuthenticatedSession(token, ttl.toSeconds(), userMapper.toResponse(details.getUser()));
    }
}
