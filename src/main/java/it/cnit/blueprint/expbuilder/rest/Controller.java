package it.cnit.blueprint.expbuilder.rest;

import it.nextworks.nfvmano.catalogue.blueprint.messages.OnboardExpBlueprintRequest;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

  private final Composer composer;

  @Autowired
  public Controller(Composer composer) {
    this.composer = composer;
  }

  @GetMapping("/experiment")
  public OnboardExpBlueprintRequest retrieveExperiment() {
    return null;
  }

  @PostMapping("/experiment")
  // TODO return should be OnboardExpBlueprintRequest
  public String composeExperiment(@RequestBody ComposeRequest composeRequest) {
    try {
      composer.composeWith(composeRequest.getVsbRequest().getNsds().get(0),
          composeRequest.getContexts());
    } catch (InvalidCtxComposeInfo e) {
      //TODO create and return a 422 response.
      e.printStackTrace();
    }
    //TODO
    return "{\"state\": \"ok\"}";
  }

}
