package org.dominoo;

import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConnectToServerThread extends BaseThread {

    private ConnectToServerThreadListener mListener = null;

    private String mAddress;
    private int mPort;
    private int mMaxTimeout;

    public interface ConnectToServerThreadListener {

        void onConnectionEstablished(Socket socket);
        void onConnectionError(String errorMessage);
    }

    public ConnectToServerThread(String address, int port, int maxTimeout) {

        mAddress = address;
        mPort = port;
        mMaxTimeout = maxTimeout;
    }

    public void setListener(ConnectToServerThreadListener listener) {

        mListener = listener;

        createHandler();
    }

    public void run() {

        InetSocketAddress address = new InetSocketAddress(mAddress, mPort);

        CommSocketEvent connectionEvent = new CommSocketEvent();

        Socket socket = new Socket();

        try {

            socket.connect(address, mMaxTimeout);

            connectionEvent.setEventType(CommSocketEvent.Type.CONNECTED);

            connectionEvent.setSocket(socket);

        }
        catch (IOException e) {

            connectionEvent.setEventType(CommSocketEvent.Type.CONNECT_ERROR);

            connectionEvent.setEventErrorMessage(e.getMessage());
        }

        //setIsConnecting(false);

        addOutputEvent(connectionEvent);
    }

    protected void dispatchEvent(ThreadEvent event) {

        if (Looper.myLooper() != Looper.getMainLooper()) {

            return;
        }

        if (mListener == null) {

            return;
        }

        if (!(event instanceof CommSocketEvent)) {

            // Received event is not of type CommSocketEvent
            return;
        }

        CommSocketEvent commSocketEvent = (CommSocketEvent) event;

        CommSocketEvent.Type eventType = commSocketEvent.getEventType();

        if (eventType == CommSocketEvent.Type.CONNECTED) {

            Socket socket = commSocketEvent.getSocket();

            mListener.onConnectionEstablished(socket);
        }
        else if (eventType == CommSocketEvent.Type.CONNECT_ERROR) {

            mListener.onConnectionError(commSocketEvent.getEventErrorMessage());
        }
        else {

            Log.e("DomLog", "ConnectToServerThread: Unknown event type");
        }
    }
}
