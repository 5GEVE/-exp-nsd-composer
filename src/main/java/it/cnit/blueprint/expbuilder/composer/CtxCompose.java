package it.cnit.blueprint.expbuilder.composer;

import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;

public abstract class CtxCompose {

  private Nsd ctxNsd;

  public CtxCompose(Nsd ctxNsd) {
    this.ctxNsd = ctxNsd;
  }

  public abstract Nsd composeWith(Nsd nsd);

}
