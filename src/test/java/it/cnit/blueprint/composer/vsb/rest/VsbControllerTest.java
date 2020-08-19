package it.cnit.blueprint.composer.vsb.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
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
public class VsbControllerTest {

  private ObjectMapper JSON_OM, YAML_OM;
  static Properties urlProp;

  @Autowired
  WebApplicationContext webApplicationContext;

  private MockMvc mvc;

  @Before
  public void setUp() throws Exception {
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
  public void validate() {
    // Given
    VsBlueprint vsb = YAML_OM
        .readValue(new URL(urlProp.getProperty("vsb_ares2t_tracker")), VsBlueprint.class);

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/vsb/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(vsb)))
        .andReturn();

    // Then
    assertEquals(200, result.getResponse().getStatus());
  }

  @Test
  @SneakyThrows
  public void schema() {
    // Given

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.get("/vsb/schema"))
        .andReturn();

    // Then
    assertEquals(200, result.getResponse().getStatus());
    JSON_OM.readValue(result.getResponse().getContentAsString(), ObjectSchema.class);
  }

  @Test
  @SneakyThrows
  public void graph() {
    // Given
    VsBlueprint vsb = YAML_OM
        .readValue(new URL(urlProp.getProperty("vsb_ares2t_tracker")), VsBlueprint.class);

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/vsb/graph")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(vsb)))
        .andReturn();

    // Then
    assertEquals(200, result.getResponse().getStatus());
    HashMap<String, String> resp = JSON_OM
        .readValue(result.getResponse().getContentAsString(), HashMap.class);
    assertTrue(resp.containsKey("graph"));
  }
}