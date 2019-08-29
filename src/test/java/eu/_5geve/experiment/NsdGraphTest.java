package eu._5geve.experiment;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu._5geve.blueprint.vsb.VsBlueprint;
import eu._5geve.experiment.nsdgraph.NsdGraph;
import eu._5geve.experiment.vsbgraph.VsbGraph;
import it.nextworks.nfvmano.libs.descriptors.nsd.Nsd;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import org.jgrapht.io.ExportException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NsdGraphTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  private static final String NL = System.getProperty("line.separator");
  private final static String VCDN_GRAPHVIZ =
      "strict graph G {" + NL +
          "  vCacheMid_profile [ label=\"vnfProfile_vCacheMid_profile\" shape=\"box\" style=\"filled\" fillcolor=\"yellowgreen\" ];"
          + NL +
          "  vCacheEdge_1_profile [ label=\"vnfProfile_vCacheEdge_1_profile\" shape=\"box\" style=\"filled\" fillcolor=\"yellowgreen\" ];"
          + NL +
          "  vCacheEdge_2_profile [ label=\"vnfProfile_vCacheEdge_2_profile\" shape=\"box\" style=\"filled\" fillcolor=\"yellowgreen\" ];"
          + NL +
          "  pDNS_profile [ label=\"pnfProfile_pDNS_profile\" shape=\"box\" style=\"filled\" fillcolor=\"yellowgreen\" ];"
          + NL +
          "  pOrigin_profile [ label=\"pnfProfile_pOrigin_profile\" shape=\"box\" style=\"filled\" fillcolor=\"yellowgreen\" ];"
          + NL +
          "  vl_profile_mgmt [ label=\"vlProfile_vl_profile_mgmt\" shape=\"oval\" style=\"filled\" fillcolor=\"dodgerblue\" ];"
          + NL +
          "  vl_profile_users [ label=\"vlProfile_vl_profile_users\" shape=\"oval\" style=\"filled\" fillcolor=\"dodgerblue\" ];"
          + NL +
          "  vl_profile_cache [ label=\"vlProfile_vl_profile_cache\" shape=\"oval\" style=\"filled\" fillcolor=\"dodgerblue\" ];"
          + NL +
          "  vl_profile_origin [ label=\"vlProfile_vl_profile_origin\" shape=\"oval\" style=\"filled\" fillcolor=\"dodgerblue\" ];"
          + NL +
          "  sap_mgmt [ label=\"sap_mgmt\" shape=\"circle\" style=\"filled\" fillcolor=\"firebrick2\" ];"
          + NL +
          "  sap_users [ label=\"sap_users\" shape=\"circle\" style=\"filled\" fillcolor=\"firebrick2\" ];"
          + NL +
          "  sap_origin [ label=\"sap_origin\" shape=\"circle\" style=\"filled\" fillcolor=\"firebrick2\" ];"
          + NL +
          "  vCacheMid_profile -- vl_profile_mgmt [ label=\"vCacheMid_mgmt_ext\" ];" + NL +
          "  vCacheMid_profile -- vl_profile_cache [ label=\"vCacheMid_cache_ext\" ];" + NL +
          "  vCacheMid_profile -- vl_profile_origin [ label=\"vCacheMid_origin_ext\" ];" + NL +
          "  vCacheEdge_1_profile -- vl_profile_mgmt [ label=\"vCacheEdge_1_mgmt_ext\" ];" + NL +
          "  vCacheEdge_1_profile -- vl_profile_cache [ label=\"vCacheEdge_1_cache_ext\" ];" + NL +
          "  vCacheEdge_1_profile -- vl_profile_users [ label=\"vCacheEdge_1_users_ext\" ];" + NL +
          "  vCacheEdge_2_profile -- vl_profile_mgmt [ label=\"vCacheEdge_2_mgmt_ext\" ];" + NL +
          "  vCacheEdge_2_profile -- vl_profile_cache [ label=\"vCacheEdge_2_cache_ext\" ];" + NL +
          "  vCacheEdge_2_profile -- vl_profile_users [ label=\"vCacheEdge_2_users_ext\" ];" + NL +
          "  pDNS_profile -- vl_profile_mgmt [ label=\"dns_users\" ];" + NL +
          "  pOrigin_profile -- vl_profile_mgmt [ label=\"origin_caches\" ];" + NL +
          "  sap_mgmt -- vl_profile_mgmt [ label=\"sap_mgmt\" ];" + NL +
          "  sap_users -- vl_profile_users [ label=\"sap_users\" ];" + NL +
          "  sap_origin -- vl_profile_origin [ label=\"sap_origin\" ];" + NL +
          "}" + NL;
  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  @Test
  public void vCDNGuiTest() throws IOException {
    InputStream isNsd = App.class.getResourceAsStream("/nsd-examples/nsd_vCDN_pnf_gui.yaml");
    Nsd nsd = OBJECT_MAPPER.readValue(isNsd, Nsd.class);

    NsdGraph nsdGraph = new NsdGraph(nsd);

    // TODO move this information
    // Export to graphviz.
    // Copy the output to a text file called 'example.txt'
    // Create a PNG with:
    // sfdp -Tpng example.txt -o example.png
    try {
      LOG.info("GraphViz export:\n{}", nsdGraph.exportGraphViz());
      assertEquals(VCDN_GRAPHVIZ, nsdGraph.exportGraphViz());
    } catch (ExportException e) {
      e.printStackTrace();
    }

    try {
      LOG.info("GraphML export:\n{}", nsdGraph.exportGraphML());
    } catch (ExportException e) {
      e.printStackTrace();
    }


  }

}
