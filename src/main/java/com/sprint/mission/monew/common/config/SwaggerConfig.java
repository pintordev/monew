package com.sprint.mission.monew.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Value("${monew.base-url}")
  private String baseUrl;

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Monew API Documentation")
            .description("Swagger API Documentation for Monew Project.")
        )
        .servers(List.of(
            new Server().url(baseUrl).description("Server")
        ));
  }
}
