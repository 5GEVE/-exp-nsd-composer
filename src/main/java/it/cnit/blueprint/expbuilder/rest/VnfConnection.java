package it.cnit.blueprint.expbuilder.rest;

import lombok.Data;

@Data
public class VnfConnection {

  private String vnfProfileId;
  private String vlProfileId;
  private String cpdId;

}
