package it.cnit.blueprint.composer.nsd.generate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.composer.nsd.graph.GraphVizExporter;
import it.cnit.blueprint.composer.nsd.graph.NsdGraphService;
import it.cnit.blueprint.composer.nsd.graph.ProfileVertex;
import it.cnit.blueprint.composer.vsb.VsbService;
import it.cnit.blueprint.composer.vsb.graph.VsbGraphService;
import it.cnit.blueprint.composer.vsb.graph.VsbVertex;
import it.nextworks.nfvmano.catalogue.blueprint.elements.CtxBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.junit.BeforeClass;
import org.junit.Test;


@Slf4j
public class NsdGeneratorTest {

  static Properties urlProp;
  static ObjectMapper oM;
  static VsbGraphService vsbGraphService;
  static NsdGraphService nsdGraphService;
  static NsdGenerator nsdGenerator;
  static VsbService vsbService;

  @BeforeClass
  @SneakyThrows
  public static void setUp() {
    // Test Setup
    urlProp = new Properties();
    InputStream input = ClassLoader.getSystemResourceAsStream("url.properties");
    urlProp.load(input);
    oM = new ObjectMapper(new YAMLFactory());
    nsdGraphService = new NsdGraphService(new GraphVizExporter());
    vsbService = new VsbService();
    nsdGenerator = new NsdGenerator(vsbService);
    vsbGraphService = new VsbGraphService();
  }


  @Test
  @SneakyThrows
  public void generateVsbPolitoSmartCity() {

    // Given
    VsBlueprint vsb;
    try (InputStream inVsb = getClass().getResourceAsStream("/vsb_polito_smartcity_nomgmt.yml")) {
      vsb = oM.readValue(inVsb, VsBlueprint.class);
    }
    Graph<VsbVertex, String> vsbGraph = vsbGraphService.buildGraph(vsb);
    log.debug("vsb graph:\n{}", vsbGraphService.export(vsbGraph));

    //When
    Nsd actualNsd = nsdGenerator.generate(vsb);
    Graph<ProfileVertex, String> nsdGraph = nsdGraphService.buildGraph(actualNsd.getSapd(),
        actualNsd.getNsDf().get(0), actualNsd.getNsDf().get(0).getDefaultInstantiationLevel());
    log.debug("Graph AFTER generation:\n{}", nsdGraphService.export(nsdGraph));

    //Then
    // A NSD from a VSB should always be a connected graph.
    assertTrue(nsdGraphService.isConnected(nsdGraph));
    Nsd expectedNsd;
    try (InputStream inNsd = getClass().getResourceAsStream("/vsb_polito_smartcity_nsd.yaml")) {
      expectedNsd = oM.readValue(inNsd, Nsd.class);
    }
    assertEquals(oM.writeValueAsString(expectedNsd), oM.writeValueAsString(actualNsd));
  }

  @Test
  @SneakyThrows
  public void generateVsbPolitoSmartCityNumberOfInstances() {

    // Given
    VsBlueprint vsb;
    try (InputStream inVsb = getClass().getResourceAsStream("/vsb_polito_smartcity_nomgmt_servers_number.yml")) {
      vsb = oM.readValue(inVsb, VsBlueprint.class);
    }
    Graph<VsbVertex, String> vsbGraph = vsbGraphService.buildGraph(vsb);
    log.debug("vsb graph:\n{}", vsbGraphService.export(vsbGraph));

    //When
    Nsd actualNsd = nsdGenerator.generate(vsb);
    Graph<ProfileVertex, String> nsdGraph = nsdGraphService.buildGraph(actualNsd.getSapd(),
        actualNsd.getNsDf().get(0), actualNsd.getNsDf().get(0).getDefaultInstantiationLevel());
    log.debug("Graph AFTER generation:\n{}", nsdGraphService.export(nsdGraph));

    //Then
    // A NSD from a VSB should always be a connected graph.
    assertTrue(nsdGraphService.isConnected(nsdGraph));
    Nsd expectedNsd;
    try (InputStream inNsd = getClass().getResourceAsStream("/vsb_polito_smartcity_nsd_number_instances.yaml")) {
      expectedNsd = oM.readValue(inNsd, Nsd.class);
    }
    assertEquals(oM.writeValueAsString(expectedNsd), oM.writeValueAsString(actualNsd));
  }
  @Test
  @SneakyThrows
  public void generateBgTrafficCtxB() {

    // Given
    CtxBlueprint ctxB = oM
        .readValue(new URL(urlProp.getProperty("ctx_bg_traffic")), CtxBlueprint.class);

    //When
    Nsd actualNsd = nsdGenerator.generate(ctxB);
    Graph<ProfileVertex, String> nsdGraph = nsdGraphService.buildGraph(actualNsd.getSapd(),
        actualNsd.getNsDf().get(0), actualNsd.getNsDf().get(0).getDefaultInstantiationLevel());
    log.debug("Graph AFTER generation:\n{}", nsdGraphService.export(nsdGraph));

    //Then
    Nsd expectedNsd = oM.readValue(new URL(urlProp.getProperty("ctx_bg_traffic_nsds")), Nsd.class);
    assertEquals(oM.writeValueAsString(expectedNsd), oM.writeValueAsString(actualNsd));
  }
}