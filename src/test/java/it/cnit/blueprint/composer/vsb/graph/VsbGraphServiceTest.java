package it.cnit.blueprint.composer.vsb.graph;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.nextworks.nfvmano.catalogue.blueprint.elements.Blueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.junit.BeforeClass;
import org.junit.Test;

@Slf4j
public class VsbGraphServiceTest {

  static Properties prop;
  static ObjectMapper oM;
  static VsbGraphService vsbGraphService;

  @BeforeClass
  @SneakyThrows
  public static void setUp() {
    // Test Setup
    prop = new Properties();
    InputStream input = ClassLoader.getSystemResourceAsStream("url.properties");
    prop.load(input);
    oM = new ObjectMapper(new YAMLFactory());
    vsbGraphService = new VsbGraphService();
  }

  @Test
  @SneakyThrows
  public void buildGraph() {
    // Given
    Blueprint b = oM
        .readValue(new URL(prop.getProperty("vsb_polito_smartcity")), VsBlueprint.class);

    // When
    Graph<VsbVertex, String> graph = vsbGraphService.buildGraph(b);
    String actual = vsbGraphService.export(graph);
    log.debug("actual graph:\n{}", actual);

    log.debug("Graph is connected: {}", vsbGraphService.isConnected(graph));

    // Then
    String expected;
    //noinspection ConstantConditions
    try (Scanner scanner = new Scanner(
        ClassLoader.getSystemResourceAsStream("vsb_polito_smartcity" + ".dot"),
        StandardCharsets.UTF_8.name())) {
      expected = scanner.useDelimiter("\\A").next();
    }
    assertEquals(expected, actual);
  }

  @Test
  @SneakyThrows
  public void buildGraphServersNumber() {
    // Given
    Blueprint b;
    try (InputStream inVsb = getClass()
        .getResourceAsStream("/vsb_polito_smartcity_servers_number.yaml")) {
      b = oM.readValue(inVsb, VsBlueprint.class);
    }

    // When
    Graph<VsbVertex, String> graph = vsbGraphService.buildGraph(b);
    String actual = vsbGraphService.export(graph);
    log.debug("actual graph:\n{}", actual);

    log.debug("Graph is connected: {}", vsbGraphService.isConnected(graph));

    // Then
    String expected;
    //noinspection ConstantConditions
    try (Scanner scanner = new Scanner(
        ClassLoader.getSystemResourceAsStream("vsb_polito_smartcity_servers_number" + ".dot"),
        StandardCharsets.UTF_8.name())) {
      expected = scanner.useDelimiter("\\A").next();
    }
    assertEquals(expected, actual);
  }

}