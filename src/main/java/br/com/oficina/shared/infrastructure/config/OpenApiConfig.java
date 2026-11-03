package br.com.oficina.shared.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auto Repair Shop API")
                        .version("v1")
                        .description("""
                                Auto repair shop management: customers, vehicles, services, parts/supplies
                                and service orders (diagnosis, quote, execution and delivery).

                                Administrative endpoints require a JWT (get one at `POST /api/v1/auth/login`
                                and click **Authorize**). `/api/v1/public/**` endpoints are used by the customer
                                and authenticated by the service order number + customer document.
                                """)
                        .contact(new Contact().name("Oficina Mecânica")))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT obtained at POST /api/v1/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
