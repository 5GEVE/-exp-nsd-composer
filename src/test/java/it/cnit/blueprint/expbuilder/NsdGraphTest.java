package it.cnit.blueprint.expbuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.nsdgraph.NsdGraph;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnBoardVsBlueprintRequest;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import org.jgrapht.io.ExportException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NsdGraphTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  @Test
  public void Ares2tTrackerTest()
      throws IOException, MalformattedElementException, ExportException {

    InputStream is = App.class.getResourceAsStream("/Ares2T_Tracker_vsb_req.json");
    OnBoardVsBlueprintRequest req = OBJECT_MAPPER.readValue(is, OnBoardVsBlueprintRequest.class);
    req.getNsds().get(0).isValid();

    NsdGraph nsdGraph = new NsdGraph(req.getNsds().get(0));
    // TODO move this information
    // Export to graphviz.
    // Copy the output to a text file called 'example.txt'
    // Create a PNG with:
    // sfdp -Tpng example.txt -o example.png
    LOG.info("GraphViz export:\n{}", nsdGraph.exportGraphViz());

    // TODO
    //String testFile = new Scanner(this.getClass().getResourceAsStream("/NsdvCDNGuiTest.dot"),
    //    "UTF-8")
    //    .useDelimiter("\\A").next();
    //assertEquals(testFile, nsdGraph.exportGraphViz());
  }

}