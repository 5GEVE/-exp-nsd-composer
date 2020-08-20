package it.cnit.blueprint.composer.vsb.rest;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import it.cnit.blueprint.composer.vsb.VsbService;
import it.cnit.blueprint.composer.vsb.graph.VsbGraphService;
import it.cnit.blueprint.composer.vsb.graph.VsbVertex;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbLink;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
@AllArgsConstructor
public class VsbController {

  private final VsbGraphService vsbGraphService;
  private final VsbService vsbService;

  @PostMapping("/vsb/addMgmt")
  public VsBlueprint addMgmtConnService(@RequestBody VsBlueprint vsb) {
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
  @PostMapping("/vsb/validate")
  public void validate(@RequestBody VsBlueprint vsb) {
    try {
      vsb.isValid();
    } catch (MalformattedElementException e) {
      log.debug("Invalid VsBlueprint: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  @GetMapping("/vsb/schema")
  public JsonSchema schema() {
    ObjectMapper J_OBJECT_MAPPER = new ObjectMapper(new JsonFactory())
        .enable(SerializationFeature.INDENT_OUTPUT);
    try {
      return new JsonSchemaGenerator(J_OBJECT_MAPPER).generateSchema(VsBlueprint.class);
    } catch (JsonMappingException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @PostMapping("/vsb/graph")
  public Map<String, String> graph(@RequestBody VsBlueprint vsb) {
    Graph<VsbVertex, String> graph = vsbGraphService.buildGraph(vsb);
    return new HashMap<String, String>() {
      {
        put("graph", vsbGraphService.export(graph));
      }
    };
  }
}
