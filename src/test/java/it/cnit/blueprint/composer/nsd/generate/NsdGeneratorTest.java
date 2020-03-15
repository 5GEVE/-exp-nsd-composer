package it.cnit.blueprint.composer.nsd.generate;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.composer.nsd.graph.GraphVizExporter;
import it.cnit.blueprint.composer.nsd.graph.NsdGraphService;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import lombok.SneakyThrows;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class NsdGeneratorTest {

  static Properties urlProp;
  static ObjectMapper oM;
  static NsdGraphService nsdGraphService;
  static NsdGenerator nsdGenerator;

  @BeforeClass
  @SneakyThrows
  public static void setUp() {
    // Test Setup
    urlProp = new Properties();
    InputStream input = ClassLoader.getSystemResourceAsStream("url.properties");
    urlProp.load(input);
    oM = new ObjectMapper(new YAMLFactory());
    nsdGraphService = new NsdGraphService(new GraphVizExporter());
    nsdGenerator = new NsdGenerator(nsdGraphService);
    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.DEBUG);
  }


  @Test
  @SneakyThrows
  public void generateTracker() {

    // Given
    VsBlueprint vsb = oM.readValue(new URL(urlProp.getProperty("vsb.tracker")), VsBlueprint.class);

    //When
    Nsd vsbNsd = nsdGenerator.generate(vsb);

  }

  @Test
  @SneakyThrows
  public void generateV360() {

    // Given
    VsBlueprint vsb = oM.readValue(new URL(urlProp.getProperty("vsb.v360")), VsBlueprint.class);

    //When
    Nsd vsbNsd = nsdGenerator.generate(vsb);

  }
}