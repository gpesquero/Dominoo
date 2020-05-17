package org.dominoo;

import java.net.Socket;

public class CommSocketEvent {

    public enum Type {

        UNKNOWN,
        IDLE,
        CONNECTED,
        CONNECT_ERROR,
        DATA_READ,
        DATA_SENT,
        SOCKET_READ_ERROR,
        SOCKET_WRITE_ERROR,
        SOCKET_CLOSED
    }

    //public Socket mSocket=null;
    //public Connection mConnection = null;
    private Type mEventType = Type.UNKNOWN;
    private String mEventErrorMessage = null;
    private String mDataRead = null;

    //private int mElapsedTime=0;
    //private int mMaxTimeout=0;

    public CommSocketEvent() {

    }

    public CommSocketEvent(Type eventType) {

        setEventType(eventType);
    }

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
