package it.cnit.blueprint.expbuilder.nsd.compose;

import static org.junit.Assert.assertEquals;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.nsd.graph.GraphVizExporter;
import it.cnit.blueprint.expbuilder.nsd.graph.NsdGraphService;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;
import java.io.InputStream;
import java.net.URL;
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
  static NsdComposer nsdComposer;

  @BeforeClass
  @SneakyThrows
  public static void setUp() {
    // Test Setup
    urlProp = new Properties();
    InputStream input = ClassLoader.getSystemResourceAsStream("url.properties");
    urlProp.load(input);
    oM = new ObjectMapper(new YAMLFactory());
    nsdGraphService = new NsdGraphService(new GraphVizExporter());
    nsdComposer = new NsdComposer(nsdGraphService);
    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.DEBUG);
  }

  @Test
  @SneakyThrows
  public void composeTrackerWithDelay() {

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
        .filter(v->v.getVirtualLinkDescId().equals("vl_tracking_mgt")).findFirst();
    if (optVsbVld.isPresent()){
      vsbMgmtVld=optVsbVld.get();
    } else {
      throw new Exception();
    }
    Nsd ctxNsd = Arrays
        .asList(oM.readValue(new URL(urlProp.getProperty("ctx.delay.nsds")), Nsd[].class)).get(0);
    NsVirtualLinkDesc ctxMgmtVld;
    Optional<NsVirtualLinkDesc> optCtxVld = ctxNsd.getVirtualLinkDesc().stream()
        .filter(v->v.getVirtualLinkDescId().equals("vl_dg_mgt")).findFirst();
    if (optCtxVld.isPresent()){
      ctxMgmtVld=optCtxVld.get();
    } else {
      throw new Exception();
    }

    // When
    nsdComposer.composePassThrough(ranSapd, vsbMgmtVld, vsbNsd, ctxMgmtVld, ctxNsd);
    // Setting ID manually for test purpose
    vsbNsd.setNsdIdentifier("58886b95-cd29-4b7b-aca0-e884caaa5c68");
    vsbNsd.setNsdInvariantId("ae66294b-8dae-406c-af70-f8516e310965");

    // Then
    InputStream in = getClass().getResourceAsStream("/expb_ares2t_tracker_delay_nsds.yaml");
    Nsd expNsd = oM.readValue(in, Nsd[].class)[0];
    assertEquals(oM.writeValueAsString(expNsd), oM.writeValueAsString(vsbNsd));
  }
}