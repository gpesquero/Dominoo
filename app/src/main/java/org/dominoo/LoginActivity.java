package org.dominoo;

import org.dominoo.Message.MsgId;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        CommSocket.CommSocketListener {

    // Controls
    ImageView mImageViewSettings;
    TextView mTextViewVersion;
    ProgressBar mProgressBar;
    TextView mTextViewConnecting;
    TextView mTextViewPlayerName;
    Button mButtonConnect;

    // App private shared preferences
    SharedPreferences mPrefs;

    private LoginViewModel mLoginViewModel=null;

    private DominooApplication mApp = null;

    private Handler mTimerHandler;

    private final int MAX_TIMEOUT=10000;    // 10 seconds
    private final int TIMER_DELAY=200;      // Timer every 200 ms

    private final int PLAYER_NAME_MIN_LENGTH = 4;
    private final int PLAYER_NAME_MAX_LENGTH = 8;

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

        Log.d("DomLog", "LoginActivity.onCreate()");

        setContentView(R.layout.activity_login);

        mLoginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        mPrefs=getPreferences(Context.MODE_PRIVATE);

        mImageViewSettings=findViewById(R.id.imageViewSettings);
        mImageViewSettings.setOnClickListener(this);

        mTextViewVersion=findViewById(R.id.textViewVersion);

        mProgressBar=findViewById(R.id.progressBar);
        mProgressBar.setMax(MAX_TIMEOUT);

        mTextViewConnecting=findViewById(R.id.textViewConnecting);

        String playerName = mPrefs.getString(getString(R.string.key_player_name),
                getString(R.string.default_player_name));

        mTextViewPlayerName=findViewById(R.id.textViewPlayerName);
        mTextViewPlayerName.setOnClickListener(this);
        mTextViewPlayerName.setText(playerName);

        mButtonConnect = findViewById(R.id.buttonConnect);
        mButtonConnect.setOnClickListener(this);

        mApp = (DominooApplication)getApplication();

        mApp.loadPreferences(this, mPrefs);

        String versionString = getString(R.string.version_);
        versionString += " " + BuildConfig.VERSION_NAME;
        mTextViewVersion.setText(versionString);

        if (mApp.mCommSocket == null) {

            mApp.mCommSocket = new CommSocket();
        }

        mApp.mCommSocket.setCommSocketListener(this);

        if (mApp.mCommSocket.isConnecting()) {

            startTimer();
        }

        if (mApp.mGame == null) {

            mApp.createGame();
        }

        updateControls();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("DomLog", "LoginActivity.onResume()");

        if (mApp.mCommSocket != null) {

            if (mApp.mCommSocket.getCommSocketListener() != this) {

                mApp.mCommSocket.setCommSocketListener(this);
            }
        }
    }

    @Override
    protected void onDestroy() {

        Log.d("DomLog", "LoginActivity.onDestroy()");

        if (mApp.mCommSocket != null) {

            mApp.mCommSocket.setCommSocketListener(null);
        }

        mApp.setCommSocket(mApp.mCommSocket);

        if (mTimerHandler!=null) {

            mTimerHandler.removeCallbacks(mTimerRunnable);
            mTimerHandler=null;
        }

        // Store mApp preferences

        SharedPreferences.Editor prefEditor=mPrefs.edit();
        prefEditor.putString(getString(R.string.key_server_address), mApp.mServerAddress);
        prefEditor.putInt(getString(R.string.key_server_port), mApp.mServerPort);
        prefEditor.putBoolean(getString(R.string.key_allow_launch_games), mApp.mAllowLaunchGames);
        prefEditor.commit();

        super.onDestroy();
    }

    @Override
    public void onClick(View view) {

        if (view == mButtonConnect) {

            String playerName=mTextViewPlayerName.getText().toString().trim();

            String resultString = checkPlayerName(playerName);

            if (resultString.compareTo(getString(R.string.player_name_is_ok)) != 0) {

                Toast toast=Toast.makeText(this, resultString, Toast.LENGTH_SHORT);

                toast.setGravity(Gravity.CENTER,0, 0);

                toast.show();

                return;
            }

            // Save valid player name in Shared Preferences...
            SharedPreferences.Editor prefEditor=mPrefs.edit();
            prefEditor.putString(getString(R.string.key_player_name), playerName);
            prefEditor.commit();

            if (mApp.mCommSocket == null) {

                mApp.mCommSocket = new CommSocket();
            }
            else {

                mApp.mCommSocket.close();
            }

            if (mApp.mCommSocket.getCommSocketListener() != this) {

                mApp.mCommSocket.setCommSocketListener(this);
            }

            mApp.mCommSocket.connectToServer(mApp.mServerAddress, mApp.mServerPort, MAX_TIMEOUT);

            mLoginViewModel.mElapsedTime = 0;

            startTimer();

            updateControls();
        }
        else if (view == mImageViewSettings) {

            Intent intent=new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else if (view == mTextViewPlayerName) {

            final EditText editTextPlayerName = new EditText(this);

            String playerName = mTextViewPlayerName.getText().toString().trim();

            editTextPlayerName.setText(playerName);
            editTextPlayerName.setGravity(Gravity.CENTER);

            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.player_name_)
                    .setView(editTextPlayerName)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            String newPlayerName = editTextPlayerName.getText().toString().trim();

                            String resultString = checkPlayerName(newPlayerName);

                            if (resultString.compareTo(getString(R.string.player_name_is_ok))!=0) {

                                Toast toast=Toast.makeText(getApplicationContext(),
                                        resultString, Toast.LENGTH_SHORT);

                                toast.setGravity(Gravity.CENTER,0, 0);

                                toast.show();
                            }
                            else {

                                mTextViewPlayerName.setText(newPlayerName);

                                dialog.dismiss();
                            }
                        }
                    }
            );



        }
    }

    private String checkPlayerName(String playerName) {

        String resultString;

        if (playerName.length() < PLAYER_NAME_MIN_LENGTH) {

            resultString = getString(R.string.player_name_is_too_short) + "\n\n" +
                    getString(R.string.insert_player_name_with_at_least_x_chars,
                            PLAYER_NAME_MIN_LENGTH);
        }
        else if (playerName.length() > PLAYER_NAME_MAX_LENGTH) {

            resultString = getString(R.string.player_name_is_too_long) + "\n\n" +
                    getString(R.string.maximum_player_name_length_is_x_characters,
                            PLAYER_NAME_MAX_LENGTH);
        }
        else {

            Pattern p = Pattern.compile("[^A-Za-z0-9]");
            Matcher m = p.matcher(playerName);

            if (m.find()) {

                resultString = getString(R.string.player_name_has_special_char);
            }
            else {

                resultString = getString(R.string.player_name_is_ok);
            }
        }

        return resultString;
    }

    private void updateControls() {

        if (mApp.mCommSocket.isConnecting()) {

            mProgressBar.setVisibility(View.VISIBLE);
            mTextViewConnecting.setVisibility(View.VISIBLE);
            mTextViewPlayerName.setEnabled(false);
            mButtonConnect.setEnabled(false);

            mProgressBar.setMax(MAX_TIMEOUT);
            mProgressBar.setProgress(mLoginViewModel.mElapsedTime);
        }
        else {

            mProgressBar.setVisibility(View.INVISIBLE);
            mTextViewConnecting.setVisibility(View.INVISIBLE);
            mTextViewPlayerName.setEnabled(true);
            mButtonConnect.setEnabled(true);
        }
    }

    private void processMessage(Message msg) {

        if (msg.mId==MsgId.GAME_INFO) {

            Game game=new Game();

            game.mMyPlayerName=mTextViewPlayerName.getText().toString();

            game.processGameInfoMessage(msg);

            DominooApplication app=(DominooApplication)getApplication();
            app.setGame(game);
            app.setCommSocket(mApp.mCommSocket);

            switch(game.mStatus) {

                case NOT_STARTED:
                case RUNNING:
                case FINISHED:
                case CANCELLED:

                    // Launch Game Management Activity...
                    Intent intent = new Intent(this, GameManagementActivity.class);
                    startActivity(intent);
                    break;

                default:

                    // Unknown game status

                    Toast toast = Toast.makeText(this,
                            R.string.unknown_game_status,
                            Toast.LENGTH_LONG);

                    toast.setGravity(Gravity.CENTER, 0, 0);

                    toast.show();
            }
        }
        else if (msg.mId== Message.MsgId.GAME_TILE_INFO) {

            // Do nothing with "Game Tile Info"...
        }
        else if (msg.mId== Message.MsgId.ROUND_INFO) {

            // Do nothing with "Round Info"...
        }
        else if (msg.mId== Message.MsgId.BOARD_TILE_INFO1) {

            // Do nothing with "Board Tile Info 1"...
        }
        else if (msg.mId== Message.MsgId.BOARD_TILE_INFO2) {

            // Do nothing with "Board Tile Info 2"...
        }
        else {

            Log.e("DomLog", "LoginActivity.processMessage() unknown message Id="+msg.mId);
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
        mApp.sendLoginMessage(mTextViewPlayerName.getText().toString());
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

        Toast toast = Toast.makeText(this,
                "GameManagementActivity.onDataReadError(): "+errorMessage,
                Toast.LENGTH_LONG);

        toast.setGravity(Gravity.CENTER, 0, 0);

        toast.show();
    }

    @Override
    public void onDataWriteError(String errorMessage) {

        Toast toast = Toast.makeText(this,
                "GameManagementActivity.onDataWriteError(): "+errorMessage,
                Toast.LENGTH_LONG);

        toast.setGravity(Gravity.CENTER, 0, 0);

        toast.show();
    }

    @Override
    public void onDataSent() {

    }

    @Override
    public void onSocketClosed() {

        Toast toast=Toast.makeText(this,
                "GameBoardActivity.onSocketClosed()",
                Toast.LENGTH_SHORT);

        toast.setGravity(Gravity.CENTER,0, 0);

        toast.show();
    }
}
