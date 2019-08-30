package eu._5geve.experiment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu._5geve.experiment.nsdgraph.NsdGraph;
import it.nextworks.nfvmano.libs.descriptors.nsd.Nsd;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import org.jgrapht.io.ExportException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuilderTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  @Test
  public void vCDNBgTrafficTest() throws IOException, ExportException {
    InputStream isvCDN = App.class.getResourceAsStream("/nsd-examples/nsd_vCDN_pnf_gui.yaml");
    Nsd vCDN = OBJECT_MAPPER.readValue(isvCDN, Nsd.class);
    InputStream isBgT = App.class.getResourceAsStream("/nsd-examples/nsd_cb_bg_traffic.yaml");
    Nsd bgT = OBJECT_MAPPER.readValue(isBgT, Nsd.class);

    ArrayList<Nsd> contexts = new ArrayList<>();
    contexts.add(bgT);
    Builder b = new Builder(vCDN, contexts);
    LOG.info(b.toString());

    NsdGraph exp = b.buildExperiment();
    LOG.info("\n---\n" + exp.exportGraphViz());
  }

}
