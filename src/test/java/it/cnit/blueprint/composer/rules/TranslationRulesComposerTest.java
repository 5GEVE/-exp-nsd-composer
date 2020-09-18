package it.cnit.blueprint.composer.rules;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsdNsdTranslationRule;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

@Slf4j
public class TranslationRulesComposerTest {

  static Properties urlProp;
  static ObjectMapper oM;
  static TranslationRulesComposer translationRulesComposer;

  @BeforeClass
  @SneakyThrows
  public static void setUp() {
    // Test Setup
    urlProp = new Properties();
    InputStream input = ClassLoader.getSystemResourceAsStream("url.properties");
    urlProp.load(input);
    oM = new ObjectMapper(new YAMLFactory());
    translationRulesComposer = new TranslationRulesComposer();
  }

  @Test
  @SneakyThrows
  public void compose() {
    // Given
    InputStream in = getClass()
        .getResourceAsStream("/expb_ares2t_tracker_delay_nsds_passthrough.yaml");
    Nsd expNsd = oM.readValue(in, Nsd[].class)[0];
    List<VsdNsdTranslationRule> vsbTr = oM
        .readValue(new URL(urlProp.getProperty("vsb_ares2t_tracker_tr")),
            new TypeReference<List<VsdNsdTranslationRule>>() {
            });
    log.info("old rules\n{}", oM.writeValueAsString(vsbTr));

    // When
    List<VsdNsdTranslationRule> newRules = translationRulesComposer.compose(expNsd, vsbTr);
    log.info("new rules\n{}", oM.writeValueAsString(newRules));

    // Then
    in = getClass()
        .getResourceAsStream("/expb_ares2t_tracker_delay_tr.yaml");
    List<VsdNsdTranslationRule> expectedRules = oM
        .readValue(in, new TypeReference<List<VsdNsdTranslationRule>>() {
        });
    assertEquals(oM.writeValueAsString(expectedRules), oM.writeValueAsString(newRules));
  }
}