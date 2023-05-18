package cleoni.adv;

/** Constants for the whole game engine */
public class Const {
	// Object types
	public static final String TYPE_ITEM = "i!";
	public static final String TYPE_ROOM = "r!";
	public static final String TYPE_CHARACTER = "c!";
	public static final String TYPE_LINK = "w!";

        // Client Commands
	public static final int CMD_SILENCE = 0;
	public static final int CMD_FOCUSMSG = 4;
	public static final int CMD_NEWMSG = 3;
	public static final int CMD_REFRCTRLS = 2;
	public static final int CMD_REFRSCENE = 1;
        public static final String CMDE_REFRSCENE = "refresh!scene";
        public static final String CMDE_REFRCTRLS = "refresh!ctrls";

        // Ctrl types
	public static final int CTRL_GHOST = 0;
	public static final int CTRL_BUTTON = 1;
	public static final int CTRL_LABEL  = 2;
	public static final int CTRL_CR = 3;
	public static final int CTRL_TEXTBOX = 4;
	public static final int CTRL_IMAGEBUTTON = 5;
	public static final int CTRL_SUBMIT = 6;
        public static final int CTRL_DROPDOWN = 7;
        public static final int CTRL_CHECKBOX = 8;
        public static final int CTRL_PASSWORD = 9;
	public static final int CTRL_HIDDEN = 10;
        public static final int CTRL_MAP = 11;

        // Utility
        public static final boolean DETECT_CLASH = true;
        public static final boolean IGNORE_CASE = true;
        public static final boolean CHECK_OPEN = true;
        public static final boolean DONT_CHECK_OPEN = false;
        public static final boolean CHECK_EVENTS = true;
        public static final boolean DONT_CHECK_EVENTS = false;
        public static final boolean MUST_BE_DEFINED = true;
	public static final boolean FORCE_REMOVE = false;
	public static final boolean KICKOUT = false;
	public static final boolean DROP_ITEMS = true;
	public static final boolean KEEP_ITEMS = false;
	public static final boolean NO_SHOWAREA = false;
        public static final boolean GETREF = true;
        public static final int ARRAYMAX = 4; // 1..10^ARRAYMAX-1

        public static final boolean REDEFINE_ID = true;
        public static final boolean KEEP_ID = false;
        
        public static final int NOTFOUND = -2;
        public static final int VARIABLE = -1;
        public static final Varspace WORLDVARS = null;
	public static final String ACCEPT_ALL = "*";
	public static final String ACCEPT_NOTHING = "";
	public static final boolean ALLOW_SHOWAREA = true;
	public static final String[] offlineCmds = {"popmsg","login","startscratch","snapshot","dump","showlog","restart","clearlog","opti","_addcode","_execute"};
        public static final String[] emptySet = {};
        public static final String[] compassDirections = {"N","E","S","W"};

	// Show modes
	public static final int OFFSCREEN = 0;
	public static final int ONSCREEN_ICON = 2;
	public static final int ONSCREEN_IMAGE = 1;
	public static final boolean STAYZOMBIE = true;

        // Scene Looks
        public static final int LOOK_1STPERSON = 0;
        public static final int LOOK_3RDPERSON = 1;
        
        // Screen sizes
        public static final String[] screenSizes = {"1024x768","1280x800","800x600","640x480","480x272","400x800"};
        
        // Print destinations
        public static final int TO_CONSOLE = 0;
        public static final int TO_RIGHTCOLUMN = 1;
}