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

        NOT_STARTED,
        RUNNING
    }

    public static final int MAX_PLAYERS = 4;

    public String mPlayerName;

    public ArrayList<String> mAllPlayerNames;

    public Status mStatus = Status.NOT_STARTED;

    public CommSocket mConnection;

    public ArrayList<DominoTile> mPlayerTiles = null;

    public ArrayList<DominoTile> mBoardTiles1 = null;
    public ArrayList<DominoTile> mBoardTiles2 = null;

    public boolean mForceDouble6Tile = false;

    public Game() {

        mAllPlayerNames = new ArrayList<String>();
    }

    ArrayList<String> getOtherPlayers() {

        ArrayList<String> otherPlayers=new ArrayList<String>();

        Iterator<String> iter=mAllPlayerNames.iterator();

        while(iter.hasNext()) {

            String name=iter.next();

            // If it's the current player name, do not add it
            if (name.compareTo(mPlayerName)==0)
                continue;

            // Check if other name already exists (for the "Robot" case)
            if (otherPlayers.indexOf(name)<0) {

                otherPlayers.add(name);
            }
        }

         return otherPlayers;
    }

    public String getPartnerName() {

        int myPlayerPos = mAllPlayerNames.indexOf(mPlayerName);

        int partnerPos = (myPlayerPos + 2) % 4;

        return mAllPlayerNames.get(partnerPos);
    }

    public String getLeftOpponentName() {

        int myPlayerPos = mAllPlayerNames.indexOf(mPlayerName);

        int leftOpponentPos = (myPlayerPos + 3) % 4;

        return mAllPlayerNames.get(leftOpponentPos);
    }

    public String getRightOpponentName() {

        int myPlayerPos = mAllPlayerNames.indexOf(mPlayerName);

        int rightOpponentPos = (myPlayerPos + 1) % 4;

         return mAllPlayerNames.get(rightOpponentPos);
    }

    public PlayerPos getPlayerPosition(int player) {

        int myPlayerPos = mAllPlayerNames.indexOf(mPlayerName);

        int relativePos = player - myPlayerPos;

        if (relativePos <0 ) {

            relativePos+=MAX_PLAYERS;
        }

        if (relativePos == 0) {

            return PlayerPos.PLAYER;
        }
        else if (relativePos == 1) {

            return PlayerPos.RIGHT_OPPONENT;
        }
        else if (relativePos == 2) {

            return PlayerPos.PARTNER;
        }
        else {

            return PlayerPos.LEFT_OPPONENT;
        }
    }
}
