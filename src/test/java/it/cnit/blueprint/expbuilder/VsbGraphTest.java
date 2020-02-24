package it.cnit.blueprint.expbuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.vsbgraph.VsbGraph;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import java.io.IOException;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.io.ExportException;
import org.junit.Test;

@Slf4j
public class VsbGraphTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  @Test
  public void Ares2tTrackerTestYAML()
      throws IOException, MalformattedElementException, ExportException {

    VsBlueprint vsb = OBJECT_MAPPER.readValue(new URL(
            "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/vsb/vsb_ares2t_tracker/vsb_ares2t_tracker.yaml"),
        VsBlueprint.class);
    vsb.isValid();

    VsbGraph vsbGraph = new VsbGraph(vsb);
    log.info("GraphViz export:\n{}", vsbGraph.exportGraphViz());

    // TODO
//    String testFile = new Scanner(this.getClass().getResourceAsStream("/VsbvCDNGuiTest.dot"),
//        "UTF-8")
//        .useDelimiter("\\A").next();
//    assertEquals(testFile, vsbGraph.exportGraphViz());
  }
}
