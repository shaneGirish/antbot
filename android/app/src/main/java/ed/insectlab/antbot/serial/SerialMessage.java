package ed.insectlab.antbot.serial;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

/**
 * Created by antbot on 04/08/14.
 */
public class SerialMessage {
    public final SerialCommand command;
    public final String[] parameters;

    public final String source;

    public SerialMessage(std_msgs.String message) {
        this(message.getData() + ";");
    }

    public SerialMessage(String message) {
        this.source = message;

        Pattern pattern = Pattern.compile("^\\d+(,[^,;]+)*;$");
        if(pattern.matcher(message).find()) {
            this.parameters = message.replaceAll(";", "").split(",");
            this.command = SerialCommand.values()[
                    Integer.parseInt(this.parameters[0])
            ];
        } else {
            this.parameters = null;
            this.command = null;
        }
    }

    @Override
    public String toString() {
        return StringUtils.join(parameters, ",") + ";";
    }
}
