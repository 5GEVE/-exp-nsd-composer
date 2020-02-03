package it.cnit.blueprint.expbuilder.compose;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.App;
import it.cnit.blueprint.expbuilder.compose.ComposableNsd.CompositionStrat;
import it.cnit.blueprint.expbuilder.compose.ComposableNsd.DfIlKey;
import it.cnit.blueprint.expbuilder.rest.CtxComposeResource;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.net.URL;
import java.util.Scanner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ComposableNsdTest {

  final static Logger LOG = LoggerFactory.getLogger(ComposableNsdTest.class);

  private static ObjectMapper OBJECT_MAPPER;
  // Test input
  @Autowired
  private ComposableNsd vCdnComposer;
  @Autowired
  private ComposableNsd trackerComposer;
  private Nsd delayNsd;

  @BeforeClass
  public static void setUpClass() {
    OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  }

  @Before
  public void setUp() throws Exception {
    Nsd vCDN = OBJECT_MAPPER.readValue(
        App.class.getResourceAsStream("/nsd-examples/nsd_vCDN_pnf_gui.yaml"),
        Nsd[].class)[0];
    vCdnComposer.setNsd(vCDN);
    Nsd tracker = OBJECT_MAPPER.readValue(new URL(
            "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/vsb/vsb_ares2t_tracker/vsb_ares2t_tracker_nsds.yaml"),
        Nsd[].class)[0];
    trackerComposer.setNsd(tracker);
    delayNsd = OBJECT_MAPPER.readValue(new URL(
            "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/ctx/ctx_delay/ctx_delay_nsds.yaml"),
        Nsd[].class)[0];
  }

  @Test
  public void buildGraphsExportVCdn() {
    assertNotEquals(0, vCdnComposer.getGraphMapKeys());
    for (DfIlKey k : vCdnComposer.getGraphMapKeys()) {
      LOG.debug("GraphViz export for '{}':\n{}", k.toString(), vCdnComposer.export(k));
      String testFile = new Scanner(
          App.class.getResourceAsStream(String.format("/%s.dot", k.getNsIlId())), "UTF-8")
          .useDelimiter("\\A").next();
      assertEquals(testFile, vCdnComposer.export(k));
    }
  }

  @Test
  public void buildGraphsExportAres2tTracker() {
    assertNotEquals(0, trackerComposer.getGraphMapKeys());
    for (DfIlKey k : trackerComposer.getGraphMapKeys()) {
      LOG.debug("GraphViz export for '{}':\n{}", k.toString(), trackerComposer.export(k));
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
  public void composeWithConnect() {
    // TODO
    // Check with ExpbNsd from the repo
  }

  @Test
  public void composeWithPassthrough() throws NotExistingEntityException {
    assertNotEquals(0, trackerComposer.getGraphMapKeys());
    CtxComposeResource ctxComposeResource = new CtxComposeResource();
    ctxComposeResource.setNsd(delayNsd);
    ctxComposeResource.setSapId("sap_tracking_mobile");
    ctxComposeResource.setStrat(CompositionStrat.PASSTHROUGH);
    trackerComposer.composeWithPassthrough(ctxComposeResource);

    // TODO
    // Check with ExpbNsd from the repo
  }
}