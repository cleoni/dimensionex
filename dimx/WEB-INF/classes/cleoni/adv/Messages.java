package cleoni.adv;

/** This class is responsible for loading and preparing localised messages
 * to be used by the game engine.
 * @author Cristiano Leoni
 */
public class Messages {
    /** Charset */    
         public String charset = "ISO-8859-1";
         public boolean specialmode = false; // sets alternate display mode
         
         /** Commands */         
         public String[] cmd = {"","Enter","Disconnect","Use","Use with",
         "Look","Open","Close","Pick up","Drop",
         /*10*/ "Search","Hide","Put","Give","Say","Cancel","Game site","Save and Exit","Back to game","Change nickname",
         /*20*/ "Go","Start from scratch","Enter","Exit","Save","Reverse","Help"}; 

         /** Messages */         
        public String[] msg = {"","You are about to play","players","Sounds","Based on",
        "Sorry, you are not connected to the server. Please hit REFRESH and connect.","Invalid nickname - choose another","Choose a password to protect this saved game","Wrong password","A saved game exists under this nickname. Enter password",
        /*10*/ "Write here and hit ENTER to speak","A voice...","Music","What's this?","https://www.dimensionex.net/en/music.htm","I didn't understand you. Please rephrase.","Supported Commands:","command","","",
        /*20*/ "More ways","People","Items","Inventory","Status","$1 view","Look around","Hide everything","Speak to all","",        
        /*30*/ "in","on","with","to","item","","","","","",
        /*40*/ "","","","","","","","","","",
        /*50*/ "","","","","","","","","","",
        /*60*/ "","","","","","","","","","",
        /*70*/ "","","","","","","","","","",
        /*80*/ "","","","","","","","","","",
        /*90*/ "","","","","","","","","","",
        /*100*/ "It's already closed.","Can't close it","is open","is closed","Done!","Nothing happens.","Can't hide it","It's hidden, already.","There's not enough room.","It's locked.",
        /*110*/ "It's already open.","Cannot be opened or closed.","joined us!!","I've dropped","Could not drop","Could not move","Cannot move something which I don't own.","Shall I move...","The selected item does not exist anymore.","I can't, they both should be accessible.",
        /*120*/ "I can only give objects to other actors.","Does not acccept it","I gave $1 to $2.","Wrong password - retry or choose a different nickname","I've picked up","Cannot restore $1","Could not take with me","It seems I cannot pick up","I have got it, already.","I can't see it",
        /*130*/ "I've put $1 into $2.","I cannot put $1 into $2.","It seems I cannot move","Could not move it","I can just put items into other items","The selcted item is unreacheable","I can only use reachable items","One of the selected items does not exist anymore","has left us.","It's not visible...",
        /*140*/ "I closed $1.","I could not do that.","I've hidden $1.","I opened $1.","I found $1!!","I found nothing new.","$1 gave me $2!!","Cannot speak because of previous attempt to use a forbidden word - I can do it in $1 minutes.","Carrying:","Containing:",
        /*150*/ "Everything I was carrying just slipped from my hands!","$1 completed the game at $2","You have been killed by $1","$1 lost the connection","Hall of fame","$1 is looking at me.","Game saved.","","","",
        /*160*/ "$1 just came from $2","$1 moved into $2","Hears sounds","Is facing","Last contact at","Yes","No","North","South","East",
        /*170*/ "West","Up","Down","Turn left","Turn right","To play, click on command button first, then on related object(s).","You have chosen to exit this game...","Background music - leave open","Which way?","You are dead",
        /*180*/ "Statistics","Cannot perform inter-dimensional jump: $1, please retry.","World \"$1\" is not loaded or does not exist","$1 is in $2"
        };
 
        /** Messages constructor comment.
         * @param msgFile
         * @throws DimxException
         */
public Messages(String msgFile) throws DimxException {
	super();
	try {
                // Loads an UTF-8 properties file
		loadFromUTF8file(msgFile);
                // Alternate: Loads an ANSI ASCII properties file
                // we call it anyway if the line encoding=ansi is found
		//load(msgFile);
	} catch (Exception e) {
		throw new DimxException("ERROR reading Messages file: " + msgFile + "\n" + e + "\n" );
	}
}
/** Loads localised messages from the specified properties file. File must be
 * encoded in ASCII (extended charset need to be converted from UTF-8 to ASCII with
 * a specific utility)
 * @param msgsFile
 * @throws Exception
 */
private void load(String msgsFile) throws Exception {
	java.util.Properties p = new java.util.Properties();
	p.load(new java.io.FileInputStream(msgsFile));

        String s = Utils.cStr(p.getProperty("charset"));
        if (!s.equals("")) charset = s;
        
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("000");
        specialmode = Utils.cStr(p.getProperty("displaymode")).equalsIgnoreCase("special");
        // Load commands
        for (int i=1; i <= cmd.length; i++) {
            s = Utils.cStr(p.getProperty("cmd" + formatter.format((long)i)));
            if (!s.equals(""))
                cmd[i] = s;
        }
        // Load messages
        for (int i=1; i < msg.length; i++) {
            s = Utils.cStr(p.getProperty("msg" + formatter.format((long)i)));
            if (!s.equals(""))
                msg[i] = s;
        }
}

/** Loads localised messages from the specified properties file. File must be
 * encoded in UTF-8 (this one makes painless to write using extended charsets)
 * @param msgsFile
 * @throws Exception
 */
private void loadFromUTF8file(String msgsFile) throws Exception {
    	String buf = Utils.fetch(msgsFile,"UTF-8");

        java.util.Vector lines = Utils.stringSplit(buf,"\n");
        
        for (int i=0; i<lines.size(); i++) {
            String line = (String) lines.elementAt(i);
            line.trim();
            if (i==0) {
                String c = line.substring(0,1);
                if (65535 - c.charAt(0) == 256) {
                    // First char is UTF-8 signature
                    line = line.substring(1);
                }
            }
            if (line.startsWith("#") || line.equals("")) {
                // Comment or empty - do nothing
            } else {
                int x = line.indexOf("=");
                if (x<=0)
                    throw new DimxException("Error in messages file: " + msgsFile + " line " + i + " cannot be understood. Please place # at the beginning if it is a comment, or follow syntax key=value");
                String key = line.substring(0,x);
                String value = line.substring(x+1);
                //Logger.echo("key="+key+" value="+value);
                if (key.startsWith("msg")) {
                    if (!value.equals("")) {
                        int n = Utils.cInt(key.substring(3));
                        msg[n] = value;
                    }
                } else if (key.startsWith("cmd")) {
                    if (!value.equals("")) {
                        int n = Utils.cInt(key.substring(3));
                        cmd[n] = value;
                    }
                } else if (key.equals("charset")) {
                    if (!value.equals("")) charset = value;
                } else if (key.equalsIgnoreCase("encoding")) {
                    if (value.equalsIgnoreCase("ansi")) {
                        Logger.echo("WARNING: encoding=ansi directive encountered. Reading ansi ascii");
                        load(msgsFile);
                        return;
                    } else {
                        throw new DimxException("encoding=" + value + " directive unknown in " + msgsFile);
                    }
                } else if (key.equalsIgnoreCase("displaymode")) {
                    if (value.equalsIgnoreCase("special")) {
                        Logger.echo("WARNING: displaymode=special directive encountered. Using special display mode.");
                        specialmode = true;
                    } else {
                        throw new DimxException("displaymode=" + value + " directive unknown in " + msgsFile);
                    }
                } else {
                    Logger.echo("WARNING: This key will be ignored: " + key + " (in " + msgsFile + ")");
                }
            }
        }
}


/** Replaces occurrences of $1 and $2 with the specified values in the specifried string
 * @param astr
 * @param par1
 * @param par2
 * @return
 */
public static String actualize(String astr, String par1, String par2) {
	return Utils.stringReplace(Utils.stringReplace(astr,"$1",par1,false),"$2",par2,false);
}




/** Replaces occurrences of $1 with par1 in the specifried string
 * @param astr
 * @param par1
 * @return
 */
public static String actualize(String astr, String par1) {
	return Utils.stringReplace(astr,"$1",par1,false);
}


}