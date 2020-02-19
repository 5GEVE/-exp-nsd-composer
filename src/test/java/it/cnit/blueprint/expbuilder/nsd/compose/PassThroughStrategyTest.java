package it.cnit.blueprint.expbuilder.nsd.compose;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.nsd.compose.NsdComposer.CompositionStrat;
import it.cnit.blueprint.expbuilder.nsd.graph.GraphVizExporter;
import it.cnit.blueprint.expbuilder.nsd.graph.NsdGraphService;
import it.cnit.blueprint.expbuilder.nsd.graph.ProfileVertex;
import it.cnit.blueprint.expbuilder.rest.CtxComposeInfo;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.junit.Test;

@Slf4j
public class PassThroughStrategyTest {

  @Test
  @SneakyThrows
  public void compose() {
    Properties prop = new Properties();
    InputStream input = ClassLoader.getSystemResourceAsStream("url.properties");
    prop.load(input);
    ObjectMapper oM = new ObjectMapper(new YAMLFactory());
    NsdGraphService nsdGraphService = new NsdGraphService(new GraphVizExporter());

    Nsd trackerNsd = oM.readValue(new URL(prop.getProperty("vsb.tracker.nsds")), Nsd[].class)[0];
    NsDf df = trackerNsd.getNsDf().get(0);
    NsLevel l = df.getNsInstantiationLevel().get(0);

    Nsd delayNsd = oM.readValue(new URL(prop.getProperty("ctx.delay.nsds")), Nsd[].class)[0];
    CtxComposeInfo ctxComposeInfo = new CtxComposeInfo();
//    ctxComposeInfo.setNsd(delayNsd);
//    ctxComposeInfo.setSapId("sap_tracking_mobile");
//    ctxComposeInfo.setStrat(CompositionStrat.PASSTHROUGH);
    log.debug("ctxComposeInfo dump:\n{}", oM.writeValueAsString(ctxComposeInfo));

    log.debug("Nsd before:\n{}", oM.writeValueAsString(trackerNsd));
    Graph<ProfileVertex, String> g = nsdGraphService.buildGraph(trackerNsd.getSapd(), df, l);
    log.debug("Graph export before:\n{}", nsdGraphService.export(g));
    new PassThroughStrategy().compose(trackerNsd, df, l, ctxComposeInfo);
    log.debug("Nsd after:\n{}", oM.writeValueAsString(trackerNsd));
    g = nsdGraphService.buildGraph(trackerNsd.getSapd(), df, l);
    log.debug("Graph export after:\n{}", nsdGraphService.export(g));

    // TODO assert
  }
}