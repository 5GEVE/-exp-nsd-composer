package eu._5geve.experiment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu._5geve.blueprint.vsb.VsBlueprint;
import it.nextworks.nfvmano.libs.descriptors.nsd.Nsd;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  public static void main(String[] args) throws IOException {
    InputStream is = App.class.getResourceAsStream("/nsd_stub.yaml");
    Nsd nsd_stub = OBJECT_MAPPER.readValue(is, Nsd.class);
    LOG.info("Dump:\n{}", OBJECT_MAPPER.writeValueAsString(nsd_stub));

    InputStream isVsb = App.class.getResourceAsStream("/blueprint-examples/vsb_asti_agv.yaml");
    VsBlueprint vsb = OBJECT_MAPPER.readValue(isVsb, VsBlueprint.class);
    LOG.info("Dump:\n{}", OBJECT_MAPPER.writeValueAsString(vsb));

  }
}
