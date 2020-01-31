package it.cnit.blueprint.expbuilder.rest;

import it.cnit.blueprint.expbuilder.compose.ComposableNsd.CompositionStrat;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.util.Map;

public class CtxComposeResource {

  private Nsd nsd;
  private CompositionStrat strat;
  private String SapId;
  // Map<vnfProfileId, vlProfileId>
  private Map<String, String> VirtualLinkIds;

  public Nsd getNsd() {
    return nsd;
  }

  public void setNsd(Nsd nsd) {
    this.nsd = nsd;
  }

  public CompositionStrat getStrat() {
    return strat;
  }

  public void setStrat(CompositionStrat strat) {
    this.strat = strat;
  }

  public String getSapId() {
    return SapId;
  }

  public void setSapId(String sapId) {
    SapId = sapId;
  }

  public Map<String, String> getVirtualLinkIds() {
    return VirtualLinkIds;
  }

  public void setVirtualLinkIds(Map<String, String> virtualLinkIds) {
    VirtualLinkIds = virtualLinkIds;
  }
}
