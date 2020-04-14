package org.dominoo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends Activity implements View.OnClickListener {

    ProgressBar mProgressBar;
    TextView mTextViewConnecting;
    TextView mEditTextPlayerName;
    Button mButtonConnect;

    SharedPreferences mPrefs;

    Handler mTimerHandler;

    ConnectToServerTask mConnectToServerTask;

    private final int mMaxTimeout=10000;    // 10 seconds
    private final int mTimerDelay=200;

    private final String KEY_IS_CONNECTING="KEY_IS_CONNECTING";
    private final String KEY_PROGRESS="KEY_PROGRESS";

    private Runnable mTimerRunnable=new Runnable() {

        @Override
        public void run() {

            int pos=mProgressBar.getProgress();

            pos+=mTimerDelay;

            mProgressBar.setProgress(pos);

            mTimerHandler.postDelayed(this, mTimerDelay);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mPrefs=getPreferences(Context.MODE_PRIVATE);

        String playerName=mPrefs.getString(getString(R.string.key_player_name),
                getString(R.string.default_player_name));

        mProgressBar=findViewById(R.id.progressBar);
        mProgressBar.setMax(mMaxTimeout);

        mTextViewConnecting=findViewById(R.id.textViewConnecting);

        mEditTextPlayerName=findViewById(R.id.editTextPlayerName);
        mEditTextPlayerName.setText(playerName);

        mButtonConnect=findViewById(R.id.buttonConnect);
        mButtonConnect.setOnClickListener(this);

        if (savedInstanceState!=null) {

            boolean isConnecting=savedInstanceState.getBoolean(KEY_IS_CONNECTING);
            int progress=savedInstanceState.getInt(KEY_PROGRESS);

            updateControls(isConnecting, progress);

            if (isConnecting) {

                mTimerHandler=new Handler();
                mTimerHandler.post(mTimerRunnable);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean(KEY_IS_CONNECTING, mTimerHandler!=null);
        outState.putInt(KEY_PROGRESS, mProgressBar.getProgress());

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {

        if (mTimerHandler!=null) {

            mTimerHandler.removeCallbacks(mTimerRunnable);

            mTimerHandler=null;
        }

        super.onDestroy();
    }

    @Override
    public void onClick(View view) {

        if (view==mButtonConnect) {

            String playerName=mEditTextPlayerName.getText().toString();

            playerName=playerName.trim();

            if (playerName.length()<4) {

                Toast toast=Toast.makeText(this, R.string.player_name_is_too_short,
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0, 0);
                toast.show();

                return;
            }

            if (playerName.length()>20) {

                Toast toast=Toast.makeText(this, R.string.player_name_is_too_long,
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                return;
            }

            Pattern p=Pattern.compile("[^A-Za-z0-9]");
            Matcher m=p.matcher(playerName);

            if (m.find()) {

                Toast toast=Toast.makeText(this, R.string.player_name_has_special_char,
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                return;
            }

            // Save valid player name in Shared Preferences...
            SharedPreferences.Editor prefEditor=mPrefs.edit();
            prefEditor.putString(getString(R.string.key_player_name), playerName);
            prefEditor.commit();

            updateControls(true, 0);

            mTimerHandler=new Handler();
            mTimerHandler.post(mTimerRunnable);

            connectToServer();
        }
    }

    private void updateControls(boolean isConnecting, int progressValue) {

        mProgressBar.setMax(mMaxTimeout);
        mProgressBar.setProgress(progressValue);

        if (isConnecting) {

            mProgressBar.setVisibility(View.VISIBLE);
            mTextViewConnecting.setVisibility(View.VISIBLE);
            mEditTextPlayerName.setEnabled(false);
            mButtonConnect.setEnabled(false);
        }
        else {

            mProgressBar.setVisibility(View.INVISIBLE);
            mTextViewConnecting.setVisibility(View.INVISIBLE);
            mEditTextPlayerName.setEnabled(true);
            mButtonConnect.setEnabled(true);
        }
    }

    private class ConnectToServerTask extends AsyncTask<Void, Void, Socket> {

        public String mErrorMessage;

        @Override
        protected Socket doInBackground(Void... params) {

            InetAddress serverAddr = null;

            try {
                serverAddr = InetAddress.getByName("127.0.0.1");

            } catch (UnknownHostException e) {

                mErrorMessage=e.getMessage();

                return null;
            }

            InetSocketAddress address=new InetSocketAddress("192.168.1.146", 52301);

            Socket socket;

            try {
                socket = new Socket();

                socket.connect(address, mMaxTimeout);

            } catch (IOException e) {

                mErrorMessage=e.getMessage();

                return null;
            }

            return socket;
        }

        protected void onPostExecute(Socket socket) {

            onConnectToServer(socket);
        }
    }

    private void connectToServer() {

        mConnectToServerTask=new ConnectToServerTask();

        mConnectToServerTask.execute();
    }

    private void onConnectToServer(Socket socket) {

        String text;

        if (socket==null) {

            text=getString(R.string.error_while_connecting)+" ("+
                    mConnectToServerTask.mErrorMessage+")";
        }
        else {

            text="Connection to server successful";
        }

        Toast toast=Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        if (mTimerHandler!=null) {

            mTimerHandler.removeCallbacks(mTimerRunnable);
            mTimerHandler=null;
        }

        updateControls(false, 0);
    }
}
