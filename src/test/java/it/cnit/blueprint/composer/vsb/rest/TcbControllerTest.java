package it.cnit.blueprint.composer.vsb.rest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnboardTestCaseBlueprintRequest;
import java.io.InputStream;
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
public class TcbControllerTest {

  private ObjectMapper JSON_OM, YAML_OM;
  static Properties urlProp;

  @Autowired
  WebApplicationContext webApplicationContext;

  private MockMvc mvc;

  @Before
  @SneakyThrows
  public void setUp() {
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
  public void validate200() {
    // Given
    InputStream in = getClass().getResourceAsStream("/tcb_cnit_smart_city_1.yaml");
    OnboardTestCaseBlueprintRequest tcb = YAML_OM
        .readValue(in, OnboardTestCaseBlueprintRequest.class);

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/tcb/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(tcb)))
        .andReturn();

    // Then
    if (result.getResponse().getErrorMessage() != null) {
      log.error(result.getResponse().getErrorMessage());
    }
    assertEquals(200, result.getResponse().getStatus());
  }

  @Test
  @SneakyThrows
  public void validate200WithoutConfig() {
    // Given
    InputStream in = getClass().getResourceAsStream("/tcb_cnit_smart_city_1_no_config_script.yaml");
    OnboardTestCaseBlueprintRequest tcb = YAML_OM
        .readValue(in, OnboardTestCaseBlueprintRequest.class);

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/tcb/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(tcb)))
        .andReturn();

    // Then
    if (result.getResponse().getErrorMessage() != null) {
      log.error(result.getResponse().getErrorMessage());
    }
    assertEquals(200, result.getResponse().getStatus());
  }

  @Test
  @SneakyThrows
  public void validate400Conf() {
    // Given
    InputStream in = getClass().getResourceAsStream("/tcb_cnit_smart_city_1_param_error_conf.yaml");
    OnboardTestCaseBlueprintRequest tcb = YAML_OM
        .readValue(in, OnboardTestCaseBlueprintRequest.class);

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/tcb/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(tcb)))
        .andReturn();

    // Then
    log.info(result.getResponse().getErrorMessage());
    assertThat(result.getResponse().getErrorMessage(),
        containsString("Parameter '$$pippo' in configurationScript is not declared"));
    assertEquals(400, result.getResponse().getStatus());
  }

  @Test
  @SneakyThrows
  public void validate400Exec() {
    // Given
    InputStream in = getClass().getResourceAsStream("/tcb_cnit_smart_city_1_param_error_exec.yaml");
    OnboardTestCaseBlueprintRequest tcb = YAML_OM
        .readValue(in, OnboardTestCaseBlueprintRequest.class);

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/tcb/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(tcb)))
        .andReturn();

    // Then
    log.info(result.getResponse().getErrorMessage());
    assertThat(result.getResponse().getErrorMessage(),
        containsString("Parameter '$$sleep$$wrong' in executionScript is not declared"));
    assertEquals(400, result.getResponse().getStatus());
  }

  @Test
  @SneakyThrows
  public void schema() {
    // Given

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.get("/tcb/schema"))
        .andReturn();

    // Then
    assertEquals(200, result.getResponse().getStatus());
    JSON_OM.readValue(result.getResponse().getContentAsString(), ObjectSchema.class);
  }
}