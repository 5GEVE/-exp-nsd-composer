package it.cnit.blueprint.composer.vsb;

import it.nextworks.nfvmano.catalogue.blueprint.elements.Blueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbEndpoint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbLink;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class VsbService {

  public Blueprint addMgmtConnServ(Blueprint b) {
    VsbEndpoint mgmtSap = new VsbEndpoint(
        "sap_" + b.getBlueprintId() + "_mgmt",
        true,
        true,
        false
    );
    b.getEndPoints().add(mgmtSap);
    List<String> mgmtEps = b.getEndPoints().stream()
        .filter(VsbEndpoint::isManagement)
        .collect(Collectors.toList()).stream()
        .map(VsbEndpoint::getEndPointId)
        .collect(Collectors.toList());
    VsbLink mgmtCS = new VsbLink(
        b,
        mgmtEps,
        true,
        null,
        "vl_" + b.getBlueprintId() + "_mgmt",
        true
    );
    b.getConnectivityServices().add(mgmtCS);
    return b;
  }
}
