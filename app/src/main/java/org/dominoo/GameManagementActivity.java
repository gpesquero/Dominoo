package org.dominoo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

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

        mApp = (DominooApplication)getApplication();

        if (mApp.mCommSocket == null) {

            mApp.mCommSocket = new CommSocket();
        }

        mApp.mCommSocket.setCommSocketListener(this);

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

        updateControls();
    }

    @Override
    public void onBackPressed () {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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

                            sendLogoutMessage();
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

            sendLaunchGameMessage();
        }
    }

    void sendLogoutMessage() {

        // Create <Logout> message
        String msg = CommProtocol.createMsgLogout(mApp.mGame.mMyPlayerName);

        // Send the message to the server
        mApp.mCommSocket.sendMessage(msg);
    }

    void sendLaunchGameMessage() {

        // Create <Launch Game> message
        String msg = CommProtocol.createMsgLaunchGame(mApp.mGame.mMyPlayerName);

        // Send the message to the server
        mApp.mCommSocket.sendMessage(msg);
    }

    private void closeActivity() {

        //DominooApplication app=(DominooApplication)getApplication();

        mApp.mGame = null;
        mApp.setGame(null);

        mApp.mCommSocket.close();
        mApp.mCommSocket.setCommSocketListener(null);
        mApp.mCommSocket = null;
        mApp.setCommSocket(null);

        // Finish the activity
        finish();
    }

    private void updateControls() {

        int pos;

        //int playerPos = mApp.mGame.mAllPlayerNames.indexOf(mApp.mGame.mPlayerName);

        ArrayList<String> otherPlayers = mApp.mGame.getOtherPlayers();

        //int partnerPos = (playerPos + 2) % 4;
        mDropdownButtonPartner.setItemsList(otherPlayers);
        //mDropdownButtonPartner.setSelection(mApp.mGame.mAllPlayerNames.get(partnerPos));
        mDropdownButtonPartner.setSelection(mApp.mGame.getPartnerName());

        //int leftOpponentPos = (playerPos + 3) % 4;
        mDropdownButtonOpponentLeft.setItemsList(otherPlayers);
        //mDropdownButtonOpponentLeft.setSelection(mApp.mGame.mAllPlayerNames.get(leftOpponentPos));
        mDropdownButtonOpponentLeft.setSelection(mApp.mGame.getLeftOpponentName());

        //int rightOpponentPos = (playerPos + 1) % 4;
        mDropdownButtonOpponentRight.setItemsList(otherPlayers);
        //mDropdownButtonOpponentRight.setSelection(mApp.mGame.mAllPlayerNames.get(rightOpponentPos));
        mDropdownButtonOpponentRight.setSelection(mApp.mGame.getRightOpponentName());

        mButtonLaunchGame.setEnabled(mApp.mAllowLaunchGames);
    }

    @Override
    public void onConnectionEstablished() {
    }

    @Override
    public void onConnectionError(String errorMessage) {
    }

    @Override
    public void onConnectionLost() {
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
    }

    @Override
    public void onDataSent() {
    }

    @Override
    public void onSocketClosed() {
    }

    private void processMessage(Message msg) {

        if (msg.mId == Message.MsgId.GAME_INFO) {

            Log.i("DomLog", "GameManagementActivity. Received Game Info Message");

            mApp.mGame.mAllPlayerNames.clear();

            mApp.mGame.mAllPlayerNames.add(msg.getArgument("player0"));
            mApp.mGame.mAllPlayerNames.add(msg.getArgument("player1"));
            mApp.mGame.mAllPlayerNames.add(msg.getArgument("player2"));
            mApp.mGame.mAllPlayerNames.add(msg.getArgument("player3"));

            String statusText=msg.getArgument("status");

            if (statusText == null) {

                mApp.mGame.mStatus= Game.Status.NOT_STARTED;
            }
            else if (statusText.compareTo("notStarted")==0) {

                mApp.mGame.mStatus= Game.Status.NOT_STARTED;
            }
            else if (statusText.compareTo("running")==0) {

                mApp.mGame.mStatus= Game.Status.RUNNING;
            }
            else {

                mApp.mGame.mStatus= Game.Status.NOT_STARTED;
            }

            if (mApp.mGame.mAllPlayerNames.indexOf(mApp.mGame.mMyPlayerName) <0 ) {

                // We have not found our player name in the player list
                Log.i("DomLog", "Player name not found in player list. Close Activity");

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

        String msg = CommProtocol.createMsgMovePlayer(selectedName, index);

        mApp.mCommSocket.sendMessage(msg);
    }
}
