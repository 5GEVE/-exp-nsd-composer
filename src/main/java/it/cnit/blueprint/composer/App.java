package it.cnit.blueprint.composer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {"it.nextworks.nfvmano.libs.ifa", "it.cnit.blueprint.composer"})
@Slf4j
public class App {

  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }

}
