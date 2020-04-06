package it.cnit.blueprint.composer.nsd.compose;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.composer.nsd.graph.GraphVizExporter;
import it.cnit.blueprint.composer.nsd.graph.NsdGraphService;
import it.cnit.blueprint.composer.rest.ConnectInput;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.BeforeClass;
import org.junit.Test;

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
  }

  @Test
  @SneakyThrows
  public void composeTrackerWithDelayPassThrough() {

    // Given
    Nsd vsbNsd = oM.readValue(new URL(urlProp.getProperty("vsb.tracker.nsds")), Nsd[].class)[0];
    NsVirtualLinkDesc ranVld;
    Optional<NsVirtualLinkDesc> optRanVl = vsbNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vl_tracking_mobile"))
        .findFirst();
    if (optRanVl.isPresent()) {
      ranVld = optRanVl.get();
    } else {
      throw new Exception();
    }
    NsVirtualLinkDesc vsbMgmtVld;
    Optional<NsVirtualLinkDesc> optVsbVld = vsbNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vl_tracking_mgmt")).findFirst();
    if (optVsbVld.isPresent()) {
      vsbMgmtVld = optVsbVld.get();
    } else {
      throw new Exception();
    }
    Nsd ctxNsd = Arrays
        .asList(oM.readValue(new URL(urlProp.getProperty("ctx.delay.nsds")), Nsd[].class)).get(0);
    NsVirtualLinkDesc ctxMgmtVld;
    Optional<NsVirtualLinkDesc> optCtxVld = ctxNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vl_dg_mgmt")).findFirst();
    if (optCtxVld.isPresent()) {
      ctxMgmtVld = optCtxVld.get();
    } else {
      throw new Exception();
    }

    // When
    passThroughComposer
        .compose(new ConnectInput(), ranVld, vsbMgmtVld, vsbNsd, ctxMgmtVld, ctxNsd);
    // Setting ID manually for test purpose
    vsbNsd.setNsdIdentifier("58886b95-cd29-4b7b-aca0-e884caaa5c68");
    vsbNsd.setNsdInvariantId("ae66294b-8dae-406c-af70-f8516e310965");
    vsbNsd.setDesigner(vsbNsd.getDesigner() + " + NSD Composer");

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
    NsVirtualLinkDesc ranVld;
    Optional<NsVirtualLinkDesc> optRanVl = vsbNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vl_tracking_mobile"))
        .findFirst();
    if (optRanVl.isPresent()) {
      ranVld = optRanVl.get();
    } else {
      throw new Exception();
    }
    NsVirtualLinkDesc vsbMgmtVld;
    Optional<NsVirtualLinkDesc> optVsbVld = vsbNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vl_tracking_mgmt")).findFirst();
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
        .filter(v -> v.getVirtualLinkDescId().equals("vl_dg_mgmt")).findFirst();
    if (optCtxVld.isPresent()) {
      ctxMgmtVld = optCtxVld.get();
    } else {
      throw new Exception();
    }

    // When
    connectComposer.compose(new ConnectInput(), ranVld, vsbMgmtVld, vsbNsd, ctxMgmtVld, ctxNsd);
    // Setting ID manually for test purpose
    vsbNsd.setNsdIdentifier("58886b95-cd29-4b7b-aca0-e884caaa5c68");
    vsbNsd.setNsdInvariantId("ae66294b-8dae-406c-af70-f8516e310965");
    vsbNsd.setDesigner(vsbNsd.getDesigner() + " + NSD Composer");

    // Then
    InputStream in = getClass().getResourceAsStream(
        "/expb_ares2t_tracker_delay_nsds_connect.yaml");
    Nsd expNsd = oM.readValue(in, Nsd[].class)[0];
    assertEquals(oM.writeValueAsString(expNsd), oM.writeValueAsString(vsbNsd));
  }

  @Test
  @SneakyThrows
  public void composeTrackerWithBackgroundConnect() {
    // Given
    Nsd vsbNsd = oM.readValue(new URL(urlProp.getProperty("vsb.tracker.nsds")), Nsd[].class)[0];
    NsVirtualLinkDesc ranVld;
    Optional<NsVirtualLinkDesc> optRanVl = vsbNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vl_tracking_mobile"))
        .findFirst();
    if (optRanVl.isPresent()) {
      ranVld = optRanVl.get();
    } else {
      throw new Exception();
    }
    NsVirtualLinkDesc vsbMgmtVld;
    Optional<NsVirtualLinkDesc> optVsbVld = vsbNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vl_tracking_mgmt")).findFirst();
    if (optVsbVld.isPresent()) {
      vsbMgmtVld = optVsbVld.get();
    } else {
      throw new Exception();
    }
    Nsd ctxNsd = Arrays
        .asList(oM.readValue(new URL(urlProp.getProperty("ctx.bg_traffic.nsds")), Nsd[].class))
        .get(0);
    NsVirtualLinkDesc ctxMgmtVld;
    Optional<NsVirtualLinkDesc> optCtxVld = ctxNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vld_1")).findFirst();
    if (optCtxVld.isPresent()) {
      ctxMgmtVld = optCtxVld.get();
    } else {
      throw new Exception();
    }

    // When
    connectComposer.compose(new ConnectInput(), ranVld, vsbMgmtVld, vsbNsd, ctxMgmtVld, ctxNsd);
    // Setting ID manually for test purpose
    vsbNsd.setNsdIdentifier("2dd7b5b1-9f39-4978-a035-6654d7bc9068");
    vsbNsd.setNsdInvariantId("d5959420-1ef7-4441-9eb9-9113172c988b");
    vsbNsd.setDesigner(vsbNsd.getDesigner() + " + NSD Composer");

    // Then
    InputStream in = getClass().getResourceAsStream(
        "/expb_ares2t_tracker_bg_traffic_nsds_connect.yaml");
    Nsd expNsd = oM.readValue(in, Nsd[].class)[0];
    assertEquals(oM.writeValueAsString(expNsd), oM.writeValueAsString(vsbNsd));

  }
  @Test
  @SneakyThrows
  public void composeTrackerWithBackgroundAndDelay() {

    // Given
    Nsd vsbNsd = oM.readValue(new URL(urlProp.getProperty("vsb.tracker.nsds")), Nsd[].class)[0];
    NsVirtualLinkDesc ranVld;
    Optional<NsVirtualLinkDesc> optRanVl = vsbNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vl_tracking_mobile"))
        .findFirst();
    if (optRanVl.isPresent()) {
      ranVld = optRanVl.get();
    } else {
      throw new Exception();
    }
    NsVirtualLinkDesc vsbMgmtVld;
    Optional<NsVirtualLinkDesc> optVsbVld = vsbNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vl_tracking_mgmt")).findFirst();
    if (optVsbVld.isPresent()) {
      vsbMgmtVld = optVsbVld.get();
    } else {
      throw new Exception();
    }
    Nsd delayNsd = Arrays
        .asList(oM.readValue(new URL(urlProp.getProperty("ctx.delay.nsds")), Nsd[].class)).get(0);
    NsVirtualLinkDesc delayMgmtVld;
    Optional<NsVirtualLinkDesc> optCtxVld = delayNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vl_dg_mgmt")).findFirst();
    if (optCtxVld.isPresent()) {
      delayMgmtVld = optCtxVld.get();
    } else {
      throw new Exception();
    }
    Nsd bgNsd = Arrays
        .asList(oM.readValue(new URL(urlProp.getProperty("ctx.bg_traffic.nsds")), Nsd[].class))
        .get(0);
    NsVirtualLinkDesc bgMgmtVld;
    optCtxVld = bgNsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals("vld_1")).findFirst();
    if (optCtxVld.isPresent()) {
      bgMgmtVld = optCtxVld.get();
    } else {
      throw new Exception();
    }

    // When
    passThroughComposer
        .compose(new ConnectInput(), ranVld, vsbMgmtVld, vsbNsd, delayMgmtVld, delayNsd);
    connectComposer.compose(new ConnectInput(), ranVld, vsbMgmtVld, vsbNsd, bgMgmtVld, bgNsd);
    // Setting ID manually for test purpose
    vsbNsd.setNsdIdentifier("bfb761be-ab0f-499c-88d7-ac6ce7263651");
    vsbNsd.setNsdInvariantId("d650cb24-28c5-41ba-8541-12a9cb93238c");
    vsbNsd.setDesigner(vsbNsd.getDesigner() + " + NSD Composer");

    // Then
    InputStream in = getClass().getResourceAsStream(
        "/expb_ares2t_tracker_delay_bg_traffic_nsds.yaml");
    Nsd expNsd = oM.readValue(in, Nsd[].class)[0];
    assertEquals(oM.writeValueAsString(expNsd), oM.writeValueAsString(vsbNsd));

  }
}