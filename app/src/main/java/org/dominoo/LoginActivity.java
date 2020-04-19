package org.dominoo;

import org.dominoo.Message.MsgId;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.widget.Toast.LENGTH_LONG;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    // Controls
    ProgressBar mProgressBar;
    TextView mTextViewConnecting;
    TextView mEditTextPlayerName;
    Button mButtonConnect;

    // App private shared preferences
    SharedPreferences mPrefs;

    ConnectionEvent mLastConnectionEvent = null;

    private ConnectionViewModel mConnectionViewModel=null;

    private Handler mTimerHandler;

    private int mElapsedTime = 0;

    private final int MAX_TIMEOUT=10000;    // 10 seconds
    private final int TIMER_DELAY=200;      // Timer every 200 ms

    private final static String SERVER_ADDRESS="192.168.1.133";
    private final static int SERVER_PORT=52301;

    // Create the observer which updates the UI.
    final Observer<ConnectionEvent> mSocketObserver = new Observer<ConnectionEvent>() {

        @Override
        public void onChanged(@Nullable final ConnectionEvent connectionEvent) {

            Log.d("Prueba", "onChanged()");

            onConnectionEvent(connectionEvent);
        }
    };

    private Runnable mTimerRunnable=new Runnable() {

        @Override
        public void run() {

            if (mLastConnectionEvent.getEventType() == ConnectionEvent.Type.CONNECTING) {

                mElapsedTime += TIMER_DELAY;

                updateControls();
            }

            mTimerHandler.postDelayed(this, TIMER_DELAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mConnectionViewModel=ViewModelProviders.of(this).get(ConnectionViewModel.class);

        mPrefs=getPreferences(Context.MODE_PRIVATE);

        mProgressBar=findViewById(R.id.progressBar);
        mProgressBar.setMax(MAX_TIMEOUT);

        mTextViewConnecting=findViewById(R.id.textViewConnecting);

        mEditTextPlayerName=findViewById(R.id.editTextPlayerName);

        String playerName = mPrefs.getString(getString(R.string.key_player_name), getString(R.string.default_player_name));

        mEditTextPlayerName.setText(playerName);

        mButtonConnect = findViewById(R.id.buttonConnect);
        mButtonConnect.setOnClickListener(this);

        LiveData<ConnectionEvent> ldConnectionEvent = mConnectionViewModel.attach();

        if (ldConnectionEvent == null) {

            mLastConnectionEvent = new ConnectionEvent();

            mLastConnectionEvent.setEventType(ConnectionEvent.Type.IDLE);
        }
        else {

            mLastConnectionEvent = ldConnectionEvent.getValue();

            ldConnectionEvent.observe(this, mSocketObserver);
        }

        updateControls();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {

        /*
        if (mLoginViewModel!=null) {

            mLoginViewModel.closeSocket();
        }
        */

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

            updateControls();

            LiveData<ConnectionEvent> ldSocket=mConnectionViewModel.connectToServer(
                    SERVER_ADDRESS, SERVER_PORT, MAX_TIMEOUT);

            //mSocketStatus=ldSocket.getValue();

            ldSocket.observe(this, mSocketObserver);

            mElapsedTime = 0;

            // Launch timer for progress bar update
            mTimerHandler=new Handler();
            mTimerHandler.postDelayed(mTimerRunnable, TIMER_DELAY);

            updateControls();
        }
    }

    private void updateControls() {

        if (mLastConnectionEvent.getEventType() == ConnectionEvent.Type.CONNECTING) {

            mProgressBar.setVisibility(View.VISIBLE);
            mTextViewConnecting.setVisibility(View.VISIBLE);
            mEditTextPlayerName.setEnabled(false);
            mButtonConnect.setEnabled(false);

            mProgressBar.setMax(MAX_TIMEOUT);
            mProgressBar.setProgress(mElapsedTime);
        }
        else {

            mProgressBar.setVisibility(View.INVISIBLE);
            mTextViewConnecting.setVisibility(View.INVISIBLE);
            mEditTextPlayerName.setEnabled(true);
            mButtonConnect.setEnabled(true);
        }
    }

    private void onConnectionEvent(ConnectionEvent connectionEvent)  {

        mLastConnectionEvent = connectionEvent;

        String toastText=null;

        boolean killTimer=false;

        switch(mLastConnectionEvent.getEventType()) {

            case ERROR:

                Log.d("Prueba", "onSocketStatusChanged() ERROR");

                toastText=getString(R.string.error_while_connecting)+" ("+
                        mLastConnectionEvent.getEventErrorMessage()+")";

                mConnectionViewModel.reset(this);

                mLastConnectionEvent=new ConnectionEvent();
                mLastConnectionEvent.setEventType(ConnectionEvent.Type.IDLE);

                killTimer=true;

                break;

            case CONNECTED:

                Log.d("Prueba", "onSocketStatusChanged() CONNECTED");

                toastText=getString(R.string.connection_successful);

                String playerName=mEditTextPlayerName.getText().toString();

                String message=CommProtocol.createMsgOpenSession(playerName);

                mConnectionViewModel.sendMessage(message);

                /*
                try {
                    PrintWriter output=new PrintWriter(mSocketStatus.mSocket.getOutputStream(),
                            true);

                    output.println("hello");

                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                */

                killTimer=true;

                break;

            case CONNECTING:

                Log.d("Prueba", "onSocketStatusChanged() CONNECTING ("+mElapsedTime+")");

                break;

            case DATA_READ:

                String dataRead=mLastConnectionEvent.getDataRead();

                Log.d("Prueba", "onSocketStatusChanged() DATA_READ ("+dataRead+")");

                Message msg=CommProtocol.processLine(dataRead);

                processMessage(msg);

                break;

            default:

                Log.d("Prueba", "onSocketStatusChanged() DEFAULT");
                break;
        }

        if (killTimer) {

            if (mTimerHandler!=null) {

                mTimerHandler.removeCallbacks(mTimerRunnable);
                mTimerHandler=null;
            }
        }

        updateControls();

        if (toastText!=null) {

            Toast toast = Toast.makeText(this, toastText, LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    private void processMessage(Message msg) {

        if (msg.mId==MsgId.SESSION_INFO) {

            Session session=new Session();
            session.mPlayerName=mEditTextPlayerName.getText().toString();

            session.mPlayer0Name=msg.getArgument("player0");
            session.mPlayer1Name=msg.getArgument("player1");
            session.mPlayer2Name=msg.getArgument("player2");
            session.mPlayer3Name=msg.getArgument("player3");

            session.mConnection=mConnectionViewModel.getConnection();

            CustomApplication app=(CustomApplication)getApplication();
            app.setSession(session);

            //mLoginViewModel.interruptSocket();

            Intent intent=new Intent(this, GameManagementActivity.class);
            startActivity(intent);
        }
        else {

            Log.d("Prueba", "processMessage() unknown message Id="+msg.mId);
        }

    }
}
