package it.cnit.blueprint.composer.nsd.generate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.composer.rest.InvalidNsd;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsComponent;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbLink;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.SecurityParameters;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
public class NsdGenerator {

  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  @SneakyThrows(JsonProcessingException.class)
  public Nsd generate(VsBlueprint vsb) throws InvalidNsd {

    log.debug(OBJECT_MAPPER.writeValueAsString(vsb));

    Nsd nsd = new Nsd();
    nsd.setNsdIdentifier(vsb.getBlueprintId() + "_nsd");
    nsd.setDesigner("NSD generator");
    nsd.setNsdInvariantId(vsb.getBlueprintId() + "_nsd");
    nsd.setVersion(vsb.getVersion());
    nsd.setNsdName(vsb.getName() + "_nsd");
    nsd.setSecurity(new SecurityParameters(
        "FC_NSD_SIGNATURE",
        "FC_NSD_ALGORITHM",
        "FC_NSD_CERTIFICATE"
    ));

    NsDf nsDf = new NsDf();
    nsDf.setNsDfId(vsb.getBlueprintId() + "_df");
    nsDf.setFlavourKey(vsb.getBlueprintId() + "_df_fk");

    NsLevel nsLevel = new NsLevel();
    nsLevel.setNsLevelId(vsb.getBlueprintId() + "_il_default");
    nsLevel.setDescription("Default Instantiation Level");
    for (VsComponent vsc : vsb.getAtomicComponents()) {
      nsd.getVnfdId().add(vsc.getComponentId());
      VnfProfile vnfp = new VnfProfile();
      vnfp.setVnfProfileId(vsc.getComponentId() + "_vnfp");
      vnfp.setVnfdId(vsc.getComponentId());
      vnfp.setFlavourId(vsc.getComponentId() + "_vnf_df");
      vnfp.setInstantiationLevel(vsc.getComponentId() + "_vnf_il");
      vnfp.setMinNumberOfInstances(1);
      vnfp.setMaxNumberOfInstances(1);
      List<NsVirtualLinkConnectivity> nsVirtualLinkConnectivities = new ArrayList<>();
      for (String ep : vsc.getEndPointsIds()) {
        for (VsbLink cs : vsb.getConnectivityServices()) {
          for (String csEp : cs.getEndPointIds()) {
            if (csEp.equals(ep)) {
              NsVirtualLinkConnectivity nsVLC = new NsVirtualLinkConnectivity();
              // TODO change this when connectivity service has a name
              nsVLC.setVirtualLinkProfileId("new Vl profile");
              nsVLC.setCpdId(Collections.singletonList("ep"));
              nsVirtualLinkConnectivities.add(nsVLC);
            }
          }
        }
      }
      vnfp.setNsVirtualLinkConnectivity(nsVirtualLinkConnectivities);
      nsDf.getVnfProfile().add(vnfp);
      nsLevel.getVnfToLevelMapping().add(new VnfToLevelMapping(vnfp.getVnfProfileId(), 1));
    }
//    for (VsbLink connService: vsb.getConnectivityServices()){
//
//    }

    nsDf.setNsInstantiationLevel(Collections.singletonList(nsLevel));
    nsDf.setDefaultNsInstantiationLevelId(nsLevel.getNsLevelId());
    nsd.setNsDf(Collections.singletonList(nsDf));

    // Nsd validation and logging
    try {
      nsd.isValid();
    } catch (MalformattedElementException e) {
      String m = "Nsd looks not valid after composition";
      log.error(m, e);
      throw new InvalidNsd(m);
    }
    log.debug(OBJECT_MAPPER.writeValueAsString(nsd));
    return nsd;
  }

}
