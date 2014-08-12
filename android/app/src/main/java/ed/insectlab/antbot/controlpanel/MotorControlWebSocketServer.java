package ed.insectlab.antbot.controlpanel;

import fi.iki.elonen.NanoWebSocketServer;
import fi.iki.elonen.WebSocket;

public class MotorControlWebSocketServer extends NanoWebSocketServer {
    private final boolean debug;

    public MotorControlWebSocketServer(int port, boolean debug) {
        super(port);
        this.debug = debug;
    }

    @Override
    public WebSocket openWebSocket(IHTTPSession handshake) {
        return new MotorControlWebSocket(handshake, debug);
    }
}