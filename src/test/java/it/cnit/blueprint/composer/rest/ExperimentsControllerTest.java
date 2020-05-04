package it.cnit.blueprint.composer.rest;

import static org.junit.Assert.assertEquals;

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
import java.util.List;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Slf4j
public class ExperimentsControllerTest {

  private ObjectMapper JSON_OM, YAML_OM;
  static Properties urlProp;

  @Autowired
  WebApplicationContext webApplicationContext;

  private MockMvc mvc;

  @Autowired
  private ExperimentsController experimentsController;

  @Before
  @SneakyThrows
  public void setup() {
    // Test Setup
    urlProp = new Properties();
    InputStream input = ClassLoader.getSystemResourceAsStream("url.properties");
    urlProp.load(input);
    JSON_OM = new ObjectMapper(new JsonFactory()).enable(SerializationFeature.INDENT_OUTPUT);
    YAML_OM = new ObjectMapper(new YAMLFactory());
    mvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .build();
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
    Context c = new Context(ctxbRequest, null);

    ComposeRequest request = new ComposeRequest(vsbRequest, new Context[]{c});
    String body = JSON_OM.writeValueAsString(request);
    log.info("Request body:\n{}", body);

    // When
    MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/experiments").contentType(
        MediaType.APPLICATION_JSON).content(body)).andReturn();
    assertEquals(200, result.getResponse().getStatus());
    ComposeResponse response = JSON_OM
        .readValue(result.getResponse().getContentAsString(), ComposeResponse.class);
    log.info("Response body:\n{}", JSON_OM.writeValueAsString(response));
  }
}