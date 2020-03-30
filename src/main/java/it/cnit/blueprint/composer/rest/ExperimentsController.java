package it.cnit.blueprint.composer.rest;

import it.cnit.blueprint.composer.nsd.compose.NsdComposer;
import it.cnit.blueprint.composer.rules.InvalidTranslationRuleException;
import it.cnit.blueprint.composer.rules.TranslationRulesComposer;
import it.nextworks.nfvmano.catalogue.blueprint.elements.Blueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.CtxBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbEndpoint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsdNsdTranslationRule;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnboardExpBlueprintRequest;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@AllArgsConstructor
public class ExperimentsController {

  // TODO Composition Strategy comes from CtxB
  private static CompositionStrategy STRAT = CompositionStrategy.CONNECT;

  @Qualifier("PASS_THROUGH")
  private NsdComposer passThroughComposer;
  @Qualifier("CONNECT")
  private NsdComposer connectComposer;

  private TranslationRulesComposer translationRulesComposer;

  @GetMapping("/experiments")
  public OnboardExpBlueprintRequest retrieveExperiment() {
    return null;
  }

  @PostMapping("/experiments")
  public ComposeResponse composeExperiment(@RequestBody ComposeRequest composeRequest) {

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
        Nsd ctxNsd = ctx.getCtxbRequest().getNsds().get(0);
        CtxBlueprint ctxB = ctx.getCtxbRequest().getCtxBlueprint();
        if (ctx.getConnectInput() == null) {
          ctx.setConnectInput(new ConnectInput());
        }

        NsVirtualLinkDesc expMgmtVld = findMgmtVld(ctxB, ctxNsd);
        NsVirtualLinkDesc ctxMgmtVld = findMgmtVld(ctxB, ctxNsd);
        if (STRAT.equals(CompositionStrategy.CONNECT)) {
          log.info("connect");
          connectComposer
              .compose(ctx.getConnectInput(), ranVld, expMgmtVld, expNsd, ctxMgmtVld, ctxNsd);
        } else if (STRAT.equals(CompositionStrategy.PASS_THROUGH)) {
          log.info("pass_through");
          if (ctxNsd.getVnfdId().size() == 1) {
            log.debug("ctxNsd has only one vnfdId.");
          } else {
            throw new InvalidContextException("More than one VNF found in Ctx for PASS_THROUGH");
          }
          passThroughComposer
              .compose(ctx.getConnectInput(), ranVld, expMgmtVld, expNsd, ctxMgmtVld, ctxNsd);
        } else {
          String m = MessageFormatter.format("Composition strategy {} not supported.", STRAT)
              .getMessage();
          log.error(m);
          throw new InvalidContextException(m);
        }

      }
    } catch (InvalidNsdException | InvalidContextException e) {
      log.error(e.getMessage());
      //TODO create and return a 422 response.
    }

    List<VsdNsdTranslationRule> translationRules = null;
    try {
      translationRules = translationRulesComposer
          .compose(expNsd, composeRequest.getVsbRequest().getTranslationRules());
    } catch (InvalidTranslationRuleException e) {
      // TODO create proper error response.
      e.printStackTrace();
    }
    return new ComposeResponse(expNsd, translationRules);
  }

  private NsVirtualLinkDesc findRanVld(Blueprint b, Nsd nsd) throws InvalidNsdException {
    Sapd ranSapd = null;
    for (VsbEndpoint e : b.getEndPoints()) {
      if (e.isRanConnection()) {
        for (Sapd sapd : nsd.getSapd()) {
          if (e.getEndPointId().equals(sapd.getCpdId())) {
            ranSapd = sapd;
            break;
          }
        }
      }
    }
    if (ranSapd == null) {
      // TODO think of a better message
      throw new InvalidNsdException("Cannot find a Sap descriptor for RAN.");
    }
    return connectComposer.getRanVlDesc(ranSapd, nsd);
  }

  private NsVirtualLinkDesc findMgmtVld(Blueprint b, Nsd nsd) {
    // TODO Visit vlDesc in nsd and check if mgmt in connectivityServices of b.
    // We need model modifications to make this work.
    return new NsVirtualLinkDesc();
  }
}
