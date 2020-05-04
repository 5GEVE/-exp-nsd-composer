package it.cnit.blueprint.composer.rest;

import it.nextworks.nfvmano.catalogue.blueprint.messages.OnBoardVsBlueprintRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
public class ComposeRequest {

  @NonNull
  @NotNull
  private OnBoardVsBlueprintRequest vsbRequest;

  @NonNull
  @NotEmpty
  private Context[] contexts;
}
