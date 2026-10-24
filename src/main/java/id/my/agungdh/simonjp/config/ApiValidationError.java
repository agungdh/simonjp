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
 * 400 untuk endpoint yang menerima body: validasi field, body tidak terbaca,
 * nama sudah digunakan, atau parameter path bukan UUID.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "400",
                description = "Validasi gagal, body tidak terbaca, nama sudah digunakan, atau parameter tidak valid",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                                @ExampleObject(name = "validasi-gagal", value = ApiErrorExamples.VALIDATION_ERROR),
                                @ExampleObject(name = "nama-duplikat", value = ApiErrorExamples.DUPLICATE_NAME),
                                @ExampleObject(name = "body-tidak-valid", value = ApiErrorExamples.INVALID_BODY),
                                @ExampleObject(name = "parameter-tidak-valid", value = ApiErrorExamples.INVALID_PARAMETER)
                        }))
})
public @interface ApiValidationError {
}
