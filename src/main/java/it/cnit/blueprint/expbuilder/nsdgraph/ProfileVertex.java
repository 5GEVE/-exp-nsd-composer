package it.cnit.blueprint.expbuilder.nsdgraph;

public abstract class ProfileVertex {

  public String getId() {
    return String.valueOf(hashCode());
  }

}
