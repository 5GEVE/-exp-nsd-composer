package it.cnit.blueprint.composer.rest;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.nextworks.nfvmano.catalogue.blueprint.elements.CtxBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsdNsdTranslationRule;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnBoardVsBlueprintRequest;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnboardCtxBlueprintRequest;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

@Slf4j
public class ExperimentsControllerTest {

  static ObjectMapper JSON_OM, YAML_OM;
  static Properties urlProp;

  @BeforeClass
  @SneakyThrows
  public static void setUp() {
    // Test Setup
    urlProp = new Properties();
    InputStream input = ClassLoader.getSystemResourceAsStream("url.properties");
    urlProp.load(input);
    JSON_OM = new ObjectMapper(new JsonFactory()).enable(SerializationFeature.INDENT_OUTPUT);
    YAML_OM = new ObjectMapper(new YAMLFactory());
  }

  @Test
  @SneakyThrows
  public void composeExperiment() {
    // Given
    VsBlueprint vsb = YAML_OM
        .readValue(new URL(urlProp.getProperty("vsb_ares2t_tracker")), VsBlueprint.class);
    List<Nsd> vsbNsd = YAML_OM.readValue(new URL(urlProp.getProperty("vsb_ares2t_tracker_nsds")),
        new TypeReference<List<Nsd>>() {
        });
    List<VsdNsdTranslationRule> vsbTr = YAML_OM
        .readValue(new URL(urlProp.getProperty("vsb_ares2t_tracker_tr")),
            new TypeReference<List<VsdNsdTranslationRule>>() {
            });
    OnBoardVsBlueprintRequest vsbRequest = new OnBoardVsBlueprintRequest(vsb, vsbNsd, vsbTr);

    CtxBlueprint ctxb = YAML_OM
        .readValue(new URL(urlProp.getProperty("ctx_delay")), CtxBlueprint.class);
    List<Nsd> ctxbNsd = YAML_OM.readValue(new URL(urlProp.getProperty("ctx_delay_nsds")),
        new TypeReference<List<Nsd>>() {
        });
    List<VsdNsdTranslationRule> ctxbTr = YAML_OM
        .readValue(new URL(urlProp.getProperty("ctx_delay_tr")),
            new TypeReference<List<VsdNsdTranslationRule>>() {
            });
    OnboardCtxBlueprintRequest ctxbRequest = new OnboardCtxBlueprintRequest(ctxb, ctxbNsd, ctxbTr);

    ComposeRequest request = new ComposeRequest(vsbRequest, new Context[]{new Context(ctxbRequest)});
    log.debug("Response:\n{}", JSON_OM.writeValueAsString(request));

  }
}