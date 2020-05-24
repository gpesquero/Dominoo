package org.dominoo;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class CommSocket {

    // Socket
    private Socket mSocket = null;

    private boolean mIsConnecting = false;

    // Print Writer
    private PrintWriter mSocketWriter = null;

    // Last Socket Error Message
    private String mSocketErrorMessage = null;

    // Threads
    private ConnectToServerThread mConnectToServerThread = null;
    private SocketReaderThread mSocketReaderThread = null;
    private CommSocketListenerTask mCommSocketListenerTask = null;
    //private CommSocketListenerThread mCommSocketListenerThread = null;

    // Event semaphore
    Semaphore mSemaphore = new Semaphore(0);

    private ArrayList<CommSocketEvent> mEventQueue = new ArrayList<CommSocketEvent>();

    // ConnectionListener interface
    public interface CommSocketListener {

        void onConnectionEstablished();
        void onConnectionError(String errorMessage);
        void onDataReceived(String data);
        void onDataReadError(String errorMessage);
        void onDataWriteError(String errorMessage);
        void onDataSent();
        void onSocketClosed();
    }

    public CommSocket() {

    }

    public CommSocket(CommSocketListener listener) {

        setCommSocketListener(listener);
    }

    public CommSocketListener getCommSocketListener() {

        CommSocketListener listener;

        if (mCommSocketListenerTask == null) {

            listener = null;
        }
        else {

            listener = mCommSocketListenerTask.mListener;
        }

        return listener;
    }

    public void setCommSocketListener(CommSocketListener listener) {

        Log.d("DomLog", "setCommSocketListener() listener= "+listener);

        if (mCommSocketListenerTask != null) {

            mCommSocketListenerTask.cancel(true);

            mCommSocketListenerTask = null;
        }

        if (listener == null) {

            // Cancel connection listener task
            return;
        }

        mCommSocketListenerTask = new CommSocketListenerTask(listener);

        mCommSocketListenerTask.execute();
    }

    synchronized private void addEvent(CommSocketEvent connectionEvent) {

        mEventQueue.add(connectionEvent);

        mSemaphore.release();
    }

    synchronized private CommSocketEvent getEvent() {

        return mEventQueue.remove(0);
    }

    synchronized private void setIsConnecting(boolean isConnecting) {

        mIsConnecting = isConnecting;
    }

    synchronized public boolean isConnecting() {

        return mIsConnecting;
    }

    public boolean connectToServer(String serverAddress, int serverPort, int maxTimeout) {

        mConnectToServerThread = new ConnectToServerThread(serverAddress, serverPort, maxTimeout);
        mConnectToServerThread.start();

        setIsConnecting(true);

        return true;
    }

    private void startSocketReader() {

        mSocketReaderThread = new SocketReaderThread();
        mSocketReaderThread.start();
    }

    public void close() {

        if (mSocketWriter != null) {

            mSocketWriter.close();

            mSocketWriter = null;
        }

        if (mSocket != null) {

            try {
                mSocketWriter = null;

                mSocket.close();

                mSocket = null;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mSocketReaderThread != null) {

            mSocketReaderThread.interrupt();

            mSocketReaderThread = null;
        }

        if (mConnectToServerThread != null) {

            mConnectToServerThread.interrupt();

            setIsConnecting(false);

            mConnectToServerThread = null;
        }
    }

    public boolean sendMessage(final String message) {

        if (mSocketWriter==null) {

            if (mSocket == null) {

                mSocketErrorMessage = "Socket==null";

                return false;
            }

            try {

                mSocketWriter = new PrintWriter(mSocket.getOutputStream(), true);
            }
            catch (IOException e) {

                CommSocketEvent commSocketEvent = new CommSocketEvent();

                commSocketEvent.setEventType(CommSocketEvent.Type.SOCKET_WRITE_ERROR);

                commSocketEvent.setEventErrorMessage("Socket.sendMessage() Error: " + e.getMessage());

                addEvent(commSocketEvent);

                return false;
            }
        }

        new Thread(new Runnable() {

            @Override
            public void run () {

                mSocketWriter.println(message);

                CommSocketEvent commSocketEvent;

                if (mSocketWriter.checkError()) {

                    commSocketEvent=new CommSocketEvent(CommSocketEvent.Type.SOCKET_WRITE_ERROR);

                    commSocketEvent.setEventErrorMessage("Socket.checkError() reports error");
                }
                else {

                    commSocketEvent=new CommSocketEvent(CommSocketEvent.Type.DATA_SENT);
                }

                addEvent(commSocketEvent);
            }
        }).start();

        return true;
    }

    private class ConnectToServerThread extends Thread {

        private String mAddress;
        private int mPort;
        private int mMaxTimeout;

        public ConnectToServerThread(String address, int port, int maxTimeout) {

            mAddress = address;
            mPort = port;
            mMaxTimeout = maxTimeout;
        }

        public void run() {

            InetSocketAddress address = new InetSocketAddress(mAddress, mPort);

            CommSocketEvent connectionEvent = new CommSocketEvent();

            try {

                mSocket = new Socket();

                mSocket.connect(address, mMaxTimeout);

                connectionEvent.setEventType(CommSocketEvent.Type.CONNECTED);

            } catch (IOException e) {

                connectionEvent.setEventType(CommSocketEvent.Type.CONNECT_ERROR);

                connectionEvent.setEventErrorMessage(e.getMessage());
            }

            setIsConnecting(false);

            addEvent(connectionEvent);
        }
    }

    private class SocketReaderThread extends Thread {

        public void run() {

            BufferedReader inputReader;

            try {

                inputReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

                String inputLine;

                while ((inputLine = inputReader.readLine()) != null) {

                    //Log.i("DomLog", "readLine:"+inputLine);

                    CommSocketEvent commSocketEvent = new CommSocketEvent();

                    commSocketEvent.setEventType(CommSocketEvent.Type.DATA_READ);
                    commSocketEvent.setDataRead(inputLine);

                    addEvent(commSocketEvent);
                }
            } catch (IOException e) {

                CommSocketEvent commSocketEvent = new CommSocketEvent();

                commSocketEvent.setEventType(CommSocketEvent.Type.SOCKET_READ_ERROR);

                commSocketEvent.setEventErrorMessage("Socket.readLine() Error: " + e.getMessage());

                addEvent(commSocketEvent);
            }
        }
    }

    private class CommSocketListenerTask extends AsyncTask<Void, CommSocketEvent, Void> {

        private CommSocketListener mListener = null;

        boolean bContinue = true;

        public CommSocketListenerTask(CommSocketListener listener) {

            if (listener!=null) {

                mListener=listener;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            while(!isCancelled()) {

                try {

                    mSemaphore.acquire();

                    CommSocketEvent event=getEvent();

                    publishProgress(event);

                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
            }

            return null;
        }

        protected void onProgressUpdate(CommSocketEvent... events) {

            if (mListener==null) {
                return;
            }

            switch (events[0].getEventType()) {

                case CONNECTED:

                    //Log.i("DomLog", "commEvent: CONNECTED");

                    mListener.onConnectionEstablished();

                    startSocketReader();

                    break;

                case CONNECT_ERROR:

                    //Log.i("DomLog", "commEvent: CONNECT ERROR");

                    mListener.onConnectionError(events[0].getEventErrorMessage());

                    break;

                case DATA_READ:

                    //Log.i("DomLog", "commEvent: DATA READ");

                    mListener.onDataReceived(events[0].getDataRead());

                    break;

                case DATA_SENT:

                    //Log.i("DomLog", "commEvent: DATA SENT");

                    mListener.onDataSent();

                    break;

                case SOCKET_READ_ERROR:

                    //Log.i("DomLog", "commEvent: SOCKET READ ERROR");

                    mListener.onDataReadError(events[0].getEventErrorMessage());

                    break;

                case SOCKET_WRITE_ERROR:

                    //Log.i("DomLog", "commEvent: SOCKET WRITE ERROR");

                    mListener.onDataWriteError(events[0].getEventErrorMessage());

                    break;

                case SOCKET_CLOSED:

                    //Log.i("DomLog", "commEvent: SOCKET CLOSED");

                    mListener.onSocketClosed();

                    break;

                default:

                    break;
            }
        }

        protected void onPostExecute(Void... voids) {

        }
    }

    /*
    private class CommSocketListenerThread extends Thread {

        private CommSocketListener mListener = null;

        boolean bContinue = true;

        public CommSocketListenerThread(CommSocketListener listener) {

            if (listener != null) {

                if (Activity.class.isInstance(listener)) {

                    mListener = listener;
                }
                else {

                    throw new ClassCastException();
                }
            }
        }

        public void run() {

            while(bContinue) {

                try {

                    mSemaphore.acquire();

                    final CommSocketEvent event=getEvent();

                    if (mListener != null) {

                        Activity activity = (Activity) mListener;

                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                dispatchEventToListener(event);
                            }
                        });
                    }
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
            }
        }

        private void dispatchEventToListener(CommSocketEvent event) {

            if (mListener == null) {

                return;
            }

            switch (event.getEventType()) {

                case CONNECTED:

                    //Log.i("DomLog", "commEvent: CONNECTED");

                    mListener.onConnectionEstablished();

                    startSocketReader();

                    break;

                case CONNECT_ERROR:

                    //Log.i("DomLog", "commEvent: CONNECT ERROR");

                    mListener.onConnectionError(event.getEventErrorMessage());

                    break;

                case DATA_READ:

                    //Log.i("DomLog", "commEvent: DATA READ");

                    mListener.onDataReceived(event.getDataRead());

                    break;

                case DATA_SENT:

                    //Log.i("DomLog", "commEvent: DATA SENT");

                    mListener.onDataSent();

                    break;

                case SOCKET_READ_ERROR:

                    //Log.i("DomLog", "commEvent: SOCKET READ ERROR");

                    mListener.onDataReadError(event.getEventErrorMessage());

                    break;

                case SOCKET_WRITE_ERROR:

                    //Log.i("DomLog", "commEvent: SOCKET WRITE ERROR");

                    mListener.onDataWriteError(event.getEventErrorMessage());

                    break;

                case SOCKET_CLOSED:

                    //Log.i("DomLog", "commEvent: SOCKET CLOSED");

                    mListener.onSocketClosed();

                    break;

                default:

                    break;
            }
        }
    }
    */
}
