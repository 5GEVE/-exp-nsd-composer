package it.cnit.blueprint.expbuilder.rest;

import it.nextworks.nfvmano.catalogue.blueprint.messages.OnBoardVsBlueprintRequest;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnboardCtxBlueprintRequest;
import lombok.Data;

@Data
public class ComposeRequest {

  public OnBoardVsBlueprintRequest vsbRequest;
  public OnboardCtxBlueprintRequest[] contexts;
}
