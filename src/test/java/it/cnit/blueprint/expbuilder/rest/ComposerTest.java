package it.cnit.blueprint.expbuilder.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.App;
import it.cnit.blueprint.expbuilder.compose.ConnectStrategy;
import it.cnit.blueprint.expbuilder.compose.PassThroughStrategy;
import it.cnit.blueprint.expbuilder.nsdgraph.GraphExporter;
import it.cnit.blueprint.expbuilder.nsdgraph.GraphVizExporter;
import it.cnit.blueprint.expbuilder.rest.Composer.CompositionStrat;
import it.cnit.blueprint.expbuilder.rest.Composer.DfIlKey;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

@Slf4j
public class ComposerTest {

  private static ObjectMapper OBJECT_MAPPER;
  // Test input
  private static String vCDNPath;
  private static URL trackerURL, delayURL;
  private static GraphExporter graphExporter;
  private static ConnectStrategy connectStrategy;
  private static PassThroughStrategy passThroughStrategy;

  @BeforeClass
  public static void setUpClass() throws MalformedURLException {
    OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
    vCDNPath = "/nsd-examples/nsd_vCDN_pnf_gui.yaml";
    trackerURL = new URL(
        "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/vsb/vsb_ares2t_tracker/vsb_ares2t_tracker_nsds.yaml");
    delayURL = new URL(
        "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/ctx/ctx_delay/ctx_delay_nsds.yaml");
    graphExporter = new GraphVizExporter();
    connectStrategy = new ConnectStrategy();
    passThroughStrategy = new PassThroughStrategy();
  }

  @Test
  public void buildGraphsExportVCdn() throws IOException {
    Nsd vCDN = OBJECT_MAPPER.readValue(App.class.getResourceAsStream(vCDNPath), Nsd[].class)[0];
    Composer vCdnComposer = new Composer(graphExporter, connectStrategy, passThroughStrategy);
    vCdnComposer.init(vCDN);
    assertNotEquals(0, vCdnComposer.getGraphMapKeys());
    for (DfIlKey k : vCdnComposer.getGraphMapKeys()) {
      log.debug("GraphViz export for '{}':\n{}", k.toString(), vCdnComposer.export(k));
      String testFile = new Scanner(
          App.class.getResourceAsStream(String.format("/%s.dot", k.getNsIlId())), "UTF-8")
          .useDelimiter("\\A").next();
      assertEquals(testFile, vCdnComposer.export(k));
    }
  }

  @Test
  public void buildGraphsExportAres2tTracker() throws IOException {
    Nsd tracker = OBJECT_MAPPER.readValue(trackerURL, Nsd[].class)[0];
    Composer trackerComposer = new Composer(graphExporter, connectStrategy, passThroughStrategy);
    trackerComposer.init(tracker);
    assertNotEquals(0, trackerComposer.getGraphMapKeys());
    for (DfIlKey k : trackerComposer.getGraphMapKeys()) {
      log.debug("GraphViz export for '{}':\n{}", k.toString(), trackerComposer.export(k));
      String testFile = new Scanner(
          App.class.getResourceAsStream(String.format("/%s.dot", k.getNsIlId())), "UTF-8")
          .useDelimiter("\\A").next();
      assertEquals(testFile, trackerComposer.export(k));
    }
  }

  @Test
  public void composeWith() {
    // TODO
  }

  @Test
  public void composeWithConnect()
      throws IOException, InvalidCtxComposeInfo, NotExistingEntityException {
    Nsd tracker = OBJECT_MAPPER.readValue(trackerURL, Nsd[].class)[0];
    Composer trackerComposer = new Composer(graphExporter, connectStrategy, passThroughStrategy);
    trackerComposer.init(tracker);
    assertNotEquals(0, trackerComposer.getGraphMapKeys());
    Nsd delayNsd = OBJECT_MAPPER.readValue(delayURL, Nsd[].class)[0];
    Map<String, String> connections = new HashMap<>();
    connections.put("vnfp_netem", "vlp_vl_tracking_mobile");
    CtxComposeInfo ctxComposeInfo = new CtxComposeInfo();
    ctxComposeInfo.setNsd(delayNsd);
    ctxComposeInfo.setConnections(connections);
    ctxComposeInfo.setStrat(CompositionStrat.CONNECT);
    log.debug("ctxComposeInfo dump:\n{}", OBJECT_MAPPER.writeValueAsString(ctxComposeInfo));
    trackerComposer.composeWith(new CtxComposeInfo[]{ctxComposeInfo});
    // TODO
    // Check with ExpbNsd from the repo
  }

  @Test
  public void composeWithPassthrough() throws NotExistingEntityException, IOException {
    Nsd tracker = OBJECT_MAPPER.readValue(trackerURL, Nsd[].class)[0];
    Composer trackerComposer = new Composer(graphExporter, connectStrategy, passThroughStrategy);
    trackerComposer.init(tracker);
    assertNotEquals(0, trackerComposer.getGraphMapKeys());
    Nsd delayNsd = OBJECT_MAPPER.readValue(delayURL, Nsd[].class)[0];
    CtxComposeInfo ctxComposeInfo = new CtxComposeInfo();
    ctxComposeInfo.setNsd(delayNsd);
    ctxComposeInfo.setSapId("sap_tracking_mobile");
    ctxComposeInfo.setStrat(CompositionStrat.PASSTHROUGH);
    log.info(OBJECT_MAPPER.writeValueAsString(ctxComposeInfo));
    trackerComposer.composeWithPassthrough(ctxComposeInfo);

    // TODO
    // Check with ExpbNsd from the repo
  }
}