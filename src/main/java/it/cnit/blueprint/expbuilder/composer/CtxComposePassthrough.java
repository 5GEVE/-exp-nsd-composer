package it.cnit.blueprint.expbuilder.composer;

import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;

public class CtxComposePassthrough extends CtxCompose {

  private String sapId;

  public CtxComposePassthrough(Nsd ctxNsd, String sapId) {
    super(ctxNsd);
    this.sapId = sapId;
  }

  @Override
  public Nsd composeWith(Nsd nsd) {
    return null;
  }
}
