package eu._5geve.experiment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu._5geve.experiment.Builder.CompositionStrat;
import eu._5geve.experiment.nsdgraph.NsdGraph;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import org.jgrapht.io.ExportException;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuilderTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  @Test
  @Ignore
  public void ASTIDelTest() throws IOException, ExportException {
    InputStream isvCDN = this.getClass().getResourceAsStream("/nsd-examples/nsd_asti.yaml");
    Nsd vCDN = OBJECT_MAPPER.readValue(isvCDN, Nsd.class);
    InputStream isDel = this.getClass().getResourceAsStream("/nsd-examples/nsd_cb_del.yaml");
    Nsd del = OBJECT_MAPPER.readValue(isDel, Nsd.class);

    ArrayList<Nsd> contexts = new ArrayList<>();
    contexts.add(del);
    Builder b = new Builder(vCDN, contexts);
    LOG.info(b.toString());

    NsdGraph exp = b.buildExperiment(CompositionStrat.PASS);
    LOG.info("\n---\n" + exp.exportGraphViz());

    // TODO
    //String testFile = new Scanner(this.getClass().getResourceAsStream("/vCDNDelayTest.dot"),
    //    "UTF-8")
    //    .useDelimiter("\\A").next();
    //assertEquals(testFile, exp.exportGraphViz());
  }


}
