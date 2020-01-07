package it.cnit.blueprint.expbuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnit.blueprint.expbuilder.vsbgraph.VsbGraph;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnBoardVsBlueprintRequest;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import org.jgrapht.io.ExportException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VsbGraphTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  @Test
  public void Ares2tTrackerTest() throws IOException, ExportException {
    InputStream is = App.class.getResourceAsStream("/Ares2T_Tracker_vsb_req.json");
    OnBoardVsBlueprintRequest req = OBJECT_MAPPER.readValue(is, OnBoardVsBlueprintRequest.class);

    VsbGraph vsbGraph = new VsbGraph(req.getVsBlueprint());
    // TODO move this information
    // Export to graphviz.
    // Copy the output to a text file called 'example.txt'
    // Create a PNG with:
    // sfdp -Tpng example.txt -o example.png
    LOG.info("GraphViz export:\n{}", vsbGraph.exportGraphViz());

    // TODO
//    String testFile = new Scanner(this.getClass().getResourceAsStream("/VsbvCDNGuiTest.dot"),
//        "UTF-8")
//        .useDelimiter("\\A").next();
//    assertEquals(testFile, vsbGraph.exportGraphViz());
  }
}
