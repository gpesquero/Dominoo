package org.dominoo;

import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketWriterThread extends BaseThread {

    private SocketWriterThreadListener mListener = null;

    private String mSocketErrorMessage = null;

    private PrintWriter mSocketWriter = null;

    public interface SocketWriterThreadListener {

        void onDataSent();
        void onDataWriteError(String errorMessage);
    }

    public SocketWriterThread(Socket socket) {

        if (socket == null) {

            mSocketErrorMessage = "Socket==null";

            return;
        }

        try {

            mSocketWriter = new PrintWriter(socket.getOutputStream(), true);
        }
        catch (IOException e) {

            if (mListener != null) {

                mListener.onDataWriteError("Socket.sendMessage() Error: " + e.getMessage());
            }

            /*
            CommSocketEvent commSocketEvent = new CommSocketEvent();

            commSocketEvent.setEventType(CommSocketEvent.Type.SOCKET_WRITE_ERROR);

            commSocketEvent.setEventErrorMessage("Socket.sendMessage() Error: " + e.getMessage());

            addEvent(commSocketEvent);
            */

            return;
        }
    }

    public void setListener(SocketWriterThreadListener listener) {

        mListener = listener;

        createHandler();
    }

    public void sendMessage(String message) {

        CommSocketEvent commSocketEvent = new CommSocketEvent(CommSocketEvent.Type.WRITE_DATA);

        //commSocketEvent.setSocketWriter(mSocketWriter);

        commSocketEvent.setData(message);

        addInputEvent(commSocketEvent);
    }

    public void close() {

        mSocketWriter.close();

        mSocketWriter = null;
    }

    @Override
    public void run() {

        try {

            while(!isInterrupted()) {

                ThreadEvent event = waitForInputEvent();

                if (!(event instanceof CommSocketEvent)) {

                    // Received event is not of type CommSocketEvent
                    continue;
                }

                CommSocketEvent commSocketEvent = (CommSocketEvent) event;

                //PrintWriter socketWriter = commSocketEvent.getSocketWriter();

                String message = commSocketEvent.getData();

                if (mSocketWriter == null) {

                    commSocketEvent=new CommSocketEvent(CommSocketEvent.Type.SOCKET_WRITE_ERROR);

                    commSocketEvent.setEventErrorMessage("mSocketWriter==null");
                }
                else {

                    mSocketWriter.println(message);

                    if (mSocketWriter.checkError()) {

                        commSocketEvent = new CommSocketEvent(CommSocketEvent.Type.SOCKET_WRITE_ERROR);

                        commSocketEvent.setEventErrorMessage("Socket.checkError() reports error");
                    } else {

                        commSocketEvent = new CommSocketEvent(CommSocketEvent.Type.DATA_SENT);
                    }
                }

                addOutputEvent(commSocketEvent);
            }

            //publishProgress(event);

        } catch (InterruptedException e) {

            //e.printStackTrace();
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

        if (eventType == CommSocketEvent.Type.DATA_SENT) {

            mListener.onDataSent();
        }
        else if (eventType == CommSocketEvent.Type.SOCKET_WRITE_ERROR) {

            mListener.onDataWriteError(commSocketEvent.getEventErrorMessage());
        }
        else {

            Log.e("DomLog", "SocketWriterThread: Unknown event type");
        }
    }
}
