package com.dataflow.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dataFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DataFlow REST API")
                        .description("Real-Time Distributed Data Pipeline and Analytics Platform API Documentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DataFlow Engineering Team")
                                .url("https://github.com/satyamk001-tech/realtime-data-pipeline"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
