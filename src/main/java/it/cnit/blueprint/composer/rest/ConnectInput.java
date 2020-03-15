package it.cnit.blueprint.composer.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectInput {

  private String srcVnfdId;
  private String srcVldId;
  private String dstVnfdId;
  private String dstVldId;
}
