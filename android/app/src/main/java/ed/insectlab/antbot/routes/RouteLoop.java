package ed.insectlab.antbot.routes;

import org.ros.concurrent.CancellableLoop;

import java.util.ArrayList;

/**
 * Created by antbot on 04/08/14.
 */
public class RouteLoop extends CancellableLoop {
    protected final ArrayList<RouteInstruction> route = new ArrayList<RouteInstruction>();
    protected int routePosition = 0;

    @Override
    protected void loop() throws InterruptedException {
        if(routePosition >= route.size()) {
            RouteInstruction.STOP_COMMAND.execute();
            this.cancel();
        } else {
            RouteInstruction instruction = route.get(routePosition);
            instruction.execute();
            ++routePosition;
            Thread.sleep(instruction.duration);
        }
    }
}
