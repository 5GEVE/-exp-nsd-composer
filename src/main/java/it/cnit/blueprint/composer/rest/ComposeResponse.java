package it.cnit.blueprint.composer.rest;

import it.nextworks.nfvmano.catalogue.blueprint.elements.VsdNsdTranslationRule;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComposeResponse {

  @NotNull
  private Nsd expNsd;
  @NotNull
  private List<VsdNsdTranslationRule> translationRules;
}
