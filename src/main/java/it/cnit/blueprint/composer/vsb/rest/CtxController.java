package it.cnit.blueprint.composer.vsb.rest;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import it.cnit.blueprint.composer.vsb.graph.VsbGraphService;
import it.cnit.blueprint.composer.vsb.graph.VsbVertex;
import it.nextworks.nfvmano.catalogue.blueprint.elements.CtxBlueprint;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/ctx")
public class CtxController {

  private final VsbGraphService vsbGraphService;

  /**
   * Validate method. Serialization errors are handled by Spring
   *
   * @param ctx object to validate
   * @return 200 if valid, 400 with validation errors if invalid
   */
  @PostMapping("/validate")
  public void validate(@RequestBody @Valid CtxBlueprint ctx) {
    try {
      ctx.isValid();
    } catch (MalformattedElementException e) {
      log.debug("Invalid CtxBlueprint: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  @GetMapping("/schema")
  public JsonSchema schema() {
    ObjectMapper J_OBJECT_MAPPER = new ObjectMapper(new JsonFactory())
        .enable(SerializationFeature.INDENT_OUTPUT);
    try {
      return new JsonSchemaGenerator(J_OBJECT_MAPPER).generateSchema(CtxBlueprint.class);
    } catch (JsonMappingException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @PostMapping("/graph")
  public Map<String, String> graph(@RequestBody @Valid CtxBlueprint ctx) {
    validate(ctx);
    Graph<VsbVertex, String> graph = vsbGraphService.buildGraph(ctx);
    return new HashMap<String, String>() {
      {
        put("graph", vsbGraphService.export(graph));
      }
    };
  }
}
