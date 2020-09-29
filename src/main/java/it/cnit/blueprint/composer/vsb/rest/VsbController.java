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
import it.cnit.blueprint.composer.vsb.VsbService;
import it.cnit.blueprint.composer.vsb.graph.VsbGraphService;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbLink;
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
@RequestMapping("/vsb")
public class VsbController {

  private final VsbGraphService vsbGraphService;
  private final VsbService vsbService;

  private final ZipService zipService;
  private final ObjectMapperService omService;

  @PostMapping("/addMgmt")
  @Operation(description = "Adds a management connectivity service to a VSB if not present")
  public VsBlueprint addMgmtConnService(@RequestBody @Valid VsBlueprint vsb) {
    validate(vsb);
    if (vsb.getConnectivityServices().stream().noneMatch(VsbLink::isManagement)) {
      vsbService.addMgmtConnServ(vsb);
    }
    validate(vsb);
    return vsb;
  }

  /**
   * Validate method. Serialization errors are handled by Spring
   *
   * @param vsb object to validate
   * @return 200 if valid, 400 with validation errors if invalid
   */
  @PostMapping("/validate")
  @Operation(description = "Validates a VSB")
  public void validate(@RequestBody @Valid VsBlueprint vsb) {
    try {
      vsb.isValid();
    } catch (MalformattedElementException e) {
      log.debug("Invalid VsBlueprint: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  @GetMapping("/schema")
  @Operation(description = "Generates the JSON Schema for a VSB")
  public JsonSchema schema() {
    try {
      return new JsonSchemaGenerator(omService.createIndentNsdWriter()).generateSchema(Nsd.class);
    } catch (JsonMappingException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @PostMapping("/graph")
  @Operation(description = "Generates PNG images to visualize the topology of a VSB",
      responses = @ApiResponse(
      responseCode = "200",
      description = "A zip file containing the images (PNG) representing the VSB in input",
      content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE),
      headers = @Header(name = HttpHeaders.CONTENT_DISPOSITION)))
  public ResponseEntity<InputStreamResource> graph(@RequestBody @Valid VsBlueprint vsb) {
    validate(vsb);
    File tempFile;
    try {
      tempFile = vsbGraphService.writeImageFile(vsb);
    } catch (IOException e) {
      log.error("Can not write file: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
    try {
      return zipService.getZipResponse(Collections.singletonList(tempFile), true);
    } catch (IOException e) {
      log.debug("Zip response error: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }
}
