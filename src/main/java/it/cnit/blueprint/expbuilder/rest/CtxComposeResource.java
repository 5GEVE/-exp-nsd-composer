package it.cnit.blueprint.expbuilder.rest;

import it.cnit.blueprint.expbuilder.compose.ComposableNsd.CompositionStrat;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.util.Map;
import lombok.Data;

@Data
public class CtxComposeResource {

  private Nsd nsd;
  private CompositionStrat strat;
  private String SapId;
  // Map<vnfProfileId, vlProfileId>
  private Map<String, String> VirtualLinkIds;

}
