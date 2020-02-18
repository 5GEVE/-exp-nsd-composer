package it.cnit.blueprint.expbuilder.rest;

import it.cnit.blueprint.expbuilder.compose.CompositionStrategy;
import it.cnit.blueprint.expbuilder.nsdgraph.NsdGraphService;
import it.cnit.blueprint.expbuilder.nsdgraph.ProfileVertex;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.jgrapht.Graph;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
public class Composer {
  //TODO rename to NsdComposer

  private NsdGraphService nsdGraphService;

  @Qualifier("connect")
  private CompositionStrategy connectStrategy;

  @Qualifier("passthrough")
  private CompositionStrategy passThroughStrategy;

  public enum CompositionStrat {
    CONNECT,
    PASSTHROUGH
  }

  public Composer(NsdGraphService nsdGraphService,
      CompositionStrategy connectStrategy,
      CompositionStrategy passThroughStrategy) {
    this.nsdGraphService = nsdGraphService;
    this.connectStrategy = connectStrategy;
    this.passThroughStrategy = passThroughStrategy;
  }


  public void composeWith(Nsd nsd, CtxComposeInfo[] ctxRArray)
      throws InvalidCtxComposeInfo, InvalidNsd {
    for (CtxComposeInfo ctxR : ctxRArray) {
      for (NsDf df : nsd.getNsDf()) {
        for (NsLevel l : df.getNsInstantiationLevel()) {
          Graph<ProfileVertex, String> g = nsdGraphService.buildGraph(nsd.getSapd(), df, l);
          log.debug("Graph export before:\n{}", nsdGraphService.export(g));
          if (ctxR.getStrat() == CompositionStrat.CONNECT) {
            connectStrategy.compose(nsd, df.getNsDfId(), l.getNsLevelId(), ctxR, g);
          } else if (ctxR.getStrat() == CompositionStrat.PASSTHROUGH) {
            passThroughStrategy.compose(nsd, df.getNsDfId(), l.getNsLevelId(), ctxR, g);
          } else {
            throw new NotImplementedException(
                String.format("Composition strategy %s not implemented", ctxR.getStrat()));
          }
          g = nsdGraphService.buildGraph(nsd.getSapd(), df, l);
          log.debug("Graph export after:\n{}", nsdGraphService.export(g));
        }
      }
    }
  }

}
