package ed.insectlab.antbot.serial;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.primavera.arduino.listener.ArduinoCommunicatorService;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.topic.Publisher;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ed.insectlab.antbot.MainActivity;
import ed.insectlab.antbot.R;

public class SerialNode extends AbstractNodeMain {
    private final String TAG = SerialNode.class.getSimpleName();
    public MainActivity scope;

    private Publisher<std_msgs.String> zumo_data_publisher = null;

    private String buffer = "";
    private static final Pattern pattern = Pattern.compile("^\\s*(\\d+,[^;]+);");

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {

        private void handleTransferedData(Intent intent, boolean receiving) {
            final byte[] newTransferedData = intent.getByteArrayExtra(ArduinoCommunicatorService.DATA_EXTRA);
            String message = new String(newTransferedData);
            // scope.appendLog(message);

            if (scope.DEBUG) Log.i(TAG, "data: " + newTransferedData.length + " \"" + message + "\"");

            buffer += message;

            Matcher matcher = pattern.matcher(buffer);
            while(matcher.find()) {
                std_msgs.String data = zumo_data_publisher.newMessage();
                data.setData(matcher.group(1));
                zumo_data_publisher.publish(data);

                buffer = matcher.replaceFirst("");
                matcher = pattern.matcher(buffer);
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (scope.DEBUG) Log.d(TAG, "onReceive() " + action);

            if (ArduinoCommunicatorService.DATA_RECEIVED_INTENT.equals(action)) {
                handleTransferedData(intent, true);
            } else if (ArduinoCommunicatorService.DATA_SENT_INTERNAL_INTENT.equals(action)) {
                // handleTransferedData(intent, false);
            }
        }
    };

    public SerialNode() {
        super();
        scope = MainActivity.instance;

        findDevice();
    }

    private static final int ARDUINO_USB_VENDOR_ID = 0x2341;
    //private static final int ARDUINO_UNO_USB_PRODUCT_ID = 0x01;
    private static final int ARDUINO_UNO_R3_USB_PRODUCT_ID = 0x43;

    public void findDevice() {
        UsbManager usbManager = (UsbManager) scope.getSystemService(Context.USB_SERVICE);
        UsbDevice usbDevice = null;
        HashMap<String, UsbDevice> usbDeviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = usbDeviceList.values().iterator();
        if (deviceIterator.hasNext()) {
            UsbDevice tempUsbDevice = deviceIterator.next();

            if (tempUsbDevice.getVendorId() == ARDUINO_USB_VENDOR_ID) {
                if (scope.DEBUG) Log.i(TAG, "Arduino device found!");

                switch (tempUsbDevice.getProductId()) {
                    /*case ARDUINO_UNO_USB_PRODUCT_ID:
                        Toast.makeText(getBaseContext(), "Arduino Uno " + getString(R.string.found), Toast.LENGTH_SHORT).show();
                        usbDevice = tempUsbDevice;
                        break;*/
                    case ARDUINO_UNO_R3_USB_PRODUCT_ID:
                        scope.createToast("Arduino Uno R3 " + scope.getString(R.string.found), Toast.LENGTH_SHORT);
                        scope.setTitleText("Arduino Uno R3");
                        usbDevice = tempUsbDevice;
                        break;
                }
            }
        }

        if (usbDevice == null) {
            if (scope.DEBUG) Log.i(TAG, "No device found!");
            scope.createToast(scope.getString(R.string.no_device_found), Toast.LENGTH_LONG);
        } else {
            if (scope.DEBUG) Log.i(TAG, "Device found!");
            Intent startIntent = new Intent(scope.getApplicationContext(), ArduinoCommunicatorService.class);
            PendingIntent pendingIntent = PendingIntent.getService(scope.getApplicationContext(), 0, startIntent, 0);
            usbManager.requestPermission(usbDevice, pendingIntent);
        }
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("antbot/SerialNode");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        zumo_data_publisher = connectedNode.newPublisher("zumo_data", std_msgs.String._TYPE);

        /*connectedNode.executeCancellableLoop(new CancellableLoop() {
            @Override
            protected void setup() {

            }

            @Override
            protected void loop() throws InterruptedException {}
        });*/
    }

    public void sendMessage(String message) {
        Intent intent = new Intent(ArduinoCommunicatorService.SEND_DATA_INTENT);
        intent.putExtra(ArduinoCommunicatorService.DATA_EXTRA, message.getBytes());
        scope.sendBroadcast(intent);
    }

    @Override
    public void onShutdown(Node node) {}
}
