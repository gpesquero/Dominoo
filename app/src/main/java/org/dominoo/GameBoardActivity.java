package org.dominoo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class GameBoardActivity extends AppCompatActivity implements CommSocket.CommSocketListener,
    PlayerTilesView.OnTileSelectedListener {

    DominooApplication mApp = null;

    GameBoardView mGameBoardView;
    PlayerTilesView mPlayerTilesView;

    private int mTurnPlayer = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        mApp = (DominooApplication)getApplication();

        if (mApp.mCommSocket == null) {

            mApp.mCommSocket = new CommSocket();
        }

        mApp.mCommSocket.setCommSocketListener(this);

        mGameBoardView = findViewById(R.id.viewGameBoard);

        mPlayerTilesView = findViewById(R.id.viewPlayerTiles);
        mPlayerTilesView.setOnTileSelectedListener(this);


        updateControls();

        /*
        // Create domino tiles
        ArrayList<DominoTile> tiles = new ArrayList<DominoTile>();

        tiles.add(new DominoTile(0, 1));
        tiles.add(new DominoTile(2, 3));
        tiles.add(new DominoTile(4, 5));
        tiles.add(new DominoTile(6, 0));

        mPlayerTilesView.mTiles = tiles;
        */

        requestTileInfo();

    }

    private void updateControls() {

        mGameBoardView.setTurnPlayer(Game.PlayerPos.PLAYER);

        mGameBoardView.setPlayerName(mApp.mGame.mPlayerName);
        mGameBoardView.setPartnerName(mApp.mGame.getPartnerName());
        mGameBoardView.setLeftOpponentName(mApp.mGame.getLeftOpponentName());
        mGameBoardView.setRightOpponentName(mApp.mGame.getRightOpponentName());

        mGameBoardView.invalidate();
    }

    private void requestTileInfo() {

        // Create <Request Tile Info> message
        String msg = CommProtocol.createMsgRequestTileInfo(mApp.mGame.mPlayerName);

        // Send the message to the server
        mApp.mCommSocket.sendMessage(msg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mApp.mCommSocket.setCommSocketListener(null);
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

            /*
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

            if (mApp.mGame.mAllPlayerNames.indexOf(mApp.mGame.mPlayerName) <0 ) {

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
            */
        }
        else if (msg.mId == Message.MsgId.GAME_TILE_INFO) {

            mTurnPlayer = Integer.parseInt(msg.getArgument("turnPlayer"));

            Game.PlayerPos playerPosition = mApp.mGame.getPlayerPosition(mTurnPlayer);

            mGameBoardView.setTurnPlayer(playerPosition);

            for (int i=0; i<Game.MAX_PLAYERS; i++) {

                String keyString = "player"+i;

                int playerTileCount = Integer.parseInt(msg.getArgument(keyString));

                Game.PlayerPos playerPos = mApp.mGame.getPlayerPosition(i);

                switch(playerPos) {

                    case PLAYER:
                        break;

                    case PARTNER:
                        mGameBoardView.setPartnerTileCount(playerTileCount);
                        break;

                    case LEFT_OPPONENT:
                        mGameBoardView.setLeftOpponentTileCount(playerTileCount);
                        break;

                    case RIGHT_OPPONENT:
                        mGameBoardView.setRightOpponentTileCount(playerTileCount);
                        break;

                    default:
                        Log.e("DomLog", "Msg.GameTileInfo.processMessage() Unknown playerpos");
                        break;
                }

            }

            mGameBoardView.invalidate();
        }
        else if (msg.mId == Message.MsgId.PLAYER_TILE_INFO) {

            String playerName = msg.getArgument("playerName");

            Log.i("DomLog", "Received Player Tile Info for player <"+playerName+">");

            int tileCount = Integer.parseInt(msg.getArgument("tileCount"));

            Log.i("DomLog", "Tile Count = "+tileCount);

            mApp.mGame.mPlayerTiles = new ArrayList<DominoTile>();

            for(int i=0; i<tileCount; i++) {

                String tileText = msg.getArgument("tile"+i);

                String n1 = tileText.substring(0, 1);

                int number1 = Integer.parseInt(n1);

                String n2 = tileText.substring(2);

                int number2 = Integer.parseInt(n2);

                DominoTile tile = new DominoTile(number1, number2);

                mApp.mGame.mPlayerTiles.add(tile);
            }

            DominoTile.sortTiles(mApp.mGame.mPlayerTiles);

            mPlayerTilesView.setTiles(mApp.mGame.mPlayerTiles);

            mPlayerTilesView.invalidate();
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

            mGameBoardView.invalidate();
        }
        else {

            Log.d("DomLog", "GameBoardActivity.processMessage() unknown message Id="+msg.mId);
        }
    }

    @Override
    public void onTileSelected(DominoTile selectedTile) {

        mGameBoardView.onTileSelected(selectedTile, mApp.mGame.mForceDouble6Tile);

        mGameBoardView.invalidate();
    }
}
