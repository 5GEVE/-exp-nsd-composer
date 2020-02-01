package it.cnit.blueprint.expbuilder.compose;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.App;
import it.cnit.blueprint.expbuilder.compose.ComposableNsd.CompositionStrat;
import it.cnit.blueprint.expbuilder.compose.ComposableNsd.DfIlKey;
import it.cnit.blueprint.expbuilder.nsdgraph.GraphVizExporter;
import it.cnit.blueprint.expbuilder.rest.CtxComposeResource;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComposableNsdTest {

  final Logger LOG = LoggerFactory.getLogger(ComposableNsdTest.class);

  private ObjectMapper OBJECT_MAPPER;
  private URL vsbAres2tTrackerNsds;
  private String nsdVcdnPnfGui;
  private URL ctxDelayNsds;

  @Before
  public void setUp() throws Exception {
    OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
    vsbAres2tTrackerNsds = new URL(
        "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/vsb/vsb_ares2t_tracker/vsb_ares2t_tracker_nsds.yaml");
    nsdVcdnPnfGui = "/nsd-examples/nsd_vCDN_pnf_gui.yaml";
    ctxDelayNsds = new URL(
        "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/ctx/ctx_delay/ctx_delay_nsds.yaml");
  }

  @Test
  public void buildGraphsExportVcdn() throws IOException {
    ComposableNsd vcdnNsd = OBJECT_MAPPER.readValue(App.class.getResourceAsStream(nsdVcdnPnfGui),
        ComposableNsd[].class)[0];
    vcdnNsd.setGraphExporter(new GraphVizExporter());
    for (DfIlKey k : vcdnNsd.getGraphMapKeys()) {
      LOG.debug("GraphViz export for '{}':\n{}", k.toString(), vcdnNsd.export(k));
      String testFile = new Scanner(
          App.class.getResourceAsStream(String.format("/%s.dot", k.getNsIlId())), "UTF-8")
          .useDelimiter("\\A").next();
      assertEquals(testFile, vcdnNsd.export(k));
    }
  }

  @Test
  public void buildGraphsExportAres2tTracker() throws IOException {
    ComposableNsd trackerNsd = OBJECT_MAPPER.readValue(vsbAres2tTrackerNsds,
        ComposableNsd[].class)[0];
    trackerNsd.setGraphExporter(new GraphVizExporter());
    for (DfIlKey k : trackerNsd.getGraphMapKeys()) {
      LOG.debug("GraphViz export for '{}':\n{}", k.toString(), trackerNsd.export(k));
      String testFile = new Scanner(
          App.class.getResourceAsStream(String.format("/%s.dot", k.getNsIlId())), "UTF-8")
          .useDelimiter("\\A").next();
      assertEquals(testFile, trackerNsd.export(k));
    }
  }

  @Test
  public void composeWith() {
    // TODO
  }

  @Test
  public void composeWithConnect() {
    // TODO
    // Check with ExpbNsd from the repo
  }

  @Test
  public void composeWithPassthrough() throws NotExistingEntityException, IOException {
    ComposableNsd trackerNsd = OBJECT_MAPPER.readValue(vsbAres2tTrackerNsds,
        ComposableNsd[].class)[0];
    Nsd delayNsd = OBJECT_MAPPER.readValue(ctxDelayNsds,
        Nsd[].class)[0];
    CtxComposeResource ctxComposeResource = new CtxComposeResource();
    ctxComposeResource.setNsd(delayNsd);
    ctxComposeResource.setSapId("sap_tracking_mobile");
    ctxComposeResource.setStrat(CompositionStrat.PASSTHROUGH);
    trackerNsd.composeWithPassthrough(ctxComposeResource);

    // TODO
    // Check with ExpbNsd from the repo
  }
}