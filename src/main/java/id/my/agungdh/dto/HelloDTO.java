package id.my.agungdh.dto;

import java.time.OffsetDateTime;

/**
 * Response DTO untuk {@code GET /hello}.
 *
 * <p>DTO sengaja dibuat sebagai {@code record} supaya immutable dan tidak perlu boilerplate.
 */
public record HelloDTO(
        String message,
        OffsetDateTime timestamp,
        String thread,
        boolean virtualThread) {
}
