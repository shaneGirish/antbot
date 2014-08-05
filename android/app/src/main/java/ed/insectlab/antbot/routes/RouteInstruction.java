package ed.insectlab.antbot.routes;

import ed.insectlab.antbot.serial.SerialCommand;

/**
 * Created by antbot on 04/08/14.
 */
public class RouteInstruction {
    public final int leftSpeed;
    public final int rightSpeed;
    public final int duration;

    private RouteInstruction(int leftSpeed, int rightSpeed, int duration) {
        this.leftSpeed = leftSpeed;
        this.rightSpeed = rightSpeed;
        this.duration = duration;
    }

    public static RouteInstruction create(int leftSpeed, int rightSpeed, int duration) {
        return new RouteInstruction(leftSpeed, rightSpeed, duration);
    }

    public String getCommandMessage() {
        return SerialCommand.MOTOR.ordinal() + "," + leftSpeed + "," + rightSpeed + ";";
    }
}
