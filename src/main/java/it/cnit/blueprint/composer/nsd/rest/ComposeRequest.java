package it.cnit.blueprint.composer.nsd.rest;

import it.nextworks.nfvmano.catalogue.blueprint.messages.OnBoardVsBlueprintRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComposeRequest {

  @NotNull
  private OnBoardVsBlueprintRequest vsbRequest;

  @NotEmpty
  private Context[] contexts;
}
