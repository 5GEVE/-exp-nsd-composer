package it.cnit.blueprint.composer.rest;

import it.cnit.blueprint.composer.exceptions.ContextInvalidException;
import it.cnit.blueprint.composer.exceptions.NsdInvalidException;
import it.cnit.blueprint.composer.exceptions.TransRuleInvalidException;
import it.cnit.blueprint.composer.exceptions.VsbInvalidException;
import it.cnit.blueprint.composer.nsd.compose.NsdComposer;
import it.cnit.blueprint.composer.rules.TranslationRulesComposer;
import it.nextworks.nfvmano.catalogue.blueprint.elements.Blueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.CompositionStrategy;
import it.nextworks.nfvmano.catalogue.blueprint.elements.CtxBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbEndpoint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbLink;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsdNsdTranslationRule;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnboardExpBlueprintRequest;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class ExperimentsController {

  @Qualifier("PASS_THROUGH")
  private final NsdComposer passThroughComposer;
  @Qualifier("CONNECT")
  private final NsdComposer connectComposer;

  private final TranslationRulesComposer translationRulesComposer;

  @GetMapping("/experiments")
  public OnboardExpBlueprintRequest retrieveExperiment() {
    return null;
  }

  @PostMapping("/experiments")
  public ComposeResponse composeExperiment(@RequestBody ComposeRequest composeRequest) {

    VsBlueprint vsb = composeRequest.getVsbRequest().getVsBlueprint();
    Nsd expNsd = composeRequest.getVsbRequest().getNsds().get(0);
    expNsd.setNsdIdentifier(UUID.randomUUID().toString());
    expNsd.setNsdInvariantId(UUID.randomUUID().toString());
    expNsd.setDesigner(expNsd.getDesigner() + " + NSD Generator");

    try {
      // Assumptions:
      // - The Vsb has only 1 Nsd.
      NsVirtualLinkDesc ranVld = findRanVld(composeRequest.getVsbRequest().getVsBlueprint(),
          expNsd);
      for (Context ctx : composeRequest.getContexts()) {
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
    }

    List<VsdNsdTranslationRule> translationRules = null;
    try {
      translationRules = translationRulesComposer
          .compose(expNsd, composeRequest.getVsbRequest().getTranslationRules());
    } catch (TransRuleInvalidException e) {
      // TODO create proper error response.
      e.printStackTrace();
    }
    return new ComposeResponse(expNsd, translationRules);
  }

  private NsVirtualLinkDesc findRanVld(Blueprint b, Nsd nsd)
      throws NsdInvalidException, VsbInvalidException {
    Optional<VsbEndpoint> ranEp = b.getEndPoints().stream()
        .filter(e -> e.isRanConnection() && e.getEndPointId().contains("sap"))
        .findFirst();
    if (ranEp.isPresent()) {
      String epId = ranEp.get().getEndPointId();
      Optional<Sapd> ranSapd = nsd.getSapd().stream()
          .filter(sapd -> sapd.getCpdId().equals(epId))
          .findFirst();
      if (ranSapd.isPresent()) {
        return connectComposer.getRanVlDesc(ranSapd.get(), nsd);
      } else {
        throw new NsdInvalidException(nsd.getNsdIdentifier(),
            "RAN Sap with ID " + epId + " not found");
      }
    } else {
      throw new VsbInvalidException("No RAN endpoint found in VSB " + b.getBlueprintId() + ".");
    }
  }

  private NsVirtualLinkDesc findMgmtVld(Blueprint b, Nsd nsd)
      throws VsbInvalidException, NsdInvalidException {
    Optional<VsbLink> optConnServ = b.getConnectivityServices().stream()
        .filter(VsbLink::isManagement)
        .findFirst();
    if (optConnServ.isPresent()) {
      String name = optConnServ.get().getName();
      Optional<NsVirtualLinkDesc> optVld = nsd.getVirtualLinkDesc().stream()
          .filter(vld -> vld.getVirtualLinkDescId().equals(name))
          .findFirst();
      if (optVld.isPresent()) {
        return optVld.get();
      } else {
        throw new NsdInvalidException(nsd.getNsdIdentifier(),
            "Management Vld with id=" + name + "not found");
      }
    } else {
      throw new VsbInvalidException(
          "No management connectivity service found in VSB " + b.getBlueprintId() + ".");
    }
  }
}
