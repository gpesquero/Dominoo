package org.dominoo;

import android.app.Application;

public class CustomApplication extends Application {

    private Session mSession;

    public Session getSession() {
        return mSession;
    }

    public void setSession(Session session) {

        mSession=session;
    }
}
