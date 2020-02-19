package it.cnit.blueprint.expbuilder.rest;

import it.nextworks.nfvmano.catalogue.blueprint.messages.OnboardCtxBlueprintRequest;
import lombok.Data;

@Data
public class CtxComposeInfo {

  private OnboardCtxBlueprintRequest ctxBReq;
  private VnfConnection[] ctxConnections;
  private VnfConnection[] vsConnections;

}
