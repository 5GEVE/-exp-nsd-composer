package it.cnit.blueprint.composer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
    info = @Info(
        title = "5G EVE Experiment NSD composer",
        description =
            "A REST API module to compose experiment NSDs. It can also validate blueprints and"
                + " generate NSDs from them.",
        version = "v0.0.4",
        license = @License(name = "Apache 2.0")
    )
)
public class OpenApiConfiguration {

}
