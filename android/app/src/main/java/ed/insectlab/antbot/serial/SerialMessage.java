package ed.insectlab.antbot.serial;

/**
 * Created by antbot on 04/08/14.
 */
public class SerialMessage {
    public final SerialCommand command;
    public final String[] parameters;

    public SerialMessage(std_msgs.String message) {
        this(message.getData());
    }

    public SerialMessage(String message) {
        this.parameters = message.split(",");
        this.command = SerialCommand.values()[Integer.parseInt(this.parameters[0])];
    }
}
