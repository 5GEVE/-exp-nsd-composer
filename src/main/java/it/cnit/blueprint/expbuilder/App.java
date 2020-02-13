package it.cnit.blueprint.expbuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EntityScan(basePackages = {"it.nextworks.nfvmano.libs.ifa", "it.cnit.blueprint.expbuilder"})
public class App {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  public static void main(String[] args) {
    ConfigurableApplicationContext c = new SpringApplication(App.class).run(args);
//    LOG.debug("Beans found: {}", (Object) c.getBeanDefinitionNames());
//    LOG.debug("nsd -> {}", c.getBean(ComposableNsd.class));
//    LOG.debug("exporter -> {}", c.getBean(ComposableNsd.class).graphExporter);
  }

}
