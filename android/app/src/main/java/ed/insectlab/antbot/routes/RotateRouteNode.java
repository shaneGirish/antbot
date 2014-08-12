package ed.insectlab.antbot.routes;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;

/**
 * Created by antbot on 04/08/14.
 */
public class RotateRouteNode extends AbstractNodeMain {

    private final int speed;

    public RotateRouteNode(int speed) {
        this.speed = speed;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("antbot/StraightRouteNode");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        connectedNode.executeCancellableLoop(new RouteLoop() {
            @Override
            protected void setup() {
                route.add(new RouteInstruction(0, 0, 1000));
                route.add(new RouteInstruction(-speed, speed, 5000));
            }
        });
    }
}
