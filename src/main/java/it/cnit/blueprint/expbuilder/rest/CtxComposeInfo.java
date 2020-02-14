package it.cnit.blueprint.expbuilder.rest;

import it.nextworks.nfvmano.catalogue.blueprint.messages.OnboardCtxBlueprintRequest;
import java.util.Map;
import lombok.Data;

@Data
public class CtxComposeInfo {

  private OnboardCtxBlueprintRequest ctxB;
  private String SapId;
  // Map<vnfProfileId, vlProfileId>
  private Map<String, String> VirtualLinkIds;

}
