package ed.insectlab.antbot.controlpanel;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;

/**
 * Created by antbot on 04/08/14.
 */
public class ControlPanelNode extends AbstractNodeMain {
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("antbot/PathNode");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {

    }

    @Override
    public void onShutdown(Node node) {

    }

    @Override
    public void onShutdownComplete(Node node) {

    }

    @Override
    public void onError(Node node, Throwable throwable) {

    }
}
