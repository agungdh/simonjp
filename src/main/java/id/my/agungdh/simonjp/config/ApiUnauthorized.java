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
 * 401 ketika token tidak ada/tidak valid, atau kredensial login salah.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Tidak terautentikasi atau kredensial salah",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                                @ExampleObject(name = "tidak-terautentikasi",
                                        value = ApiErrorExamples.UNAUTHORIZED),
                                @ExampleObject(name = "kredensial-salah",
                                        value = ApiErrorExamples.BAD_CREDENTIALS)
                        }))
})
public @interface ApiUnauthorized {
}
