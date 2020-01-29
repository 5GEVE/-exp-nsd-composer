package it.cnit.blueprint.expbuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import org.jgrapht.io.ExportException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComposerTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  @Test
  public void Ares2tTrackerDelayTest()
      throws IOException, ExportException, NotExistingEntityException {

    Nsd trackerNsd = OBJECT_MAPPER.readValue(new URL(
            "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/vsb/vsb_ares2t_tracker/vsb_ares2t_tracker_nsds.yaml"),
        Nsd[].class)[0];

    Nsd delayNsd = OBJECT_MAPPER.readValue(new URL(
            "https://raw.githubusercontent.com/5GEVE/blueprint-yaml/master/ctx/ctx_delay/ctx_delay_nsds.yaml"),
        Nsd[].class)[0];

    Nsd[] contexts = {delayNsd};
//    Composer.buildExperiment(trackerNsd,contexts)
//    LOG.info(b.toString());

//    NsdGraph exp = b.buildExperiment(CompositionStrat.PASS);
//    LOG.info("\n---\n" + exp.exportGraphViz());

    // TODO
    //String testFile = new Scanner(this.getClass().getResourceAsStream("/vCDNDelayTest.dot"),
    //    "UTF-8")
    //    .useDelimiter("\\A").next();
    //assertEquals(testFile, exp.exportGraphViz());

    // TODO
    // Check with ExpbNsd from the repo
  }


}
