package it.cnit.blueprint.composer.vsb.rest;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import it.nextworks.nfvmano.catalogue.blueprint.elements.TestCaseBlueprint;
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
  public void validate() {
    // Given
    InputStream in = getClass().getResourceAsStream("/TCB_Ares2T_Service_v5-RC-v2.yaml");
    TestCaseBlueprint tcb = YAML_OM.readValue(in, TestCaseBlueprint.class);

    // When
    MvcResult result = mvc.perform(
        MockMvcRequestBuilders.post("/tcb/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_OM.writeValueAsString(tcb)))
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
        MockMvcRequestBuilders.get("/tcb/schema"))
        .andReturn();

    // Then
    assertEquals(200, result.getResponse().getStatus());
    JSON_OM.readValue(result.getResponse().getContentAsString(), ObjectSchema.class);
  }
}