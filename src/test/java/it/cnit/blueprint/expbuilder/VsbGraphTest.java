package it.cnit.blueprint.expbuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.vsbgraph.VsbGraph;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import org.jgrapht.io.ExportException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VsbGraphTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());


  @Test
  public void Ares2tTrackerTestYAML()
      throws IOException, MalformattedElementException, ExportException {

    VsBlueprint vsb = OBJECT_MAPPER.readValue(new URL(
            "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/vsb/vsb_ares2t_tracker/vsb_ares2t_tracker.yaml"),
        VsBlueprint.class);
    vsb.isValid();

    VsbGraph vsbGraph = new VsbGraph(vsb);
    LOG.info("GraphViz export:\n{}", vsbGraph.exportGraphViz());

    // TODO
//    String testFile = new Scanner(this.getClass().getResourceAsStream("/VsbvCDNGuiTest.dot"),
//        "UTF-8")
//        .useDelimiter("\\A").next();
//    assertEquals(testFile, vsbGraph.exportGraphViz());
  }
}
