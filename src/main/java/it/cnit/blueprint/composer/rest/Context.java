package it.cnit.blueprint.composer.rest;

import it.nextworks.nfvmano.catalogue.blueprint.messages.OnboardCtxBlueprintRequest;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class Context {

  @NotNull
  private OnboardCtxBlueprintRequest ctxbRequest;

  private Map<String, String> connectInput;
}
