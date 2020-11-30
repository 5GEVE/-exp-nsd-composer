package it.cnit.blueprint.composer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    final BuildProperties buildProperties;

    public OpenApiConfiguration(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new io.swagger.v3.oas.models.info.Info()
            .title("5G EVE Experiment NSD composer")
            .description("A REST API module to compose experiment NSDs. "
                + "It can also validate blueprints and generate NSDs from them.")
            .version(buildProperties.getVersion())
            .license(new io.swagger.v3.oas.models.info.License().name("Apache 2.0"))
            .contact(new Contact()
                .name("GitHub")
                .url("https://github.com/5GEVE/-exp-nsd-composer/")));
    }

}
