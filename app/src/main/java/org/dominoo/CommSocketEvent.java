package org.dominoo;

import java.io.PrintWriter;
import java.net.Socket;

public class CommSocketEvent extends ThreadEvent {

    public enum Type {

        UNKNOWN,
        IDLE,
        CONNECTED,
        CONNECT_ERROR,
        DATA_READ,
        DATA_SENT,
        SOCKET_READ_ERROR,
        SOCKET_WRITE_ERROR,
        SOCKET_CLOSED,
        WRITE_DATA
    }

    private Type mEventType = Type.UNKNOWN;
    private Socket mSocket = null;
    private PrintWriter mSocketWriter = null;
    private String mEventErrorMessage = null;
    private String mData = null;

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

    void setSocket(Socket socket) {

        mSocket = socket;
    }

    Socket getSocket() {

        return mSocket;
    }

    void setSocketWriter(PrintWriter socketWriter) {

        mSocketWriter = socketWriter;
    }

    PrintWriter getSocketWriter() {

        return mSocketWriter;
    }

    void setEventErrorMessage(String errorMessage) {

        mEventErrorMessage = errorMessage;
    }

    String getEventErrorMessage() {

        return mEventErrorMessage;
    }

    void setData(String data) {

        mData = data;
    }

    String getData() {

        return mData;
    }
}
