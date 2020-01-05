package eu._5geve.experiment;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import java.awt.Dimension;
import javax.swing.JApplet;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;

public class GraphJApplet extends JApplet {

  private static final long serialVersionUID = 2202072534703043194L;

  private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);

  private JGraphXAdapter<String, DefaultEdge> jgxAdapter;

  public void init(ListenableGraph<String, DefaultEdge> g) {
    // create a visualization using JGraph, via an adapter
    jgxAdapter = new JGraphXAdapter<>(g);

    setPreferredSize(DEFAULT_SIZE);
    mxGraphComponent component = new mxGraphComponent(jgxAdapter);
    component.setConnectable(false);
    component.getGraph().setAllowDanglingEdges(true);
    getContentPane().add(component);
    resize(DEFAULT_SIZE);

    // positioning via jgraphx layouts
    mxCircleLayout layout = new mxCircleLayout(jgxAdapter);

    // center the circle
    int radius = 100;
    layout.setX0((DEFAULT_SIZE.width / 2.0) - radius);
    layout.setY0((DEFAULT_SIZE.height / 2.0) - radius);
    layout.setRadius(radius);
    layout.setMoveCircle(true);

    layout.execute(jgxAdapter.getDefaultParent());
    // that's all there is to it!...
  }

}
