package it.cnit.blueprint.expbuilder.rest;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

  private final AtomicLong counter = new AtomicLong();

  @RequestMapping(value = "/expb", method = RequestMethod.GET)
  public ExpBlueprintResource getExpBlueprint() {
    return new ExpBlueprintResource(counter.incrementAndGet());
  }

}
