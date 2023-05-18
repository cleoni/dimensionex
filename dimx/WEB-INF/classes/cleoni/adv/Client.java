package cleoni.adv;

import java.util.*;

/** Represents the user's client. Most browser-targeted functions are here. */
public class Client {
	private Dict messages = new Dict();
        public  Utils session = null;
	private Player owner;
        private World world;
	private int maxSize=0; // no maxsize
	private StringBuffer console = new StringBuffer("");
        private StringBuffer rightcolumn = new StringBuffer("");
	private int count = 0;
        public String browser = null;
	public boolean audioSupport = false;
	public boolean musicSupport = false;
	private String backgroundSound = null;
	private boolean backgroundSoundLoop = false;
        private StringBuffer miscCommands = new StringBuffer(); 
	private int commandMatrix[] = {0,0,0,0,0,0};
	private	boolean newMessages = false; // Use: Read-only from outside
	private String sound = null;        
        public double factorx = 1.0;
        public double factory = 1.0;
	public Calendar lastContact = null;
        public String clientFile = null; // null=Defaults to standard.client

public void display(String shortMsg) {
	console.append(shortMsg);
	console.append("\n");
}

public void displayRight(String shortMsg) {
	rightcolumn.append(shortMsg);
}

/*
 * al contrario di display che accoda alla console
 * questa mette in testa ai messaggi
 */
public void displayh(String shortMsg) {
	console = new StringBuffer(shortMsg + "\n" + console);
}

public void displayJust(String shortMsg) {
	console.append(shortMsg);
}


public String readConsole() {
	String str = console.toString();
	return str;
}

public String getConsole() {
	String str = console.toString();
	console = new StringBuffer("");
	return str;
}

public String getRightColumn() {
	String str = rightcolumn.toString();
	rightcolumn = new StringBuffer("");
	return str;
}

public boolean contactSince(int seconds) {

    Calendar contactLimit = (Calendar) lastContact.clone(); 
    contactLimit.add(Calendar.SECOND,seconds);
    Calendar now = Calendar.getInstance();
    return now.before(contactLimit);
}
public Message getMessage(int index) {
	return (Message) messages.elementAt(index);
}

public int getMessageCount() {
	return messages.size();
}

public Message popMessage(String key) {
	Message myMsg = (Message) messages.get(key);
	if (myMsg != null) {
		messages.remove(key);
		return myMsg;
	} else {
		return null;
	}
}
public boolean receive(Message aMsg) throws DimxException  {
	if (maxSize > 0 && messages.size() >= maxSize) {
		messages.removeAt(0);
	}
	String id = Utils.cStr(count++);
	aMsg.setId(id);
	messages.put(id,aMsg);
	newMessages = true;

	return true;
}

public String toString() {
        if (owner == null) { 
            return "Client di null"; 
        } else {
        	String ret = "Client di " + owner.name + "(" + owner.id + ") - mondo: ";
                if (owner.world != null) ret = ret + world.name; else ret = ret + " null";
                return ret;
        }
}



/**
 * MsgBoard constructor comment.
 */
public Client(World aWorld, int aMaxSize, boolean audio, boolean music, int screenwidth, int screenheight, String aBrowser) {
        world = aWorld;
	maxSize = aMaxSize;
	audioSupport = audio;
        musicSupport = music;
        browser = aBrowser;
        if (world != null) {
            //Patch trial for cellphones
            if (screenwidth==400) screenwidth=550;
            factorx = Utils.cDbl(screenwidth)/world.screenwidth;
            factory = Utils.cDbl(screenheight)/world.screenheight;
        }
        lastContact = Calendar.getInstance();
}

protected void setContact()  throws DimxException {
	lastContact = Calendar.getInstance();
}

public void setOwner(Player someone) {
	owner = someone;
}

public void cmdFocusmsg() {
	commandMatrix[Const.CMD_FOCUSMSG] = 1;
}

public void cmdCustom(String command) {
	miscCommands.append(command+";");
}

public void cmdNewmsg() {
	commandMatrix[Const.CMD_NEWMSG] = 1;
}

public void cmdRefrCtrls() {
	commandMatrix[Const.CMD_REFRCTRLS] = 1;
}

public void cmdRefrScene() {
	commandMatrix[Const.CMD_REFRSCENE] = 1;
}

public void cmdSilence() {
	commandMatrix[Const.CMD_SILENCE] = 1;
}

public String getCommands(String view) {
// Gets the current commands string. The parameter specifies the current view
	StringBuffer commands = new StringBuffer("");

	if (commandMatrix[Const.CMD_NEWMSG] != 0) {
		commands.append("newmsg;");
		commandMatrix[Const.CMD_NEWMSG] = 0;
	}
	if (commandMatrix[Const.CMD_FOCUSMSG] != 0) {
		commands.append("focusmsg;");
		commandMatrix[Const.CMD_FOCUSMSG] = 0;
	}
	if (commandMatrix[Const.CMD_REFRSCENE] != 0) {
		if (!view.equalsIgnoreCase("scene"))
			commands.append("refresh!scene;");
		commandMatrix[Const.CMD_REFRSCENE] = 0;
	}
	if (commandMatrix[Const.CMD_REFRCTRLS] != 0) {
		if (view.equalsIgnoreCase("ctrls")) {
			// Ensures the ctrls have been refreshed
			commandMatrix[Const.CMD_REFRCTRLS] = 0;
		} else {
			commands.append("refresh!ctrls;");
		}
	}
	if (musicSupport) {
		if (commandMatrix[Const.CMD_SILENCE] != 0) {
			commands.append("silence;");
			commandMatrix[Const.CMD_SILENCE] = 0;
			backgroundSound = null;
		} else {
			if (backgroundSound!=null && !backgroundSound.equals("")) {
				if (backgroundSoundLoop) {
					commands.append("playLooping!"+Utils.absolutizeUrl(backgroundSound,this.owner.world.imagesFolder) + ";");
				} else {
					commands.append("play!"+Utils.absolutizeUrl(backgroundSound,this.owner.world.imagesFolder) + ";");
				}
				setBackgroundSound(null,false);
			}
		}
	}
        commands.append(miscCommands.toString());
        miscCommands = new StringBuffer();
	String ret = commands.toString();
	if (ret.length()>0) {
		ret = ret.substring(0,ret.length()-1); // Cuts final semicolon
		if (owner.world != null) owner.world.logger.debug("Commands: " + ret);
		return ret;
	} else { 
		return null;
	}

}

public boolean getNewMessages() {
	boolean ret = newMessages;
	newMessages = false;
	return ret;
}

public String getPanelIds() {
	// Returns the panels-string associated to this player

	Panel pan0 = owner.getPanel();
	Panel pan1 = null;
	
	AdvObject container = owner.container;
	if (container != null) pan1 = container.getPanel();

	String id0 = "";
	String id1 = "";

	if (pan0 != null) id0 = pan0.id;
	if (pan1 != null) id1 = pan1.id;
	
	return "_" + id0 + "+_" + id1;
}

protected String getSound() {
	String s = sound;
	sound = null;
	return s;
}

protected void setBackgroundSound(String s, boolean loop)
{ 
	if (musicSupport) {
		backgroundSound = s;
		backgroundSoundLoop = loop;
	} 
}

protected void setSound(String s)
{ 
	if (audioSupport) {
		sound = s;
	}
}
}