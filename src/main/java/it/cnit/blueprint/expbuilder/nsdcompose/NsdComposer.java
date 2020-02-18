package it.cnit.blueprint.expbuilder.nsdcompose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.nsdcompose.CompositionStrategy;
import it.cnit.blueprint.expbuilder.nsdgraph.NsdGraphService;
import it.cnit.blueprint.expbuilder.nsdgraph.ProfileVertex;
import it.cnit.blueprint.expbuilder.rest.CtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidCtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidNsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
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
@AllArgsConstructor
public class NsdComposer {

  private NsdGraphService nsdGraphService;

  @Qualifier("connect")
  private CompositionStrategy connectStrategy;

  @Qualifier("passthrough")
  private CompositionStrategy passThroughStrategy;

  public enum CompositionStrat {
    CONNECT,
    PASSTHROUGH
  }

  @SneakyThrows(JsonProcessingException.class)
  public void composeWith(Nsd nsd, CtxComposeInfo[] ctxRArray)
      throws InvalidCtxComposeInfo, InvalidNsd {
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    for (CtxComposeInfo ctxR : ctxRArray) {
      for (NsDf df : nsd.getNsDf()) {
        for (NsLevel l : df.getNsInstantiationLevel()) {
          log.debug("Nsd before:\n{}", objectMapper.writeValueAsString(nsd));
          Graph<ProfileVertex, String> g = nsdGraphService.buildGraph(nsd.getSapd(), df, l);
          log.debug("Graph export before:\n{}", nsdGraphService.export(g));
          if (ctxR.getStrat() == CompositionStrat.CONNECT) {
            connectStrategy.compose(nsd, df, l, ctxR);
          } else if (ctxR.getStrat() == CompositionStrat.PASSTHROUGH) {
            passThroughStrategy.compose(nsd, df, l, ctxR);
          } else {
            throw new NotImplementedException(
                String.format("Composition strategy %s not implemented", ctxR.getStrat()));
          }
          log.debug("Nsd after:\n{}", objectMapper.writeValueAsString(nsd));
          g = nsdGraphService.buildGraph(nsd.getSapd(), df, l);
          log.debug("Graph export after:\n{}", nsdGraphService.export(g));
        }
      }
    }
  }

}
