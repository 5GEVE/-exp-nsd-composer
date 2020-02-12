package it.cnit.blueprint.expbuilder.rest;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

  private final AtomicLong counter = new AtomicLong();

  @GetMapping(value = "/expb")
  public ExpBlueprintResource getExpBlueprint() {
    return new ExpBlueprintResource(counter.incrementAndGet());
  }

  @PostMapping(value = "/expb")
  public void postExpB() {
  }

}
