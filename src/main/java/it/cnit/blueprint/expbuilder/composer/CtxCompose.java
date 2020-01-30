package it.cnit.blueprint.expbuilder.composer;

import it.cnit.blueprint.expbuilder.nsdgraph.NsdGraph;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;

public abstract class CtxCompose {

  protected NsdGraph ctxNsdG;

  public CtxCompose(Nsd ctxNsd) throws NotExistingEntityException {
    // We assume only one Df and one InstantiationLevel for contexts (one graph)
    this.ctxNsdG = new NsdGraph(ctxNsd, ctxNsd.getNsDf().get(0).getNsDfId(),
        ctxNsd.getNsDf().get(0).getDefaultNsInstantiationLevelId());
  }

  public abstract void composeWith(NsdGraph serviceNsdG);

}
