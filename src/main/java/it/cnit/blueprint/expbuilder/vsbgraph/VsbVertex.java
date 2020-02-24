package it.cnit.blueprint.expbuilder.vsbgraph;

public abstract class VsbVertex {

  public String getId() {
    return String.valueOf(hashCode());
  }

}
