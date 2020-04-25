package org.dominoo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class GameManagementActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener, CommSocket.CommSocketListener {

    TextView mTextViewUserName;
    ImageButton mButtonExit;

    Spinner mSpinnerPartner;
    Spinner mSpinnerOpponentLeft;
    Spinner mSpinnerOpponentRight;

    ArrayAdapter<String> mSpinnerAdapter = null;

    private Session mSession = null;

    private int mSpinnerEventCount = 3;

    private CommSocket mCommSocket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_management);

        DominooApplication app=(DominooApplication)getApplication();
        mSession=app.getSession();

        mCommSocket=app.getCommSocket();

        if (mCommSocket == null) {

            mCommSocket = new CommSocket();
        }

        mCommSocket.setCommSocketListener(this);

        String[] otherPlayers = mSession.getOtherPlayers();

        mSpinnerAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, R.id.textview, otherPlayers);

        mSpinnerPartner=findViewById(R.id.spinnerPartner);
        mSpinnerPartner.setAdapter(mSpinnerAdapter);
        mSpinnerPartner.setOnItemSelectedListener(this);

        mSpinnerOpponentLeft=findViewById(R.id.spinnerOpponentLeft);
        mSpinnerOpponentLeft.setAdapter(mSpinnerAdapter);
        mSpinnerOpponentLeft.setOnItemSelectedListener(this);

        mSpinnerOpponentRight=findViewById(R.id.spinnerOpponentRight);
        mSpinnerOpponentRight.setAdapter(mSpinnerAdapter);
        mSpinnerOpponentRight.setOnItemSelectedListener(this);

        mTextViewUserName=findViewById(R.id.textViewUserName);
        mTextViewUserName.setText(mSession.mPlayerName);

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

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.do_you_want_to_exit_)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity

                        sendCloseSession();
                    }
                })
                .setNegativeButton(android.R.string.no,new DialogInterface.OnClickListener() {
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

    void sendCloseSession() {

        // Create <Close Session> message
        String msg = CommProtocol.createMsgCloseSession(mSession.mPlayerName);

        // Send the message to the server
        mCommSocket.sendMessage(msg);
    }

    private void closeActivity() {

        DominooApplication app=(DominooApplication)getApplication();

        mSession = null;
        app.setSession(null);

        mCommSocket.close();
        mCommSocket = null;
        app.setCommSocket(null);

        // Finish the activity
        finish();
    }

    private void updateControls() {

        int pos;

        int playerPos = mSession.mAllPlayerNames.indexOf(mSession.mPlayerName);

        int partnerPos = (playerPos + 2) % 4;
        pos = mSpinnerAdapter.getPosition(mSession.mAllPlayerNames.get(partnerPos));
        mSpinnerPartner.setSelection(pos);

        int leftOpponentPos = (playerPos + 3) % 4;
        pos = mSpinnerAdapter.getPosition(mSession.mAllPlayerNames.get(leftOpponentPos));
        mSpinnerOpponentLeft.setSelection(pos);

        int rightOpponentPos = (playerPos + 1) % 4;
        pos = mSpinnerAdapter.getPosition(mSession.mAllPlayerNames.get(rightOpponentPos));
        mSpinnerOpponentRight.setSelection(pos);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (mSpinnerEventCount != 0) {

            mSpinnerEventCount--;
            return;
        }

        int playerPos = mSession.mAllPlayerNames.indexOf(mSession.mPlayerName);

        int delta;

        String selectedName;

        if (parent == mSpinnerOpponentRight) {

            delta = 1;
            selectedName = (String) mSpinnerOpponentRight.getSelectedItem();
        }
        else if (parent == mSpinnerPartner) {

            delta = 2;
            selectedName = (String) mSpinnerPartner.getSelectedItem();
        }
        else if (parent == mSpinnerOpponentLeft) {

            delta = 3;
            selectedName = (String) mSpinnerOpponentLeft.getSelectedItem();
        }
        else {

            return;
        }

        int index = (playerPos + delta) % 4;

        String msg = CommProtocol.createMsgMovePlayer(selectedName, index);

        mCommSocket.sendMessage(msg);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

        if (msg.mId == Message.MsgId.SESSION_INFO) {

            Log.i("DomLog", "GameManagementActivity. Received Session Info Message");

            mSession.mAllPlayerNames.clear();

            mSession.mAllPlayerNames.add(msg.getArgument("player0"));
            mSession.mAllPlayerNames.add(msg.getArgument("player1"));
            mSession.mAllPlayerNames.add(msg.getArgument("player2"));
            mSession.mAllPlayerNames.add(msg.getArgument("player3"));

            if (mSession.mAllPlayerNames.indexOf(mSession.mPlayerName) <0 ) {

                // We have not found our player name in the player list
                Log.i("DomLog", "Player name not found in palyer list. Close Activity");

                // We have to close the Activity
                closeActivity();
            }
            else {

                // Update UI control
                updateControls();
            }
        }
    }
}
