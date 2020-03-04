package it.cnit.blueprint.expbuilder.master;

import it.cnit.blueprint.expbuilder.nsd.compose.NsdComposer;
import it.cnit.blueprint.expbuilder.rest.CtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidCtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidNsd;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbEndpoint;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnBoardVsBlueprintRequest;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
@AllArgsConstructor
public class MasterComposer {

  private NsdComposer nsdComposer;
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
      if (STRAT.equals(CompositionStrategy.CONNECT)) {
        log.info("connect");
      } else if (STRAT.equals(CompositionStrategy.PASS_THROUGH)) {
        log.info("pass_through");
        // compose Nsd
        Sapd ranSapd = findRanSapd(vsbRequest.getVsBlueprint(), vsbNsd);
        NsVirtualLinkDesc ranVld = findSapVld(ranSapd, vsbNsd);
        nsdComposer.composePassThrough(ranVld, vsbNsd, ctxNsd);
        // compose Exp blueprint
      } else {
        log.error("not supported");
        throw new InvalidCtxComposeInfo("Strategy x not supported.");
      }

    }
  }

  private Sapd findRanSapd(VsBlueprint vsb, Nsd nsd) {
    for (VsbEndpoint e : vsb.getEndPoints()) {
      if (e.isRanConnection()) {
        for (Sapd sapd : nsd.getSapd()) {
          if (e.getEndPointId().equals(sapd.getCpdId())) {
            return sapd;
          }
        }
      }
    }
    // TODO exception
    return null;
  }

  private NsVirtualLinkDesc findSapVld(Sapd sapd, Nsd nsd) {
    for (NsVirtualLinkDesc vld : nsd.getVirtualLinkDesc()) {
      if (vld.getVirtualLinkDescId().equals(sapd.getNsVirtualLinkDescId())) {
        return vld;
      }
    }
    // TODO exception
    return null;
  }

}
