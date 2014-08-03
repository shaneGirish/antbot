package ed.insectlab.antbot;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.iki.elonen.ServerRunner;
import fi.iki.elonen.debug.DebugServer;

public class MainActivity extends RosActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private static UsbSerialDriver driver = null;

    private TextView mTitleTextView;
    private TextView mDumpTextView;
    private ScrollView mScrollView;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager = null;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };

    public MainActivity() {
        super("Antbot", "Antbot");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mTitleTextView = (TextView) findViewById(R.id.demoTitle);
        mDumpTextView = (TextView) findViewById(R.id.consoleText);
        mScrollView = (ScrollView) findViewById(R.id.demoScroller);

        ServerRunner.run(DebugServer.class);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mSerialIoManager != null) {
            mSerialIoManager.stop();
        }

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);

        if (!availableDrivers.isEmpty()) {
            driver = availableDrivers.get(0);

            Log.e(TAG, "onResume, driver=" + driver
                    + ";ports=" + driver.getPorts().size()
                    + ";port=" + driver.getPorts().get(0)
            );

            mSerialIoManager = new SerialInputOutputManager(driver.getPorts().get(0), mListener);
            mExecutor.submit(mSerialIoManager);

            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
            if (connection == null) {
                mTitleTextView.setText("Opening device failed");
                return;
            }

            UsbSerialPort port = driver.getPorts().get(0);

            try {
                port.open(connection);
                port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                mTitleTextView.setText("Error opening device: " + e.getMessage());
                try {
                    port.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                driver = null;
                return;
            }
            mTitleTextView.setText("Serial device: " + port.getClass().getSimpleName());
        } else {
            Log.d(TAG, "onResume, No drivers found");
            mTitleTextView.setText("No serial device.");
        }
    }

    private String buffer = "";
    private Pattern pattern = Pattern.compile("^\\s*(\\d+,[^;]+);");

    private void updateReceivedData(byte[] data) {
        Log.d(TAG, "updateReceivedData, buffer=" + buffer);

        Matcher matcher;
        String newData = new String(data);
        if(newData != null) {
            buffer += newData;
        }

        while(true) {
            matcher = pattern.matcher(buffer);
            if(matcher.find()) {
                processNewCommand(matcher.group(1));
                buffer = matcher.replaceFirst("");
            } else {
                break;
            }
        }
    }

    private void processNewCommand(String command) {
        mDumpTextView.append(command+ "\n");
        mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());

        Log.d(TAG, "processNewCommand, command=" + command);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        NodeConfiguration nodeConfiguration =
                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration.setMasterUri(getMasterUri());

        Log.d(TAG, "init, setMasterUri=" + getMasterUri());

        // mSerialIoManager = new SerialInputOutputManager(driver.getPorts().get(0), mListener);
        // mExecutor.submit(mSerialIoManager);
    }
}
