package org.dominoo;

import android.app.Application;

public class DominooApplication extends Application {

    private CommSocket mCommSocket = null;

    private Session mSession = null;

    public Session getSession() {

        return mSession;
    }

    public void setSession(Session session) {

        mSession=session;
    }

    public void createSession() {

        mSession=new Session();
    }

    public CommSocket getCommSocket() {

        return mCommSocket;
    }

    public void setCommSocket(CommSocket commSocket) {

        mCommSocket = commSocket;
    }
}
