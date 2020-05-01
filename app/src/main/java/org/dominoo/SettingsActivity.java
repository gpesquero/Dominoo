package org.dominoo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    EditText mEditTextServerAddress;
    EditText mEditTextServerPort;
    Button mButtonResetDefaultValues;
    CheckBox mCheckboxAllowLaunchGames;

    DominooApplication mApp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_settings);

        mApp = (DominooApplication)getApplication();

        mEditTextServerAddress = findViewById(R.id.editTextServerAddress);
        mEditTextServerAddress.setText(mApp.mServerAddr);

        mEditTextServerPort = findViewById(R.id.editTextServerPort);
        mEditTextServerPort.setText(Integer.toString(mApp.mServerPort));

        mCheckboxAllowLaunchGames = findViewById(R.id.checkBoxAllowLaunchGames);
        mCheckboxAllowLaunchGames.setChecked(mApp.mAllowLaunchGames);

        mButtonResetDefaultValues = findViewById(R.id.buttonResetDefaultValues);
        mButtonResetDefaultValues.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v == mButtonResetDefaultValues) {

            mEditTextServerAddress.setText(mApp.DEFAULT_SERVER_ADDRESS);

            mEditTextServerAddress.setText(Integer.toString(mApp.DEFAULT_SERVER_PORT));

            mCheckboxAllowLaunchGames.setEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mApp.mAllowLaunchGames = mCheckboxAllowLaunchGames.isChecked();
    }
}

