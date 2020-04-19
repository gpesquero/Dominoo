package org.dominoo;

import org.dominoo.Message.MsgId;

public class CommProtocol {

    public static String createMsgOpenSession(String playerName) {

        String message;

        message="<open_session, playerName="+playerName+">";

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

        if (command.compareTo("session_info")==0) {

            msg.mId=MsgId.SESSION_INFO;
        }
        else {

            msg.mErrorString="Received message '"+line+"' with unknown command '"+command+"'";

            return msg;
        }

        return msg;
    }
}
