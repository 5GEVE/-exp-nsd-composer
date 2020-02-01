package it.cnit.blueprint.expbuilder.compose;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.App;
import it.cnit.blueprint.expbuilder.compose.ComposableNsd.DfIlKey;
import it.cnit.blueprint.expbuilder.nsdgraph.GraphVizExporter;
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
  public static List<ComposableNsd> nsdList = new ArrayList<>();

  @Before
  public void setUp() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    nsdList.add(objectMapper.readValue(new URL(
            "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/vsb/vsb_ares2t_tracker/vsb_ares2t_tracker_nsds.yaml"),
        ComposableNsd[].class)[0]);
    nsdList.add(objectMapper.readValue(
        App.class.getResourceAsStream("/nsd-examples/nsd_vCDN_pnf_gui.yaml"),
        ComposableNsd[].class)[0]);
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
  }

  @Test
  public void composeWithConnect() {
  }

  @Test
  public void composeWithPassthrough() {
  }
}