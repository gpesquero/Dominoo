package org.dominoo;

import android.os.AsyncTask;
import android.os.Handler;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class LoginViewModel extends ViewModel {

    private final int TIMER_DELAY=200;

    private SocketStatus mSocketStatus=new SocketStatus();

    private MutableLiveData<SocketStatus> mLiveDataSocketStatus=null;

    private ConnectToServerTask mConnectToServerTask;

    private Handler mTimerHandler;

    private class ConnectToServerTask extends AsyncTask<Void, Void, SocketStatus> {

        private String mAddress;
        private int mPort;
        private int mMaxTimeout;

        public ConnectToServerTask(String address, int port, int maxTimeout) {

            mAddress=address;
            mPort=port;
            mMaxTimeout=maxTimeout;
        }

        @Override
        protected SocketStatus doInBackground(Void... params) {

            InetSocketAddress address=new InetSocketAddress(mAddress, mPort);

            Socket socket;

            try {
                socket=new Socket();

                mSocketStatus.mSocket=socket;

                socket.connect(address, mMaxTimeout);

                mSocketStatus.mStatus=SocketStatus.Status.CONNECTED;
            }
            catch (IOException e) {

                mSocketStatus.mStatus=SocketStatus.Status.ERROR;
                mSocketStatus.mSocketErrorMessage=e.getMessage();
            }

            return mSocketStatus;
        }

        protected void onPostExecute(SocketStatus socketStatus) {

            mLiveDataSocketStatus.postValue(socketStatus);

            if (mTimerHandler!=null) {

                mTimerHandler.removeCallbacks(mTimerRunnable);
                mTimerHandler=null;
            }
        }
    }

    private Runnable mTimerRunnable=new Runnable() {

        @Override
        public void run() {

            if (mSocketStatus.mStatus==SocketStatus.Status.CONNECTING) {

                mSocketStatus.mElapsedTime+=TIMER_DELAY;

                mLiveDataSocketStatus.postValue(mSocketStatus);
            }

            mTimerHandler.postDelayed(this, TIMER_DELAY);
        }
    };

    public LiveData<SocketStatus> attach() {

        return mLiveDataSocketStatus;
    }

    public void reset(LifecycleOwner owner) {

        if (mLiveDataSocketStatus!=null) {

            mLiveDataSocketStatus.removeObservers(owner);
            mLiveDataSocketStatus=null;
        }

        if (mTimerHandler!=null) {

            mTimerHandler.removeCallbacks(mTimerRunnable);
            mTimerHandler=null;
        }

        mSocketStatus=new SocketStatus();
    }

    public LiveData<SocketStatus> connectToServer(String serverAddress, int serverPort,
                                                  int maxTimeout) {

        mSocketStatus.mElapsedTime=0;
        mSocketStatus.mMaxTimeout=maxTimeout;

        mConnectToServerTask=new ConnectToServerTask(serverAddress, serverPort, maxTimeout);
        mConnectToServerTask.execute();

        mTimerHandler=new Handler();
        mTimerHandler.postDelayed(mTimerRunnable, TIMER_DELAY);

        if (mLiveDataSocketStatus==null) {

            mLiveDataSocketStatus=new MutableLiveData<SocketStatus>();
        }

        mSocketStatus.mStatus=SocketStatus.Status.CONNECTING;

        mLiveDataSocketStatus.setValue(mSocketStatus);

        return mLiveDataSocketStatus;
    }
}
