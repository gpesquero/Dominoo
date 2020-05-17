package org.dominoo;

import java.util.ArrayList;
import java.util.Iterator;

public class Game {

    enum PlayerPos {

        NONE,
        PLAYER,
        PARTNER,
        LEFT_OPPONENT,
        RIGHT_OPPONENT
    }

    enum Status {

        UNKNOWN,
        NOT_STARTED,
        RUNNING,
        FINISHED
    }

    static final int MAX_PLAYERS = 4;

    String mMyPlayerName;

    ArrayList<String> mAllPlayerNames;

    Status mStatus = Status.UNKNOWN;

    CommSocket mConnection;

    ArrayList<DominoTile> mPlayerTiles = null;

    ArrayList<DominoTile> mBoardTiles1 = null;
    ArrayList<DominoTile> mBoardTiles2 = null;

    int mRoundCount = -1;

    boolean mForceDouble6Tile = false;

    PlayerPos mTurnPlayerPos = PlayerPos.NONE;

    PlayerPos mHandPlayerPos = PlayerPos.NONE;

    int[] mPlayerPoints = new int[MAX_PLAYERS];

    int mPair1Points = 0;
    int mPair2Points = 0;

    Game() {

        mAllPlayerNames = new ArrayList<String>();
    }

    ArrayList<String> getOtherPlayers() {

        ArrayList<String> otherPlayers=new ArrayList<String>();

        for (String name : mAllPlayerNames) {

            // If it's the current player name, do not add it
            if (name.compareTo(mMyPlayerName)==0)
                continue;

            // Check if other name already exists (for the "Robot" case)
            if (otherPlayers.indexOf(name)<0) {

                otherPlayers.add(name);
            }
        }

         return otherPlayers;
    }

    int getMyPlayerPos() {

        return mAllPlayerNames.indexOf(mMyPlayerName);
    }

    String getPartnerName() {

        int myPlayerPos = mAllPlayerNames.indexOf(mMyPlayerName);

        int partnerPos = (myPlayerPos + 2) % 4;

        return mAllPlayerNames.get(partnerPos);
    }

    public String getLeftOpponentName() {

        int myPlayerPos = mAllPlayerNames.indexOf(mMyPlayerName);

        int leftOpponentPos = (myPlayerPos + 3) % 4;

        return mAllPlayerNames.get(leftOpponentPos);
    }

    public String getRightOpponentName() {

        int myPlayerPosIndex = mAllPlayerNames.indexOf(mMyPlayerName);

        int rightOpponentPosIndex = (myPlayerPosIndex + 1) % 4;

        return mAllPlayerNames.get(rightOpponentPosIndex);
    }

    public PlayerPos getPlayerPosition(int player) {

        int myPlayerPosIndex = mAllPlayerNames.indexOf(mMyPlayerName);

        int relativePosIndex = player - myPlayerPosIndex;

        if (relativePosIndex <0 ) {

            relativePosIndex+=MAX_PLAYERS;
        }

        if (relativePosIndex == 0) {

            return PlayerPos.PLAYER;
        }
        else if (relativePosIndex == 1) {

            return PlayerPos.RIGHT_OPPONENT;
        }
        else if (relativePosIndex == 2) {

            return PlayerPos.PARTNER;
        }
        else {

            return PlayerPos.LEFT_OPPONENT;
        }
    }

    public int getPlayerPosIndex(PlayerPos playerPos) {

        int myPlayerPosIndex = mAllPlayerNames.indexOf(mMyPlayerName);

        int playerIndex;

        switch (playerPos) {

            case PLAYER:
                playerIndex = myPlayerPosIndex;
                break;

            case RIGHT_OPPONENT:

                playerIndex = (myPlayerPosIndex + 1) % MAX_PLAYERS;
                break;

            case PARTNER:

                playerIndex = (myPlayerPosIndex + 2) % MAX_PLAYERS;
                break;

            case LEFT_OPPONENT:

                playerIndex = (myPlayerPosIndex + 3) % MAX_PLAYERS;
                break;

            default:
                playerIndex = -1;
                break;
        }

        return playerIndex;
    }

    public String getPlayerName(int playerPosIndex) {

        return mAllPlayerNames.get(playerPosIndex);
    }

    public int getEndNumber1() {

        if (mBoardTiles1 == null) {

            return -1;
        }

        if (mBoardTiles1.size() == 0) {

            return -1;
        }

        return mBoardTiles1.get(mBoardTiles1.size()-1).mNumber2;
    }

    public int getEndNumber2() {

        if (mBoardTiles1 == null) {

            return -1;
        }

        if (mBoardTiles1.size() == 0) {

            return -1;
        }

        if (mBoardTiles2 == null) {

            return -1;
        }

        if (mBoardTiles2.size() == 0) {

            return mBoardTiles1.get(mBoardTiles1.size()-1).mNumber1;
        }

        return mBoardTiles2.get(mBoardTiles2.size()-1).mNumber2;
    }

    public boolean removeTile(int number1, int number2) {

        Iterator<DominoTile> iter = mPlayerTiles.iterator();

        while (iter.hasNext()) {

            DominoTile tile = iter.next();

            if ((tile.mNumber1 == number1) && (tile.mNumber2 == number2)) {

                mPlayerTiles.remove(tile);

                return true;
            }

            if ((tile.mNumber1 == number2) && (tile.mNumber2 == number1)) {

                mPlayerTiles.remove(tile);

                return true;
            }
        }

        return false;
    }

    public int getPair1Points() {

        return mPair1Points;
    }

    public int getPair2Points() {

        return mPair2Points;
    }

    void processGameInfoMessage(Message msg) {

        mAllPlayerNames.clear();

        mAllPlayerNames.add(msg.getArgument("player0"));
        mAllPlayerNames.add(msg.getArgument("player1"));
        mAllPlayerNames.add(msg.getArgument("player2"));
        mAllPlayerNames.add(msg.getArgument("player3"));

        String statusText=msg.getArgument("status");

        if (statusText == null) {

            mStatus= Game.Status.NOT_STARTED;
        }
        else if (statusText.compareTo("notStarted")==0) {

            mStatus= Game.Status.NOT_STARTED;
        }
        else if (statusText.compareTo("running")==0) {

            mStatus= Game.Status.RUNNING;
        }
        else if (statusText.compareTo("finished")==0) {

            mStatus= Game.Status.FINISHED;
        }
        else {

            mStatus= Game.Status.UNKNOWN;
        }

    }
}
