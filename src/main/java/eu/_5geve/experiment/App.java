package eu._5geve.experiment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu._5geve.blueprint.vsb.VsBlueprint;
import it.nextworks.nfvmano.libs.descriptors.nsd.Nsd;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import javax.swing.JFrame;
import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  public static void main(String[] args) throws IOException {
    InputStream is = App.class.getResourceAsStream("/nsd-examples/nsd_vCDN_pnf_gui.yaml");
    Nsd nsd_stub = OBJECT_MAPPER.readValue(is, Nsd.class);
    LOG.info("Dump:\n{}", OBJECT_MAPPER.writeValueAsString(nsd_stub));

    InputStream isVsb = App.class.getResourceAsStream("/nsd-examples/vsb_vCDN_gui.yaml");
    VsBlueprint vsb = OBJECT_MAPPER.readValue(isVsb, VsBlueprint.class);
    LOG.info("Dump:\n{}", OBJECT_MAPPER.writeValueAsString(vsb));

    createNSDGraph();

  }

  private static void createNSDGraph() throws IOException {

    // create a JGraphT graph
    ListenableGraph<String, DefaultEdge> g = new DefaultListenableGraph<>(
        new DefaultUndirectedGraph<>(DefaultEdge.class));
    String v1 = "v1";
    String v2 = "v2";
    String v3 = "v3";
    String v4 = "v4";

    // add some sample data (graph manipulated via JGraphX)
    g.addVertex(v1);
    g.addVertex(v2);
    g.addVertex(v3);
    g.addVertex(v4);

    g.addEdge(v1, v2);
    g.addEdge(v2, v3);
    g.addEdge(v3, v1);
    g.addEdge(v4, v3);

    GraphJApplet applet = new GraphJApplet();
    applet.init(g);

    JFrame frame = new JFrame();
    frame.getContentPane().add(applet);
    frame.setTitle("JGraphT Adapter to JGraphX Demo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }
}
