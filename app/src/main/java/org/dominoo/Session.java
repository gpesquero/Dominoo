package org.dominoo;

import java.util.ArrayList;
import java.util.Iterator;

public class Session {

    public String mPlayerName;

    public ArrayList<String> mAllPlayerNames;

    /*
    public String mPlayer0Name;
    public String mPlayer1Name;
    public String mPlayer2Name;
    public String mPlayer3Name;
    */

    //public String mSessionId;
    //public Socket mSocket;
    public CommSocket mConnection;

    public Session() {

        mAllPlayerNames = new ArrayList<String>();
    }

    String[] getOtherPlayers() {

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

        String[] result=new String[otherPlayers.size()];

        result=otherPlayers.toArray(result);

        return result;
    }
}
