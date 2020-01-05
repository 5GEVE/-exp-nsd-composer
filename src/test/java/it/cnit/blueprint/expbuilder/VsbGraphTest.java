package it.cnit.blueprint.expbuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.vsbgraph.VsbGraph;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import org.jgrapht.io.ExportException;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VsbGraphTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  @Test
  @Ignore
  public void ASTITest() throws IOException, ExportException {
    InputStream isVsb = App.class.getResourceAsStream("/vsb-examples/vsb_asti_agv.yaml");
    VsBlueprint vsb = OBJECT_MAPPER.readValue(isVsb, VsBlueprint.class);

    VsbGraph vsbGraph = new VsbGraph(vsb);

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
