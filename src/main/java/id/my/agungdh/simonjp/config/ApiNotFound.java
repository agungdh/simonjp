package id.my.agungdh.simonjp.config;

import id.my.agungdh.simonjp.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 404 ketika {@code uuid} tidak ada atau sudah soft deleted.
 * Pesan menyesuaikan resource: Kategori, Bidang, Pegawai, atau Produk.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "404", description = "Resource tidak ditemukan",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(name = "tidak-ditemukan", value = ApiErrorExamples.NOT_FOUND)))
})
public @interface ApiNotFound {
}
