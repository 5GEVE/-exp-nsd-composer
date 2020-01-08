package it.cnit.blueprint.expbuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.vsbgraph.VsbGraph;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnBoardVsBlueprintRequest;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import org.jgrapht.io.ExportException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VsbGraphTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  @Test
  public void Ares2tTrackerTest()
      throws IOException, MalformattedElementException, ExportException {

    InputStream is = App.class.getResourceAsStream("/Ares2T_Tracker_vsb_req.json");
    OnBoardVsBlueprintRequest req = OBJECT_MAPPER.readValue(is, OnBoardVsBlueprintRequest.class);
    req.getVsBlueprint().isValid();

    VsbGraph vsbGraph = new VsbGraph(req.getVsBlueprint());
    LOG.info("GraphViz export:\n{}", vsbGraph.exportGraphViz());

    // TODO
//    String testFile = new Scanner(this.getClass().getResourceAsStream("/VsbvCDNGuiTest.dot"),
//        "UTF-8")
//        .useDelimiter("\\A").next();
//    assertEquals(testFile, vsbGraph.exportGraphViz());
  }

  @Test
  public void Ares2tTrackerTestYAML()
      throws IOException, MalformattedElementException, ExportException {

    final ObjectMapper OJ_YAML = new ObjectMapper(new YAMLFactory());
    VsBlueprint req = OJ_YAML.readValue(new URL(
            "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/vsb/vsb_ares2t_tracker.yaml"),
        VsBlueprint.class);
    req.isValid();

    VsbGraph vsbGraph = new VsbGraph(req);
    LOG.info("GraphViz export:\n{}", vsbGraph.exportGraphViz());

    // TODO
//    String testFile = new Scanner(this.getClass().getResourceAsStream("/VsbvCDNGuiTest.dot"),
//        "UTF-8")
//        .useDelimiter("\\A").next();
//    assertEquals(testFile, vsbGraph.exportGraphViz());
  }
}
