package it.cnit.blueprint.expbuilder.rest;

import it.cnit.blueprint.expbuilder.nsdcompose.NsdComposer.CompositionStrat;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.util.Map;
import lombok.Data;

@Data
public class CtxComposeInfo {

  private Nsd nsd;
  private CompositionStrat strat;
  private String SapId;
  // Map< ctxVnfProfileId, vsbVlProfileId>
  private Map<String, String> connections;

}
