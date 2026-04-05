package zorvyn.assessment.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .description("Enter JWT token: ")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .info(new Info()
                        .title("Finance Dashboard System API")
                        .version("1.0")
                        .contact(
                                new Contact()
                                        .name("Mohd Junaid")
                                        .email("mjunaid7082@gmail.com")
                        )
                        .description("All API endpoints are secured with RateLimiting (20 requests/minute except /api/auth/**) and JWT authentication. " +
                                "\n To access the endpoints, first register than obtain a JWT token via the /api/auth/login endpoint" +
                                " and include it in the Authorization header."))
                .servers(
                        List.of(
                                new Server().url("https://finance-dashboard-system-backend-production.up.railway.app").description("Financial Dashboard Server"),
                                new Server().url("http://localhost:9090").description("Finance Dashboard System")
                        ))
                ;
    }
}
