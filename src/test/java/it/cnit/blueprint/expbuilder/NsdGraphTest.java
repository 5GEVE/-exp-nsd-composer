package it.cnit.blueprint.expbuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.nsdgraph.NsdGraph;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
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

    Nsd[] nsds = OBJECT_MAPPER.readValue(new URL(
            "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/vsb/vsb_ares2t_tracker/vsb_ares2t_tracker_nsds.yaml"),
        Nsd[].class);

    NsdGraph nsdGraph = new NsdGraph(nsds[0]);
    LOG.info("GraphViz export:\n{}", nsdGraph.exportGraphViz());

    // TODO
    //String testFile = new Scanner(this.getClass().getResourceAsStream("/NsdvCDNGuiTest.dot"),
    //    "UTF-8")
    //    .useDelimiter("\\A").next();
    //assertEquals(testFile, nsdGraph.exportGraphViz());
  }

}
