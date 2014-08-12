package ed.insectlab.antbot.controlpanel;


import android.content.res.Resources;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import ed.insectlab.antbot.MainActivity;
import ed.insectlab.antbot.R;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWebSocketServer;

public class ControlPanelNode extends AbstractNodeMain {
    private final String TAG = ControlPanelNode.class.getSimpleName();

    protected NanoHTTPD http_server;
    protected NanoWebSocketServer socket_server;

    private final int http_port = 8080;
    private final int socket_port = 8000;

    public ControlPanelNode() {
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("antbot/ControlPanelNode");
    }

    static InetAddress ip() {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            NetworkInterface ni;
            while (nis.hasMoreElements()) {
                ni = nis.nextElement();
                if (!ni.isLoopback()/*not loopback*/ && ni.isUp()/*it works now*/) {
                    for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                        //filter for ipv4/ipv6
                        if (ia.getAddress().getAddress().length == 4) {
                            //4 for ipv4, 16 for ipv6
                            return ia.getAddress();
                        }
                    }
                }
            }
        } catch(SocketException e) {
            return null;
        }
        return null;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        http_server = new NanoHTTPD(http_port) {
            @Override
            public Response serve(IHTTPSession session) {
                String control_panel = "Could not read raw HTML !";

                try {
                    control_panel = IOUtils.toString(MainActivity.instance.getResources().openRawResource(
                            R.raw.control_panel
                    ), "UTF-8");
                } catch(IOException e) {
                    e.printStackTrace();
                }

                control_panel = control_panel.replaceAll("\\$ip", ip().getHostAddress());
                control_panel = control_panel.replaceAll("\\$port", socket_port + "");

                return new Response(control_panel);
            }
        };
        socket_server = new MotorControlWebSocketServer(socket_port, true);

        try {
            http_server.start();
            socket_server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onShutdown(Node node) {
        try {
            http_server.stop();
            socket_server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onShutdownComplete(Node node) {

    }

    @Override
    public void onError(Node node, Throwable throwable) {

    }
}
