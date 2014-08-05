package ed.insectlab.antbot;

import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.primavera.arduino.listener.ArduinoCommunicatorService;

import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import ed.insectlab.antbot.navigator.NavigatorNode;
import ed.insectlab.antbot.routes.RotateRouteNode;
import ed.insectlab.antbot.routes.StraightRouteNode;
import ed.insectlab.antbot.serial.SerialNode;

public class MainActivity extends RosActivity {
    private final String TAG = MainActivity.class.getSimpleName();

    public static MainActivity instance = null;

    private TextView title;
    private TextView log;
    private ScrollView scrollView;

    public SerialNode serialNode;

    public final boolean DEBUG = true;

    public MainActivity() {
        super("Antbot", "Antbot");
    }

    public void setTitleText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                title.setText(text);
            }
        });
    }

    public void appendLog(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                log.append(text + "\n");
                scrollView.smoothScrollTo(0, log.getBottom());
            }
        });
    }

    public void createToast(final String text, final int length) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), text, length).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        MainActivity.instance = this;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // new DebugServer().start();

        title = (TextView) findViewById(R.id.demoTitle);
        log = (TextView) findViewById(R.id.consoleText);
        scrollView = (ScrollView) findViewById(R.id.demoScroller);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "onNewIntent() " + intent);
        super.onNewIntent(intent);

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.contains(intent.getAction())) {
            if (DEBUG) Log.d(TAG, "onNewIntent() " + intent);
            serialNode.findDevice();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy()");
        super.onDestroy();

        unregisterReceiver(serialNode.mReceiver);
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

        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        NodeConfiguration nodeConfiguration =
                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration.setMasterUri(getMasterUri());

        serialNode = new SerialNode();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ArduinoCommunicatorService.DATA_RECEIVED_INTENT);
        filter.addAction(ArduinoCommunicatorService.DATA_SENT_INTERNAL_INTENT);
        registerReceiver(serialNode.mReceiver, filter);

        nodeMainExecutor.execute(serialNode, nodeConfiguration);
        nodeMainExecutor.execute(new NavigatorNode(), nodeConfiguration);
        nodeMainExecutor.execute(new RotateRouteNode(), nodeConfiguration);

        Log.d(TAG, "init, setMasterUri=" + getMasterUri());
    }
}
