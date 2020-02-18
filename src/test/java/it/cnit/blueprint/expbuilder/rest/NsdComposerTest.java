package it.cnit.blueprint.expbuilder.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.nsd.compose.ConnectStrategy;
import it.cnit.blueprint.expbuilder.nsd.compose.NsdComposer;
import it.cnit.blueprint.expbuilder.nsd.compose.PassThroughStrategy;
import it.cnit.blueprint.expbuilder.nsd.graph.GraphVizExporter;
import it.cnit.blueprint.expbuilder.nsd.graph.NsdGraphService;
import it.cnit.blueprint.expbuilder.nsd.compose.NsdComposer.CompositionStrat;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

@Slf4j
public class NsdComposerTest {

  private static ObjectMapper OBJECT_MAPPER;
  // Test input
  private static String vCDNPath;
  private static URL trackerURL, delayURL;
  private static NsdComposer nsdComposer;

  @BeforeClass
  public static void setUpClass() throws MalformedURLException {
    OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
    vCDNPath = "/nsd-examples/nsd_vCDN_pnf_gui.yaml";
    trackerURL = new URL(
        "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/vsb/vsb_ares2t_tracker/vsb_ares2t_tracker_nsds.yaml");
    delayURL = new URL(
        "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/ctx/ctx_delay/ctx_delay_nsds.yaml");
    nsdComposer = new NsdComposer(new NsdGraphService(new GraphVizExporter()),
        new ConnectStrategy(), new PassThroughStrategy());
  }

  // TODO move to another test class
//  @Test
//  public void buildGraphsExportVCdn() throws IOException {
//    Nsd vCDN = OBJECT_MAPPER.readValue(App.class.getResourceAsStream(vCDNPath), Nsd[].class)[0];
//    Composer vCdnComposer = new Composer(graphExporter, connectStrategy, passThroughStrategy);
//    vCdnComposer.init(vCDN);
//    assertNotEquals(0, vCdnComposer.getGraphMapKeys());
//    for (DfIlKey k : vCdnComposer.getGraphMapKeys()) {
//      log.debug("GraphViz export for '{}':\n{}", k.toString(), vCdnComposer.export(k));
//      String testFile = new Scanner(
//          App.class.getResourceAsStream(String.format("/%s.dot", k.getNsIlId())), "UTF-8")
//          .useDelimiter("\\A").next();
//      assertEquals(testFile, vCdnComposer.export(k));
//    }
//  }
//
//  @Test
//  public void buildGraphsExportAres2tTracker() throws IOException {
//    Nsd tracker = OBJECT_MAPPER.readValue(trackerURL, Nsd[].class)[0];
//    Composer trackerComposer = new Composer(graphExporter, connectStrategy, passThroughStrategy);
//    trackerComposer.init(tracker);
//    assertNotEquals(0, trackerComposer.getGraphMapKeys());
//    for (DfIlKey k : trackerComposer.getGraphMapKeys()) {
//      log.debug("GraphViz export for '{}':\n{}", k.toString(), trackerComposer.export(k));
//      String testFile = new Scanner(
//          App.class.getResourceAsStream(String.format("/%s.dot", k.getNsIlId())), "UTF-8")
//          .useDelimiter("\\A").next();
//      assertEquals(testFile, trackerComposer.export(k));
//    }
//  }

  @Test
  public void composeWith() {
    // TODO
  }

  @Test
  public void composeWithConnect() throws IOException, InvalidCtxComposeInfo, InvalidNsd {
    Nsd tracker = OBJECT_MAPPER.readValue(trackerURL, Nsd[].class)[0];
    Nsd delayNsd = OBJECT_MAPPER.readValue(delayURL, Nsd[].class)[0];
    Map<String, String> connections = new HashMap<>();
    connections.put("vnfp_netem", "vlp_vl_tracking_mobile");
    CtxComposeInfo ctxComposeInfo = new CtxComposeInfo();
    ctxComposeInfo.setNsd(delayNsd);
    ctxComposeInfo.setConnections(connections);
    ctxComposeInfo.setStrat(CompositionStrat.CONNECT);
    log.debug("ctxComposeInfo dump:\n{}", OBJECT_MAPPER.writeValueAsString(ctxComposeInfo));
    nsdComposer.composeWith(tracker, new CtxComposeInfo[]{ctxComposeInfo});
    // TODO
    // Check with ExpbNsd from the repo
  }

  @Test
  public void composeWithPassthrough() throws IOException, InvalidCtxComposeInfo, InvalidNsd {
    Nsd tracker = OBJECT_MAPPER.readValue(trackerURL, Nsd[].class)[0];
    Nsd delayNsd = OBJECT_MAPPER.readValue(delayURL, Nsd[].class)[0];
    CtxComposeInfo ctxComposeInfo = new CtxComposeInfo();
    ctxComposeInfo.setNsd(delayNsd);
    ctxComposeInfo.setSapId("sap_tracking_mobile");
    ctxComposeInfo.setStrat(CompositionStrat.PASSTHROUGH);
    log.info(OBJECT_MAPPER.writeValueAsString(ctxComposeInfo));
    nsdComposer.composeWith(tracker, new CtxComposeInfo[]{ctxComposeInfo});

    // TODO
    // Check with ExpbNsd from the repo
  }
}