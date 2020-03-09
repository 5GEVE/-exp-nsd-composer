package it.cnit.blueprint.expbuilder.rest;

import lombok.Data;

@Data
public class ConnectInput {

  String srcVnfdId;
  String srcVlId;
  String dstVnfdId;
  String dstVlId;
}
