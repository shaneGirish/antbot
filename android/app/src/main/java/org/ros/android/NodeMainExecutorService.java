package org.ros.android;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.google.common.base.Preconditions;

import org.ros.RosCore;
import org.ros.concurrent.ListenerGroup;
import org.ros.concurrent.SignalRunnable;
import org.ros.exception.RosRuntimeException;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeListener;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import ed.insectlab.antbot.R;

public class NodeMainExecutorService extends Service implements NodeMainExecutor {

  private static final String TAG = "NodeMainExecutorService";

  private static final int ONGOING_NOTIFICATION = 1;

  static final String ACTION_START = "org.ros.android.ACTION_START_NODE_RUNNER_SERVICE";
  static final String ACTION_SHUTDOWN = "org.ros.android.ACTION_SHUTDOWN_NODE_RUNNER_SERVICE";
  static final String EXTRA_NOTIFICATION_TITLE = "org.ros.android.EXTRA_NOTIFICATION_TITLE";
  static final String EXTRA_NOTIFICATION_TICKER = "org.ros.android.EXTRA_NOTIFICATION_TICKER";

  private final NodeMainExecutor nodeMainExecutor;
  private final IBinder binder;
  private final ListenerGroup<NodeMainExecutorServiceListener> listeners;

  private WakeLock wakeLock;
  private WifiLock wifiLock;
  private RosCore rosCore;
  private URI masterUri;

  class LocalBinder extends Binder {
    NodeMainExecutorService getService() {
      return NodeMainExecutorService.this;
    }
  }

  public NodeMainExecutorService() {
    super();
    nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
    binder = new LocalBinder();
    listeners =
        new ListenerGroup<NodeMainExecutorServiceListener>(
            nodeMainExecutor.getScheduledExecutorService());
  }

  @Override
  public void onCreate() {
    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    wakeLock.acquire();
    int wifiLockType = WifiManager.WIFI_MODE_FULL;
    try {
      wifiLockType = WifiManager.class.getField("WIFI_MODE_FULL_HIGH_PERF").getInt(null);
    } catch (Exception e) {
      Log.w(TAG, "Unable to acquire high performance wifi lock.");
    }
    WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
    wifiLock = wifiManager.createWifiLock(wifiLockType, TAG);
    wifiLock.acquire();
  }

  @Override
  public void execute(NodeMain nodeMain, NodeConfiguration nodeConfiguration,
      Collection<NodeListener> nodeListeneners) {
    nodeMainExecutor.execute(nodeMain, nodeConfiguration, nodeListeneners);
  }

  @Override
  public void execute(NodeMain nodeMain, NodeConfiguration nodeConfiguration) {
    execute(nodeMain, nodeConfiguration, null);
  }

  @Override
  public ScheduledExecutorService getScheduledExecutorService() {
    return nodeMainExecutor.getScheduledExecutorService();
  }

  @Override
  public void shutdownNodeMain(NodeMain nodeMain) {
    nodeMainExecutor.shutdownNodeMain(nodeMain);
  }

  @Override
  public void shutdown() {
    signalOnShutdown();

    nodeMainExecutor.shutdown();
    if (rosCore != null) {
      rosCore.shutdown();
    }
    if (wakeLock.isHeld()) {
      wakeLock.release();
    }
    if (wifiLock.isHeld()) {
      wifiLock.release();
    }
    stopForeground(true);
    stopSelf();
  }

  public void addListener(NodeMainExecutorServiceListener listener) {
    listeners.add(listener);
  }

  private void signalOnShutdown() {
    listeners.signal(new SignalRunnable<NodeMainExecutorServiceListener>() {
      @Override
      public void run(NodeMainExecutorServiceListener nodeMainExecutorServiceListener) {
        nodeMainExecutorServiceListener.onShutdown(NodeMainExecutorService.this);
      }
    });
  }

  @Override
  public void onDestroy() {
    shutdown();
    super.onDestroy();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent.getAction() == null) {
      return START_NOT_STICKY;
    }
    if (intent.getAction().equals(ACTION_START)) {
      Preconditions.checkArgument(intent.hasExtra(EXTRA_NOTIFICATION_TICKER));
      Preconditions.checkArgument(intent.hasExtra(EXTRA_NOTIFICATION_TITLE));
      Notification notification =
          new Notification(R.drawable.ic_ant, intent.getStringExtra(EXTRA_NOTIFICATION_TICKER),
              System.currentTimeMillis());
      Intent notificationIntent = new Intent(this, NodeMainExecutorService.class);
      notificationIntent.setAction(NodeMainExecutorService.ACTION_SHUTDOWN);
      PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
      notification.setLatestEventInfo(this, intent.getStringExtra(EXTRA_NOTIFICATION_TITLE),
          "Tap to shutdown.", pendingIntent);
      startForeground(ONGOING_NOTIFICATION, notification);
    }
    if (intent.getAction().equals(ACTION_SHUTDOWN)) {
      shutdown();
    }
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  public URI getMasterUri() {
    return masterUri;
  }

  public void setMasterUri(URI uri) {
    masterUri = uri;
  }

  /**
   * This version of startMaster can only create private masters.
   *
   * @deprecated use {@link public void startMaster(Boolean isPrivate)} instead.
   */
  @Deprecated
  public void startMaster() {
    startMaster(true);
  }

  public void startMaster(Boolean isPrivate) {
    if (isPrivate) {
      rosCore = RosCore.newPrivate();
    } else {
      rosCore = RosCore.newPublic("localhost", 11311);
    }
    rosCore.start();
    try {
      rosCore.awaitStart();
    } catch (Exception e) {
      throw new RosRuntimeException(e);
    }
    masterUri = rosCore.getUri();
  }
}
