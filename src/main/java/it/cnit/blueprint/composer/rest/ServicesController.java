package it.cnit.blueprint.composer.rest;

import it.cnit.blueprint.composer.exceptions.NsdInvalidException;
import it.cnit.blueprint.composer.nsd.generate.NsdGenerator;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ServicesController {

  private NsdGenerator nsdGenerator;

  @GetMapping("/services")
  public Nsd retrieveService() {
    return null;
  }

  @PostMapping("/services")
  public Nsd generateService(@RequestBody VsBlueprint vsb) {
    Nsd nsd;
    try {
      nsd = nsdGenerator.generate(vsb);
    } catch (NsdInvalidException e) {
      //TODO handle exception
      nsd = null;
      e.printStackTrace();
    }
    return nsd;
  }

}
