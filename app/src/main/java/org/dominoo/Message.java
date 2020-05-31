package org.dominoo;

import java.util.HashMap;

public class Message {

    public enum MsgId {

        UNKNOWN,
        GAME_INFO,
        GAME_TILE_INFO,
        PLAYER_TILE_INFO,
        BOARD_TILE_INFO1,
        BOARD_TILE_INFO2,
        ROUND_INFO,
        PLAYED_TILE_INFO
    };

    public MsgId mId = MsgId.UNKNOWN;

    public String mErrorString = null;

    public HashMap<String, String> mArgs = new HashMap<String, String>();

    public Message(MsgId id) {

        mId=id;
    }

    public void addArgument(String key, String value) {

        mArgs.put(key, value);
    }

    public String getArgument(String key) {

        return mArgs.get(key);
    }
}
