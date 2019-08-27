package eu._5geve.experiment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu._5geve.blueprint.vsb.VsBlueprint;
import eu._5geve.experiment.vsbgraph.VsbGraph;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import org.jgrapht.io.ExportException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VsbGraphTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  @Test
  public void vCDNGuiTest() throws IOException {
    InputStream isVsb = App.class.getResourceAsStream("/nsd-examples/vsb_vCDN_gui.yaml");
    VsBlueprint vsb = OBJECT_MAPPER.readValue(isVsb, VsBlueprint.class);
    LOG.info("Dump:\n{}", OBJECT_MAPPER.writeValueAsString(vsb));

    VsbGraph vsbGraph = new VsbGraph(vsb);

    // TODO move this information
    // Export to graphviz.
    // Copy the output to a text file called 'example.txt'
    // Create a PNG with:
    // sfdp -Tpng example.txt -o example.png
    try {
      LOG.info("GraphViz export:\n{}", vsbGraph.exportGraphViz());
    } catch (ExportException e) {
      e.printStackTrace();
    }

    try {
      LOG.info("GraphML export:\n{}", vsbGraph.exportGraphML());
    } catch (ExportException e) {
      e.printStackTrace();
    }


  }

}
