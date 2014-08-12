package ed.insectlab.antbot.navigator;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.topic.Subscriber;

import java.util.LinkedList;

import ed.insectlab.antbot.MainActivity;
import ed.insectlab.antbot.serial.SerialCommand;
import ed.insectlab.antbot.serial.SerialMessage;

public class NavigatorNode extends AbstractNodeMain implements SensorEventListener {
    private final String TAG = NavigatorNode.class.getSimpleName();
    private static final double PI = Math.PI;
    private static final double TWO_PI = PI * 2;

    private double countsPerRevolution = 48;
    private double diameterWheel = 4.2;
    private double trackWidth = 9.8 - 0.73;

    private double distancePerCount = PI * diameterWheel / countsPerRevolution;
    private double radiansPerCount = PI * (diameterWheel/trackWidth) / countsPerRevolution;
    private double X = 0;
    private double Y = 0;
    private double heading = 0;
    private float savedAzimuthValue = 0.0f;

    static MainActivity scope;
    SensorManager sensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    float[] gravityData;
    float[] geomagneticData;
    LinkedList<Float> azimuth = new LinkedList<Float>();
    protected final int AZIMUTH_SMOOTHING_FACTOR = 25;

    public NavigatorNode() {
        scope = MainActivity.instance;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("antbot/NavigatorNode");
    }

    @Override
    public void onShutdown(Node node) {
        super.onShutdown(node);
        unregisterListeners();
    }

    public void registerListeners() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    public void unregisterListeners() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        sensorManager = (SensorManager) scope.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        registerListeners();

        Subscriber<std_msgs.String> wheel_data = connectedNode.newSubscriber("zumo_data", std_msgs.String._TYPE);
        wheel_data.addMessageListener(new MessageListener<std_msgs.String>() {
            @Override
            public void onNewMessage(std_msgs.String encoded_message) {
                scope.appendLog(encoded_message.getData());
                SerialMessage message = new SerialMessage(encoded_message);

                if(message.command != null) {
                    double deltaDistance = 0.5 * distancePerCount;
                    double deltaHeading, deltaX = 0, deltaY = 0;

                    switch (message.command) {
                        case LEFT_WHEEL_DATA:
                            // left_track_count += value;
                            deltaX = Math.cos(heading);
                            deltaHeading = -1;
                            break;
                        case RIGHT_WHEEL_DATA:
                            // right_track_count += value;
                            deltaY = Math.sin(heading);
                            deltaHeading = 1;
                            break;
                        default:
                            return;
                    }

                    Log.i(TAG, "Message Source : " + message.source);
                    Log.i(TAG, "Message Command : " + message.command + " (" + message.command.ordinal() + ")");
                    Log.i(TAG, "Message Parameters : " + message.parameters[1]);


                    int value = Integer.parseInt(message.parameters[1]);
                    deltaDistance *= value;
                    deltaHeading *= value;
                    deltaX *= deltaDistance;
                    deltaY *= deltaDistance;

                    deltaHeading *= radiansPerCount;

                    synchronized (this) {
                        X += deltaX;
                        Y += deltaY;
                        heading += deltaHeading;

                        if(heading > PI) {
                            heading -= TWO_PI;
                        } else if(heading <= -PI) {
                            heading += TWO_PI;
                        }

                        updateTitle();
                    }
                } else {
                    Log.i(TAG, "Bad message : " + message.source);
                }
            }
        });

        /*connectedNode.newServiceServer("add_two_ints", test_ros.AddTwoInts._TYPE,
                new ServiceResponseBuilder<test_ros.AddTwoInts.Request, test_ros.AddTwoInts.Response>() {
                    @Override
                    public void build(test_ros.AddTwoInts.Request request,
                                      test_ros.AddTwoInts.Response response) {
                        response.setSum(request.getA() + request.getB());
                    }
                });*/

        /*connectedNode.executeCancellableLoop(new CancellableLoop() {
            @Override protected void setup() {}

            @Override
            protected void loop() throws InterruptedException {
                SerialMessage[] loop_queue;
                synchronized (this) {
                    loop_queue = (SerialMessage[]) queue.toArray();
                    queue.clear();
                }
                while(true) {

                    break;
                }
                Thread.sleep(250);
            }
        });*/
    }

    private double roundDouble(double value) {
        return roundDouble(value, 2);
    }

    private double roundDouble(double value, int digits) {
        if(digits == 0) {
            return Math.round(value);
        }
        double factor = Math.pow(10.0, digits);
        return Math.round( value * factor ) / factor;
    }

    private void updateTitle() {
        scope.setTitleText(
                roundDouble(X) + "x" + roundDouble(Y)
                + " @ "
                + roundDouble(Math.toDegrees(heading)) + "°"
                + "\n"
                + roundDouble((180/PI) * (((-getAzimuthValue() + PI + savedAzimuthValue) % TWO_PI) - PI), 0) + "°"
        );
    }

    protected int leftSpeed = 0;
    protected int rightSpeed = 0;

    protected void updateSpeeds(boolean enableTransition) {
        int command;
        if(enableTransition) {
            command = SerialCommand.TRANSITION_TO_SPEEDS.ordinal();
        } else {
            command = SerialCommand.SET_SPEEDS.ordinal();
        }
        scope.serialNode.sendMessage(command + "," + this.leftSpeed + "," + this.rightSpeed + ";");
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setSpeeds(int leftSpeed, int rightSpeed) {
        setSpeeds(leftSpeed, rightSpeed, true);
    }

    public void setSpeeds(int leftSpeed, int rightSpeed, boolean enableTransition) {
        this.leftSpeed = leftSpeed;
        this.rightSpeed = rightSpeed;
        updateSpeeds(enableTransition);
    }

    public void resetPose() {
        X = 0;
        Y = 0;
        heading = 0;
        savedAzimuthValue = getAzimuthValue();

        updateTitle();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravityData = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geomagneticData = event.values;
        if (gravityData != null && geomagneticData != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravityData, geomagneticData);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                if(scope.DEBUG) Log.i(TAG, "Azimuth : " + orientation[0]);

                synchronized (azimuth) {
                    azimuth.offer(orientation[0]);

                    if(azimuth.size() == AZIMUTH_SMOOTHING_FACTOR) {
                        savedAzimuthValue = getAzimuthValue();
                    } else if(azimuth.size() > AZIMUTH_SMOOTHING_FACTOR) {
                        azimuth.poll();
                    }
                }

                updateTitle();
            }
        }
    }

    protected Float getAzimuthValue() {
        Float sum = 0.0f;
        Float sign = 0.0f;
        synchronized (azimuth) {
            for (Float val : azimuth) {
                sign += Math.signum(val);
                sum += Math.abs(val);
            }

            sum /= azimuth.size();
        }
        sign = Math.signum(sign);
        if(sign == 0) {
            ++sign;
        }

        return sum * sign;
    }

    static final float LOW_PASS_SMOOTHING_ALPHA = 0.2f;

    @SuppressWarnings("UnusedDeclaration")
    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + LOW_PASS_SMOOTHING_ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @Override public void onAccuracyChanged(Sensor sensor, int i) {}
}