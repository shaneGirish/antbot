package ed.insectlab.antbot.routes;

import ed.insectlab.antbot.MainActivity;

/**
 * Created by antbot on 04/08/14.
 */
public class RouteInstruction {
    public final int leftSpeed;
    public final int rightSpeed;
    public final int duration;

    public boolean enableTransition;

    public final static RouteInstruction EMERGENCY_STOP_COMMAND = new RouteInstruction(0,0,0, false);
    public final static RouteInstruction STOP_COMMAND = new RouteInstruction(0,0,0);

    public RouteInstruction(int leftSpeed, int rightSpeed, int duration) {
        this(leftSpeed, rightSpeed, duration, true);
    }

    public RouteInstruction(int leftSpeed, int rightSpeed, int duration, boolean enableTransition) {
        this.leftSpeed = leftSpeed;
        this.rightSpeed = rightSpeed;
        this.duration = duration;

        this.enableTransition = enableTransition;
    }

    public void execute() {
        MainActivity.instance.navigatorNode.setSpeeds(leftSpeed, rightSpeed, enableTransition);
    }
}
