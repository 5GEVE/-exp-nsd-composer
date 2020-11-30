package it.cnit.blueprint.composer.commons;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.nextworks.nfvmano.catalogue.blueprint.elements.Blueprint;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ObjectMapperService {

  private final ObjectMapper objectMapper;

  /**
   * @return An ObjectMapper with FAIL_ON_UNKNOWN_PROPERTIES=false
   */
  public ObjectReader createSafeBlueprintReader() {
    return objectMapper.readerFor(Blueprint.class)
        .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  /**
   * @return An ObjectMapper with INDENT_OUTPUT=true
   */
  public ObjectWriter createIndentNsdWriter() {
    return objectMapper.writerFor(Nsd.class).with(SerializationFeature.INDENT_OUTPUT);
  }

}
