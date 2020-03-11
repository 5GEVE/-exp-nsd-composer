package it.cnit.blueprint.composer.vsbgraph;

public abstract class VsbVertex {

  public String getId() {
    return String.valueOf(hashCode());
  }

}
