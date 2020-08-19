package it.cnit.blueprint.composer.nsd.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphResponse {

  private String nsDfId;
  private String nsLvlId;
  private String graph;

}
