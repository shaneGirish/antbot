package ed.insectlab.antbot.routes;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;

import java.util.ArrayList;

/**
 * Created by antbot on 04/08/14.
 */
public class StraightRouteNode extends AbstractNodeMain {
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("antbot/StraightRouteNode");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        connectedNode.executeCancellableLoop(new RouteLoop() {
            @Override
            protected void setup() {
                route.add(RouteInstruction.create(0, 0, 1000));
                route.add(RouteInstruction.create(25, 25, 1000));
                route.add(RouteInstruction.create(50, 50, 1000));
                route.add(RouteInstruction.create(25, 25, 1000));
            }
        });
    }
}
