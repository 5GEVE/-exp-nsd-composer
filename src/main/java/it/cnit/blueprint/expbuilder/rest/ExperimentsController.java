package it.cnit.blueprint.expbuilder.rest;

import it.cnit.blueprint.expbuilder.master.MasterComposer;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnboardExpBlueprintRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ExperimentsController {

  private final MasterComposer masterComposer;

  @GetMapping("/experiments")
  public OnboardExpBlueprintRequest retrieveExperiment() {
    return null;
  }

  @PostMapping("/experiments")
  public OnboardExpBlueprintRequest composeExperiment(@RequestBody ComposeRequest composeRequest) {
    try {
      masterComposer.compose(composeRequest.getVsbRequest(), composeRequest.getContexts());
    } catch (InvalidCtxComposeInfo e) {
      //TODO create and return a 422 response.
      e.printStackTrace();
    } catch (InvalidNsd e) {
      e.printStackTrace();
      //TODO create and return a 422 response.
    }
    //TODO
    return new OnboardExpBlueprintRequest();
  }

}
