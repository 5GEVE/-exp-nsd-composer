package it.cnit.blueprint.composer.nsd.generate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.composer.exceptions.NsdGenerationException;
import it.cnit.blueprint.composer.exceptions.NsdInvalidException;
import it.cnit.blueprint.composer.nsd.graph.NsdGraphService;
import it.nextworks.nfvmano.catalogue.blueprint.elements.Blueprint;
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
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

  private final NsdGraphService nsdGraphService;

  @SneakyThrows(JsonProcessingException.class)
  public Nsd generate(Blueprint b) throws NsdInvalidException, NsdGenerationException {

    log.debug("blueprint {}:\n{}", b.getBlueprintId(),
        OBJECT_MAPPER.writeValueAsString(b));

    boolean mgmt = b.getConnectivityServices().stream().anyMatch(VsbLink::isManagement);
    if (!mgmt) {
      log.info("Generate a mgmt sap and connectivity service");
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
    }

    Nsd nsd = new Nsd();
    nsd.setNsdIdentifier(b.getBlueprintId() + "_nsd");
    nsd.setDesigner("NSD generator");
    nsd.setNsdInvariantId(b.getBlueprintId() + "_nsd");
    nsd.setVersion(b.getVersion());
    nsd.setNsdName(b.getName() + " NSD");
    nsd.setSecurity(new SecurityParameters(
        "FC_NSD_SIGNATURE",
        "FC_NSD_ALGORITHM",
        "FC_NSD_CERTIFICATE"
    ));

    NsDf nsDf = new NsDf();
    nsDf.setNsDfId(b.getBlueprintId() + "_df");
    nsDf.setFlavourKey(b.getBlueprintId() + "_df_fk");

    NsLevel nsLevel = new NsLevel();
    nsLevel.setNsLevelId(b.getBlueprintId() + "_il_default");
    nsLevel.setDescription("Default Instantiation Level");

    for (VsbLink connService : b.getConnectivityServices()) {
      NsVirtualLinkDesc vld = new NsVirtualLinkDesc();
      vld.setVirtualLinkDescId(connService.getName());
      vld.setVirtualLinkDescProvider(nsd.getDesigner());
      vld.setVirtuaLinkDescVersion(nsd.getVersion());
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
    }

    List<Sapd> sapdList = new ArrayList<>();
    for (VsbEndpoint e : b.getEndPoints()) {
      // An insecure way to determine sap endpoints.
      if (e.isExternal() && e.getEndPointId().toLowerCase().contains("sap")) {
        Sapd sapd = new Sapd();
        sapd.setCpdId(e.getEndPointId());
        sapd.setLayerProtocol(LayerProtocol.IPV4);
        sapd.setCpRole(CpRole.ROOT);
        sapd.setSapAddressAssignment(false);
        for (VsbLink cs : b.getConnectivityServices()) {
          for (String ep : cs.getEndPointIds()) {
            if (ep.equals(sapd.getCpdId())) {
              sapd.setNsVirtualLinkDescId(cs.getName());
              break;
            }
          }
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

    for (VsComponent vsc : b.getAtomicComponents()) {
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
        for (VsbLink cs : b.getConnectivityServices()) {
          for (String csEp : cs.getEndPointIds()) {
            if (csEp.equals(ep)) {
              NsVirtualLinkConnectivity nsVLC = new NsVirtualLinkConnectivity();
              String vldId = cs.getName();
              Optional<VirtualLinkProfile> optVlp = nsDf.getVirtualLinkProfile().stream()
                  .filter(vlp -> vlp.getVirtualLinkDescId().equals(vldId)).findFirst();
              if (optVlp.isPresent()) {
                nsVLC.setVirtualLinkProfileId(optVlp.get().getVirtualLinkProfileId());
                nsVLC.setCpdId(Collections.singletonList(ep));
                nsVirtualLinkConnectivities.add(nsVLC);
              }
            }
          }
        }
      }
      vnfp.setNsVirtualLinkConnectivity(nsVirtualLinkConnectivities);
      nsDf.getVnfProfile().add(vnfp);
      nsLevel.getVnfToLevelMapping().add(new VnfToLevelMapping(vnfp.getVnfProfileId(), 1));
    }

    nsDf.setNsInstantiationLevel(Collections.singletonList(nsLevel));
    nsDf.setDefaultNsInstantiationLevelId(nsLevel.getNsLevelId());
    nsd.setNsDf(Collections.singletonList(nsDf));

    log.debug("Generated NSD:\n{}", OBJECT_MAPPER.writeValueAsString(nsd));

    // Nsd validation and logging
    try {
      nsd.isValid();
    } catch (MalformattedElementException e) {
      throw new NsdGenerationException(nsd.getNsdIdentifier(),
          "Nsd not valid after generation", e);
    }

    return nsd;
  }

}
