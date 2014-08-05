package ed.insectlab.antbot.navigator;

/**
 * Created by antbot on 04/08/14.
 */
public class Pose {
    public final float x;
    public final float y;
    public final float heading;

    public Pose() {
        this.x = 0;
        this.y = 0;
        this.heading = 0;
    }

    public Pose(float x, float y, float heading) {
        this.x = x;
        this.y = y;
        this.heading = heading;
    }
}