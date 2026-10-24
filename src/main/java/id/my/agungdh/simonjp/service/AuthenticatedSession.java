package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.dto.UserResponse;

/**
 * Hasil login: token asli (hanya ada di sini), masa berlaku, dan data user.
 */
public record AuthenticatedSession(String token, long expiresIn, UserResponse user) {
}
