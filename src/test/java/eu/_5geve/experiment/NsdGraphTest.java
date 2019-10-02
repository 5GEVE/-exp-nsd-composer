package eu._5geve.experiment;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu._5geve.experiment.nsdgraph.NsdGraph;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Scanner;
import org.jgrapht.io.ExportException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NsdGraphTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  @Test
  public void vCDNGuiTest() throws IOException, ExportException {
    InputStream isNsd = App.class.getResourceAsStream("/nsd-examples/nsd_vCDN_pnf_gui.yaml");
    Nsd nsd = OBJECT_MAPPER.readValue(isNsd, Nsd.class);

    NsdGraph nsdGraph = new NsdGraph(nsd);

    // TODO move this information
    // Export to graphviz.
    // Copy the output to a text file called 'example.txt'
    // Create a PNG with:
    // sfdp -Tpng example.txt -o example.png
    LOG.info("GraphViz export:\n{}", nsdGraph.exportGraphViz());

    String testFile = new Scanner(this.getClass().getResourceAsStream("/NsdvCDNGuiTest.dot"),
        "UTF-8")
        .useDelimiter("\\A").next();
    assertEquals(testFile, nsdGraph.exportGraphViz());
  }

}
