package it.cnit.blueprint.expbuilder.rest;

import it.cnit.blueprint.expbuilder.composer.Composer;
import it.cnit.blueprint.expbuilder.composer.Composer.CompositionStrat;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;

public class CtxComposeResource {

  private Nsd nsd;
  private Composer.CompositionStrat strat;
  private String SapId;
  private String[] VirtualLinkIds;

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

  public String[] getVirtualLinkIds() {
    return VirtualLinkIds;
  }

  public void setVirtualLinkIds(String[] virtualLinkIds) {
    VirtualLinkIds = virtualLinkIds;
  }
}
