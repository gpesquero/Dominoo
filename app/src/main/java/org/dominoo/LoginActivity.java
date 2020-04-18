package org.dominoo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    SocketStatus mSocketStatus=null;

    private LoginViewModel mLoginViewModel=null;

    private final int MAX_TIMEOUT=10000;    // 10 seconds

    private final static String SERVER_ADDRESS="192.168.1.146";
    private final static int SERVER_PORT=52301;

    // Create the observer which updates the UI.
    final Observer<SocketStatus> mSocketObserver = new Observer<SocketStatus>() {

        @Override
        public void onChanged(@Nullable final SocketStatus newSocketStatus) {

            onSocketStatusChanged(newSocketStatus);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mLoginViewModel=ViewModelProviders.of(this).get(LoginViewModel.class);

        mPrefs=getPreferences(Context.MODE_PRIVATE);

        mProgressBar=findViewById(R.id.progressBar);
        mProgressBar.setMax(MAX_TIMEOUT);

        mTextViewConnecting=findViewById(R.id.textViewConnecting);

        mEditTextPlayerName=findViewById(R.id.editTextPlayerName);

        String playerName=mPrefs.getString(getString(R.string.key_player_name), getString(R.string.default_player_name));

        mEditTextPlayerName.setText(playerName);

        mButtonConnect=findViewById(R.id.buttonConnect);
        mButtonConnect.setOnClickListener(this);

        //boolean isConnecting;
        //int progress;

        LiveData<SocketStatus> ldSocketStatus=mLoginViewModel.attach();

        if (ldSocketStatus==null) {

            mSocketStatus=new SocketStatus();

            mSocketStatus.mStatus=SocketStatus.Status.IDLE;
        }
        else {

            mSocketStatus=ldSocketStatus.getValue();

            ldSocketStatus.observe(this, mSocketObserver);
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

            LiveData<SocketStatus> ldSocket=mLoginViewModel.connectToServer(
                    SERVER_ADDRESS, SERVER_PORT, MAX_TIMEOUT);

            mSocketStatus=ldSocket.getValue();

            ldSocket.observe(this, mSocketObserver);

            updateControls();
        }
    }

    private void updateControls() {

        if (mSocketStatus.mStatus==SocketStatus.Status.CONNECTING) {

            mProgressBar.setVisibility(View.VISIBLE);
            mTextViewConnecting.setVisibility(View.VISIBLE);
            mEditTextPlayerName.setEnabled(false);
            mButtonConnect.setEnabled(false);

            mProgressBar.setMax(MAX_TIMEOUT);
            mProgressBar.setProgress(mSocketStatus.mElapsedTime);
        }
        else {

            mProgressBar.setVisibility(View.INVISIBLE);
            mTextViewConnecting.setVisibility(View.INVISIBLE);
            mEditTextPlayerName.setEnabled(true);
            mButtonConnect.setEnabled(true);
        }
    }

    private void onSocketStatusChanged(SocketStatus newSocketStatus)  {

        mSocketStatus=newSocketStatus;

        String toastText=null;

        switch(mSocketStatus.mStatus) {

            case ERROR:

                Log.d("Prueba", "onSocketStatusChanged() ERROR");

                toastText=getString(R.string.error_while_connecting)+" ("+
                    mSocketStatus.mSocketErrorMessage+")";

                mLoginViewModel.reset(this);

                mSocketStatus=new SocketStatus();
                mSocketStatus.mStatus=SocketStatus.Status.IDLE;

                break;

            case CONNECTED:

                Log.d("Prueba", "onSocketStatusChanged() CONNECTED");

                toastText=getString(R.string.connection_successful);

                Intent intent=new Intent(this, GameManagementActivity.class);
                startActivity(intent);

                break;

            case CONNECTING:

                Log.d("Prueba", "onSocketStatusChanged() CONNECTING ("+
                        mSocketStatus.mElapsedTime+")");

                break;

            default:

                Log.d("Prueba", "onSocketStatusChanged() DEFAULT");
                break;
        }

        updateControls();

        if (toastText!=null) {

            Toast toast = Toast.makeText(this, toastText, LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
}
