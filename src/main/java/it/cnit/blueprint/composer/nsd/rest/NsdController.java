package it.cnit.blueprint.composer.nsd.rest;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.cnit.blueprint.composer.commons.ObjectMapperService;
import it.cnit.blueprint.composer.commons.ZipService;
import it.cnit.blueprint.composer.exceptions.ContextInvalidException;
import it.cnit.blueprint.composer.exceptions.NsdCompositionException;
import it.cnit.blueprint.composer.exceptions.NsdGenerationException;
import it.cnit.blueprint.composer.exceptions.NsdInvalidException;
import it.cnit.blueprint.composer.exceptions.VsbInvalidException;
import it.cnit.blueprint.composer.nsd.compose.NsdComposer;
import it.cnit.blueprint.composer.nsd.generate.NsdGenerator;
import it.cnit.blueprint.composer.nsd.graph.NsdGraphService;
import it.cnit.blueprint.composer.vsb.VsbService;
import it.cnit.blueprint.composer.vsb.rest.CtxController;
import it.cnit.blueprint.composer.vsb.rest.VsbController;
import it.nextworks.nfvmano.catalogue.blueprint.elements.Blueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.CompositionStrategy;
import it.nextworks.nfvmano.catalogue.blueprint.elements.CtxBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbEndpoint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbLink;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
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
@RequestMapping("/nsd")
public class NsdController {

  private final NsdGenerator nsdGenerator;
  private final NsdGraphService nsdGraphService;

  @Qualifier("PASS_THROUGH")
  private final NsdComposer passThroughComposer;
  @Qualifier("CONNECT")
  private final NsdComposer connectComposer;

  private final VsbService vsbService;
  private final VsbController vsbController;
  private final CtxController ctxController;

  private final ZipService zipService;
  private final ObjectMapperService omService;

  /**
   * @param httpEntity Here we manually deserialize the body to support any kind of Blueprint
   * @return The generated NSD
   */
  @PostMapping("/generate")
  @Operation(description = "Generates a NSD from a VSB",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "A VSB or CtxB in JSON format",
          content = @Content(schema = @Schema(implementation = Blueprint.class)),
          required = true))
  public Nsd generate(HttpEntity<String> httpEntity) {
    if (httpEntity.getBody() == null) {
      log.warn("Empty body for generate request");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty body");
    }
    Blueprint b;
    try {
      b = omService.createSafeBlueprintReader().readValue(httpEntity.getBody());
    } catch (IOException e) {
      log.error("Can not read JSON", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
    try {
      b.isValid();
    } catch (MalformattedElementException e) {
      log.warn("Invalid Blueprint: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
    try {
      return nsdGenerator.generate(b);
    } catch (NsdGenerationException e) {
      log.error("Error generating NSD", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @PostMapping("/generate/details")
  @Operation(description = "Generates a NSD with details from a VSB",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "A VSB or CtxB in JSON format",
          content = @Content(schema = @Schema(implementation = Blueprint.class)),
          required = true),
      responses = @ApiResponse(
          responseCode = "200",
          description = "A zip file containing the generated NSD (JSON) and the images representing it (PNG)",
          content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE),
          headers = @Header(name = HttpHeaders.CONTENT_DISPOSITION)))
  public ResponseEntity<InputStreamResource> generateDetails(HttpEntity<String> httpEntity) {
    return getDetailsResponse(generate(httpEntity));
  }

  @PostMapping("/compose")
  @Operation(description = "Composes a NSD with several contexts and returns a new NSD",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Translation rules are ignored, leave them null",
          required = true))
  public Nsd compose(@RequestBody @Valid ComposeRequest composeRequest) {
    VsBlueprint vsb = composeRequest.getVsbRequest().getVsBlueprint();
    vsbController.validate(vsb);
    Nsd expNsd = composeRequest.getVsbRequest().getNsds().get(0);
    validate(expNsd);
    expNsd.setNsdIdentifier(UUID.randomUUID().toString());
    expNsd.setNsdInvariantId(UUID.randomUUID().toString());
    expNsd.setDesigner(expNsd.getDesigner() + " + NSD Composer");

    try {
      // Assumptions:
      // - The Vsb has only 1 Nsd.
      NsVirtualLinkDesc ranVld = findRanVld(vsb, expNsd);
      for (Context ctx : composeRequest.getContexts()) {
        // - The Ctx has only 1 Nsd.
        CtxBlueprint ctxB = ctx.getCtxbRequest().getCtxBlueprint();
        ctxController.validate(ctxB);
        Nsd ctxNsd = ctx.getCtxbRequest().getNsds().get(0);
        validate(ctxNsd);

        log.info("Current CtxB: {}", ctxB.getBlueprintId());

        if (ctx.getConnectInput() == null) {
          ctx.setConnectInput(new HashMap<>());
        }
        NsVirtualLinkDesc expMgmtVld = findMgmtVld(vsb, expNsd);
        NsVirtualLinkDesc ctxMgmtVld = findMgmtVld(ctxB, ctxNsd);
        if (ctxB.getCompositionStrategy().equals(CompositionStrategy.CONNECT)) {
          log.info("Strategy is CONNECT");
          connectComposer
              .compose(ctx.getConnectInput(), ranVld, expMgmtVld, expNsd, ctxMgmtVld, ctxNsd);
        } else if (ctxB.getCompositionStrategy().equals(CompositionStrategy.PASS_THROUGH)) {
          log.info("Strategy is PASS_THROUGH");
          if (ctxNsd.getVnfdId().size() == 1) {
            log.info("ctxNsd has only one vnfdId.");
          } else {
            throw new ContextInvalidException("More than one VNFD ID found for PASS_THROUGH");
          }
          passThroughComposer
              .compose(ctx.getConnectInput(), ranVld, expMgmtVld, expNsd, ctxMgmtVld, ctxNsd);
        } else {
          String m = MessageFormatter.format("Composition strategy {} not supported.",
              ctxB.getCompositionStrategy().name())
              .getMessage();
          throw new ContextInvalidException(m);
        }

      }
    } catch (VsbInvalidException | NsdInvalidException | ContextInvalidException e) {
      log.warn("Invalid element: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
    } catch (NsdCompositionException e) {
      log.error("Error composing NSD", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

    return expNsd;
  }

  @PostMapping("/compose/details")
  @Operation(description = "Composes a NSD with several contexts and returns a new NSD with details",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Translation rules are ignored, leave them null"),
      responses = @ApiResponse(
          responseCode = "200",
          description = "A zip file containing the generated NSD (JSON) and the images representing it (PNG)",
          content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE),
          headers = @Header(name = HttpHeaders.CONTENT_DISPOSITION)))
  public ResponseEntity<InputStreamResource> composeDetails(
      @RequestBody @Valid ComposeRequest composeRequest) {
    return getDetailsResponse(compose(composeRequest));
  }

  /**
   * Validate an NSD. Serialization errors are handled by Spring
   *
   * @param nsd nsd to validate
   * @return 200 if valid, 400 with validation errors if invalid
   */
  @PostMapping("/validate")
  @Operation(description = "Validates a NSD")
  public void validate(@RequestBody @Valid Nsd nsd) {
    try {
      nsd.isValid();
    } catch (MalformattedElementException e) {
      log.warn("Invalid NSD: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  @GetMapping("/schema")
  @Operation(description = "Generates the JSON Schema for a NSD")
  public JsonSchema schema() {
    try {
      return new JsonSchemaGenerator(omService.createIndentNsdWriter()).generateSchema(Nsd.class);
    } catch (JsonMappingException e) {
      log.error("Error generating JSON Schema", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @PostMapping("/graph")
  @Operation(description = "Generates PNG images to visualize the topology of a NSD",
      responses = @ApiResponse(
          responseCode = "200",
          description = "A zip file containing the images (PNG) representing the NSD in input",
          content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE),
          headers = @Header(name = HttpHeaders.CONTENT_DISPOSITION)))
  public ResponseEntity<InputStreamResource> graph(@RequestBody @Valid Nsd nsd) {
    validate(nsd);
    List<File> graphs;
    try {
      graphs = nsdGraphService.writeImageFiles(nsd);
    } catch (NsdInvalidException e) {
      log.warn("Invalid NSD: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
    } catch (IOException e) {
      log.error("Can not write file", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
    try {
      return zipService.getZipResponse(graphs, true);
    } catch (IOException e) {
      log.error("Zip response error", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  private NsVirtualLinkDesc findRanVld(Blueprint b, Nsd nsd)
      throws NsdInvalidException, VsbInvalidException {
    List<VsbEndpoint> ranEps = b.getEndPoints().stream()
        .filter(VsbEndpoint::isRanConnection)
        .collect(Collectors.toList());
    if (ranEps.isEmpty()) {
      throw new VsbInvalidException(b.getBlueprintId(), "No RAN endpoint found in VSB");
    }
    for (VsbEndpoint rep : ranEps) {
      for (Sapd sapd : nsd.getSapd()) {
        if (rep.getEndPointId().equals(sapd.getCpdId())) {
          return connectComposer.getRanVlDesc(sapd, nsd);
        }
      }
    }
    throw new NsdInvalidException(nsd.getNsdIdentifier(),
        "RAN Sap not found for endpoints " +
            ranEps.stream()
                .map(VsbEndpoint::getEndPointId)
                .collect(Collectors.toList())
                .toString());
  }

  private NsVirtualLinkDesc findMgmtVld(Blueprint b, Nsd nsd)
      throws VsbInvalidException, NsdInvalidException {

    if (b.getConnectivityServices().stream().noneMatch(VsbLink::isManagement)) {
      vsbService.addMgmtConnServ(b);
    }
    List<VsbLink> mgmtConnServs = b.getConnectivityServices().stream()
        .filter(VsbLink::isManagement)
        .collect(Collectors.toList());
    if (mgmtConnServs.isEmpty()) {
      throw new VsbInvalidException(b.getBlueprintId(),
          "No management connectivity service found in VSB");
    }
    for (VsbLink cs : mgmtConnServs) {
      for (NsVirtualLinkDesc vld : nsd.getVirtualLinkDesc()) {
        if (cs.getName().equals(vld.getVirtualLinkDescId())) {
          return vld;
        }
      }
    }
    throw new NsdInvalidException(nsd.getNsdIdentifier(),
        "Management VLD not found for connectivity services " +
            mgmtConnServs.stream()
                .map(VsbLink::getName)
                .collect(Collectors.toList())
                .toString());
  }

  private ResponseEntity<InputStreamResource> getDetailsResponse(Nsd nsd) {
    List<File> files;
    try {
      files = nsdGraphService.writeImageFiles(nsd);
      File nsdFile = Files.createTempFile("nsd-", ".json").toFile();
      omService.createIndentNsdWriter().writeValue(nsdFile, nsd);
      files.add(nsdFile);
    } catch (NsdInvalidException e) {
      log.warn("Invalid NSD: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
    } catch (IOException e) {
      log.error("Can not write file", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
    try {
      return zipService.getZipResponse(files, true);
    } catch (IOException e) {
      log.error("Zip response error", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

}
