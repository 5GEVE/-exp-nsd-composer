package it.cnit.blueprint.expbuilder.rest;

import it.nextworks.nfvmano.catalogue.blueprint.messages.OnboardCtxBlueprintRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CtxComposeInfo {

  private OnboardCtxBlueprintRequest ctxbRequest;
  private ConnectInput connectInput;
}
