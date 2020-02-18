package it.cnit.blueprint.expbuilder.rest;

import it.nextworks.nfvmano.catalogue.blueprint.messages.OnboardExpBlueprintRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class Controller {

  private final Composer composer;

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
    } catch (InvalidNsd e) {
      e.printStackTrace();
      //TODO create and return a 422 response.
    }
    //TODO
    return "{\"state\": \"ok\"}";
  }

}
