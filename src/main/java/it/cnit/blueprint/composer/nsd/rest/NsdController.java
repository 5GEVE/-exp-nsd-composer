package it.cnit.blueprint.composer.nsd.rest;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import it.cnit.blueprint.composer.exceptions.ContextInvalidException;
import it.cnit.blueprint.composer.exceptions.NsdCompositionException;
import it.cnit.blueprint.composer.exceptions.NsdGenerationException;
import it.cnit.blueprint.composer.exceptions.NsdInvalidException;
import it.cnit.blueprint.composer.exceptions.TransRuleCompositionException;
import it.cnit.blueprint.composer.exceptions.TransRuleInvalidException;
import it.cnit.blueprint.composer.exceptions.VsbInvalidException;
import it.cnit.blueprint.composer.nsd.compose.NsdComposer;
import it.cnit.blueprint.composer.nsd.generate.NsdGenerator;
import it.cnit.blueprint.composer.nsd.graph.NsdGraphService;
import it.cnit.blueprint.composer.nsd.graph.ProfileVertex;
import it.cnit.blueprint.composer.rules.TranslationRulesComposer;
import it.cnit.blueprint.composer.vsb.VsbService;
import it.nextworks.nfvmano.catalogue.blueprint.elements.Blueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.CompositionStrategy;
import it.nextworks.nfvmano.catalogue.blueprint.elements.CtxBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbEndpoint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbLink;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsdNsdTranslationRule;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
@AllArgsConstructor
public class NsdController {

  private final NsdGenerator nsdGenerator;
  private final NsdGraphService nsdGraphService;

  @Qualifier("PASS_THROUGH")
  private final NsdComposer passThroughComposer;
  @Qualifier("CONNECT")
  private final NsdComposer connectComposer;

  private final VsbService vsbService;
  private final TranslationRulesComposer translationRulesComposer;

  @PostMapping("/nsd/generate")
  public Nsd generate(@RequestBody VsBlueprint vsb) {
    try {
      return nsdGenerator.generate(vsb);
    } catch (NsdGenerationException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @PostMapping("/nsd/compose")
  public ComposeResponse compose(@RequestBody ComposeRequest composeRequest) {
    VsBlueprint vsb = composeRequest.getVsbRequest().getVsBlueprint();
    Nsd expNsd = composeRequest.getVsbRequest().getNsds().get(0);
    List<VsdNsdTranslationRule> vsbTransRules = composeRequest.getVsbRequest()
        .getTranslationRules();
    expNsd.setNsdIdentifier(UUID.randomUUID().toString());
    expNsd.setNsdInvariantId(UUID.randomUUID().toString());
    expNsd.setDesigner(expNsd.getDesigner() + " + NSD Composer");

    Context[] contexts = composeRequest.getContexts();

    try {
      // Assumptions:
      // - The Vsb has only 1 Nsd.
      NsVirtualLinkDesc ranVld = findRanVld(vsb, expNsd);
      for (Context ctx : contexts) {
        // - The Ctx has only 1 Nsd.
        CtxBlueprint ctxB = ctx.getCtxbRequest().getCtxBlueprint();
        Nsd ctxNsd = ctx.getCtxbRequest().getNsds().get(0);

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
            log.debug("ctxNsd has only one vnfdId.");
          } else {
            throw new ContextInvalidException("More than one VNFD ID found for PASS_THROUGH");
          }
          passThroughComposer
              .compose(ctx.getConnectInput(), ranVld, expMgmtVld, expNsd, ctxMgmtVld, ctxNsd);
        } else {
          String m = MessageFormatter.format("Composition strategy {} not supported.",
              ctxB.getCompositionStrategy().name())
              .getMessage();
          log.error(m);
          throw new ContextInvalidException(m);
        }

      }
    } catch (VsbInvalidException | NsdInvalidException | ContextInvalidException e) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
    } catch (NsdCompositionException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

    List<VsdNsdTranslationRule> expTransRules;
    try {
      expTransRules = translationRulesComposer.compose(expNsd, vsbTransRules);
    } catch (TransRuleInvalidException | TransRuleCompositionException e) {
      log.warn("{}. Return empty translation rules.", e.getMessage());
      expTransRules = new ArrayList<>();
    }
    return new ComposeResponse(expNsd, expTransRules);
  }

  /**
   * Validate an NSD. Serialization errors are handled by Spring
   *
   * @param nsd nsd to validate
   * @return 200 if valid, 400 with validation errors if invalid
   */
  @PostMapping("/nsd/validate")
  public void validate(@RequestBody Nsd nsd) {
    try {
      nsd.isValid();
    } catch (MalformattedElementException e) {
      log.debug("Invalid NSD: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  @GetMapping("/nsd/schema")
  public JsonSchema schema() throws JsonProcessingException {
    ObjectMapper J_OBJECT_MAPPER = new ObjectMapper(new JsonFactory())
        .enable(SerializationFeature.INDENT_OUTPUT);
    return new JsonSchemaGenerator(J_OBJECT_MAPPER).generateSchema(Nsd.class);
  }

  @PostMapping("/nsd/graph")
  public List<GraphResponse> graph(@RequestBody Nsd nsd) {
    ArrayList<GraphResponse> graphs = new ArrayList<>();
    for (NsDf nsDf : nsd.getNsDf()) {
      for (NsLevel nsLvl : nsDf.getNsInstantiationLevel()) {
        try {
          Graph<ProfileVertex, String> graph = nsdGraphService
              .buildGraph(nsd.getSapd(), nsDf, nsLvl);
          graphs.add(
              new GraphResponse(nsDf.getNsDfId(), nsLvl.getNsLevelId(),
                  nsdGraphService.export(graph).replace("\n", "").replace("\r", "")));
        } catch (NsdInvalidException e) {
          throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
        }
      }
    }
    return graphs;
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
}
