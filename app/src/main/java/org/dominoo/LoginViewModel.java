package org.dominoo;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LoginViewModel extends ViewModel /*implements CommSocket.CommSocketListener*/ {

    public int mElapsedTime = 0;



    //private SocketStatus mSocketStatus=new SocketStatus();

    private CommSocket mConnection=null;

    //private Socket mSocket=null;

    private MutableLiveData<CommSocketEvent> mLiveDataConnectionStatus = null;

    LiveData<CommSocketEvent> attach() {

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

    /*
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
    */

    /*
    public boolean sendMessage(String message) {

        if (mConnection == null) {

            return false;
        }

        return mConnection.sendMessage(message);
    }

    public Connection getConnection() {

        return mConnection;
    }
    */

    /*
    @Override
    public void onConnectionEvent(ConnectionEvent connectionEvent) {

        if (mLiveDataConnectionStatus != null) {

            mLiveDataConnectionStatus.postValue(connectionEvent);
        }
    }
    */

    /*
    @Override
    public void onConnectionEstablished() {

    }

    @Override
    public void onConnectionError(String errorMessage) {

    }

    @Override
    public void onConnectionLost() {

    }

    @Override
    public void onDataReceived(String data) {

    }

    @Override
    public void onDataReadError(String errorMessage) {

    }

    @Override
    public void onDataSent() {

    }

    @Override
    public void onSocketClosed() {

    }
    */
}
