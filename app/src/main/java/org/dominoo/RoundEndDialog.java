package org.dominoo;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

public class RoundEndDialog {

    private Activity mActivity;

    private AlertDialog mDialog = null;

    private String mTitle = null;

    TextView mTextViewPair1Count;
    TextView mTextViewPair2Count;

    TextView[] mTextViewPlayerName;
    TextView[] mTextViewPlayerPoints;

    PlayerTilesView[] mViewPlayerTiles;

    TextView mTextViewWinningPair;

    TextView mTextViewRemainingPoints;

    TextView mTextViewPair1Points;
    TextView mTextViewPair2Points;


    public RoundEndDialog(Activity activity, DialogInterface.OnClickListener listener) {

        mActivity = activity;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);

        LayoutInflater inflater = mActivity.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_round_result, null);

        mTextViewPair1Count = dialogView.findViewById(R.id.textViewPair1Count);
        mTextViewPair2Count = dialogView.findViewById(R.id.textViewPair2Count);

        mTextViewPlayerName = new TextView[Game.MAX_PLAYERS];

        mTextViewPlayerName[0] = dialogView.findViewById(R.id.textViewPlayer0Name);
        mTextViewPlayerName[1] = dialogView.findViewById(R.id.textViewPlayer1Name);
        mTextViewPlayerName[2] = dialogView.findViewById(R.id.textViewPlayer2Name);
        mTextViewPlayerName[3] = dialogView.findViewById(R.id.textViewPlayer3Name);

        mTextViewPlayerPoints = new TextView[Game.MAX_PLAYERS];

        mTextViewPlayerPoints[0] = dialogView.findViewById(R.id.textViewPlayer0Points);
        mTextViewPlayerPoints[1] = dialogView.findViewById(R.id.textViewPlayer1Points);
        mTextViewPlayerPoints[2] = dialogView.findViewById(R.id.textViewPlayer2Points);
        mTextViewPlayerPoints[3] = dialogView.findViewById(R.id.textViewPlayer3Points);

        mViewPlayerTiles = new PlayerTilesView[Game.MAX_PLAYERS];

        mViewPlayerTiles[0] = dialogView.findViewById(R.id.viewPlayer0Tiles);
        mViewPlayerTiles[1] = dialogView.findViewById(R.id.viewPlayer1Tiles);
        mViewPlayerTiles[2] = dialogView.findViewById(R.id.viewPlayer2Tiles);
        mViewPlayerTiles[3] = dialogView.findViewById(R.id.viewPlayer3Tiles);

        mTextViewWinningPair = dialogView.findViewById(R.id.textViewWinningPair);

        mTextViewRemainingPoints = dialogView.findViewById(R.id.textViewRemainingPoints);

        mTextViewPair1Points = dialogView.findViewById(R.id.textViewPair1Points);
        mTextViewPair2Points = dialogView.findViewById(R.id.textViewPair2Points);

        alertDialogBuilder.setView(dialogView);

        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setNeutralButton(android.R.string.ok, listener);

        // Create alert dialog
        mDialog = alertDialogBuilder.create();
    }

    public void show() {

        mDialog.show();
    }

    public void setGameInfo(Game game) {

        int winningPair = 0;

        int pair1Count = game.mPlayersPoints[0] + game.mPlayersPoints[2];

        int pair2Count = game.mPlayersPoints[1] + game.mPlayersPoints[3];

        String winnerComment = null;

        if (game.mWinnerPlayerPos >= 0) {

            String winnerName = game.getPlayerName(game.mWinnerPlayerPos);

            mTitle = mActivity.getString(R.string.player_has_won_the_round, winnerName);

            winningPair = (game.mWinnerPlayerPos % 2) +1;
        }
        else if (game.mCloserPlayerPos >= 0) {

            String closerName = game.getPlayerName(game.mCloserPlayerPos);

            mTitle = mActivity.getString(R.string.player_has_closed_the_round, closerName);

            if (pair1Count < pair2Count) {

                winningPair = 1;
            }
            else if (pair2Count < pair1Count) {

                winningPair = 2;
            }
            else {

                // 'pair1Count' equals 'pair2Count'

                // The winner is the hand...

                Game.PlayerPos handPos = game.mHandPlayerPos;

                int handPosIndex = game.getPlayerPosIndex(handPos);

                if ((handPosIndex == 0) || (handPosIndex == 2)) {

                    // The winner is pair 1...
                    winningPair = 1;
                }
                else {

                    // The winner is pair 2...
                    winningPair = 2;
                }

                winnerComment = mActivity.getString(R.string.in_case_of_tie_hand_wins);
            }
        }

        mDialog.setTitle(mTitle);

        mTextViewPair1Count.setText(mActivity.getString(R.string.pair_x_y_pts,
                1, pair1Count));

        mTextViewPair2Count.setText(mActivity.getString(R.string.pair_x_y_pts,
                2, pair2Count));

        for(int i=0; i<Game.MAX_PLAYERS; i++) {

            mTextViewPlayerName[i].setText(game.mAllPlayerNames.get(i));

            mTextViewPlayerPoints[i].setText("(" + mActivity.getString(R.string.x_points, game.mPlayersPoints[i]) + ")");

            ArrayList<DominoTile> tiles = new ArrayList<DominoTile>();

            String tileString = game.mPlayersTiles[i];

            if (tileString == null) {

                continue;
            }

            while (tileString.length() > 0) {

                int number1 = Integer.parseInt(tileString.substring(0, 1));
                int number2 = Integer.parseInt(tileString.substring(1, 2));

                tiles.add(new DominoTile(number1, number2));

                tileString = tileString.substring(2);
            }

            mViewPlayerTiles[i].setTiles(tiles);
        }

        String winningPairText = mActivity.getString(R.string.pair_x_has_won_the_round, winningPair);

        if (winnerComment != null) {

            winningPairText += " " + winnerComment;
        }

        mTextViewWinningPair.setText(winningPairText);

        int totalRemainingPoints = pair1Count + pair2Count;

        int addingPoints = (totalRemainingPoints-1) / 10 +1;

        mTextViewRemainingPoints.setText(mActivity.getString(R.string.total_remaining_points_) +
                " " + pair1Count+"+"+pair2Count+"="+totalRemainingPoints + " (+"+addingPoints+")");

        String pair1Text = null;
        String pair2Text = null;

        if (winningPair == 1) {

            pair1Text = mActivity.getString(R.string.pair_x_y_plus_z_w_pts, 1,
                    game.mPair1Points, addingPoints, game.mPair1Points+addingPoints);

            pair2Text = mActivity.getString(R.string.pair_x_y_pts, 2, game.mPair2Points);
        }
        else if (winningPair == 2) {

            pair1Text = mActivity.getString(R.string.pair_x_y_pts, 1, game.mPair1Points);

            pair2Text = mActivity.getString(R.string.pair_x_y_plus_z_w_pts, 2,
                    game.mPair2Points, addingPoints, game.mPair2Points+addingPoints);
        }

        mTextViewPair1Points.setText(pair1Text);

        mTextViewPair2Points.setText(pair2Text);
    }

    public String getTitleString() {

        return mTitle;
    }
}
