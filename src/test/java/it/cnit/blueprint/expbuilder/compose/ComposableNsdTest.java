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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Theories.class)
public class ComposableNsdTest {

  final Logger LOG = LoggerFactory.getLogger(ComposableNsdTest.class);

  @DataPoints
  public static List<ComposableNsd> vsbNsdList = new ArrayList<>();

  public static List<Nsd> ctxNsdList = new ArrayList<>();

  public static List<Nsd> expNsdList = new ArrayList<>();

  @Before
  public void setUp() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    vsbNsdList.add(objectMapper.readValue(new URL(
            "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/vsb/vsb_ares2t_tracker/vsb_ares2t_tracker_nsds.yaml"),
        ComposableNsd[].class)[0]);
    vsbNsdList.add(objectMapper.readValue(
        App.class.getResourceAsStream("/nsd-examples/nsd_vCDN_pnf_gui.yaml"),
        ComposableNsd[].class)[0]);

    ctxNsdList.add(objectMapper.readValue(new URL(
            "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/ctx/ctx_delay/ctx_delay_nsds.yaml"),
        Nsd[].class)[0]);
  }

  @Test
  @Theory
  public void buildGraphsExport(ComposableNsd nsd) {
    nsd.setGraphExporter(new GraphVizExporter());
    for (DfIlKey k : nsd.getGraphMapKeys()) {
      LOG.debug("GraphViz export for '{}':\n{}", k.toString(), nsd.export(k));
      String testFile = new Scanner(
          App.class.getResourceAsStream(String.format("/%s.dot", k.getNsIlId())), "UTF-8")
          .useDelimiter("\\A").next();
      assertEquals(testFile, nsd.export(k));
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
    ComposableNsd trackerNsd = vsbNsdList.get(0);
    CtxComposeResource ctxComposeResource = new CtxComposeResource();
    ctxComposeResource.setNsd(ctxNsdList.get(0));
    ctxComposeResource.setSapId("sap_tracking_mobile");
    ctxComposeResource.setStrat(CompositionStrat.PASSTHROUGH);
    trackerNsd.composeWithPassthrough(ctxComposeResource);

    // TODO
    // Check with ExpbNsd from the repo
  }
}