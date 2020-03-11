package it.cnit.blueprint.composer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EntityScan(basePackages = {"it.nextworks.nfvmano.libs.ifa", "it.cnit.blueprint.composer"})
@Slf4j
public class App {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  public static void main(String[] args) {
    ConfigurableApplicationContext c = new SpringApplication(App.class).run(args);
//    log.debug("Beans found: {}", (Object) c.getBeanDefinitionNames());
//    log.debug("nsd -> {}", c.getBean(ComposableNsd.class));
//    log.debug("exporter -> {}", c.getBean(ComposableNsd.class).graphExporter);
  }

}
