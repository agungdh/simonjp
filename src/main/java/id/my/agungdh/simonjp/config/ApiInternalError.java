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
 * 500 untuk kegagalan tak terduga; detail internal tidak pernah dikirim ke klien.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan internal",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(name = "internal-error", value = ApiErrorExamples.INTERNAL_ERROR)))
})
public @interface ApiInternalError {
}
