package it.cnit.blueprint.composer.nsd.graph;

import static org.junit.Assert.assertEquals;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;
import lombok.SneakyThrows;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class NsdGraphServiceTest {

  static Properties prop;
  static ObjectMapper oM;
  static NsdGraphService nsdGraphService;

  @BeforeClass
  @SneakyThrows
  public static void setUp() {
    // Test Setup
    prop = new Properties();
    InputStream input = ClassLoader.getSystemResourceAsStream("url.properties");
    prop.load(input);
    oM = new ObjectMapper(new YAMLFactory());
    nsdGraphService = new NsdGraphService(new GraphVizExporter());
    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.ERROR);
  }

  @Test
  @SneakyThrows
  public void buildGraphAres2TTrackerBig() {
    // Given
    Nsd nsd = oM.readValue(new URL(prop.getProperty("vsb.tracker.nsds")), Nsd[].class)[0];
    String nsLevel = "ns_ares2t_tracker_il_big";

    // When
    String actual = nsdGraphService.export(nsdGraphService
        .buildGraph(nsd.getSapd(), nsd.getNsDf().get(0), nsd.getNsDf().get(0).getNsLevel(nsLevel)));

    // Then
    InputStream in = ClassLoader.getSystemResourceAsStream(nsLevel + ".dot");
    String expected;
    //noinspection ConstantConditions
    try (Scanner scanner = new Scanner(ClassLoader.getSystemResourceAsStream(nsLevel + ".dot"),
        StandardCharsets.UTF_8.name())) {
      expected = scanner.useDelimiter("\\A").next();
    }
    assertEquals(expected, actual);
  }

  @Test
  @SneakyThrows
  public void buildGraphAres2TTrackerSmall() {
    // Given
    Nsd nsd = oM.readValue(new URL(prop.getProperty("vsb.tracker.nsds")), Nsd[].class)[0];
    String nsLevel = "ns_ares2t_tracker_il_small";

    // When
    String actual = nsdGraphService.export(nsdGraphService
        .buildGraph(nsd.getSapd(), nsd.getNsDf().get(0), nsd.getNsDf().get(0).getNsLevel(nsLevel)));

    // Then
    InputStream in = ClassLoader.getSystemResourceAsStream(nsLevel + ".dot");
    String expected;
    //noinspection ConstantConditions
    try (Scanner scanner = new Scanner(ClassLoader.getSystemResourceAsStream(nsLevel + ".dot"),
        StandardCharsets.UTF_8.name())) {
      expected = scanner.useDelimiter("\\A").next();
    }
    assertEquals(expected, actual);
  }

  @Test
  @SneakyThrows
  public void buildGraphAres2TDelayExperimentBig() {
    // Given
    Nsd nsd = oM.readValue(new URL(prop.getProperty("expb.tracker.delay.nsds")), Nsd[].class)[0];
    String nsLevel = "ns_ares2t_tracker_exp_il_big";

    // When
    String actual = nsdGraphService.export(nsdGraphService
        .buildGraph(nsd.getSapd(), nsd.getNsDf().get(0), nsd.getNsDf().get(0).getNsLevel(nsLevel)));

    // Then
    InputStream in = ClassLoader.getSystemResourceAsStream(nsLevel + ".dot");
    String expected;
    //noinspection ConstantConditions
    try (Scanner scanner = new Scanner(ClassLoader.getSystemResourceAsStream(nsLevel + ".dot"),
        StandardCharsets.UTF_8.name())) {
      expected = scanner.useDelimiter("\\A").next();
    }
    assertEquals(expected, actual);
  }

  @Test
  @SneakyThrows
  public void buildGraphAres2TDelayExperimentSmall() {
    // Given
    Nsd nsd = oM.readValue(new URL(prop.getProperty("expb.tracker.delay.nsds")), Nsd[].class)[0];
    String nsLevel = "ns_ares2t_tracker_exp_il_small";

    // When
    String actual = nsdGraphService.export(nsdGraphService
        .buildGraph(nsd.getSapd(), nsd.getNsDf().get(0), nsd.getNsDf().get(0).getNsLevel(nsLevel)));

    // Then
    InputStream in = ClassLoader.getSystemResourceAsStream(nsLevel + ".dot");
    String expected;
    //noinspection ConstantConditions
    try (Scanner scanner = new Scanner(ClassLoader.getSystemResourceAsStream(nsLevel + ".dot"),
        StandardCharsets.UTF_8.name())) {
      expected = scanner.useDelimiter("\\A").next();
    }
    assertEquals(expected, actual);
  }

}
