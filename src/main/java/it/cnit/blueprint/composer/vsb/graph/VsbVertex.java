package it.cnit.blueprint.composer.vsb.graph;

public abstract class VsbVertex {

  public String getId() {
    return String.valueOf(hashCode());
  }

}
