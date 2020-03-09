package it.cnit.blueprint.expbuilder.rest;

import it.nextworks.nfvmano.catalogue.blueprint.messages.OnBoardVsBlueprintRequest;
import lombok.Data;

@Data
public class ComposeRequest {

  public OnBoardVsBlueprintRequest vsbRequest;
  public CtxComposeInfo[] contexts;
}
