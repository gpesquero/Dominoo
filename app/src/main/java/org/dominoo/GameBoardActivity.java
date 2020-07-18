package org.dominoo;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class GameBoardActivity extends AppCompatActivity implements
        CommSocket.CommSocketListener, PlayerTilesView.OnTileSelectedListener,
        GameBoardView.OnGameBoardViewListener, View.OnClickListener,
        TextToSpeech.OnInitListener {

    DominooApplication mApp = null;

    GameBoardView mGameBoardView;
    PlayerTilesView mPlayerTilesView;

    TextView mTextViewPair1XPoints;
    TextView mTextViewPair2XPoints;

    TextView mTextViewPlayer0Name;
    TextView mTextViewPlayer1Name;
    TextView mTextViewPlayer2Name;
    TextView mTextViewPlayer3Name;

    Button mButtonPassTurn;

    private TextToSpeech mTextToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        Log.d("DomLog", "GameBoardActivity.onCreate()");

        mApp = (DominooApplication)getApplication();

        mGameBoardView = findViewById(R.id.viewGameBoard);
        mGameBoardView.setGameBoardViewListener(this);

        mPlayerTilesView = findViewById(R.id.viewPlayerTiles);
        mPlayerTilesView.setOnTileSelectedListener(this);

        mTextViewPair1XPoints = findViewById(R.id.textViewPair1XPoints);
        //mTextViewPair1XPoints.setOnClickListener(this);

        mTextViewPair2XPoints = findViewById(R.id.textViewPair2XPoints);
        //mTextViewPair2XPoints.setOnClickListener(this);

        mTextViewPlayer0Name = findViewById(R.id.textViewPlayer0Name);
        mTextViewPlayer1Name = findViewById(R.id.textViewPlayer1Name);
        mTextViewPlayer2Name = findViewById(R.id.textViewPlayer2Name);
        mTextViewPlayer3Name = findViewById(R.id.textViewPlayer3Name);

        mButtonPassTurn = findViewById(R.id.buttonPassTurn);
        mButtonPassTurn.setOnClickListener(this);
        mButtonPassTurn.setEnabled(false);

        mTextToSpeech = new TextToSpeech(this, this);

        updateControls();

        if (!mApp.sendMessageRequestTileInfo()) {

            // Error sending message. Finish the activity...
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("DomLog", "GameBoardActivity.onResume()");

        if (mApp.mCommSocket == null) {

            finish();

            return;
        }

        mApp.mCommSocket.setCommSocketListener(this);

        if (!mApp.sendRequestGameInfoMessage()) {

            // Error sending message. Finish the activity...
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("DomLog", "GameBoardActivity.onPause()");

        if (mApp.mCommSocket != null) {

            mApp.mCommSocket.setCommSocketListener(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("DomLog", "GameBoardActivity.onDestroy()");

        if (mTextToSpeech != null) {

            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
    }

    @Override
    public void onBackPressed () {

    }

    private void updateControls() {

        mGameBoardView.mDrawExitButton = mApp.mAllowLaunchGames;

        mGameBoardView.mSilentModeOn = mApp.mSilentModeOn;

        mGameBoardView.setTurnPlayer(mApp.mGame.mTurnPlayerPos);

        mGameBoardView.setHandPlayer(mApp.mGame.mHandPlayerPos);

        mGameBoardView.setPlayerName(mApp.mGame.mMyPlayerName);
        mGameBoardView.setPartnerName(mApp.mGame.getPartnerName());
        mGameBoardView.setLeftOpponentName(mApp.mGame.getLeftOpponentName());
        mGameBoardView.setRightOpponentName(mApp.mGame.getRightOpponentName());

        mGameBoardView.invalidate();

        mPlayerTilesView.invalidate();

        mTextViewPlayer0Name.setText(mApp.mGame.getPlayerName(0));
        mTextViewPlayer1Name.setText(mApp.mGame.getPlayerName(1));
        mTextViewPlayer2Name.setText(mApp.mGame.getPlayerName(2));
        mTextViewPlayer3Name.setText(mApp.mGame.getPlayerName(3));

        mTextViewPair1XPoints.setText(getString(R.string.pair_x_y_pts, 1,
                mApp.mGame.getPair1Points()));

        mTextViewPair2XPoints.setText(getString(R.string.pair_x_y_pts, 2,
                mApp.mGame.getPair2Points()));

        checkPassTurnButton();
    }

    @Override
    public void onConnectionEstablished() {

    }

    @Override
    public void onConnectionError(String errorMessage) {

        Toast toast=Toast.makeText(this,
                "GameBoardActivity.onConnectionError(): +"+errorMessage,
                Toast.LENGTH_SHORT);

        toast.setGravity(Gravity.CENTER,0, 0);

        toast.show();
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

        Toast toast=Toast.makeText(this,
                "GameBoardActivity.onDataReadError(): +"+errorMessage,
                Toast.LENGTH_SHORT);

        toast.setGravity(Gravity.CENTER,0, 0);

        toast.show();
    }

    @Override
    public void onDataWriteError(String errorMessage) {

        Toast toast=Toast.makeText(this,
                "GameBoardActivity.onDataWriteError(): +"+errorMessage,
                Toast.LENGTH_SHORT);

        toast.setGravity(Gravity.CENTER,0, 0);

        toast.show();

        // Finish activity
        finish();
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

        // Finish activity
        finish();
    }

    private void processMessage(Message msg) {

        if (msg.mId == Message.MsgId.GAME_INFO) {

            //Log.i("DomLog", "GameManagementActivity. Received Game Info Message");

            mApp.mGame.processGameInfoMessage(msg);

            if (mApp.mGame.mAllPlayerNames.indexOf(mApp.mGame.mMyPlayerName) < 0 ) {

                // We have not found our player name in the player list
                //Log.i("DomLog", "Player name not found in player list. Close Activity");

                // We have to finish the activity
                finish();
            }
            else if (mApp.mGame.mStatus == Game.Status.CANCELLED) {

                showGameCancelledDialog();
            }
            else {

                mApp.mGame.mPair1Points = Integer.parseInt(msg.getArgument("pair1Points"));
                mApp.mGame.mPair2Points = Integer.parseInt(msg.getArgument("pair2Points"));

                // Update UI control
                updateControls();
            }
        }
        else if (msg.mId == Message.MsgId.GAME_TILE_INFO) {

            int turnPlayerPos = Integer.parseInt(msg.getArgument("turnPlayer"));

            mApp.mGame.mTurnPlayerPos = mApp.mGame.getPlayerPosition(turnPlayerPos);

            mGameBoardView.setTurnPlayer(mApp.mGame.mTurnPlayerPos);

            mPlayerTilesView.setTurnPlayer(mApp.mGame.mTurnPlayerPos);

            int handPlayerPos = Integer.parseInt(msg.getArgument("handPlayer"));

            mApp.mGame.mHandPlayerPos = mApp.mGame.getPlayerPosition(handPlayerPos);

            mGameBoardView.setHandPlayer(mApp.mGame.mHandPlayerPos);

            for (int i=0; i<Game.MAX_PLAYERS; i++) {

                String keyString = "player"+i;

                int playerTileCount = Integer.parseInt(msg.getArgument(keyString));

                Game.PlayerPos playerPos = mApp.mGame.getPlayerPosition(i);

                switch(playerPos) {

                    case PLAYER:
                        break;

                    case PARTNER:
                        mGameBoardView.setPartnerTileCount(playerTileCount);
                        mGameBoardView.clearHighlights();
                        break;

                    case LEFT_OPPONENT:
                        mGameBoardView.setLeftOpponentTileCount(playerTileCount);
                        mGameBoardView.clearHighlights();
                        break;

                    case RIGHT_OPPONENT:
                        mGameBoardView.setRightOpponentTileCount(playerTileCount);
                        mGameBoardView.clearHighlights();
                        break;

                    default:
                        Log.e("DomLog", "Msg.GameTileInfo.processMessage() Unknown playerpos");
                        break;
                }
            }

            updateControls();
        }
        else if (msg.mId == Message.MsgId.PLAYER_TILE_INFO) {

            String playerName = msg.getArgument("playerName");

            //Log.i("DomLog", "Received Player Tile Info for player <"+playerName+">");

            int tileCount = Integer.parseInt(msg.getArgument("tileCount"));

            //Log.i("DomLog", "Tile Count = "+tileCount);

            mApp.mGame.mMyPlayerTiles = new ArrayList<DominoTile>();

            for(int i=0; i<tileCount; i++) {

                String tileText = msg.getArgument("tile"+i);

                String n1 = tileText.substring(0, 1);

                int number1 = Integer.parseInt(n1);

                String n2 = tileText.substring(2);

                int number2 = Integer.parseInt(n2);

                DominoTile tile = new DominoTile(number1, number2);

                mApp.mGame.mMyPlayerTiles.add(tile);
            }

            DominoTile.sortTiles(mApp.mGame.mMyPlayerTiles);

            mPlayerTilesView.setTiles(mApp.mGame.mMyPlayerTiles);

            updateControls();
        }
        else if (msg.mId == Message.MsgId.BOARD_TILE_INFO1) {

            mApp.mGame.mForceDouble6Tile = Boolean.parseBoolean(msg.getArgument("forceDouble6Tile"));

            int boardTiles1Count = Integer.parseInt(msg.getArgument("tileCount"));

            mApp.mGame.mBoardTiles1 = new ArrayList<DominoTile>();

            for(int i=0; i<boardTiles1Count; i++) {

                String tileText = msg.getArgument("tile"+i);

                String n1 = tileText.substring(0, 1);

                int number1 = Integer.parseInt(n1);

                String n2 = tileText.substring(2);

                int number2 = Integer.parseInt(n2);

                DominoTile tile = new DominoTile(number1, number2);

                mApp.mGame.mBoardTiles1.add(tile);
            }

            mGameBoardView.setBoardTiles1(mApp.mGame.mBoardTiles1);
        }
        else if (msg.mId == Message.MsgId.BOARD_TILE_INFO2) {

            int boardTiles2Count = Integer.parseInt(msg.getArgument("tileCount"));

            mApp.mGame.mBoardTiles2 = new ArrayList<DominoTile>();

            for(int i=0; i<boardTiles2Count; i++) {

                String tileText = msg.getArgument("tile"+i);

                String n1 = tileText.substring(0, 1);

                int number1 = Integer.parseInt(n1);

                String n2 = tileText.substring(2);

                int number2 = Integer.parseInt(n2);

                DominoTile tile = new DominoTile(number1, number2);

                mApp.mGame.mBoardTiles2.add(tile);
            }

            mGameBoardView.setBoardTiles2(mApp.mGame.mBoardTiles2);

            updateControls();
        }
        else if (msg.mId == Message.MsgId.ROUND_INFO) {

            mApp.mGame.processGameRoundMessage(msg);

            if ((mApp.mGame.mRoundStatus == Game.RoundStatus.CLOSED) ||
                    (mApp.mGame.mRoundStatus == Game.RoundStatus.WON)) {

                RoundEndDialog dialog = new RoundEndDialog(this,

                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();

                                if (!mApp.sendMessageRequestTileInfo()) {

                                    // Error sending message. Finish the activity...
                                    finish();

                                    return;
                                }

                                if (mApp.mGame.mStatus == Game.Status.FINISHED) {

                                    // Game has finished...
                                    // Show closing message and quit...

                                    showGameFinishedDialog();
                                }
                            }
                        });

                dialog.setGameInfo(mApp.mGame);

                String title = dialog.getTitleString();

                if (title != null) {

                    speak(title);
                }

                // Show the dialog
                dialog.show();
            }

            //dialog.setTitle(R.string.round_finished);

            /*
            dialog.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();

                            if (!mApp.sendMessageRequestTileInfo()) {

                                // Error sending message. Finish the activity...
                                finish();

                                return;
                            }

                            if (mApp.mGame.mStatus == Game.Status.FINISHED) {

                                // Game has finished...
                                // Show closing message and quit...

                                showGameFinishedDialog();
                            }
                        }
                    });

            // Show it
            dialog.show();
            */

            /*
            if (alertString != null) {

                speak(titleString);

                AlertDialog.Builder alertDialogBuilder = null;

                alertDialogBuilder = new AlertDialog.Builder(this);

                alertDialogBuilder
                        .setTitle(titleString)
                        .setMessage(alertString)
                        .setCancelable(false)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();

                                if (!mApp.sendMessageRequestTileInfo()) {

                                    // Error sending message. Finish the activity...
                                    finish();

                                    return;
                                }

                                if (mApp.mGame.mStatus == Game.Status.FINISHED) {

                                    // Game has finished...
                                    // Show closing message and quit...

                                    showGameFinishedDialog();
                                }
                            }
                        });

                // Create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // Show it
                alertDialog.show();
            }
            */

            updateControls();
        }
        else if (msg.mId == Message.MsgId.PLAYED_TILE_INFO) {

            String playerName = msg.getArgument("playerName");

            if (playerName.compareTo(mApp.mGame.mMyPlayerName) == 0) {

                // Played tile info from ourselves
                // There is no need to print played tile info

                return;
            }

            String tileText = msg.getArgument("playedTile");

            String text;

            if (tileText.compareTo("null") == 0) {

                text = getString(R.string.x_has_passed, playerName);
            }
            else {

                text = getString(R.string.x_has_played_tile_y, playerName, tileText);
            }

            Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, -100);
            toast.show();

            if (mTextToSpeech != null) {

                text = prepareTextToSpeech(text);

                speak(text);
            }
        }
        else {

            Log.d("DomLog", "GameBoardActivity.processMessage() unknown message Id="+msg.mId);
        }
    }

    @Override
    public void onTileSelected(DominoTile selectedTile) {

        /*
        Log.i("DomLog", "onTileSelected() Tile: "+selectedTile.mNumber1+"-"+
                selectedTile.mNumber2);
        */

        mGameBoardView.onTileSelected(selectedTile, mApp.mGame.mForceDouble6Tile);

        mGameBoardView.invalidate();
    }

    @Override
    public void onTilePlayed(DominoTile tile, int boardSide) {

        /*
        Log.i("DomLog", "onTilePlayed() Tile: "+tile.mNumber1+"-"+
                tile.mNumber2+", boardSide="+boardSide);
        */

        if (!mApp.sendMessagePlayTile(tile, boardSide)) {

            // Error sending message. Finish the activity...
            finish();

            return;
        }
        else {

            mPlayerTilesView.clearSelection();

            mGameBoardView.clearHighlights();

            mApp.mGame.mTurnPlayerPos = Game.PlayerPos.RIGHT_OPPONENT;

            updateControls();
        }

        if (!mApp.mGame.removeTile(tile.mNumber1, tile.mNumber2)) {

            Log.e("DomLog", "removeTile(): Tile not found");
        }

        updateControls();
    }

    @Override
    public void onExitButtonClicked() {

        AlertDialog.Builder alertDialogBuilder = null;

        alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder
                .setTitle(R.string.game_cancellation)
                .setMessage(R.string.do_you_want_to_cancel_the_game_)
                .setCancelable(true)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        // Send the cancel game message
                        mApp.sendCancelGameMessage();

                        // Close the alert dialog
                        dialog.cancel();
                    }
                });

        // Create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // Show it
        alertDialog.show();
    }

    @Override
    public void onSilentModeClicked() {

        mApp.mSilentModeOn = !mApp.mSilentModeOn;

        updateControls();
    }

    private void checkPassTurnButton() {

        int endNumber1 = mApp.mGame.getEndNumber1();

        int endNumber2 = mApp.mGame.getEndNumber2();

        /*
        String text = "EndNumbers= "+endNumber1+" & "+endNumber2;
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
        */

        boolean activatePassTurn = true;

        if (mApp.mGame.mMyPlayerTiles == null) {

            activatePassTurn = false;
        }
        else if (mApp.mGame.mTurnPlayerPos != Game.PlayerPos.PLAYER) {

            activatePassTurn = false;
        }
        else if (endNumber1 < 0) {

            activatePassTurn = false;
        }
        else if (endNumber2 < 0) {

            activatePassTurn = false;
        }
        else {

            Iterator<DominoTile> iter = mApp.mGame.mMyPlayerTiles.iterator();

            while (iter.hasNext()) {

                DominoTile tile = iter.next();

                if (tile.contains(endNumber1)) {

                    activatePassTurn = false;

                    break;
                }

                if (tile.contains(endNumber2)) {

                    activatePassTurn = false;

                    break;
                }
            }
        }

        if (activatePassTurn) {

            mButtonPassTurn.setEnabled(true);
            mButtonPassTurn.setText(R.string.pass_turn);
            mButtonPassTurn.setTextColor(Color.YELLOW);
        }
        else {

            mButtonPassTurn.setEnabled(false);
            mButtonPassTurn.setText("");
            mButtonPassTurn.setTextColor(Color.WHITE);
        }
    }

    @Override
    public void onClick(View v) {

        if (v == mButtonPassTurn) {

            //Log.i("DomLog", "Pass turn");

            if (!mApp.sendMessagePlayTile(null, 0)) {

                // Error sending message. Finish the activity...
                finish();
            }
            else {

                mPlayerTilesView.clearSelection();

                mGameBoardView.clearHighlights();

                mApp.mGame.mTurnPlayerPos = Game.PlayerPos.RIGHT_OPPONENT;

                updateControls();
            }
        }
        else if (v == mTextViewPair1XPoints) {

            //String alertString = createRoundFinishedMessage(0);

            mApp.mGame.mWinnerPlayerPos = 1;

            RoundEndDialog dialog =new RoundEndDialog(this,

                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                        }
                    });

            dialog.setGameInfo(mApp.mGame);

            //dialog.setTitle(R.string.round_finished);

            // Show it
            dialog.show();
        }
        /*
        else if (v == mTextViewPair2XPoints) {

            String alertString = createRoundClosedMessage(0);

            AlertDialog.Builder alertDialogBuilder = null;

            alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder
                    .setTitle(R.string.round_closed)
                    .setMessage(alertString)
                    .setCancelable(false)
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                        }
                    });

            // Create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // Show it
            alertDialog.show();
        }
        */
    }

    /*
    private String createRoundFinishedMessage(int winnerPlayerPos) {

        int playersPoints[] = mApp.mGame.mPlayersPoints;
        int totalPoints =   playersPoints[0] + playersPoints[1] +
                            playersPoints[2] + playersPoints[3];

        int addingPoints = (totalPoints-1) / 10 +1;

        int winningPair = (winnerPlayerPos % 2) +1;

        String winnerName = mApp.mGame.getPlayerName(winnerPlayerPos);

        String text = getString(R.string.player_has_won_the_round, winnerName);

        text += "\n\n";

        text += getString(R.string.total_remaining_points_)+" ";

        text += getString(R.string.x_points, totalPoints)+ " (+"+addingPoints+")";

        text += "\n";

        text += "(";
        text += mApp.mGame.getPlayerName(0)+"="+playersPoints[0]+", ";
        text += mApp.mGame.getPlayerName(1)+"="+playersPoints[1]+", ";
        text += mApp.mGame.getPlayerName(2)+"="+playersPoints[2]+", ";
        text += mApp.mGame.getPlayerName(3)+"="+playersPoints[3];
        text += ")";

        text += "\n\n";

        text += getString(R.string.pair_1)+" ("+mApp.mGame.mAllPlayerNames.get(0)+"+"+
                mApp.mGame.mAllPlayerNames.get(2)+"): ";

        if (winningPair == 1) {

            int finalPair1Points = mApp.mGame.mPair1Points + addingPoints;

            text += String.valueOf(mApp.mGame.mPair1Points)+" + "+addingPoints+" = "+
                    getString(R.string.x_points, finalPair1Points);
        }
        else {

            text += getString(R.string.x_points, mApp.mGame.mPair1Points);
        }

        text += "\n";

        text += getString(R.string.pair_2)+" ("+mApp.mGame.mAllPlayerNames.get(1)+"+"+
                mApp.mGame.mAllPlayerNames.get(3)+"): ";

        if (winningPair == 2) {

            int finalPair2Points = mApp.mGame.mPair2Points + addingPoints;

            text += String.valueOf(mApp.mGame.mPair2Points)+" + "+addingPoints+" = "+
                    getString(R.string.x_points, finalPair2Points);
        }
        else {

            text += getString(R.string.x_points, mApp.mGame.mPair2Points);
        }

        text += "\n";

        return text;
    }
    */

    /*
    private String createRoundClosedMessage(int closerPlayerPos) {

        int playersPoints[] = mApp.mGame.mPlayersPoints;

        int pair1Points = playersPoints[0] + playersPoints[2];

        int pair2Points = playersPoints[1] + playersPoints[3];

        int totalPoints = pair1Points + pair2Points;

        int addingPoints = (totalPoints-1) / 10 +1;

        int winningPair;

        if (pair1Points < pair2Points) {

            winningPair = 1;
        }
        else if (pair2Points < pair1Points) {

            winningPair = 2;
        }
        else {

            // pair1Points == pair2Points

            // The winner is the hand...

            Game.PlayerPos handPos = mApp.mGame.mHandPlayerPos;

            int handPosIndex = mApp.mGame.getPlayerPosIndex(handPos);

            if ((handPosIndex == 0) || (handPosIndex == 2)) {

                // The winner is pair 1...

                winningPair = 1;
            }
            else {

                winningPair = 2;
            }
        }

        String closerName = mApp.mGame.getPlayerName(closerPlayerPos);

        String text = getString(R.string.player_has_closed_the_round, closerName);

        text += "\n\n";

        text += getString(R.string.pair_1)+": "+mApp.mGame.mAllPlayerNames.get(0)+" ("+
                playersPoints[0]+") + "+mApp.mGame.mAllPlayerNames.get(2)+" ("+
                playersPoints[2]+") = "+getString(R.string.x_points, pair1Points);

        text += "\n";

        text += getString(R.string.pair_2)+": "+mApp.mGame.mAllPlayerNames.get(1)+" ("+
                playersPoints[1]+") + "+mApp.mGame.mAllPlayerNames.get(3)+" ("+
                playersPoints[3]+") = "+getString(R.string.x_points, pair2Points);

        text += "\n";

        text += getString(R.string.total_remaining_points_)+" "+
                getString(R.string.x_points, totalPoints)+ " (+"+addingPoints+")";

        text += "\n";

        text += getString(R.string.winner_is_pair_x, winningPair);

        if (pair1Points == pair2Points) {

            text += "\n";

            text += getString(R.string.in_case_of_tie_hand_wins);
        }

        text += "\n\n";

        text += getString(R.string.pair_1)+" ("+mApp.mGame.mAllPlayerNames.get(0)+"+"+
                mApp.mGame.mAllPlayerNames.get(2)+"): ";

        if (winningPair == 1) {

            int finalPair1Points = mApp.mGame.mPair1Points + addingPoints;

            text += String.valueOf(mApp.mGame.mPair1Points)+" + "+addingPoints+" = "+
                    getString(R.string.x_points, finalPair1Points);
        }
        else {

            text += getString(R.string.x_points, mApp.mGame.mPair1Points);
        }

        text += "\n";

        text += getString(R.string.pair_2)+" ("+mApp.mGame.mAllPlayerNames.get(1)+"+"+
                mApp.mGame.mAllPlayerNames.get(3)+"): ";

        if (winningPair == 2) {

            int finalPair2Points = mApp.mGame.mPair2Points + addingPoints;

            text += String.valueOf(mApp.mGame.mPair2Points)+" + "+addingPoints+" = "+
                    getString(R.string.x_points, finalPair2Points);
        }
        else {

            text += getString(R.string.x_points, mApp.mGame.mPair2Points);
        }

        text += "\n";

        return text;
    }
    */

    private void showGameFinishedDialog() {

        String alertString = createGameFinishedMessage();

        AlertDialog.Builder alertDialogBuilder = null;

        alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder
                .setTitle(R.string.game_finished)
                .setMessage(alertString)
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        // Close the alert dialog
                        dialog.cancel();

                        // Finish the activity
                        finish();
                    }
                });

        // Create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // Show it
        alertDialog.show();
    }

    private String createGameFinishedMessage() {

        int winningPair;

        if (mApp.mGame.mPair1Points > mApp.mGame.mPair2Points) {

            winningPair = 1;
        }
        else {

            winningPair = 2;
        }

        String text = getString(R.string.pair_x_has_won_the_game, winningPair);

        text += "\n\n";

        text += getString(R.string.pair_1)+" ("+mApp.mGame.mAllPlayerNames.get(0)+"+"+
                mApp.mGame.mAllPlayerNames.get(2)+"): "+
                getString(R.string.x_points, mApp.mGame.mPair1Points);

        text += "\n";

        text += getString(R.string.pair_2)+" ("+mApp.mGame.mAllPlayerNames.get(1)+"+"+
                mApp.mGame.mAllPlayerNames.get(3)+"): "+
                getString(R.string.x_points, mApp.mGame.mPair2Points);

        text += "\n";

        return text;
    }

    private void showGameCancelledDialog() {

        AlertDialog.Builder alertDialogBuilder = null;

        alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder
                .setTitle(R.string.game_cancelled)
                .setMessage(R.string.the_game_has_been_cancelled_)
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        // Close the alert dialog
                        dialog.cancel();

                        // Finish the activity
                        finish();
                    }
                });

        speak(getString(R.string.the_game_has_been_cancelled_));

        // Create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // Show it
        alertDialog.show();
    }

    @Override
    public void onInit(int status) {

        String text;

        if (status == TextToSpeech.SUCCESS) {

            Locale locale = Locale.getDefault();

            mTextToSpeech.setSpeechRate(2);

            int ttsLang = mTextToSpeech.setLanguage(locale);

            if (ttsLang == TextToSpeech.LANG_MISSING_DATA ||
                    ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {

                text = "TextToSpeech language <"+locale.getLanguage()+">  NOT supported!";
            }
            else {

                text = "TextToSpeech language <"+locale.getLanguage()+"> supported!";

                text = null;
            }
        }
        else {

            text = "TextToSpeech onInit() failed!!";
        }

        if (text != null) {

            Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    private void speak(String text) {

        if (mTextToSpeech == null) {

            return;
        }

        if (mApp.mSilentModeOn) {

            return;
        }

        mTextToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
    }

    private String prepareTextToSpeech(String inText) {

        // Eliminate the <'> characters
        String outText = inText.replace("'", "").trim();

        if (outText.indexOf("-") < 0) {

            // No <-> character has been found. This is a pass text...

            return outText;
        }

        // Eliminate the '-' character
        outText = outText.replace("-", " ").trim();

        int length = outText.length();

        String tileString = outText.substring(length-3);

        outText = outText.substring(0, length-3);

        String number1String = tileString.substring(0, 1);

        int number1 = Integer.valueOf(number1String);

        String number2String = tileString.substring(2, 3);

        int number2 = Integer.valueOf(number2String);

        String ttsNumberStrings[]={
                getString(R.string.tile_number_0),
                getString(R.string.tile_number_1),
                getString(R.string.tile_number_2),
                getString(R.string.tile_number_3),
                getString(R.string.tile_number_4),
                getString(R.string.tile_number_5),
                getString(R.string.tile_number_6)
            };

        String tileSpeechText = ttsNumberStrings[number1];

        if (number1 == number2) {

            // This is a double tile...
            tileSpeechText = getString(R.string.tile_number_double, tileSpeechText);
        }
        else {

            // This is NOT a double tile...
            tileSpeechText += " " + ttsNumberStrings[number2];
        }

        outText += tileSpeechText;

        return outText;
    }
}
