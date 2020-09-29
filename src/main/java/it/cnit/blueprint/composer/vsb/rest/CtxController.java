package it.cnit.blueprint.composer.vsb.rest;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.cnit.blueprint.composer.commons.ObjectMapperService;
import it.cnit.blueprint.composer.commons.ZipService;
import it.cnit.blueprint.composer.vsb.graph.VsbGraphService;
import it.nextworks.nfvmano.catalogue.blueprint.elements.CtxBlueprint;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

  private final ZipService zipService;
  private final ObjectMapperService omService;

  /**
   * Validate method. Serialization errors are handled by Spring
   *
   * @param ctx object to validate
   * @return 200 if valid, 400 with validation errors if invalid
   */
  @PostMapping("/validate")
  @Operation(description = "Validates a CtxB")
  public void validate(@RequestBody @Valid CtxBlueprint ctx) {
    try {
      ctx.isValid();
    } catch (MalformattedElementException e) {
      log.warn("Invalid CtxBlueprint: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  @GetMapping("/schema")
  @Operation(description = "Generates the JSON Schema for a CtxB")
  public JsonSchema schema() {
    try {
      return new JsonSchemaGenerator(omService.createIndentNsdWriter()).generateSchema(Nsd.class);
    } catch (JsonMappingException e) {
      log.error("Error generating JSON Schema: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @PostMapping("/graph")
  @Operation(description = "Generates PNG images to visualize the topology of a CtxB",
      responses = @ApiResponse(
          responseCode = "200",
          description = "A zip file containing the images (PNG) representing the CtxB in input",
          content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE),
          headers = @Header(name = HttpHeaders.CONTENT_DISPOSITION)))
  public ResponseEntity<InputStreamResource> graph(@RequestBody @Valid CtxBlueprint ctx) {
    validate(ctx);
    File tempFile;
    try {
      tempFile = vsbGraphService.writeImageFile(ctx);
    } catch (IOException e) {
      log.error("Can not write file: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
    try {
      return zipService.getZipResponse(Collections.singletonList(tempFile), true);
    } catch (IOException e) {
      log.error("Zip response error: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }
}
