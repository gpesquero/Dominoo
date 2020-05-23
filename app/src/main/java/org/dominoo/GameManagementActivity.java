package org.dominoo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class GameManagementActivity extends AppCompatActivity implements View.OnClickListener,
        CommSocket.CommSocketListener, DropdownButton.OnSelectionChangedListener {

    TextView mTextViewUserName;

    Button mButtonLaunchGame;

    ImageButton mButtonExit;

    DropdownButton mDropdownButtonPartner;
    DropdownButton mDropdownButtonOpponentRight;
    DropdownButton mDropdownButtonOpponentLeft;

    DominooApplication mApp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_management);

        Log.d("DomLog", "GameManagementActivity.onCreate()");

        mApp = (DominooApplication)getApplication();

        mDropdownButtonPartner = findViewById(R.id.dropdownButtonPartner);
        mDropdownButtonPartner.setOnSelectionChangedListener(this);

        mDropdownButtonOpponentRight = findViewById(R.id.dropdownButtonOpponentRight);
        mDropdownButtonOpponentRight.setOnSelectionChangedListener(this);

        mDropdownButtonOpponentLeft = findViewById(R.id.dropdownButtonOpponentLeft);
        mDropdownButtonOpponentLeft.setOnSelectionChangedListener(this);

        mTextViewUserName=findViewById(R.id.textViewPlayerName);
        mTextViewUserName.setText(mApp.mGame.mMyPlayerName);

        mButtonLaunchGame=findViewById(R.id.buttonLaunchGame);
        mButtonLaunchGame.setOnClickListener(this);

        mButtonExit=findViewById(R.id.imageButtonExit);
        mButtonExit.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("DomLog", "GameManagementActivity.onResume()");

        if (mApp.mCommSocket == null) {

            // Socket is not valid
            // Finish the activity
            finish();

            //mApp.mCommSocket = new CommSocket();

            return;
        }

        mApp.mCommSocket.setCommSocketListener(this);

        if (!mApp.sendRequestGameInfoMessage()) {

            // Error sending message. Close activity...
            closeActivity();
        }

        updateControls();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("DomLog", "GameManagementActivity.onPause()");

        if (mApp.mCommSocket != null) {

            mApp.mCommSocket.setCommSocketListener(null);
        }
    }

    @Override
    public void onBackPressed () {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("DomLog", "GameManagementActivity.onDestroy()");
    }

    @Override
    public void onClick(View view) {

        if (view == mButtonExit) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            // set dialog message
            alertDialogBuilder
                    .setMessage(R.string.do_you_want_to_exit_)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, close
                            // current activity

                            if (!mApp.sendLogoutMessage()) {

                                // Error sending message. Close activity...
                                closeActivity();
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    });

            // Create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // Show it
            alertDialog.show();
        }
        else if (view == mButtonLaunchGame) {

            if (!mApp.sendLaunchGameMessage()) {

                // Error sending message. Close activity...
                closeActivity();
            }
        }
    }

    private void closeActivity() {

        mApp.mGame = null;

        if (mApp.mCommSocket != null) {

            mApp.mCommSocket.close();
            mApp.mCommSocket.setCommSocketListener(null);
        }

        mApp.mCommSocket = null;

        // Finish the activity
        finish();
    }

    private void updateControls() {

        ArrayList<String> otherPlayers = mApp.mGame.getOtherPlayers();

        mDropdownButtonPartner.setItemsList(otherPlayers);
        mDropdownButtonPartner.setSelection(mApp.mGame.getPartnerName());

        mDropdownButtonOpponentLeft.setItemsList(otherPlayers);
        mDropdownButtonOpponentLeft.setSelection(mApp.mGame.getLeftOpponentName());

        mDropdownButtonOpponentRight.setItemsList(otherPlayers);
        mDropdownButtonOpponentRight.setSelection(mApp.mGame.getRightOpponentName());

        mButtonLaunchGame.setEnabled(mApp.mAllowLaunchGames);
    }

    @Override
    public void onConnectionEstablished() {
    }

    @Override
    public void onConnectionError(String errorMessage) {

        Toast toast = Toast.makeText(this,
                "GameManagementActivity.onConnectionError(): "+errorMessage,
                Toast.LENGTH_LONG);

        toast.setGravity(Gravity.CENTER, 0, 0);

        toast.show();

        closeActivity();
    }

    @Override
    public void onDataReceived(String data) {

        /*
        Toast toast=Toast.makeText(this, "Received data: "+data, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0, 0);
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

        if (!mApp.sendRequestGameInfoMessage()) {

            closeActivity();
        }
    }

    @Override
    public void onDataWriteError(String errorMessage) {

        Toast toast = Toast.makeText(this,
                "GameManagementActivity.onDataWriteError(): "+errorMessage,
                Toast.LENGTH_LONG);

        toast.setGravity(Gravity.CENTER, 0, 0);

        toast.show();

        closeActivity();
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

        closeActivity();
    }

    private void processMessage(Message msg) {

        if (msg.mId == Message.MsgId.GAME_INFO) {

            //Log.i("DomLog", "GameManagementActivity. Received Game Info Message");

            mApp.mGame.processGameInfoMessage(msg);

            if (mApp.mGame.mAllPlayerNames.indexOf(mApp.mGame.mMyPlayerName) <0 ) {

                // We have not found our player name in the player list
                //Log.i("DomLog", "Player name not found in player list. Close Activity");

                // We have to close the Activity
                closeActivity();
            }
            else {

                if (mApp.mGame.mStatus == Game.Status.RUNNING) {

                    Intent intent=new Intent(this, GameBoardActivity.class);
                    startActivity(intent);
                }

                // Update UI control
                updateControls();
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

            Log.d("DomLog", "GameManagementActivity.processMessage() unknown message Id="+msg.mId);
        }
    }

    @Override
    public void onSelectionChanged(View view, int position, String selectedItemText) {

        int playerPos = mApp.mGame.mAllPlayerNames.indexOf(mApp.mGame.mMyPlayerName);

        int delta;

        String selectedName;

        if (view == mDropdownButtonOpponentRight) {

            delta = 1;
            selectedName = mDropdownButtonOpponentRight.getSelectedItemText();
        }
        else if (view == mDropdownButtonPartner) {

            delta = 2;
            selectedName = mDropdownButtonPartner.getSelectedItemText();
        }
        else if (view == mDropdownButtonOpponentLeft) {

            delta = 3;
            selectedName = mDropdownButtonOpponentLeft.getSelectedItemText();
        }
        else {

            return;
        }

        int index = (playerPos + delta) % 4;

        if (!mApp.sendMovePlayerMessage(selectedName, index)) {

            // Error sending message. Close activity...
            closeActivity();
        }
    }
}
