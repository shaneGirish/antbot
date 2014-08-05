package ed.insectlab.antbot.routes;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;

/**
 * Created by antbot on 04/08/14.
 */
public class RotateRouteNode extends AbstractNodeMain {
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("antbot/StraightRouteNode");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        connectedNode.executeCancellableLoop(new RouteLoop() {
            @Override
            protected void setup() {
                route.add(RouteInstruction.create(0, 0, 5000));
                route.add(RouteInstruction.create(-200, 200, 10000));
            }
        });
    }
}
