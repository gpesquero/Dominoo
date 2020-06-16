package org.dominoo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class DominooApplication extends Application {

    public String mVersion = BuildConfig.VERSION_NAME;

    public final static String DEFAULT_SERVER_ADDRESS="gpesquero.hopto.org";
    public final static int DEFAULT_SERVER_PORT=52301;
    public final static boolean DEFAULT_ALLOW_LAUNCH_GAMES = false;
    public final static boolean DEFAULT_SILENT_MODE_ON = false;

    public CommSocket mCommSocket = null;

    public Game mGame = null;

    public String mServerAddress = DEFAULT_SERVER_ADDRESS;
    public int mServerPort = DEFAULT_SERVER_PORT;
    public boolean mAllowLaunchGames = DEFAULT_ALLOW_LAUNCH_GAMES;
    public boolean mSilentModeOn = DEFAULT_SILENT_MODE_ON;

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

        mServerAddress = prefs.getString(context.getString(R.string.key_server_address),
                DEFAULT_SERVER_ADDRESS);

        mServerPort = prefs.getInt(context.getString(R.string.key_server_port),
                DEFAULT_SERVER_PORT);

        mAllowLaunchGames = prefs.getBoolean(context.getString(R.string.key_allow_launch_games),
                DEFAULT_ALLOW_LAUNCH_GAMES);

        mSilentModeOn = prefs.getBoolean(context.getString(R.string.key_silent_mode_on),
                DEFAULT_SILENT_MODE_ON);
    }

    public void savePreferences(Context context, SharedPreferences prefs) {

        // Save mApp preferences

        SharedPreferences.Editor prefEditor=prefs.edit();

        prefEditor.putString(getString(R.string.key_server_address), mServerAddress);
        prefEditor.putInt(getString(R.string.key_server_port), mServerPort);
        prefEditor.putBoolean(getString(R.string.key_allow_launch_games), mAllowLaunchGames);
        prefEditor.putBoolean(getString(R.string.key_silent_mode_on), mSilentModeOn);

        prefEditor.commit();
    }

    public boolean sendLoginMessage(String playerName) {

        // Create <Login> message
        String message = CommProtocol.createMsgLogin(playerName, mVersion);

        // Send the message to the server
        return mCommSocket.sendMessage(message);
    }

    public boolean sendLogoutMessage() {

        // Create <Logout> message
        String msg = CommProtocol.createMsgLogout(mGame.mMyPlayerName);

        // Send the message to the server
        return mCommSocket.sendMessage(msg);
    }

    public boolean sendLaunchGameMessage() {

        // Create <Launch Game> message
        String msg = CommProtocol.createMsgLaunchGame(mGame.mMyPlayerName);

        // Send the message to the server
        return mCommSocket.sendMessage(msg);
    }

    public boolean sendCancelGameMessage() {

        // Create <Cancel Game> message
        String msg = CommProtocol.createMsgCancelGame(mGame.mMyPlayerName);

        // Send the message to the server
        return mCommSocket.sendMessage(msg);
    }

    public boolean sendRequestGameInfoMessage() {

        // Create <Request Game Info> message
        String msg = CommProtocol.createMsgRequestGameInfo(mGame.mMyPlayerName);

        // Send the message to the server
        return mCommSocket.sendMessage(msg);
    }

    public boolean sendMovePlayerMessage(String selectedName, int index) {

        String msg = CommProtocol.createMsgMovePlayer(selectedName, index);

        return mCommSocket.sendMessage(msg);
    }

    public boolean sendMessageRequestTileInfo() {

        // Create <Request Tile Info> message
        String msg = CommProtocol.createMsgRequestTileInfo(mGame.mMyPlayerName);

        // Send the message to the server
        return mCommSocket.sendMessage(msg);
    }

    public boolean sendMessagePlayTile(DominoTile tile, int boardSide) {

        String msg = CommProtocol.createMsgPlayTile(mGame.mMyPlayerName,
                mGame.getMyPlayerPos(), tile, boardSide);

        // Send the message to the server
        return mCommSocket.sendMessage(msg);
    }
}
