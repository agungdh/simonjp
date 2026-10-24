package id.my.agungdh.simonjp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

/**
 * Mendaftarkan dua cara autentikasi yang setara: cookie (web) dan bearer (mobile).
 * Dua security requirement di root berarti OR, sehingga Swagger UI menerima keduanya.
 */
@Component
@RequiredArgsConstructor
public class OpenApiSecurityCustomizer implements OpenApiCustomizer {

    private final AuthProperties authProperties;

    @Override
    public void customise(OpenAPI openApi) {
        Components components = openApi.getComponents() != null ? openApi.getComponents() : new Components();
        components.addSecuritySchemes("cookieAuth", new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name(authProperties.cookieName())
                .description("Cookie HttpOnly hasil POST /auth/login"));
        components.addSecuritySchemes("bearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("opaque")
                .description("Token opaque hasil POST /auth/token"));
        openApi.setComponents(components);
        openApi.addSecurityItem(new SecurityRequirement().addList("cookieAuth"));
        openApi.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
