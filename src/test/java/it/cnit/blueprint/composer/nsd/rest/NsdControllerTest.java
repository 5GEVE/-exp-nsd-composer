package it.cnit.blueprint.composer.nsd.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import it.nextworks.nfvmano.catalogue.blueprint.elements.CtxBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnBoardVsBlueprintRequest;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnboardCtxBlueprintRequest;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
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
public class NsdControllerTest {

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

  @Test
  @SneakyThrows
  public void generateFromVsb() {
    // Given
    VsBlueprint vsb;
    try (InputStream inVsb = getClass().getResourceAsStream("/vsb_polito_smartcity_nomgmt.yml")) {
      vsb = YAML_OM.readValue(inVsb, VsBlueprint.class);
    }

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/nsd/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(vsb)))
        .andReturn();

    // Then
    log.info(result.getResponse().getContentAsString());
    assertEquals(200, result.getResponse().getStatus());
    Nsd actualNsd = JSON_OM.readValue(result.getResponse().getContentAsString(), Nsd.class);
    Nsd expectedNsd;
    try (InputStream inNsd = getClass().getResourceAsStream("/vsb_polito_smartcity_nsd.yaml")) {
      expectedNsd = YAML_OM.readValue(inNsd, Nsd.class);
    }
    assertEquals(YAML_OM.writeValueAsString(expectedNsd), YAML_OM.writeValueAsString(actualNsd));
  }

  @Test
  @SneakyThrows
  public void generateFromCtx() {
    // Given
    CtxBlueprint ctx = YAML_OM
        .readValue(new URL(urlProp.getProperty("ctx_smartcity_traffic")), CtxBlueprint.class);

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/nsd/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(ctx)))
        .andReturn();

    // Then
    log.info(result.getResponse().getContentAsString());
    assertEquals(200, result.getResponse().getStatus());
    Nsd actualNsd = JSON_OM.readValue(result.getResponse().getContentAsString(), Nsd.class);
    Nsd expectedNsd;
    try (InputStream inNsd = getClass().getResourceAsStream("/ctx_smartcity_traffic_nsd.yaml")) {
      expectedNsd = YAML_OM.readValue(inNsd, Nsd.class);
    }
    assertEquals(YAML_OM.writeValueAsString(expectedNsd), YAML_OM.writeValueAsString(actualNsd));
  }

  @Test
  @SneakyThrows
  public void generate400Wrong() {
    // Given
    VsBlueprint vsb = YAML_OM
        .readValue(new URL(urlProp.getProperty("vsb_polito_smartcity")), VsBlueprint.class);

    // When
    // We pass only the VsbRequest as body to make the REST fail
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/nsd/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(vsb.getCompatibleSites())))
        .andReturn();

    // Then
    assertEquals(400, result.getResponse().getStatus());
    if (result.getResolvedException() != null) {
      log.info("Error message: {}", result.getResolvedException().getMessage());
    }
  }

  @Test
  @SneakyThrows
  public void generate400Empty() {
    // Given

    // When
    // We pass only the VsbRequest as body to make the REST fail
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/nsd/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
        .andReturn();

    // Then
    assertEquals(400, result.getResponse().getStatus());
    if (result.getResolvedException() != null) {
      log.info("Error message: {}", result.getResolvedException().getMessage());
    }
  }

  @SneakyThrows
  private ComposeRequest getAres2TRequest() {
    VsBlueprint vsb = YAML_OM
        .readValue(new URL(urlProp.getProperty("vsb_ares2t_tracker")), VsBlueprint.class);
    Nsd vsbNsd = YAML_OM
        .readValue(new URL(urlProp.getProperty("vsb_ares2t_tracker_nsds")), Nsd.class);
    OnBoardVsBlueprintRequest vsbRequest = new OnBoardVsBlueprintRequest(vsb,
        Collections.singletonList(vsbNsd), null);

    CtxBlueprint ctxb = YAML_OM
        .readValue(new URL(urlProp.getProperty("ctx_delay")), CtxBlueprint.class);
    Nsd ctxbNsd = YAML_OM
        .readValue(new URL(urlProp.getProperty("ctx_delay_nsds")), Nsd.class);
    OnboardCtxBlueprintRequest ctxbRequest = new OnboardCtxBlueprintRequest(ctxb,
        Collections.singletonList(ctxbNsd), null);
    Context c = new Context(ctxbRequest, null);

    ComposeRequest request = new ComposeRequest(vsbRequest, new Context[]{c});
    String body = JSON_OM.writeValueAsString(request);
    log.info("Request body:\n{}", body);
    return request;
  }

  @SneakyThrows
  private ComposeRequest getPolitoRequest() {
    VsBlueprint vsb;
    try (InputStream inVsb = getClass().getResourceAsStream("/vsb_polito_smartcity_nomgmt.yml")) {
      vsb = YAML_OM.readValue(inVsb, VsBlueprint.class);
    }
    Nsd vsbNsd;
    try (InputStream inNsd = getClass().getResourceAsStream("/vsb_polito_smartcity_nsd.yaml")) {
      vsbNsd = YAML_OM.readValue(inNsd, Nsd.class);
    }
    OnBoardVsBlueprintRequest vsbRequest = new OnBoardVsBlueprintRequest(vsb,
        Collections.singletonList(vsbNsd), null);

    CtxBlueprint delayCtxB = YAML_OM
        .readValue(new URL(urlProp.getProperty("ctx_delay")), CtxBlueprint.class);
    Nsd delayNsd = YAML_OM
        .readValue(new URL(urlProp.getProperty("ctx_delay_nsds")), Nsd.class);
    OnboardCtxBlueprintRequest delayRequest = new OnboardCtxBlueprintRequest(delayCtxB,
        Collections.singletonList(delayNsd), null);
    Context delay = new Context(delayRequest, null);

    CtxBlueprint trafficCtxB = YAML_OM
        .readValue(new URL(urlProp.getProperty("ctx_smartcity_traffic")), CtxBlueprint.class);
    Nsd trafficNsd = YAML_OM
        .readValue(new URL(urlProp.getProperty("ctx_smartcity_traffic_nsd")), Nsd.class);
    OnboardCtxBlueprintRequest trafficRequest = new OnboardCtxBlueprintRequest(trafficCtxB,
        Collections.singletonList(trafficNsd), null);
    Context traffic = new Context(trafficRequest, null);

    ComposeRequest request = new ComposeRequest(vsbRequest, new Context[]{delay, traffic});
    String body = JSON_OM.writeValueAsString(request);
    log.info("Request body:\n{}", body);
    return request;
  }

  @Test
  @SneakyThrows
  public void composeAres2T200() {
    // Given
    ComposeRequest request = getAres2TRequest();

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/nsd/compose")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(request)))
        .andReturn();

    // Then
    assertEquals(200, result.getResponse().getStatus());
    Nsd actualNsd = JSON_OM .readValue(result.getResponse().getContentAsString(),Nsd.class);
    actualNsd.setNsdIdentifier("58886b95-cd29-4b7b-aca0-e884caaa5c68");
    actualNsd.setNsdInvariantId("ae66294b-8dae-406c-af70-f8516e310965");
    InputStream in = getClass().getResourceAsStream(
        "/expb_ares2t_tracker_delay_nsds_passthrough.yaml");
    Nsd expectedNsd = YAML_OM.readValue(in, Nsd[].class)[0];
    assertEquals(YAML_OM.writeValueAsString(expectedNsd), YAML_OM.writeValueAsString(actualNsd));
  }

  @Test
  @SneakyThrows
  public void composePolito200() {
    // Given
    ComposeRequest request = getPolitoRequest();

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/nsd/compose")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(request)))
        .andReturn();

    // Then
    assertEquals(200, result.getResponse().getStatus());
    Nsd actualNsd = JSON_OM.readValue(result.getResponse().getContentAsString(), Nsd.class);
    actualNsd.setNsdIdentifier("0176eb11-f613-4b40-bd71-c7a4ea4f0896");
    actualNsd.setNsdInvariantId("71544f81-653a-4dc1-a49c-a34bc1e17976");
    InputStream in = getClass().getResourceAsStream("/expb_polito_smartcity_nsd.yaml");
    Nsd expectedNsd = YAML_OM.readValue(in, Nsd.class);
    assertEquals(YAML_OM.writeValueAsString(expectedNsd), YAML_OM.writeValueAsString(actualNsd));
  }

  @Test
  @SneakyThrows
  public void composeDetailsPolito200() {
    // Given
    ComposeRequest request = getPolitoRequest();

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/nsd/compose/details")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(request)))
        .andReturn();

    // Then
    assertEquals(200, result.getResponse().getStatus());
    assertEquals("application/octet-stream", result.getResponse().getContentType());
  }

  @Test
  @SneakyThrows
  public void compose400Wrong() {
    // Given
    ComposeRequest request = getAres2TRequest();

    // When
    // We pass only the VsbRequest as body to make the REST fail
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/nsd/compose")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(request.getVsbRequest().getVsBlueprint())))
        .andReturn();

    // Then
    log.info(result.getResponse().getContentAsString());
    assertEquals(400, result.getResponse().getStatus());
    if (result.getResolvedException() != null) {
      log.info("Error message: {}", result.getResolvedException().getMessage());
    }
  }

  @Test
  @SneakyThrows
  public void compose400Empty() {
    // Given

    // When
    // We pass only the VsbRequest as body to make the REST fail
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/nsd/compose")
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
  public void compose422() {
    // Given
    ComposeRequest request = getAres2TRequest();

    // When
    // We change the request to make it unprocessable
    request.getVsbRequest().getNsds().get(0).getVirtualLinkDesc().get(0)
        .setVirtualLinkDescId("wrong-vld-id");
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/nsd/compose")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(request)))
        .andReturn();

    // Then
    assertEquals(422, result.getResponse().getStatus());
    if (result.getResolvedException() != null) {
      log.info("Error message: {}", result.getResolvedException().getMessage());
    }
  }

  @Test
  @SneakyThrows
  public void validate200() {
    // Given
    Nsd nsd = getPolitoRequest().getVsbRequest().getNsds().get(0);

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/nsd/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(nsd)))
        .andReturn();

    // Then
    assertEquals(200, result.getResponse().getStatus());
  }

  @Test
  @SneakyThrows
  public void validate400() {
    // Given
    Nsd nsd = getPolitoRequest().getVsbRequest().getNsds().get(0);
    nsd.setNsdIdentifier(null);

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/nsd/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(nsd)))
        .andReturn();

    // Then
    assertEquals(400, result.getResponse().getStatus());
    if (result.getResolvedException() != null) {
      log.info("Error message: {}", result.getResolvedException().getMessage());
    }
  }

  @Test
  @SneakyThrows
  public void schema200() {
    // Given

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.get("/nsd/schema"))
        .andReturn();

    // Then
    assertEquals(200, result.getResponse().getStatus());
    JSON_OM.readValue(result.getResponse().getContentAsString(), ObjectSchema.class);
  }

  @Test
  @SneakyThrows
  public void graph200() {
    // Given
    Nsd nsd = getPolitoRequest().getVsbRequest().getNsds().get(0);

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/nsd/graph")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(nsd)))
        .andReturn();

    // Then
    assertEquals(200, result.getResponse().getStatus());
    assertEquals("application/octet-stream", result.getResponse().getContentType());
  }
}
