package it.cnit.blueprint.composer.vsb.rest;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import it.nextworks.nfvmano.catalogue.blueprint.elements.TestCaseBlueprint;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
@AllArgsConstructor
public class TcbController {

  /**
   * Validate method. Serialization errors are handled by Spring
   *
   * @param tcb object to validate
   * @return 200 if valid, 400 with validation errors if invalid
   */
  @PostMapping("/tcb/validate")
  public void validate(@RequestBody TestCaseBlueprint tcb) {
    try {
      tcb.isValid();
    } catch (MalformattedElementException e) {
      log.debug("Invalid TestCaseBlueprint: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  @GetMapping("/tcb/schema")
  public JsonSchema schema() {
    ObjectMapper J_OBJECT_MAPPER = new ObjectMapper(new JsonFactory())
        .enable(SerializationFeature.INDENT_OUTPUT);
    try {
      return new JsonSchemaGenerator(J_OBJECT_MAPPER).generateSchema(TestCaseBlueprint.class);
    } catch (JsonMappingException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }
}
