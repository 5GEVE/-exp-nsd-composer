package it.cnit.blueprint.expbuilder.compose;

import com.fasterxml.jackson.databind.util.StdConverter;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;

public class NsdConverter extends StdConverter<ComposableNsd, ComposableNsd> {

  @Override
  public ComposableNsd convert(ComposableNsd composableNsd) {
    try {
      composableNsd.buildGraphs();
    } catch (NotExistingEntityException e) {
      e.printStackTrace();
    }
    return composableNsd;
  }
}
