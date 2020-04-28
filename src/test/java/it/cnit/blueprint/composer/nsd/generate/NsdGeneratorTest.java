package it.cnit.blueprint.composer.nsd.generate;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.composer.nsd.graph.GraphVizExporter;
import it.cnit.blueprint.composer.nsd.graph.NsdGraphService;
import it.nextworks.nfvmano.catalogue.blueprint.elements.CtxBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import lombok.SneakyThrows;
import org.junit.BeforeClass;
import org.junit.Test;


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
  }


  @Test
  @SneakyThrows
  public void generateVsbPolitoSmartCity() {

    // Given
    VsBlueprint vsb = oM
        .readValue(new URL(urlProp.getProperty("vsb_polito_smartcity")), VsBlueprint.class);

    //When
    Nsd actualNsd = nsdGenerator.generate(vsb);

    //Then
    Nsd expectedNsd = oM
        .readValue(new URL(urlProp.getProperty("vsb_polito_smartcity_nsds")), Nsd[].class)[0];
    assertEquals(oM.writeValueAsString(expectedNsd), oM.writeValueAsString(actualNsd));
  }

  @Test
  @SneakyThrows
  public void generateBgTrafficCtxB() {

    // Given
    CtxBlueprint ctxB = oM
        .readValue(new URL(urlProp.getProperty("ctx_bg_traffic")), CtxBlueprint.class);

    //When
    Nsd actual = nsdGenerator.generate(ctxB);

    //Then
    Nsd expected = oM
        .readValue(new URL(urlProp.getProperty("ctx_bg_traffic_nsds")), Nsd[].class)[0];
    assertEquals(oM.writeValueAsString(expected), oM.writeValueAsString(actual));

  }
}