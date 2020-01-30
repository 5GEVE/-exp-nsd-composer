package it.cnit.blueprint.expbuilder.composer;

import it.cnit.blueprint.expbuilder.nsdgraph.NsdGraph;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;

public class CtxComposeConnect extends CtxCompose {

  private String[] virtualLinkIds;

  public CtxComposeConnect(Nsd ctxNsd, String[] virtualLinkIds) throws NotExistingEntityException {
    super(ctxNsd);
    this.virtualLinkIds = virtualLinkIds;
  }

  @Override
  public void composeWith(NsdGraph serviceNsdG) {

  }
}
