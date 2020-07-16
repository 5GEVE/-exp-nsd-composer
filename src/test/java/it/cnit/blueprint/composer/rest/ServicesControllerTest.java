package it.cnit.blueprint.composer.rest;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.InputStream;
import java.net.URL;
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
public class ServicesControllerTest {

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
  public void generateService200() {
    // Given
    VsBlueprint vsb = YAML_OM
        .readValue(new URL(urlProp.getProperty("vsb_polito_smartcity")), VsBlueprint.class);

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/services")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(vsb)))
        .andReturn();

    // Then
    assertEquals(200, result.getResponse().getStatus());
    Nsd actualNsd = JSON_OM.readValue(result.getResponse().getContentAsString(), Nsd.class);
    Nsd expectedNsd = YAML_OM
        .readValue(new URL(urlProp.getProperty("vsb_polito_smartcity_nsds")), Nsd.class);
    assertEquals(YAML_OM.writeValueAsString(expectedNsd), YAML_OM.writeValueAsString(actualNsd));
  }

  @Test
  @SneakyThrows
  public void composeExperiment400Wrong() {
    // Given
    VsBlueprint vsb = YAML_OM
        .readValue(new URL(urlProp.getProperty("vsb_polito_smartcity")), VsBlueprint.class);

    // When
    // We pass only the VsbRequest as body to make the REST fail
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/services")
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
  public void composeExperiment400Empty() {
    // Given

    // When
    // We pass only the VsbRequest as body to make the REST fail
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/services")
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
        .andReturn();

    // Then
    assertEquals(400, result.getResponse().getStatus());
    if (result.getResolvedException() != null) {
      log.info("Error message: {}", result.getResolvedException().getMessage());
    }
  }
}