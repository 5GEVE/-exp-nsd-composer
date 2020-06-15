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
import java.util.Collections;
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

  @SneakyThrows
  private ComposeRequest getAres2TRequest() {
    VsBlueprint vsb = YAML_OM
        .readValue(new URL(urlProp.getProperty("vsb_ares2t_tracker")), VsBlueprint.class);
    Nsd vsbNsd = YAML_OM
        .readValue(new URL(urlProp.getProperty("vsb_ares2t_tracker_nsds")), Nsd.class);
    List<VsdNsdTranslationRule> vsbTr = YAML_OM
        .readValue(new URL(urlProp.getProperty("vsb_ares2t_tracker_tr")),
            new TypeReference<List<VsdNsdTranslationRule>>() {
            });
    OnBoardVsBlueprintRequest vsbRequest = new OnBoardVsBlueprintRequest(vsb,
        Collections.singletonList(vsbNsd), vsbTr);

    CtxBlueprint ctxb = YAML_OM
        .readValue(new URL(urlProp.getProperty("ctx_delay")), CtxBlueprint.class);
    Nsd ctxbNsd = YAML_OM
        .readValue(new URL(urlProp.getProperty("ctx_delay_nsds")), Nsd.class);
    List<VsdNsdTranslationRule> ctxbTr = YAML_OM
        .readValue(new URL(urlProp.getProperty("ctx_delay_tr")),
            new TypeReference<List<VsdNsdTranslationRule>>() {
            });
    OnboardCtxBlueprintRequest ctxbRequest = new OnboardCtxBlueprintRequest(ctxb,
        Collections.singletonList(ctxbNsd), ctxbTr);
    Context c = new Context(ctxbRequest, null);

    ComposeRequest request = new ComposeRequest(vsbRequest, new Context[]{c});
    String body = JSON_OM.writeValueAsString(request);
    log.info("Request body:\n{}", body);
    return request;
  }

  @SneakyThrows
  private ComposeRequest getPolitoRequest() {
    return new ComposeRequest();
  }

  @Test
  @SneakyThrows
  public void composeExperimentAres2T200() {
    // Given
    ComposeRequest request = getAres2TRequest();

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/experiments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(request)))
        .andReturn();

    // Then
    assertEquals(200, result.getResponse().getStatus());
    ComposeResponse response = JSON_OM
        .readValue(result.getResponse().getContentAsString(), ComposeResponse.class);
    log.info("Response body:\n{}", JSON_OM.writeValueAsString(response));
    Nsd actualNsd = response.getExpNsd();
    actualNsd.setNsdIdentifier("58886b95-cd29-4b7b-aca0-e884caaa5c68");
    actualNsd.setNsdInvariantId("ae66294b-8dae-406c-af70-f8516e310965");
    InputStream in = getClass().getResourceAsStream(
        "/expb_ares2t_tracker_delay_nsds_passthrough.yaml");
    Nsd expectedNsd = YAML_OM.readValue(in, Nsd[].class)[0];
    assertEquals(YAML_OM.writeValueAsString(expectedNsd),
        YAML_OM.writeValueAsString(response.getExpNsd()));
  }

  @Test
  @SneakyThrows
  public void composeExperimentPolito200() {

  }

  @Test
  @SneakyThrows
  public void composeExperiment400Wrong() {
    // Given
    ComposeRequest request = getAres2TRequest();

    // When
    // We pass only the VsbRequest as body to make the REST fail
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/experiments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(request.getVsbRequest())))
        .andReturn();

    // Then
    assertEquals(400, result.getResponse().getStatus());
    if (result.getResolvedException() != null) {
      log.info("Error message: {}", result.getResolvedException().getMessage());
    }
  }

  @Test
  @SneakyThrows
  public void composeExperiment400Empty() {
    // Given

    // When
    // We pass only the VsbRequest as body to make the REST fail
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/experiments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
        .andReturn();

    // Then
    assertEquals(400, result.getResponse().getStatus());
    if (result.getResolvedException() != null) {
      log.info("Error message: {}", result.getResolvedException().getMessage());
    }
  }

  @Test
  @SneakyThrows
  public void composeExperiment422() {
    // Given
    ComposeRequest request = getAres2TRequest();

    // When
    // We change the request to make it unprocessable
    request.getVsbRequest().getNsds().get(0).getVirtualLinkDesc().get(0)
        .setVirtualLinkDescId("wrong-vld-id");
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/experiments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(request)))
        .andReturn();

    // Then
    assertEquals(422, result.getResponse().getStatus());
    if (result.getResolvedException() != null) {
      log.info("Error message: {}", result.getResolvedException().getMessage());
    }
  }
}