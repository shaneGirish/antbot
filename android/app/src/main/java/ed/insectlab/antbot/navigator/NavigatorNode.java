package ed.insectlab.antbot.navigator;

import org.ros.concurrent.CancellableLoop;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import java.util.concurrent.ConcurrentLinkedQueue;

import ed.insectlab.antbot.MainActivity;
import ed.insectlab.antbot.serial.SerialMessage;
import ed.insectlab.antbot.serial.SerialNode;

/**
 * Created by antbot on 04/08/14.
 */

public class NavigatorNode extends AbstractNodeMain {

    protected int left_track_count = 0;
    protected int right_track_count = 0;

    public static MainActivity scope;

    protected ConcurrentLinkedQueue<SerialMessage> queue = new ConcurrentLinkedQueue<SerialMessage>();

    protected Pose pose = new Pose();

    public NavigatorNode() {
        scope = MainActivity.instance;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("antbot/NavigatorNode");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        Subscriber<std_msgs.String> wheel_data = connectedNode.newSubscriber("zumo_data", std_msgs.String._TYPE);
        wheel_data.addMessageListener(new MessageListener<std_msgs.String>() {
            @Override
            public void onNewMessage(std_msgs.String encoded_message) {
                SerialMessage message = new SerialMessage(encoded_message);
                queue.add(message);

                scope.appendLog(encoded_message.getData());

                int value = Integer.parseInt(message.parameters[1]);

                switch (message.command) {
                    case LEFT_WHEEL:
                        left_track_count += value;
                        return;
                    case RIGHT_WHEEL:
                        right_track_count += value;
                        return;
                    default:
                        return;
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

    protected int leftSpeed = 0;
    protected int rightSpeed = 0;

    protected void updateSpeeds() {
        scope.serialNode.sendMessage("2," + this.leftSpeed + "," + this.rightSpeed + ";");
    }

    public void setSpeeds(int leftSpeed, int rightSpeed) {
        this.leftSpeed = leftSpeed;
        this.rightSpeed = rightSpeed;
        updateSpeeds();
    }

    public void setLeftSpeed(int speed) {
        this.leftSpeed = speed;
        updateSpeeds();
    }

    public void setRightSpeed(int speed) {
        this.rightSpeed = speed;
        updateSpeeds();
    }

    public int getLeftSpeed() {
        return leftSpeed;
    }

    public int getRightSpeed() {
        return rightSpeed;
    }

    public void resetPose() {
        pose = new Pose();
    }

    public Pose getPose() {
        return pose;
    }
}