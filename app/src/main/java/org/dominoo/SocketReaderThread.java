package org.dominoo;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketReaderThread extends BaseThread {

    private SocketReaderThreadListener mListener = null;

    private Socket mSocket = null;

    public interface SocketReaderThreadListener {

        void onDataReceived(String data);
        void onDataReadError(String errorMessage);
    }

    public SocketReaderThread(Socket socket) {

        mSocket = socket;
    }

    public void setListener(SocketReaderThreadListener listener) {

        mListener = listener;

        createHandler();
    }

    @Override
    public void run() {

        BufferedReader inputReader;

        try {

            inputReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

            String inputLine;

            while ((inputLine = inputReader.readLine()) != null) {

                //Log.i("DomLog", "readLine:"+inputLine);

                CommSocketEvent commSocketEvent = new CommSocketEvent();

                commSocketEvent.setEventType(CommSocketEvent.Type.DATA_READ);
                commSocketEvent.setData(inputLine);

                addOutputEvent(commSocketEvent);
            }
        } catch (IOException e) {

            CommSocketEvent commSocketEvent = new CommSocketEvent();

            commSocketEvent.setEventType(CommSocketEvent.Type.SOCKET_READ_ERROR);

            commSocketEvent.setEventErrorMessage("Socket.readLine() Error: " + e.getMessage());

            addOutputEvent(commSocketEvent);
        }
    }

    @Override
    protected void dispatchEvent(ThreadEvent event) {

        if (mListener == null) {

            return;
        }

        if (!(event instanceof CommSocketEvent)) {

            // Received event is not of type CommSocketEvent
            return;
        }

        CommSocketEvent commSocketEvent = (CommSocketEvent) event;

        CommSocketEvent.Type eventType = commSocketEvent.getEventType();

        if (eventType == CommSocketEvent.Type.DATA_READ) {

            String data = commSocketEvent.getData();

            mListener.onDataReceived(data);
        }
        else if (eventType == CommSocketEvent.Type.SOCKET_READ_ERROR) {

            mListener.onDataReadError(commSocketEvent.getEventErrorMessage());
        }
        else {

            Log.e("DomLog", "SocketReadThread: Unknown event type");
        }
    }
}
