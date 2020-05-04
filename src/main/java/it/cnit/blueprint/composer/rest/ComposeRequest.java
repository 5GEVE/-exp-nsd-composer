package it.cnit.blueprint.composer.rest;

import it.nextworks.nfvmano.catalogue.blueprint.messages.OnBoardVsBlueprintRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComposeRequest {

  @NotNull
  private OnBoardVsBlueprintRequest vsbRequest;
  @NotEmpty
  private Context[] contexts;
}
