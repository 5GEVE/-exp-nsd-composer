package it.cnit.blueprint.composer.nsd.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import guru.nidi.graphviz.engine.GraphvizException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.junit.BeforeClass;
import org.junit.Test;

@Slf4j
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
  }

  @Test
  @SneakyThrows
  public void buildGraphAres2TTrackerBig() {
    // Given
    Nsd nsd = oM.readValue(new URL(prop.getProperty("vsb_ares2t_tracker_nsds")), Nsd.class);
    String nsLevel = "ns_ares2t_tracker_il_big";

    // When
    String actual = nsdGraphService.export(nsdGraphService
        .buildGraph(nsd.getSapd(), nsd.getNsDf().get(0), nsd.getNsDf().get(0).getNsLevel(nsLevel)));
    log.debug("actual graph:\n{}", actual);

    // Then
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
  public void renderSVGGraphAres2TTrackerBig() {
    // Given
    Nsd nsd = oM.readValue(new URL(prop.getProperty("vsb_ares2t_tracker_nsds")), Nsd.class);
    String nsLevel = "ns_ares2t_tracker_il_big";

    // When
    nsdGraphService.renderSVG(nsdGraphService.export(nsdGraphService
        .buildGraph(nsd.getSapd(), nsd.getNsDf().get(0),
            nsd.getNsDf().get(0).getNsLevel(nsLevel)))).toFile(new File("/tmp/graph.svg"));

//    // Then
//    String expected;
//    //noinspection ConstantConditions
//    try (Scanner scanner = new Scanner(ClassLoader.getSystemResourceAsStream(nsLevel + ".dot"),
//        StandardCharsets.UTF_8.name())) {
//      expected = scanner.useDelimiter("\\A").next();
//    }
//    assertEquals(expected, actual);
  }

  @Test
  @SneakyThrows
  public void renderPNGGraphAres2TTrackerBig() {
    // Given
    Nsd nsd = oM.readValue(new URL(prop.getProperty("vsb_ares2t_tracker_nsds")), Nsd.class);
    String nsLevel = "ns_ares2t_tracker_il_big";

    // When
    nsdGraphService.renderPNG(nsdGraphService.export(nsdGraphService
        .buildGraph(nsd.getSapd(), nsd.getNsDf().get(0),
            nsd.getNsDf().get(0).getNsLevel(nsLevel)))).toFile(new File("/tmp/graph.png"));

//    // Then
//    String expected;
//    //noinspection ConstantConditions
//    try (Scanner scanner = new Scanner(ClassLoader.getSystemResourceAsStream(nsLevel + ".dot"),
//        StandardCharsets.UTF_8.name())) {
//      expected = scanner.useDelimiter("\\A").next();
//    }
//    assertEquals(expected, actual);
  }

  @Test(expected = GraphvizException.class)
  @SneakyThrows
  public void renderPNGGraphEmpty() {
    // Given
    String empty = "";

    // When
    nsdGraphService.renderPNG(empty).toFile(new File("/tmp/graph.png"));

    // Then
    // throws GraphvizException
  }

  @Test
  @SneakyThrows
  public void buildGraphAres2TTrackerSmall() {
    // Given
    Nsd nsd = oM.readValue(new URL(prop.getProperty("vsb_ares2t_tracker_nsds")), Nsd.class);
    String nsLevel = "ns_ares2t_tracker_il_small";

    // When
    String actual = nsdGraphService.export(nsdGraphService
        .buildGraph(nsd.getSapd(), nsd.getNsDf().get(0), nsd.getNsDf().get(0).getNsLevel(nsLevel)));
    log.debug("actual graph:\n{}", actual);

    // Then
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
    Nsd nsd = oM.readValue(new URL(prop.getProperty("expb_ares2t_tracker_delay_nsds")), Nsd.class);
    String nsLevel = "ns_ares2t_tracker_exp_il_big";

    // When
    String actual = nsdGraphService.export(nsdGraphService
        .buildGraph(nsd.getSapd(), nsd.getNsDf().get(0), nsd.getNsDf().get(0).getNsLevel(nsLevel)));
    log.debug("actual graph:\n{}", actual);

    // Then
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
    Nsd nsd = oM.readValue(new URL(prop.getProperty("expb_ares2t_tracker_delay_nsds")), Nsd.class);
    String nsLevel = "ns_ares2t_tracker_exp_il_small";

    // When
    String actual = nsdGraphService.export(nsdGraphService
        .buildGraph(nsd.getSapd(), nsd.getNsDf().get(0), nsd.getNsDf().get(0).getNsLevel(nsLevel)));
    log.debug("actual graph:\n{}", actual);

    // Then
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
  public void buildGraphPolitoNumberOfInstances() {
    // Given
    Nsd nsd;
    try (InputStream inVsb = getClass()
        .getResourceAsStream("/vsb_polito_smartcity_nsd_number_instances.yaml")) {
      nsd = oM.readValue(inVsb, Nsd.class);
    }
    String nsLevel = "vsb_polito_smartcity_il_default";

    // When
    String actual = nsdGraphService.export(nsdGraphService
        .buildGraph(nsd.getSapd(), nsd.getNsDf().get(0), nsd.getNsDf().get(0).getNsLevel(nsLevel)));
    log.debug("actual graph:\n{}", actual);

    // Then
    String expected = "";
    //noinspection ConstantConditions
    try (Scanner scanner = new Scanner(ClassLoader.getSystemResourceAsStream("vsb_polito_smartcity_il_3.dot"),
        StandardCharsets.UTF_8.name())) {
      expected = scanner.useDelimiter("\\A").next();
    }
    assertEquals(expected, actual);
  }

  @Test
  @SneakyThrows
  public void isConnectedAres2TTrackerSmallTest() {

    // Given
    Nsd nsd = oM.readValue(new URL(prop.getProperty("vsb_ares2t_tracker_nsds")), Nsd.class);
    String nsLevel = "ns_ares2t_tracker_il_small";

    // When
    Graph<ProfileVertex, String> g = nsdGraphService
        .buildGraph(nsd.getSapd(), nsd.getNsDf().get(0), nsd.getNsDf().get(0).getNsLevel(nsLevel));

    // Then
    assertTrue(nsdGraphService.isConnected(g));
  }

  @Test
  @SneakyThrows
  public void isNotConnectedAres2TTrackerSmallTest() {

    // Given
    Nsd nsd = oM.readValue(new URL(prop.getProperty("vsb_ares2t_tracker_nsds")), Nsd.class);
    String nsLevel = "ns_ares2t_tracker_il_small";

    // When
    Graph<ProfileVertex, String> g = nsdGraphService
        .buildGraph(nsd.getSapd(), nsd.getNsDf().get(0), nsd.getNsDf().get(0).getNsLevel(nsLevel));
    // Get the first vertex and remove its edges
    ProfileVertex v = g.vertexSet().iterator().next();
    Set<String> edges = new HashSet<>(g.edgesOf(v));
    for (String e: edges){
      g.removeEdge(e);
    }

    // Then
    assertFalse(nsdGraphService.isConnected(g));
  }
}
