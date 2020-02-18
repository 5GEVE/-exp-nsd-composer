package it.cnit.blueprint.expbuilder.nsdcompose;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.nsdcompose.NsdComposer.CompositionStrat;
import it.cnit.blueprint.expbuilder.nsdgraph.GraphVizExporter;
import it.cnit.blueprint.expbuilder.nsdgraph.NsdGraphService;
import it.cnit.blueprint.expbuilder.nsdgraph.ProfileVertex;
import it.cnit.blueprint.expbuilder.rest.CtxComposeInfo;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
public class ConnectStrategyTest {

  @Test
  @SneakyThrows
  public void compose() {
    Properties prop = new Properties();
    InputStream input = ClassLoader.getSystemResourceAsStream("url.properties");
    prop.load(input);
    ObjectMapper oM = new ObjectMapper(new YAMLFactory());
    NsdGraphService nsdGraphService = new NsdGraphService(new GraphVizExporter());

    Nsd trackerNsd = oM.readValue(new URL(prop.getProperty("tracker.url")), Nsd[].class)[0];
    NsDf df = trackerNsd.getNsDf().get(0);
    NsLevel l = df.getNsInstantiationLevel().get(0);

    Nsd delayNsd = oM.readValue(new URL(prop.getProperty("delay.url")), Nsd[].class)[0];
    Map<String, String> connections = new HashMap<>();
    connections.put("vnfp_netem", "vlp_vl_tracking_mobile");
    CtxComposeInfo ctxComposeInfo = new CtxComposeInfo();
    ctxComposeInfo.setNsd(delayNsd);
    ctxComposeInfo.setConnections(connections);
    ctxComposeInfo.setStrat(CompositionStrat.CONNECT);
    log.debug("ctxComposeInfo dump:\n{}", oM.writeValueAsString(ctxComposeInfo));

    log.debug("Nsd before:\n{}", oM.writeValueAsString(trackerNsd));
    Graph<ProfileVertex, String> g = nsdGraphService.buildGraph(trackerNsd.getSapd(), df, l);
    log.debug("Graph export before:\n{}", nsdGraphService.export(g));
    new ConnectStrategy().compose(trackerNsd, df, l, ctxComposeInfo);
    log.debug("Nsd after:\n{}", oM.writeValueAsString(trackerNsd));
    g = nsdGraphService.buildGraph(trackerNsd.getSapd(), df, l);
    log.debug("Graph export after:\n{}", nsdGraphService.export(g));

    // TODO assert
  }
}