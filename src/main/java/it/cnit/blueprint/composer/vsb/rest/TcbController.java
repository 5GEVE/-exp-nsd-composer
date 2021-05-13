package it.cnit.blueprint.composer.vsb.rest;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import io.swagger.v3.oas.annotations.Operation;
import it.cnit.blueprint.composer.commons.ObjectMapperService;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnboardTestCaseBlueprintRequest;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/tcb")
public class TcbController {

  private final ObjectMapperService omService;

  private final Pattern paramPattern = Pattern.compile("\\$\\$([^\\s|^:|^;]*)");

  /**
   * Validate method. Serialization errors are handled by Spring
   *
   * @param tcbR object to validate
   * @return 200 if valid, 400 with validation errors if invalid
   */
  @PostMapping("/validate")
  @Operation(description = "Validates a TcB")
  public void validate(@RequestBody @Valid OnboardTestCaseBlueprintRequest tcbR) {
    try {
      tcbR.isValid();
      // Check params
      String confScript = tcbR.getTestCaseBlueprint().getConfigurationScript();
      if (confScript != null) {
        Matcher mConf = paramPattern.matcher(confScript);
        while (mConf.find()) {
          String gr = mConf.group();
          if (!tcbR.getTestCaseBlueprint().getUserParameters().containsValue(mConf.group())
              && !tcbR.getTestCaseBlueprint().getInfrastructureParameters().containsKey(mConf.group())) {
            throw new MalformattedElementException(
                "Parameter '" + mConf.group() + "' in configurationScript is not declared");
          }
        }
      }
      Matcher mExec = paramPattern.matcher(tcbR.getTestCaseBlueprint().getExecutionScript());
      while (mExec.find()) {
        String gr = mExec.group();
        if (!tcbR.getTestCaseBlueprint().getUserParameters().containsValue(mExec.group())
            && !tcbR.getTestCaseBlueprint().getInfrastructureParameters().containsKey(mExec.group())) {
          throw new MalformattedElementException(
              "Parameter '" + mExec.group() + "' in executionScript is not declared");
        }
      }
    } catch (MalformattedElementException e) {
      log.warn("Invalid TestCaseBlueprint: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  @GetMapping("/schema")
  @Operation(description = "Generates the JSON Schema for a TcB")
  public JsonSchema schema() {
    try {
      return new JsonSchemaGenerator(omService.createIndentNsdWriter())
          .generateSchema(OnboardTestCaseBlueprintRequest.class);
    } catch (JsonMappingException e) {
      log.error("Error generating JSON Schema", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }
}
