package org.dominoo;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.PrintWriter;

public class ConnectionViewModel extends ViewModel implements Connection.ConnectionEventListener {

    //private SocketStatus mSocketStatus=new SocketStatus();

    private Connection mConnection=null;

    //private Socket mSocket=null;

    private MutableLiveData<ConnectionEvent> mLiveDataConnectionStatus = null;

    LiveData<ConnectionEvent> attach() {

        return mLiveDataConnectionStatus;
    }

    void reset(LifecycleOwner owner) {

        if (mLiveDataConnectionStatus!=null) {

            mLiveDataConnectionStatus.removeObservers(owner);
            mLiveDataConnectionStatus=null;
        }

        //closeSocket();

        /*
        if (mTimerHandler!=null) {

            mTimerHandler.removeCallbacks(mTimerRunnable);
            mTimerHandler=null;
        }

        mSocketStatus=new SocketStatus();
        */
    }

    public LiveData<ConnectionEvent> connectToServer(String serverAddress, int serverPort,
                                                     int maxTimeout) {

        if (mConnection == null) {

            mConnection = new Connection(this);
        }

        mConnection.connectToServer(serverAddress, serverPort, maxTimeout);

        if (mLiveDataConnectionStatus == null) {

            mLiveDataConnectionStatus = new MutableLiveData<ConnectionEvent>();
        }

        ConnectionEvent connectionEvent = new ConnectionEvent();

        connectionEvent.setEventType(ConnectionEvent.Type.CONNECTING);

        mLiveDataConnectionStatus.postValue(connectionEvent);

        return mLiveDataConnectionStatus;
    }

    public boolean sendMessage(String message) {

        if (mConnection == null) {

            return false;
        }

        return mConnection.sendMessage(message);
    }

    public Connection getConnection() {

        return mConnection;
    }

    @Override
    public void onConnectionEvent(ConnectionEvent connectionEvent) {

        if (mLiveDataConnectionStatus != null) {

            mLiveDataConnectionStatus.postValue(connectionEvent);
        }
    }
}
