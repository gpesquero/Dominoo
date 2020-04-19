package org.dominoo;

import java.net.Socket;

public class ConnectionEvent {

    public enum Type {

        UNKNOWN,
        IDLE,
        CONNECTING,
        CONNECTED,
        ERROR,
        DATA_READ,
        SOCKET_READ_ERROR,
        SOCKET_CLOSED
    }

    //public Socket mSocket=null;
    //public Connection mConnection = null;
    private Type mEventType = Type.UNKNOWN;
    private String mEventErrorMessage = null;
    private String mDataRead = null;

    //private int mElapsedTime=0;
    //private int mMaxTimeout=0;

    void setEventType(Type eventType) {

        mEventType = eventType;
    }

    Type getEventType() {

        return mEventType;
    }

    void setEventErrorMessage(String errorMessage) {

        mEventErrorMessage = errorMessage;
    }

    String getEventErrorMessage() {

        return mEventErrorMessage;
    }

    void setDataRead(String data) {

        mDataRead = data;
    }

    String getDataRead() {

        return mDataRead;
    }

}
