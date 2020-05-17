package org.dominoo;

import org.dominoo.Message.MsgId;

public class CommProtocol {

    public static String createMsgLogin(String playerName) {

        String message;

        message="<login, playerName="+playerName+">";

        return message;
    }

    public static String createMsgLogout(String playerName) {

        String message;

        message="<logout, playerName="+playerName+">";

        return message;
    }

    public static String createMsgMovePlayer(String playerName, int newPos) {

        String message;

        message="<move_player, playerName="+playerName+", newPos="+newPos+">";

        return message;
    }

    public static String createMsgLaunchGame(String playerName) {

        String message;

        message="<launch_game, playerName="+playerName+">";

        return message;
    }

    public static String createMsgRequestTileInfo(String playerName) {

        String message;

        message="<request_tile_info, playerName="+playerName+">";

        return message;
    }

    public static String createMsgRequestGameInfo(String playerName) {

        String message;

        message="<request_game_info, playerName="+playerName+">";

        return message;
    }

    public static String createMsgPlayTile(String playerName, int playerPos,
                                           DominoTile tile, int boardSide) {

        String tileString;

        if (tile == null) {

            tileString = null;
        }
        else {

            tileString = String.valueOf(tile.mNumber1)+"-"+tile.mNumber2;
        }

        String message;

        message="<play_tile, playerName="+playerName+", playerPos="+playerPos+
                ", tile="+tileString+", boardSide="+boardSide+">";

        return message;
    }

    static public Message processLine(String line) {

        Message msg=new Message(MsgId.UNKNOWN);

        line=line.trim();

        if (!line.startsWith("<") ) {

            msg.mId=MsgId.UNKNOWN;

            msg.mErrorString="Received message '"+line+"' does not start with '<'";

            return msg;
        }

        if (!line.endsWith(">") ) {

            msg.mId=MsgId.UNKNOWN;

            msg.mErrorString="Received message '"+line+"' does not end with '>'";

            return msg;
        }

        if (line.length()<5) {

            msg.mId=MsgId.UNKNOWN;

            msg.mErrorString="Received message '"+line+"' is too short";

            return msg;
        }

        line=line.substring(1, line.length()-1);

        line.trim();

        String command;
        String args;

        int commaPos=line.indexOf(",");

        if (commaPos<0) {

            command=line.trim();
            args=null;
        }
        else {

            command=line.substring(0, commaPos).trim();
            args=line.substring(commaPos+1).trim();
        }

        if (args!=null) {

            // Process arguments...

            while(args.length()>0) {

                String arg;

                commaPos=args.indexOf(",");

                if (commaPos<0) {

                    arg=args;

                    args="";
                }
                else {

                    arg=args.substring(0, commaPos).trim();
                    args=args.substring(commaPos+1).trim();
                }

                // Analyze message argument

                int equalPos=arg.indexOf("=");

                if (equalPos<0) {

                    msg.mId=MsgId.UNKNOWN;

                    msg.mErrorString="Received message '"+line+"'. Arg '"+arg+"' does no have '=' char";

                    return msg;
                }
                else {

                    String key=arg.substring(0, equalPos).trim();
                    String value=arg.substring(equalPos+1).trim();

                    msg.addArgument(key, value);
                }
            }
        }

        if (command.compareTo("game_info")==0) {

            msg.mId=MsgId.GAME_INFO;
        }
        else if (command.compareTo("game_tile_info")==0) {

            msg.mId=MsgId.GAME_TILE_INFO;
        }
        else if (command.compareTo("player_tile_info")==0) {

            msg.mId=MsgId.PLAYER_TILE_INFO;
        }
        else if (command.compareTo("board_tile_info1")==0) {

            msg.mId=MsgId.BOARD_TILE_INFO1;
        }
        else if (command.compareTo("board_tile_info2")==0) {

            msg.mId=MsgId.BOARD_TILE_INFO2;
        }
        else if (command.compareTo("round_info")==0) {

            msg.mId=MsgId.ROUND_INFO;
        }
        else {

            msg.mErrorString="Received message '"+line+"' with unknown command '"+command+"'";

            return msg;
        }

        return msg;
    }
}
