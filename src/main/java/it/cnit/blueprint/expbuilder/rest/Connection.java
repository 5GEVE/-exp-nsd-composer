package it.cnit.blueprint.expbuilder.rest;

import lombok.Data;

@Data
public class Connection {

  private String vnfProfileId;
  private String vlProfileId;
  private String cpdId;

}
