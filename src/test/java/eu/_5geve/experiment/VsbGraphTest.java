package eu._5geve.experiment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu._5geve.blueprint.vsb.VsBlueprint;
import eu._5geve.experiment.vsbgraph.VsbGraph;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import org.jgrapht.io.ExportException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class VsbGraphTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  private static final String NL = System.getProperty("line.separator");
  private final static String VCDN_GRAPHVIZ =
      "strict graph G {" + NL +
          "  vCacheMid_01 [ label=\"atomicComponent_vCacheMid_01\" shape=\"box\" style=\"filled\" fillcolor=\"yellowgreen\" ];"
          + NL +
          "  pDNS_v01 [ label=\"atomicComponent_pDNS_v01\" shape=\"box\" style=\"filled\" fillcolor=\"yellowgreen\" ];"
          + NL +
          "  vCacheEdge_1_01 [ label=\"atomicComponent_vCacheEdge_1_01\" shape=\"box\" style=\"filled\" fillcolor=\"yellowgreen\" ];"
          + NL +
          "  pOrigin_v01 [ label=\"atomicComponent_pOrigin_v01\" shape=\"box\" style=\"filled\" fillcolor=\"yellowgreen\" ];"
          + NL +
          "  1089418272 [ label=\"vsbLink_vCacheEdge_1_users_ext\" shape=\"oval\" style=\"filled\" fillcolor=\"dodgerblue\" ];"
          + NL +
          "  1233990028 [ label=\"vsbLink_vCacheEdge_1_mgmt_ext_dns_users_vCacheMid_mgmt_ext_origin_caches\" shape=\"oval\" style=\"filled\" fillcolor=\"dodgerblue\" ];"
          + NL +
          "  1847008471 [ label=\"vsbLink_vCacheEdge_1_cache_ext_vCacheMid_cache_ext\" shape=\"oval\" style=\"filled\" fillcolor=\"dodgerblue\" ];"
          + NL +
          "  1076607567 [ label=\"vsbLink_vCacheMid_origin_ext\" shape=\"oval\" style=\"filled\" fillcolor=\"dodgerblue\" ];"
          + NL +
          "  vCacheMid_01 -- 1233990028 [ label=\"vCacheMid_mgmt_ext\" ];" + NL +
          "  vCacheMid_01 -- 1847008471 [ label=\"vCacheMid_cache_ext\" ];" + NL +
          "  vCacheMid_01 -- 1076607567 [ label=\"vCacheMid_origin_ext\" ];" + NL +
          "  pDNS_v01 -- 1233990028 [ label=\"dns_users\" ];" + NL +
          "  vCacheEdge_1_01 -- 1233990028 [ label=\"vCacheEdge_1_mgmt_ext\" ];" + NL +
          "  vCacheEdge_1_01 -- 1847008471 [ label=\"vCacheEdge_1_cache_ext\" ];" + NL +
          "  vCacheEdge_1_01 -- 1089418272 [ label=\"vCacheEdge_1_users_ext\" ];" + NL +
          "  pOrigin_v01 -- 1233990028 [ label=\"origin_caches\" ];" + NL +
          "}" + NL;
  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  @Test
  public void vCDNGuiTest() throws IOException {
    InputStream isVsb = App.class.getResourceAsStream("/nsd-examples/vsb_vCDN_gui.yaml");
    VsBlueprint vsb = OBJECT_MAPPER.readValue(isVsb, VsBlueprint.class);

    VsbGraph vsbGraph = new VsbGraph(vsb);

    // TODO move this information
    // Export to graphviz.
    // Copy the output to a text file called 'example.txt'
    // Create a PNG with:
    // sfdp -Tpng example.txt -o example.png
    try {
      LOG.info("GraphViz export:\n{}", vsbGraph.exportGraphViz());
      assertEquals(VCDN_GRAPHVIZ, vsbGraph.exportGraphViz());
    } catch (ExportException e) {
      e.printStackTrace();
    }

    try {
      LOG.info("GraphML export:\n{}", vsbGraph.exportGraphML());
    } catch (ExportException e) {
      e.printStackTrace();
    }


  }

}
