package it.cnit.blueprint.expbuilder.master;

import it.cnit.blueprint.expbuilder.nsd.compose.NsdComposer;
import it.cnit.blueprint.expbuilder.rest.ConnectInput;
import it.cnit.blueprint.expbuilder.rest.CtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidCtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidNsd;
import it.nextworks.nfvmano.catalogue.blueprint.elements.Blueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.CtxBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbEndpoint;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnBoardVsBlueprintRequest;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
@AllArgsConstructor
public class MasterComposer {

  @Qualifier("PASS_THROUGH")
  private NsdComposer passThroughComposer;
  @Qualifier("CONNECT")
  private NsdComposer connectComposer;
  // TODO vsbComposer

  // TODO Composition Strategy comes from CtxB
  private static CompositionStrategy STRAT = CompositionStrategy.CONNECT;

  public void compose(OnBoardVsBlueprintRequest vsbRequest, CtxComposeInfo[] contexts)
      throws InvalidCtxComposeInfo, InvalidNsd {
    // Assumptions:
    // - The Vsb has only 1 Nsd.
    Nsd vsbNsd = vsbRequest.getNsds().get(0);
    for (CtxComposeInfo ctx : contexts) {
      // - The Ctx has only 1 Nsd.
      Nsd ctxNsd = ctx.getCtxBReq().getNsds().get(0);
      CtxBlueprint ctxB = ctx.getCtxBReq().getCtxBlueprint();
      if (ctx.getConnectInput() == null) {
        ctx.setConnectInput(new ConnectInput());
      }

      Sapd ranSapd = findRanSapd(vsbRequest.getVsBlueprint(), vsbNsd);
      NsVirtualLinkDesc vsbMgmtVld = findMgmtVld(ctxB, ctxNsd);
      NsVirtualLinkDesc ctxMgmtVld = findMgmtVld(ctxB, ctxNsd);
      if (STRAT.equals(CompositionStrategy.CONNECT)) {
        log.info("connect");
        connectComposer
            .compose(ctx.getConnectInput(), ranSapd, vsbMgmtVld, vsbNsd, ctxMgmtVld, ctxNsd);
      } else if (STRAT.equals(CompositionStrategy.PASS_THROUGH)) {
        log.info("pass_through");
        // compose Nsd
        if (ctxNsd.getVnfdId().size() == 1) {
          log.debug("ctxNsd has only one vnfdId.");
        } else {
          throw new InvalidCtxComposeInfo("More than one VNF found in Ctx for PASS_THROUGH");
        }
        passThroughComposer
            .compose(ctx.getConnectInput(), ranSapd, vsbMgmtVld, vsbNsd, ctxMgmtVld, ctxNsd);
        // TODO compose Exp blueprint
      } else {
        log.error("not supported");
        throw new InvalidCtxComposeInfo("Strategy x not supported.");
      }

    }
  }

  private NsVirtualLinkDesc findMgmtVld(Blueprint b, Nsd nsd) {
    // TODO Visit vlDesc in nsd and check if mgmt in b.
    // We need model modifications to make this work.
    return new NsVirtualLinkDesc();
  }

  private Sapd findRanSapd(Blueprint b, Nsd nsd) {
    for (VsbEndpoint e : b.getEndPoints()) {
      if (e.isRanConnection()) {
        for (Sapd sapd : nsd.getSapd()) {
          if (e.getEndPointId().equals(sapd.getCpdId())) {
            return sapd;
          }
        }
      }
    }
    // TODO exception invalid blueprint or nsd
    return null;
  }

}
