package org.dominoo;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener,
        TextToSpeech.OnInitListener {

    private EditText mEditTextServerAddress;
    private EditText mEditTextServerPort;
    private Button mButtonResetDefaultValues;
    private CheckBox mCheckboxAllowLaunchGames;

    private DominooApplication mApp = null;

    private TextToSpeech mTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_settings);

        mApp = (DominooApplication)getApplication();

        mEditTextServerAddress = findViewById(R.id.editTextServerAddress);
        mEditTextServerAddress.setText(mApp.mServerAddress);

        mEditTextServerPort = findViewById(R.id.editTextServerPort);
        mEditTextServerPort.setText(Integer.toString(mApp.mServerPort));

        mCheckboxAllowLaunchGames = findViewById(R.id.checkBoxAllowLaunchGames);
        mCheckboxAllowLaunchGames.setChecked(mApp.mAllowLaunchGames);

        mButtonResetDefaultValues = findViewById(R.id.buttonResetDefaultValues);
        mButtonResetDefaultValues.setOnClickListener(this);

        /*
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {

                if (status==TextToSpeech.SUCCESS) {

                    mTTS.setLanguage(Locale.ENGLISH);
                }
            }
        });
        */
    }

    @Override
    public void onResume() {
        super.onResume();



    }

    @Override
    public void onClick(View v) {

        if (v == mButtonResetDefaultValues) {

            mEditTextServerAddress.setText(mApp.DEFAULT_SERVER_ADDRESS);

            mEditTextServerPort.setText(Integer.toString(mApp.DEFAULT_SERVER_PORT));

            mCheckboxAllowLaunchGames.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {

        String serverAddress = mEditTextServerAddress.getText().toString();

        if (serverAddress.isEmpty()) {

            Toast.makeText(this, R.string.invalid_server_address, Toast.LENGTH_LONG).show();

            return;
        }

        mApp.mServerAddress = serverAddress;

        try {

            mApp.mServerPort = Integer.parseInt(mEditTextServerPort.getText().toString());
        }
        catch (NumberFormatException e) {

            Toast.makeText(this, R.string.invalid_port_number, Toast.LENGTH_LONG).show();

            return;
        }

        mApp.mAllowLaunchGames = mCheckboxAllowLaunchGames.isChecked();

        super.onBackPressed();
    }

    @Override
    public void onInit(int status) {

        String text;

        if (status == TextToSpeech.SUCCESS) {

            text = "TextToSpeech.onInit() status=SUCCESS";
        }
        else if (status == TextToSpeech.ERROR) {

            text = "TextToSpeech.onInit() status=ERROR";
        }
        else {

            text = "Other";
        }

        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        mTTS.setLanguage(Locale.ENGLISH);
    }
}

