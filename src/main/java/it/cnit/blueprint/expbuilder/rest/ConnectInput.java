package it.cnit.blueprint.expbuilder.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectInput {

  String srcVnfdId;
  String srcVldId;
  String dstVnfdId;
  String dstVldId;
}
