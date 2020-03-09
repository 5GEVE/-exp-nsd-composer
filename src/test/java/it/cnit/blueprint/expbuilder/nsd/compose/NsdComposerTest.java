package it.cnit.blueprint.expbuilder.nsd.compose;

import static org.junit.Assert.assertEquals;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.nsd.graph.GraphVizExporter;
import it.cnit.blueprint.expbuilder.nsd.graph.NsdGraphService;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import lombok.SneakyThrows;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class NsdComposerTest {

  static Properties urlProp;
  static ObjectMapper oM;
  static NsdGraphService nsdGraphService;
  static NsdComposer passThroughComposer;
  static NsdComposer connectComposer;

  @BeforeClass
  @SneakyThrows
  public static void setUp() {
    // Test Setup
    urlProp = new Properties();
    InputStream input = ClassLoader.getSystemResourceAsStream("url.properties");
    urlProp.load(input);
    oM = new ObjectMapper(new YAMLFactory());
    nsdGraphService = new NsdGraphService(new GraphVizExporter());
    passThroughComposer = new PassThroughComposer(nsdGraphService);
    connectComposer = new ConnectComposer(nsdGraphService);
    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.INFO);
  }

  @Test
  @SneakyThrows
  public void composeTrackerWithDelayPassThrough() {

    // Given
    Nsd vsbNsd = oM.readValue(new URL(urlProp.getProperty("vsb.tracker.nsds")), Nsd[].class)[0];
    Sapd ranSapd;
    Optional<Sapd> optSapd = vsbNsd.getSapd().stream()
        .filter(s -> s.getCpdId().equals("sap_tracking_mobile")).findFirst();
    if (optSapd.isPresent()) {
      ranSapd = optSapd.get();
    } else {
      throw new Exception();
    }
    NsVirtualLinkDesc vsbMgmtVld;
    Optional<NsVirtualLinkDesc> optVsbVld = vsbNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vl_tracking_mgt")).findFirst();
    if (optVsbVld.isPresent()) {
      vsbMgmtVld = optVsbVld.get();
    } else {
      throw new Exception();
    }
    Nsd ctxNsd = Arrays
        .asList(oM.readValue(new URL(urlProp.getProperty("ctx.delay.nsds")), Nsd[].class)).get(0);
    NsVirtualLinkDesc ctxMgmtVld;
    Optional<NsVirtualLinkDesc> optCtxVld = ctxNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vl_dg_mgt")).findFirst();
    if (optCtxVld.isPresent()) {
      ctxMgmtVld = optCtxVld.get();
    } else {
      throw new Exception();
    }

    // When
    passThroughComposer.compose(ranSapd, vsbMgmtVld, vsbNsd, ctxMgmtVld, ctxNsd);
    // Setting ID manually for test purpose
    vsbNsd.setNsdIdentifier("58886b95-cd29-4b7b-aca0-e884caaa5c68");
    vsbNsd.setNsdInvariantId("ae66294b-8dae-406c-af70-f8516e310965");

    // Then
    InputStream in = getClass().getResourceAsStream(
        "/expb_ares2t_tracker_delay_nsds_passthrough.yaml");
    Nsd expNsd = oM.readValue(in, Nsd[].class)[0];
    assertEquals(oM.writeValueAsString(expNsd), oM.writeValueAsString(vsbNsd));
  }

  @Test
  @SneakyThrows
  public void composeTrackerWithDelayConnect() {

    // Given
    Nsd vsbNsd = oM.readValue(new URL(urlProp.getProperty("vsb.tracker.nsds")), Nsd[].class)[0];
    Sapd ranSapd;
    Optional<Sapd> optSapd = vsbNsd.getSapd().stream()
        .filter(s -> s.getCpdId().equals("sap_tracking_mobile")).findFirst();
    if (optSapd.isPresent()) {
      ranSapd = optSapd.get();
    } else {
      throw new Exception();
    }
    NsVirtualLinkDesc vsbMgmtVld;
    Optional<NsVirtualLinkDesc> optVsbVld = vsbNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vl_tracking_mgt")).findFirst();
    if (optVsbVld.isPresent()) {
      vsbMgmtVld = optVsbVld.get();
    } else {
      throw new Exception();
    }
    Nsd ctxNsd = Arrays
        .asList(oM.readValue(new URL(urlProp.getProperty("ctx.delay.nsds")), Nsd[].class)).get(0);
    // Add twice the vnfd to fake the connect algorithm TODO use a real CONNECT ctx
//    ctxNsd.getVnfdId().add(ctxNsd.getVnfdId().get(0));
    VnfProfile ctxVnfp = ctxNsd.getNsDf().get(0).getVnfProfile().get(0);
    VnfProfile ctxVnfpFake = new VnfProfile(ctxNsd.getNsDf().get(0), "vnfp_netem_fake",
        "396d1b6b-331b-4dd7-b48e-376517d3654a", "vnf_df_netem", "vnf_il_netem",
        1, 1, null, null, null);
    for (NsVirtualLinkConnectivity vlc : ctxVnfp.getNsVirtualLinkConnectivity()) {
      NsVirtualLinkConnectivity vlcFake = new NsVirtualLinkConnectivity(ctxVnfpFake,
          vlc.getVirtualLinkProfileId(), new ArrayList<>(vlc.getCpdId()));
      ctxVnfpFake.getNsVirtualLinkConnectivity().add(vlcFake);
    }
    VnfToLevelMapping ctxVnfLvlFake = new VnfToLevelMapping(ctxVnfpFake.getVnfProfileId(), 1);
    VnfInfo ctxVnfFakeInfo = new VnfInfo(ctxVnfpFake.getVnfdId(), ctxVnfpFake, ctxVnfLvlFake);
    connectComposer.addVnf(ctxVnfFakeInfo, ctxNsd, ctxNsd.getNsDf().get(0),
        ctxNsd.getNsDf().get(0).getNsInstantiationLevel().get(0));

    NsVirtualLinkDesc ctxMgmtVld;
    Optional<NsVirtualLinkDesc> optCtxVld = ctxNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vl_dg_mgt")).findFirst();
    if (optCtxVld.isPresent()) {
      ctxMgmtVld = optCtxVld.get();
    } else {
      throw new Exception();
    }

    // When
    connectComposer.compose(ranSapd, vsbMgmtVld, vsbNsd, ctxMgmtVld, ctxNsd);
    // Setting ID manually for test purpose
    vsbNsd.setNsdIdentifier("58886b95-cd29-4b7b-aca0-e884caaa5c68");
    vsbNsd.setNsdInvariantId("ae66294b-8dae-406c-af70-f8516e310965");

    // Then
    InputStream in = getClass().getResourceAsStream(
        "/expb_ares2t_tracker_delay_nsds_connect.yaml");
    Nsd expNsd = oM.readValue(in, Nsd[].class)[0];
    assertEquals(oM.writeValueAsString(expNsd), oM.writeValueAsString(vsbNsd));
  }
}