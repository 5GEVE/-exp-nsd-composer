package it.cnit.blueprint.expbuilder.rest;

import it.nextworks.nfvmano.catalogue.blueprint.messages.OnBoardVsBlueprintRequest;
import java.util.List;
import lombok.Data;

@Data
public class ComposeRequest {

  public OnBoardVsBlueprintRequest vsbRequest;
  public CtxComposeInfo[] contexts;

}
