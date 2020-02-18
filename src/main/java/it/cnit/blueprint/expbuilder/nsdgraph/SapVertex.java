package it.cnit.blueprint.expbuilder.nsdgraph;

import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SapVertex extends ProfileVertex {

  @Getter
  private final Sapd sapd;

  @Override
  public String getElementId() {
    return this.sapd.getCpdId();
  }
}
