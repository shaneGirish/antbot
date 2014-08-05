package ed.insectlab.antbot.routes;

import org.ros.concurrent.CancellableLoop;

import java.util.ArrayList;

import ed.insectlab.antbot.MainActivity;
import ed.insectlab.antbot.serial.SerialNode;

/**
 * Created by antbot on 04/08/14.
 */
public class RouteLoop extends CancellableLoop {
    protected final ArrayList<RouteInstruction> route = new ArrayList<RouteInstruction>();
    protected int routePosition = 0;

    @Override
    protected void loop() throws InterruptedException {
        if(routePosition >= route.size()) {
            MainActivity.instance.serialNode.sendMessage(RouteInstruction.create(0, 0, 0).getCommandMessage());
            this.cancel();
        } else {
            RouteInstruction instruction = route.get(routePosition);
            MainActivity.instance.serialNode.sendMessage(instruction.getCommandMessage());
            ++routePosition;
            Thread.sleep(instruction.duration);
        }
    }
}
