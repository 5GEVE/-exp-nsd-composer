package it.cnit.blueprint.expbuilder;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.nsdgraph.NsdGraph;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Scanner;
import org.jgrapht.io.ExportException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NsdGraphTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  public void exportGraphTest(Nsd n) throws NotExistingEntityException, ExportException {
    for (NsDf df : n.getNsDf()) {
      for (NsLevel l : df.getNsInstantiationLevel()) {
        NsdGraph nsdGraph = new NsdGraph(n, df.getNsDfId(), l.getNsLevelId());
        LOG.info("GraphViz export:\n{}", nsdGraph.exportGraphViz());
        String testFile = new Scanner(
            App.class.getResourceAsStream(String.format("/%s.dot", l.getNsLevelId())), "UTF-8")
            .useDelimiter("\\A").next();
        assertEquals(testFile, nsdGraph.exportGraphViz());
      }
    }
  }

  @Test
  public void Ares2tTrackerTest()
      throws IOException, ExportException, NotExistingEntityException {

    Nsd[] nsds = OBJECT_MAPPER.readValue(new URL(
            "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/vsb/vsb_ares2t_tracker/vsb_ares2t_tracker_nsds.yaml"),
        Nsd[].class);

    for (Nsd n : nsds) {
      this.exportGraphTest(n);
    }

  }

  @Test
  public void vcdnTest() throws IOException, NotExistingEntityException, ExportException {

    Nsd[] nsds = OBJECT_MAPPER.readValue(
        App.class.getResourceAsStream("/nsd-examples/nsd_vCDN_pnf_gui.yaml"),
        Nsd[].class);

    for (Nsd n : nsds) {
      this.exportGraphTest(n);
    }
  }

}
