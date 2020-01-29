package it.cnit.blueprint.expbuilder.composer;

import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;

public class CtxComposeConnect extends CtxCompose {

  private String[] virtualLinkIds;

  public CtxComposeConnect(Nsd ctxNsd, String[] virtualLinkIds) {
    super(ctxNsd);
    this.virtualLinkIds = virtualLinkIds;
  }

  @Override
  public Nsd composeWith(Nsd nsd) {
    return null;
  }
}
