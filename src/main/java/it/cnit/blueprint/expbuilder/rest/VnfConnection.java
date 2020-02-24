package it.cnit.blueprint.expbuilder.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VnfConnection {

  private String vnfProfileId;
  private String cpdId;
  private String vlProfileId;

}
