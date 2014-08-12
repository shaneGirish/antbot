package ed.insectlab.antbot.controlpanel;

import android.util.Log;

import java.io.IOException;

import ed.insectlab.antbot.MainActivity;
import ed.insectlab.antbot.serial.SerialMessage;
import ed.insectlab.antbot.serial.SerialNode;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.WebSocket;
import fi.iki.elonen.WebSocketFrame;

public class MotorControlWebSocket extends WebSocket {
    private final String TAG = MotorControlWebSocket.class.getSimpleName();
    private final boolean debug;
    private final SerialNode serialNode;

    public MotorControlWebSocket(NanoHTTPD.IHTTPSession handshake, boolean debug) {
        super(handshake);
        this.debug = debug;

        this.serialNode = MainActivity.instance.serialNode;
    }

    @Override
    protected void onPong(WebSocketFrame pongFrame) {
        if (debug) {
            Log.i(TAG, "P " + pongFrame);
        }
    }

    @Override
    protected void onMessage(WebSocketFrame messageFrame) {
        serialNode.sendMessage(new SerialMessage(messageFrame.getTextPayload()));
        /*try {
            messageFrame.setUnmasked();
            sendFrame(messageFrame);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }

    @Override
    protected void onClose(WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
        if (debug) {
            Log.i(TAG, "C [" + (initiatedByRemote ? "Remote" : "Self") + "] " +
                    (code != null ? code : "UnknownCloseCode[" + code + "]") +
                    (reason != null && !reason.isEmpty() ? ": " + reason : ""));
        }
    }

    @Override
    protected void onException(IOException e) {
        e.printStackTrace();
    }

    @Override
    protected void handleWebsocketFrame(WebSocketFrame frame) throws IOException {
        if (debug) {
            Log.i(TAG, "R " + frame);
        }
        super.handleWebsocketFrame(frame);
    }

    @Override
    public synchronized void sendFrame(WebSocketFrame frame) throws IOException {
        if (debug) {
            Log.i(TAG, "S " + frame);
        }
        super.sendFrame(frame);
    }
}
