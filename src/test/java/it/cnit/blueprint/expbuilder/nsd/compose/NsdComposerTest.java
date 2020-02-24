package it.cnit.blueprint.expbuilder.nsd.compose;

import static org.junit.Assert.assertEquals;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.nsd.graph.GraphVizExporter;
import it.cnit.blueprint.expbuilder.nsd.graph.NsdGraphService;
import it.cnit.blueprint.expbuilder.rest.CtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.VnfConnection;
import it.nextworks.nfvmano.catalogue.blueprint.messages.OnboardCtxBlueprintRequest;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import lombok.SneakyThrows;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class NsdComposerTest {

  static Properties prop;
  static ObjectMapper oM;
  static NsdGraphService nsdGraphService;
  static NsdComposer nsdComposer;

  @BeforeClass
  @SneakyThrows
  public static void setUp() {
    // Test Setup
    prop = new Properties();
    InputStream input = ClassLoader.getSystemResourceAsStream("url.properties");
    prop.load(input);
    oM = new ObjectMapper(new YAMLFactory());
    nsdGraphService = new NsdGraphService(new GraphVizExporter());
    nsdComposer = new NsdComposer(nsdGraphService);
    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.ERROR);
  }

  @Test
  @SneakyThrows
  public void composeTrackerWithDelay() {

    // Given
    Nsd trackerNsd = oM.readValue(new URL(prop.getProperty("vsb.tracker.nsds")), Nsd[].class)[0];

    List<Nsd> delayNsds = Arrays
        .asList(oM.readValue(new URL(prop.getProperty("ctx.delay.nsds")), Nsd[].class));
    OnboardCtxBlueprintRequest onbCtxReq = new OnboardCtxBlueprintRequest(null, delayNsds, null);
    VnfConnection[] ctxConnections = new VnfConnection[3];
    ctxConnections[0] = new VnfConnection("vnfp_netem", "cp_dg_traffic_in",
        "vlp_vl_tracking_mobile");
    ctxConnections[1] = new VnfConnection("vnfp_netem", "cp_dg_traffic_out",
        "vlp_vl_dg_out");
    ctxConnections[2] = new VnfConnection("vnfp_netem", "cp_dg_mgt",
        "vlp_vl_tracking_mgt");
    VnfConnection[] vsConnections = new VnfConnection[2];
    vsConnections[0] = new VnfConnection("vnfp_big_Ares2T_Tracker", "cp_tracker_ext_in",
        "vlp_vl_dg_out");
    vsConnections[1] = new VnfConnection("vnfp_small_Ares2T_Tracker", "cp_tracker_ext_in",
        "vlp_vl_dg_out");
    CtxComposeInfo ctxComposeInfo = new CtxComposeInfo(onbCtxReq, ctxConnections, vsConnections);

    // When
    nsdComposer.compose(trackerNsd, new CtxComposeInfo[]{ctxComposeInfo});
    // Setting ID manually for test purpose
    trackerNsd.setNsdIdentifier("58886b95-cd29-4b7b-aca0-e884caaa5c68");
    trackerNsd.setNsdInvariantId("ae66294b-8dae-406c-af70-f8516e310965");

    // Then
    InputStream in = getClass().getResourceAsStream("/expb_ares2t_tracker_delay_nsds.yaml");
    Nsd expNsd = oM.readValue(in, Nsd[].class)[0];
    assertEquals(oM.writeValueAsString(expNsd), oM.writeValueAsString(trackerNsd));
  }
}