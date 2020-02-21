package it.cnit.blueprint.expbuilder.nsd.compose;

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
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class NsdComposerTest {

  @Test
  @SneakyThrows
  public void composeTrackerWithDelay() {

    Properties prop = new Properties();
    InputStream input = ClassLoader.getSystemResourceAsStream("url.properties");
    prop.load(input);
    ObjectMapper oM = new ObjectMapper(new YAMLFactory());
    NsdGraphService nsdGraphService = new NsdGraphService(new GraphVizExporter());
    NsdComposer nsdComposer = new NsdComposer(nsdGraphService);

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
    log.debug("ctxComposeInfo dump:\n{}", oM.writeValueAsString(ctxComposeInfo));

    nsdComposer.compose(trackerNsd, new CtxComposeInfo[]{ctxComposeInfo});
  }
}