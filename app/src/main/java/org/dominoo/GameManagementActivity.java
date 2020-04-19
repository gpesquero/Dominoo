package org.dominoo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class GameManagementActivity extends AppCompatActivity implements View.OnClickListener {

    TextView mTextViewUserName;
    ImageButton mButtonExit;

    private ConnectionViewModel mConnectionViewModel=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_management);

        mConnectionViewModel= ViewModelProviders.of(this).get(ConnectionViewModel.class);

        CustomApplication app=(CustomApplication)getApplication();
        Session session=app.getSession();

        mTextViewUserName=findViewById(R.id.textViewUserName);
        mTextViewUserName.setText(getString(R.string.player_name_)+" "+session.mPlayerName);

        mButtonExit=findViewById(R.id.imageButtonExit);
        mButtonExit.setOnClickListener(this);
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

        // set title
        //alertDialogBuilder.setTitle(R.string.exit);

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.do_you_want_to_exit_)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        GameManagementActivity.this.finish();
                    }
                })
                .setNegativeButton(android.R.string.no,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
