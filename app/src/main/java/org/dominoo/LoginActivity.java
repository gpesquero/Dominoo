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

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        CommSocket.CommSocketListener {

    // Controls
    ProgressBar mProgressBar;
    TextView mTextViewConnecting;
    TextView mEditTextPlayerName;
    Button mButtonConnect;

    // App private shared preferences
    SharedPreferences mPrefs;

    private LoginViewModel mLoginViewModel=null;

    private CommSocket mCommSocket = null;

    private Handler mTimerHandler;

    private final int MAX_TIMEOUT=10000;    // 10 seconds
    private final int TIMER_DELAY=200;      // Timer every 200 ms

    private final static String SERVER_ADDRESS="192.168.1.136";
    private final static int SERVER_PORT=52301;

    /*
    // Create the observer which updates the UI.
    final Observer<CommSocketEvent> mSocketObserver = new Observer<CommSocketEvent>() {

        @Override
        public void onChanged(@Nullable final CommSocketEvent connectionEvent) {

            Log.d("DomLog", "onChanged()");

            onConnectionEvent(connectionEvent);
        }
    };
    */

    private Runnable mTimerRunnable=new Runnable() {

        @Override
        public void run() {

            mLoginViewModel.mElapsedTime += TIMER_DELAY;

            updateControls();

            mTimerHandler.postDelayed(this, TIMER_DELAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mLoginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        mPrefs=getPreferences(Context.MODE_PRIVATE);

        mProgressBar=findViewById(R.id.progressBar);
        mProgressBar.setMax(MAX_TIMEOUT);

        mTextViewConnecting=findViewById(R.id.textViewConnecting);

        mEditTextPlayerName=findViewById(R.id.editTextPlayerName);

        String playerName = mPrefs.getString(getString(R.string.key_player_name), getString(R.string.default_player_name));

        mEditTextPlayerName.setText(playerName);

        mButtonConnect = findViewById(R.id.buttonConnect);
        mButtonConnect.setOnClickListener(this);

        DominooApplication app=(DominooApplication)getApplication();
        Session session=app.getSession();

        mCommSocket=app.getCommSocket();

        if (mCommSocket == null) {

            mCommSocket = new CommSocket();
        }

        mCommSocket.setCommSocketListener(this);

        if (mCommSocket.isConnecting()) {

            startTimer();
        }

        if (session==null) {

            app.createSession();
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

        DominooApplication app=(DominooApplication)getApplication();

        mCommSocket.setCommSocketListener(null);

        app.setCommSocket(mCommSocket);

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

            //updateControls();

            /*
            LiveData<ConnectionEvent> ldSocket=mConnectionViewModel.connectToServer(
                    SERVER_ADDRESS, SERVER_PORT, MAX_TIMEOUT);

            //mSocketStatus=ldSocket.getValue();

            ldSocket.observe(this, mSocketObserver);
            */

            mCommSocket.close();

            mCommSocket.connectToServer(SERVER_ADDRESS, SERVER_PORT, MAX_TIMEOUT);

            mLoginViewModel.mElapsedTime = 0;

            startTimer();

            updateControls();
        }
    }

    private void updateControls() {

        //if (mLastConnectionEvent.getEventType() == ConnectionEvent.Type.CONNECTING) {

        if (mCommSocket.isConnecting()) {

            mProgressBar.setVisibility(View.VISIBLE);
            mTextViewConnecting.setVisibility(View.VISIBLE);
            mEditTextPlayerName.setEnabled(false);
            mButtonConnect.setEnabled(false);

            mProgressBar.setMax(MAX_TIMEOUT);
            mProgressBar.setProgress(mLoginViewModel.mElapsedTime);
        }
        else {

            mProgressBar.setVisibility(View.INVISIBLE);
            mTextViewConnecting.setVisibility(View.INVISIBLE);
            mEditTextPlayerName.setEnabled(true);
            mButtonConnect.setEnabled(true);
        }
    }

    private void onConnectionEvent(CommSocketEvent connectionEvent)  {

        /*
        mLastConnectionEvent = connectionEvent;

        String toastText=null;

        boolean killTimer=false;

        switch(mLastConnectionEvent.getEventType()) {

            case ERROR:

                Log.d("DomLog", "onSocketStatusChanged() ERROR");

                toastText=getString(R.string.error_while_connecting)+" ("+
                        mLastConnectionEvent.getEventErrorMessage()+")";

                //mConnectionViewModel.reset(this);

                mLastConnectionEvent=new ConnectionEvent();
                mLastConnectionEvent.setEventType(ConnectionEvent.Type.IDLE);

                killTimer=true;

                break;

            case CONNECTED:

                Log.d("DomLog", "onSocketStatusChanged() CONNECTED");

                toastText=getString(R.string.connection_successful);

                String playerName=mEditTextPlayerName.getText().toString();

                String message=CommProtocol.createMsgOpenSession(playerName);

                //mConnectionViewModel.sendMessage(message);

                killTimer=true;

                break;

            case CONNECTING:

                Log.d("DomLog", "onSocketStatusChanged() CONNECTING ("+mElapsedTime+")");

                break;

            case DATA_READ:

                String dataRead=mLastConnectionEvent.getDataRead();

                Log.d("DomLog", "onSocketStatusChanged() DATA_READ ("+dataRead+")");

                break;

            default:

                Log.d("DomLog", "onSocketStatusChanged() DEFAULT");
                break;
        }

        updateControls();

        if (toastText!=null) {


        }
        */
    }

    private void processMessage(Message msg) {

        if (msg.mId==MsgId.SESSION_INFO) {

            Session session=new Session();
            session.mPlayerName=mEditTextPlayerName.getText().toString();

            session.mAllPlayerNames.clear();

            session.mAllPlayerNames.add(msg.getArgument("player0"));
            session.mAllPlayerNames.add(msg.getArgument("player1"));
            session.mAllPlayerNames.add(msg.getArgument("player2"));
            session.mAllPlayerNames.add(msg.getArgument("player3"));

            //session.mConnection=mConnectionViewModel.getConnection();

            DominooApplication app=(DominooApplication)getApplication();
            app.setSession(session);
            app.setCommSocket(mCommSocket);

            //mLoginViewModel.interruptSocket();

            Intent intent=new Intent(this, GameManagementActivity.class);
            startActivity(intent);
        }
        else {

            Log.d("DomLog", "processMessage() unknown message Id="+msg.mId);
        }

    }

    private void startTimer() {

        // Launch timer for progress bar update
        mTimerHandler=new Handler();
        mTimerHandler.postDelayed(mTimerRunnable, TIMER_DELAY);
    }

    private void stopTimer() {

        if (mTimerHandler!=null) {

            mTimerHandler.removeCallbacks(mTimerRunnable);
            mTimerHandler=null;
        }
    }

    @Override
    public void onConnectionEstablished() {

        /*
        String toastText = getString(R.string.connection_successful);
        Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        */

        stopTimer();

        updateControls();

        // Request open session to server...
        String playerName=mEditTextPlayerName.getText().toString();

        String message=CommProtocol.createMsgOpenSession(playerName);

        mCommSocket.sendMessage(message);
    }

    @Override
    public void onConnectionError(String errorMessage) {

        String toastText = getString(R.string.error_while_connecting)+" ("+errorMessage+")";

        Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        stopTimer();

        updateControls();
    }

    @Override
    public void onConnectionLost() {

    }

    @Override
    public void onDataReceived(String data) {

        /*
        String toastText = "Data received: "+data;
        Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        */

        Message msg=CommProtocol.processLine(data);

        processMessage(msg);
    }

    @Override
    public void onDataReadError(String errorMessage) {

    }

    @Override
    public void onDataSent() {

    }

    @Override
    public void onSocketClosed() {

    }
}
