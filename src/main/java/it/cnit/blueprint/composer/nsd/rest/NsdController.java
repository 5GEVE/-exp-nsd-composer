package it.cnit.blueprint.composer.nsd.rest;

import it.cnit.blueprint.composer.exceptions.NsdGenerationException;
import it.cnit.blueprint.composer.nsd.generate.NsdGenerator;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
public class NsdController {

  private final NsdGenerator nsdGenerator;

  @PostMapping("/nsd/generate")
  public Nsd generateService(@RequestBody VsBlueprint vsb) {
    try {
      return nsdGenerator.generate(vsb);
    } catch (NsdGenerationException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

}
