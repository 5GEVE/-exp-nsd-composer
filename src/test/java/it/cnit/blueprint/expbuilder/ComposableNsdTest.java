package it.cnit.blueprint.expbuilder;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.compose.ComposableNsd;
import it.cnit.blueprint.expbuilder.compose.ComposableNsd.DfIlKey;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Scanner;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComposableNsdTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  public void exportGraphTest(ComposableNsd nsd) {
    for (DfIlKey k : nsd.getGraphMapKeys()) {
      LOG.info("GraphViz export for '{}':\n{}", k.toString(), nsd.export(k));
      String testFile = new Scanner(
          App.class.getResourceAsStream(String.format("/%s.dot", k.getNsIlId())), "UTF-8")
          .useDelimiter("\\A").next();
      assertEquals(testFile, nsd.export(k));
    }
  }

  @Test
  public void Ares2tTrackerTest() throws IOException {

    ComposableNsd nsd = OBJECT_MAPPER.readValue(new URL(
            "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/vsb/vsb_ares2t_tracker/vsb_ares2t_tracker_nsds.yaml"),
        ComposableNsd[].class)[0];
    exportGraphTest(nsd);
  }

  @Test
  public void vCdnTest() throws IOException {

    ComposableNsd nsd = OBJECT_MAPPER.readValue(
        App.class.getResourceAsStream("/nsd-examples/nsd_vCDN_pnf_gui.yaml"),
        ComposableNsd[].class)[0];
    exportGraphTest(nsd);
  }

}
