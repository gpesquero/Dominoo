package org.dominoo;

import java.net.Socket;

public class SocketStatus {

    public enum Status {

        UNKNOWN,
        IDLE,
        CONNECTING,
        CONNECTED,
        ERROR
    }

    public Socket mSocket=null;
    public Status mStatus=Status.UNKNOWN;
    public String mSocketErrorMessage=null;
    public int mElapsedTime=0;
    public int mMaxTimeout=0;

}
