package ed.insectlab.antbot.serial;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by antbot on 04/08/14.
 */
public enum SerialCommand {
    ACKNOWLEDGE,
    ERROR,
    MOTOR,
    STOP,
    LEFT_WHEEL,
    RIGHT_WHEEL,
    ANDROID_TEST;
}
