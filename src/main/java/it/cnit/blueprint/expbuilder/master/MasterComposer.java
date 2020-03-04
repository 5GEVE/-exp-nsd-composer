package it.cnit.blueprint.expbuilder.master;

import it.cnit.blueprint.expbuilder.nsd.compose.NsdComposer;
import it.cnit.blueprint.expbuilder.rest.CtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidCtxComposeInfo;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnBoardVsBlueprintRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
@AllArgsConstructor
public class MasterComposer {

  private NsdComposer nsdComposer;
  // TODO vsbComposer

  // TODO Composition Strategy comes from CtxB
  private static CompositionStrategy STRAT = CompositionStrategy.CONNECT;

  public void compose(OnBoardVsBlueprintRequest vsbRequest, CtxComposeInfo[] contexts)
      throws InvalidCtxComposeInfo {
    for (CtxComposeInfo ctx : contexts) {
      if (STRAT.equals(CompositionStrategy.CONNECT)){
        log.info("connect");
      } else if (STRAT.equals(CompositionStrategy.PASS_THROUGH)){
        log.info("pass_through");
      } else {
        log.error("not supported");
        throw new InvalidCtxComposeInfo("Strategy x not supported.");
      }

    }
  }

}
