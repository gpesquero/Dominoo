package org.dominoo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class DominooApplication extends Application {

    public final static String DEFAULT_SERVER_ADDRESS="192.168.1.136";
    public final static int DEFAULT_SERVER_PORT=52301;
    public final static boolean DEFAULT_ALLOW_LAUNCH_GAMES = false;

    public CommSocket mCommSocket = null;

    public Game mGame = null;

    public String mServerAddr = DEFAULT_SERVER_ADDRESS;
    public int mServerPort = DEFAULT_SERVER_PORT;
    public boolean mAllowLaunchGames = DEFAULT_ALLOW_LAUNCH_GAMES;

    public static final int VERSION_MAJOR = 0;
    public static final int VERSION_MINOR = 1;

    public Game getGame() {

        return mGame;
    }

    public void setGame(Game game) {

        mGame=game;
    }

    public void createGame() {

        mGame=new Game();
    }

    public CommSocket getCommSocket() {

        return mCommSocket;
    }

    public void setCommSocket(CommSocket commSocket) {

        mCommSocket = commSocket;
    }

    public void loadPreferences(Context context, SharedPreferences prefs) {

        mServerAddr = prefs.getString(context.getString(R.string.key_server_address),
                DEFAULT_SERVER_ADDRESS);

        mServerPort = prefs.getInt(context.getString(R.string.key_server_port),
                DEFAULT_SERVER_PORT);

        mAllowLaunchGames = prefs.getBoolean(context.getString(R.string.key_allow_launch_games),
                DEFAULT_ALLOW_LAUNCH_GAMES);
    }
}
