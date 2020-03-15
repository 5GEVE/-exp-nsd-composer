package it.cnit.blueprint.composer.nsd.generate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.composer.nsd.graph.NsdGraphService;
import it.cnit.blueprint.composer.nsd.graph.ProfileVertex;
import it.cnit.blueprint.composer.rest.InvalidNsd;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsComponent;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbEndpoint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbLink;
import it.nextworks.nfvmano.libs.ifa.common.enums.AddressType;
import it.nextworks.nfvmano.libs.ifa.common.enums.CpRole;
import it.nextworks.nfvmano.libs.ifa.common.enums.IpVersion;
import it.nextworks.nfvmano.libs.ifa.common.enums.LayerProtocol;
import it.nextworks.nfvmano.libs.ifa.common.enums.ServiceAvailabilityLevel;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.AddressData;
import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.ConnectivityType;
import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.LinkBitrateRequirements;
import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.VirtualLinkDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.VirtualLinkProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.SecurityParameters;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VirtualLinkToLevelMapping;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
@AllArgsConstructor
public class NsdGenerator {

  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  private NsdGraphService nsdGraphService;

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

    int vldCount = 0;
    for (VsbLink connService : vsb.getConnectivityServices()) {
      NsVirtualLinkDesc vld = new NsVirtualLinkDesc();
//    TODO  vld.setVirtualLinkDescId(connService.getName());
      vld.setVirtualLinkDescId("vld_" + vldCount);
      vld.setVirtualLinkDescProvider(nsd.getDesigner());
      vld.setVirtuaLinkDescVersion(nsd.getVersion());
      vld.setDescription(vld.getVirtualLinkDescId());
      vld.setConnectivityType(new ConnectivityType(LayerProtocol.IPV4, ""));
      VirtualLinkDf vldf = new VirtualLinkDf();
      vldf.setFlavourId(vld.getVirtualLinkDescId() + "_df");
      vldf.setServiceAvaibilityLevel(ServiceAvailabilityLevel.LEVEL_1);
      vld.setVirtualLinkDf(Collections.singletonList(vldf));
      nsd.getVirtualLinkDesc().add(vld);

      VirtualLinkProfile vlp = new VirtualLinkProfile();
      vlp.setVirtualLinkProfileId(vld.getVirtualLinkDescId() + "_vlp");
      vlp.setVirtualLinkDescId(vld.getVirtualLinkDescId());
      vlp.setFlavourId(vldf.getFlavourId());
      vlp.setMaxBitrateRequirements(new LinkBitrateRequirements("1", "1"));
      vlp.setMinBitrateRequirements(new LinkBitrateRequirements("1", "1"));
      nsDf.getVirtualLinkProfile().add(vlp);

      VirtualLinkToLevelMapping vlMap = new VirtualLinkToLevelMapping();
      vlMap.setVirtualLinkProfileId(vlp.getVirtualLinkProfileId());
      vlMap.setBitRateRequirements(new LinkBitrateRequirements("1", "1"));
      nsLevel.getVirtualLinkToLevelMapping().add(vlMap);

      vldCount++;
    }

    List<Sapd> sapdList = new ArrayList<>();
    for (VsbEndpoint e : vsb.getEndPoints()) {
      if (e.isExternal() && e.getEndPointId().contains("sap")) {
        Sapd sapd = new Sapd();
        sapd.setCpdId(e.getEndPointId());
        sapd.setDescription("A generated Sapd");
        sapd.setLayerProtocol(LayerProtocol.IPV4);
        sapd.setCpRole(CpRole.ROOT); // TODO meaning of this?
        sapd.setSapAddressAssignment(false);
        int count = 0;
        for (VsbLink cs : vsb.getConnectivityServices()) {
          for (String ep : cs.getEndPointIds()) {
            if (ep.equals(sapd.getCpdId())) {
              // TODO change this when connectivity service has a name
              sapd.setNsVirtualLinkDescId("vld_" + count);
              break;
            }
          }
          count++;
        }
        AddressData addressData = new AddressData();
        addressData.setAddressType(AddressType.IP_ADDRESS);
        addressData.setiPAddressType(IpVersion.IPv4);
        addressData.setiPAddressAssignment(false);
        addressData.setFloatingIpActivated(true);
        addressData.setNumberOfIpAddress(1);
        sapd.setAddressData(Collections.singletonList(addressData));

        sapdList.add(sapd);
      }
    }
    nsd.setSapd(sapdList);

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
        int count = 0;
        for (VsbLink cs : vsb.getConnectivityServices()) {
          for (String csEp : cs.getEndPointIds()) {
            if (csEp.equals(ep)) {
              NsVirtualLinkConnectivity nsVLC = new NsVirtualLinkConnectivity();
              // TODO change this when connectivity service has a name
              String vldId = "vld_" + count;
              Optional<VirtualLinkProfile> optVlp = nsDf.getVirtualLinkProfile().stream()
                  .filter(vlp -> vlp.getVirtualLinkDescId().equals(vldId)).findFirst();
              if (optVlp.isPresent()) {
                nsVLC.setVirtualLinkProfileId(optVlp.get().getVirtualLinkProfileId());
                nsVLC.setCpdId(Collections.singletonList(ep));
                nsVirtualLinkConnectivities.add(nsVLC);
              }
            }
          }
          count++;
        }
      }
      vnfp.setNsVirtualLinkConnectivity(nsVirtualLinkConnectivities);
      nsDf.getVnfProfile().add(vnfp);
      nsLevel.getVnfToLevelMapping().add(new VnfToLevelMapping(vnfp.getVnfProfileId(), 1));
    }

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
    log.debug("Nsd AFTER generation with {}:\n{}",
        nsd.getNsdIdentifier(), OBJECT_MAPPER.writeValueAsString(nsd));

    Graph<ProfileVertex, String> g = nsdGraphService.buildGraph(nsd.getSapd(), nsDf, nsLevel);
    log.debug("Graph AFTER generation with {}:\n{}",
        nsd.getNsdIdentifier(), nsdGraphService.export(g));

    return nsd;
  }

}
