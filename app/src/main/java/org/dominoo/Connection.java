package org.dominoo;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Connection {

    private Socket mSocket = null;

    private String mSocketErrorMessage = null;

    private PrintWriter mSocketWriter = null;

    private ConnectToServerTask mConnectToServerTask = null;

    private SocketReaderTask mSocketReaderTask = null;

    private ConnectionEventListener mListener = null;

    public interface ConnectionEventListener {

        void onConnectionEvent(ConnectionEvent connectionEvent);

    }

    private class ConnectToServerTask extends AsyncTask<Void, Void, ConnectionEvent> {

        private String mAddress;
        private int mPort;
        private int mMaxTimeout;

        public ConnectToServerTask(String address, int port, int maxTimeout) {

            mAddress = address;
            mPort = port;
            mMaxTimeout = maxTimeout;
        }

        @Override
        protected ConnectionEvent doInBackground(Void... params) {

            InetSocketAddress address = new InetSocketAddress(mAddress, mPort);

            ConnectionEvent connectionEvent = new ConnectionEvent();

            try {

                mSocket = new Socket();

                mSocket.connect(address, mMaxTimeout);

                /*
                PrintWriter output=new PrintWriter(mSocket.getOutputStream(), true);

                output.write("hola");
                output.println("hola");

                 */

                //connectionEvent.mSocket = mSocket;

                connectionEvent.setEventType(ConnectionEvent.Type.CONNECTED);

            } catch (IOException e) {

                connectionEvent.setEventType(ConnectionEvent.Type.ERROR);

                connectionEvent.setEventErrorMessage("socket.connect() error:"+e.getMessage());
            }

            return connectionEvent;
        }

        protected void onPostExecute(ConnectionEvent connectionEvent) {

            if (mListener!=null) {

                mListener.onConnectionEvent(connectionEvent);
            }

            if (mSocket.isConnected()) {

                // Launch socket reader...

                mSocketReaderTask = new SocketReaderTask();

                mSocketReaderTask.execute();
            }
        }
    }

    private class SocketReaderTask extends AsyncTask<Void, Void, Void> {

        private String mAddress;
        private int mPort;
        private int mMaxTimeout;

        public SocketReaderTask(/*String address, int port, int maxTimeout*/) {

            /*
            mAddress=address;
            mPort=port;
            mMaxTimeout=maxTimeout;
            */
        }

        @Override
        protected Void doInBackground(Void... params) {

            //InetSocketAddress address=new InetSocketAddress(mAddress, mPort);

            //SocketStatus socketStatus=new SocketStatus();

            //Socket socket;

            BufferedReader inputReader;

            try {

                inputReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

                String inputLine;

                while ((inputLine = inputReader.readLine()) != null) {

                    if (mListener != null) {

                        ConnectionEvent connectionEvent = new ConnectionEvent();

                        connectionEvent.setEventType(ConnectionEvent.Type.DATA_READ);
                        connectionEvent.setDataRead(inputLine);

                        mListener.onConnectionEvent(connectionEvent);
                    }
                }
            }
            catch (IOException e) {

                if (mListener != null) {

                    ConnectionEvent connectionEvent = new ConnectionEvent();

                    connectionEvent.setEventType(ConnectionEvent.Type.SOCKET_READ_ERROR);

                    connectionEvent.setEventErrorMessage("Socket.readLine() Error: " + e.getMessage());

                    mListener.onConnectionEvent(connectionEvent);
                }
            }

            return null;
        }

        protected void onPostExecute(ConnectionEvent socketStatus) {

            /*
            mLiveDataSocketStatus.postValue(socketStatus);

            if (socketStatus.mStatus==SocketStatus.Status.CONNECTED) {

                // Launch socket reader...

                launchReader();
            }
            */
        }
    }

    public Connection(ConnectionEventListener listener) {

        mListener=listener;
    }

    public boolean connectToServer(String serverAddress, int serverPort, int maxTimeout) {

        /*
        if (mSocket!=null) {

            try {
                mSocket.close();

                mSocket=null;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        */

        //mSocketStatus.mElapsedTime=0;
        //mSocketStatus.mMaxTimeout=maxTimeout;

        mConnectToServerTask = new ConnectToServerTask(serverAddress, serverPort, maxTimeout);
        mConnectToServerTask.execute();

        //mTimerHandler=new Handler();
        //mTimerHandler.postDelayed(mTimerRunnable, TIMER_DELAY);

        return true;
    }

    public void closeSocket() {

        if (mSocket != null) {

            try {
                mSocket.close();

                mSocket = null;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void interruptSocket() {

        if (mSocketReaderTask != null) {

            mSocketReaderTask.cancel(true);
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

                mSocketWriter = null;

                mSocketErrorMessage = e.getMessage();

                return false;
            }
        }

        new Thread(new Runnable() {

            @Override
            public void run () {

                mSocketWriter.println(message);
            }
        }).start();

        return true;
    }
}
