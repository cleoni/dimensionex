package cleoni.adv;

import java.util.*;

/** Implements the WORLD object  */
public class World extends DimxObject {

    // World attributes
    private int dynObjCount = 0;    // Counts dynamic objects
    public multiplayer server = null;
    public boolean simplifyNavigation = true;
    public String instanceid = "";
    public String site = "";
    public String worldFile = null;
    public boolean debugging = false; // debugMode = "1"?
    public boolean passchecked = false; // to avoid bouncing when debug is active
    public boolean hideSourcePath = false; // Hide source path in case of errors?
    public boolean tracing = false; // DB tracing enabled?
    public int muting = 1; // muting level 0=off,1=basic,2=normal,3=cumulative
    private Varspace objects = new Varspace(); // World contents (objects) - for properties see: properties
    public String version = "";
    private int waysCount = 0;
    public String author = "";
    public String defaultRoom = null;
    public boolean disableAutoRestore = false; // Is AutoRestore feature disabled? Default=no
    public Character defaultCharacter = null;
    public Client defaultClient = null;
    public String systemDir = null;
    public int screenwidth = 800; // default display sizes
    public int screenheight = 600;
    private int sceneWidth = 350;
    private int sceneHeight = 235;
    public Page sceneTemplate = null;// either null for default, or HTML Template
    public String authoremail = "";
    public String charactersDefaultAttrlist = null;
    public boolean compass = true;
    public String counterHtml = "";
    public int iconSize = 16;
    public int killedCounter = 0;	// Killed players counter (for stats)
    public Logger logger = null;
    public String logoSrc = "";
    public Messages msgs = null;
    private Panel panel = null;		// either null or a custom, non-default panel
    public Vector showproperties = null;
    private DictSorted panels = new DictSorted(); // Command panels
    public Dict hooks = new Dict(); // Hooks list
    public Dict players = new Dict();
    public int playersCounter = 0;	// Players Counter (for player ID)
    private int roomsCount = 0;
    public int savegamePersistence = 2;	// 2 = Hi 1= Med 0=low
    public int sceneLook = Const.LOOK_1STPERSON;
    public String adminPasswd = "";
    public String msgsFile = "msgs_eng.properties";
    public String clientFile = "standard.client";
    public String clientScript = "client.script";
    public Vector stopwords = new Vector();
    protected Dict skins = new Dict();
    private int peopleCount = 0;
    public Token interphone = new Token(0.0);
    protected boolean reset = false;
    public String encoding = "UTF-8"; // Default encoding format for files
    protected boolean eventsEnabled = false; // Are events enabled?
    protected Calendar startDate;
    public String navigatorUrl = "/dimx/servlet/cleoni.adv.multiplayer";
    private Calendar nextTick = null;
    public int tickLength = 30; // Seconds for a tick (30)
    public String urlHelp = null;
    public String imagesFolder = "";
    public int msgListSize = 10;
    public String map = null; // URL to the world's map
    public Object lock = new Object(); // Synchronisation tool
    public Dict moveQueue = new Dict(); // MoveOutside queue (objects to be moved)
    public Dict moveQueueDest = new Dict(); // MoveOutside queue destinations (referred to moveQueue)
    public Object moveQueueLock = new Object(); // Synchronisation tool
    public Object custCmdProc = null; // Custom Command Processor
    public String cluster = null; // Cluster this world has been assigned to
    public String slot = null; // Slot the world has been assigned to
    public String analytics = ""; // Analytics (visit tracking) code

    /**
     * Evaluates a specific SmallBasic expression and returns the result
     * @param expression String expression (SmallBasic code)
     * @param owner 
     * @param agent 
     * @param targetid 
     * @param actualpar Vector that will be considered as "input"
     * @throws cleoni.adv.DimxException 
     * @return a token with the result
     */
    public Token evaluateExpression(String expression, DimxObject owner, AdvObject agent, String targetid, DictSorted input) throws DimxException {
        Token eventres = null;

        Varspace newvarsp = new Varspace();
        newvarsp.varSet("input", new Token(input));
        logger.debug("Evaluating expression");

        String ownerId = null;
        if (owner != null) {
            ownerId = owner.id;
        }
        DimxParser p = new DimxParser(world, newvarsp, 0, ownerId);
        p.agent = agent;
        p.target = targetid;
        p.feed(expression);
        Token t = p.evalExpression(p.lookupToken(), 0);

        return t;
    }

    /**
     * Executes an EVENT
     * @param eventId ID ov the event to be executed
     * @param owner 
     * @param agent 
     * @param actualpar 
     * @param defaultResult 
     * @param mustExist if true, the event must exist and if missing a DimxException is thrown. If false, missing event is silently tolerated
     * @throws cleoni.adv.DimxException 
     * @return 
     */
    public Token execute(String eventId, DimxObject owner, AdvObject agent, DictSorted actualpar, Token defaultResult, boolean mustExist) throws DimxException {
        Event e = null;
        try {
            ManageableObject moe = owner.varGet(eventId).objVal();
            if (moe != null) {
                e = (Event) moe;
            }
        } catch (ClassCastException exc) {
        }
        Token eventres = null;
        if (e != null) {
            Varspace newvarsp = new Varspace(e.getFormalParams(), actualpar, true);
            newvarsp.varSet("input", new Token(actualpar));
            logger.debug("Calling " + e.toString() + " with varspace: " + newvarsp);
            ActionsRunner runner = new ActionsRunner(this, newvarsp, e.getActions(), e.actionsStartLine, e.fileName, e.fileShort, owner.id, agent, "", 1);
            try {
                String myid = e.fileShort + ":" + e.actionsStartLine;
                this.optiCount(myid);
                Calendar now = Calendar.getInstance();
                eventres = runner.run();
                Calendar then = Calendar.getInstance();
                this.optiConsumed(myid, then.getTimeInMillis() - now.getTimeInMillis());
            } catch (DimxException exc) {
                // Already documented - pass on as Nested
                throw new NestedException(exc.getMessage());
            }
        } else if (mustExist) {
            throw new DimxException("EVENT/Sub/Function does not exist: " + eventId);
        }
        if (eventres == null) {
            eventres = defaultResult;
        }
        return eventres;
    }

    /** Triggers an EVENT, so that the corresponding SmallBasic code is executed.
     * If the EVENT is not defined, the defaultResult is returned
     * NOTE - This function is almost equal to fireEvent_t (modified copy of this one) - please keep them aligned or merge them
     * @return result of the EVENT execution (Return statement)
     * @param defaultResult
     * @param e - Must exist and be non-null
     * @param owner
     * @param agent
     * @param target
     * @throws DimxException
     */
    public boolean fireEvent(Event e, AdvObject owner, String agent, String target, boolean defaultResult) throws DimxException {
        if (eventsEnabled) {
            logger.debug("world.fireEvent: event: " + e.id + "($OWNER=" + owner + ",$AGENT=" + agent + ",$TARGET=" + target + ")");
            return e.fire(owner, agent, target, new DictSorted(), defaultResult);
        }
        return defaultResult;
    }

    /** Triggers an EVENT, so that the corresponding SmallBasic code is executed.
     * If the EVENT is not defined, the defaultResult is returned
     * @return result of the EVENT execution (Return statement)
     * @param defaultResult
     * @param eventId
     * @param owner
     * @param agent
     * @param target
     * @throws DimxException
     */
    public boolean fireEvent(String eventId, DimxObject owner, String agent, String target, DictSorted input, boolean defaultResult, boolean mustExist) throws DimxException {
        if (eventsEnabled) {
            logger.debug("world.fireEvent: " + eventId + "($OWNER=" + owner + ",$AGENT=" + agent + ",$TARGET=" + target + ",input=" + input + ")");

            if (owner == null) {
                owner = this;
            }
            Token t = owner.varGet(eventId, false);
            Event e = null;

            if (t != null && t.isObject()) {
                try {
                    e = (Event) t.objVal();
                } catch (ClassCastException ex) {
                    // We searched for the EVENT, and got a NULL.
                    logger.debug("Not an event: " + t);
                }
            }

            if (e == null) {
                // It might be a "Missing event code" error. But let's look at a special case
                if (owner != this) {
                    // Last chance - custom defined generic EVENTS
                    t = this.varGet(eventId, false);
                    if (t != null) {
                        e = null;
                        try {
                            e = (Event) t.objVal();
                        } catch (ClassCastException ex) {
                            // We searched for the EVENT, and got a NULL.
                        }
                    }
                }
            }

            if (e != null) {
                return e.fire(owner, agent, target, input, defaultResult);
            } else {
                if (mustExist) {
                    throw new DimxException("Missing event code for: " + eventId);
                }
            }
        } else {
            logger.debug(eventId + " but events disabled");
        }
        return defaultResult;
    }

    /** Triggers an EVENT, so that the corresponding SmallBasic code is executed.
     * If the EVENT is not defined, the defaultResult is returned.
     * NOTE - This function is almost equal to fireEvent - please keep them aligned or merge them
     * @return result of the EVENT execution (Return statement)
     * @param defaultResult
     * @param eventId
     * @param owner
     * @param agent
     * @param target
     * @throws DimxException
     */
    public Token fireEvent_t(String eventId, DimxObject owner, String agent, String target, DictSorted params, Token defaultResult, boolean mustExist) throws DimxException {
        if (eventsEnabled) {
            logger.debug("world.fireEvent: " + eventId + "($OWNER=" + owner + ",$AGENT=" + agent + ",$TARGET=" + target + ",params=" + params + ")");

            if (owner == null) {
                owner = this;
            }
            Token t = owner.varGet(eventId, false);
            Event e = null;

            if (t != null && t.isObject()) {
                try {
                    e = (Event) t.objVal();
                } catch (ClassCastException ex) {
                    // We searched for the EVENT, and got a NULL.
                    logger.debug("Not an event: " + t);
                }
            }

            if (e == null) {
                // It might be a "Missing event code" error. But let's look at a special case
                if (owner != this) {
                    // Last chance - custom defined generic EVENTS
                    t = this.varGet(eventId, false);
                    if (t != null) {
                        e = null;
                        try {
                            e = (Event) t.objVal();
                        } catch (ClassCastException ex) {
                            // We searched for the EVENT, and got a NULL.
                        }
                    }
                }
            }

            if (e != null) {
                return e.fire_t(owner, agent, target, params, defaultResult);
            } else {
                if (mustExist) {
                    throw new DimxException("Missing event code for: " + eventId);
                }
            }
        } else {
            logger.debug(eventId + " but events disabled");
        }
        return defaultResult;
    }

    /**
     * @param eventId
     * @param owner
     * @param agent
     * @param target
     * @param defaultResult
     * @throws DimxException
     * @return
     */
    public boolean fireEvent(String eventId, AdvObject owner, String agent, String target, boolean defaultResult) throws DimxException {
        return fireEvent(eventId, owner, agent, target, new DictSorted(), defaultResult, false);
    }

    /**
     * @return
     */
    public String getNextPeopleId() {
        return "_p" + (playersCounter++);
    }

    /**
     * @return
     */
    public String getNextId() {
        return "_obj" + (dynObjCount++);
    }

    /**
     * @param id
     * @return
     */
    public AdvObject getObject(String id) {
        AdvObject x = objects.getAdvObject(id);
        if (x == null || x.world == this) {
            return x;
        } else {
            return null;
        }
    }

    /** Gets an object from current world
     * @param t Token incapsulating argument object itself or object id (String)
     * @return The object or null
     */
    public AdvObject getObject(Token t) {
        if (t.isObject()) {
            AdvObject x = (AdvObject) t.objVal();
            if (x.world == this) {
                return x;
            } else {
                return null;
            }
        } else {
            return objects.getAdvObject(t.strVal());
        }
    }

    /**
     * @param roomId
     * @return
     */
    public Room getRoom(String roomId) {
        return objects.getRoom(roomId);
    }

    /**
     * @param key
     * @throws DimxException
     * @return
     */
    public String getSetting(String key, String adefault) throws DimxException {

        Cluster mycluster = world.getCluster();
        java.sql.Connection dbc = null;
        if (mycluster.dbConnected) {
            dbc = mycluster.dbConn(null);
        }
        if (dbc == null) { // Normal get
            try {
                java.util.Properties p = new java.util.Properties();
                try {
                    p.load(new java.io.FileInputStream(this.getSavegameFile()));
                } catch (java.io.FileNotFoundException e) {
                    return adefault;
                }

                String res = p.getProperty(key);
                if (res == null || res.equals("")) {
                    res = adefault;
                }
                return res;
            } catch (Exception e) {
                logger.log(e);
                throw new DimxException(e.toString() + "\n" + e.getMessage());
            }
        } else { // DB supported
            String res = Utils.getSettingDB(key, adefault, dbc, mycluster.id);
            try {
                dbc.close();
            } catch (Exception e) {
            }
            return res;
        }

    }

    /*
     * Ritorna il riferimento alla skin richiesta
     * se non la trova torna la prima della lista
     *
     */
    /**
     * @param skinId
     * @return chosen skin, default (first one) if not found
     */
    public Skin getSkin(String skinId) {
        if (skinId == null) {
            skinId = "";
        }
        Skin s = (Skin) skins.get(skinId);
        if (s == null) {
            s = (Skin) skins.elementAt(0);
        }
        return s;
    }

    /**
     * @param id
     * @return
     */
    public void recordLogin(String playerId, String itemslist) throws DimxException {
        String ukey = Utils.stringReplace(playerId.toLowerCase(), " ", "_", false);
        //if (getSetting(ukey+"_when", null) == null) { // Saved profile....

        //}
        saveSetting(ukey + "_login", Utils.cTimeStamp(java.util.Calendar.getInstance().getTime()));
        if (itemslist != null && !itemslist.equals("")) {
            saveSetting(ukey + "_items", itemslist);
        }
    }

    /**
     * Removes an object from current world
     * @return true or false upon success
     * @param x object to be removed
     */
    public boolean removeObject(AdvObject x) {
        x.world = null;
        if (objects.remove(x.id)) {
            if (x.container != null) {
                return x.container.getContents().remove(x.id);
            }
            return true;
        } else {
            return false;
        }
    }

    /*
     *
     *	Returns null if accepted
     *
     */
    /**
     * @param what
     * @return null if accepted, non null in case of problems (result is error message)
     *
     */
    public String requestMovement(AdvObject what) {
        logger.debug("Preparing to receive " + what + " from world: " + what.world);
        return null;
    }

    /**
     * @param key
     * @param val
     * @throws DimxException
     * @return
     */
    public boolean saveSetting(String key, String val) throws DimxException {
        String savegameFile = getSavegameFile();
        try {
            Cluster mycluster = world.getCluster();
            java.sql.Connection dbc = null;
            if (mycluster.dbConnected) {
                dbc = mycluster.dbConn(null);
            }
            if (dbc == null) { // Normal save
                java.util.Properties p = new java.util.Properties();
                try {
                    p.load(new java.io.FileInputStream(savegameFile));
                } catch (java.io.FileNotFoundException e) {
                    logger.debug("Creating game profiles database");
                }
                synchronized (this.getCluster().saveFileLock) {
                    // Save properties
                    java.io.FileOutputStream out = new java.io.FileOutputStream(savegameFile);
                    p.setProperty(key, val);
                    p.store(out, "--- This is a generated file. Do not edit ---");
                    out.close();
                }
            } else { // DB supported
                boolean res = Utils.saveSettingDB(key, val, dbc, mycluster.id);
                try {
                    dbc.close();
                } catch (Exception e) {
                }
                return res;
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new DimxException(e.toString() + " catched in Player.saveGame.\n" + e.getStackTrace().toString());
        }
        return true;
    }

    /**
     * @param cmd
     * @throws DimxException
     * @return
     */
    public boolean sendCmd(String cmd) throws DimxException {
        // sends the specified command
        logger.debug("Sending: " + cmd + " to the whole world");

        AdvObject o;
        for (int i = 0; i < objects.size(); i++) {
            try {
                o = (AdvObject) objects.elementAt(i);
                o.sendCmd(cmd);
            } catch (ClassCastException e) {
                // Skip this - not a player
            }

        }
        return true;
    }

    /**
     * @throws DimxException
     */
    public void senseLogoff() throws DimxException {
        logger.debug("Sensing logoff...");
        for (int i = 0; i < players.size(); i++) {
            Player p = (Player) players.elementAt(i);
            if (!p.getClient().contactSince(tickLength * 2)) {
                logger.debug("lost contact with " + p + " (using " + p.getClient().browser + " - forcing disconnect");
                removePeople(p, Const.KICKOUT, Const.DROP_ITEMS, msgs.actualize(msgs.msg[153], p.getName())); // + " (using " + p.getClient().browser);
            }
        }
    }

    /**
     * @param sw
     */
    public void switchEvents(boolean sw) {
        eventsEnabled = sw;
        logger.debug("Events are now " + (sw ? "on" : "off"));
    }

    /**
     * @throws DimxException
     */
    public void tick() throws DimxException {
        Calendar stopNow = Calendar.getInstance();

        if (stopNow.after(nextTick)) { // Tick time!!
            stopNow.add(Calendar.SECOND, tickLength);
            nextTick = stopNow;
            logger.debug("*Tick* - next at " + nextTick.getTime().toString());

            fireEvent("onTick", null, null, "", false);

            // Check DB conn if broken
            if (server.isDBConfigured() && !world.getCluster().dbConnected) {
                java.sql.Connection dbc = world.getCluster().dbConn(null);
                try {
                    dbc.close();
                } catch (Exception e) {
                }
            }

            // Check survival
            logger.debug("Checking survival...");

            // For Living() exit status
            for (int i = objects.size() - 1; i >= 0; i--) {
                AdvObject p = objects.advObjectAt(i);
                if (p != null && p.isaCharacter() && p.container != null) {
                    // If it is a Character and it's somewhere in the game...

                    if (muting > 1 && p.isPlayer()) {  // Decrement mute
                        Token t = p.varGet("mute", Const.GETREF);
                        if (t.intVal() > 0) {
                            t.setVal(t.intVal() - 1);
                        } else {
                            t.setVal(""); // Erase from profile
                        }
                    }

                    boolean result = fireEvent("Living", p, p.id, p.id, true);

                    if (!result) {
                        logger.debug(p.getName() + " dies because EVENT Living returned false:");
                        if (p.isPlayer()) {
                            fireEvent("onDie", p, null, null, false);
                            p.sendCmd(Const.CMDE_REFRSCENE);
                            p.hear(defaultCharacter, msgs.msg[179]);
                            killedCounter++;
                            removePeople((Character) p, Const.STAYZOMBIE, Const.DROP_ITEMS, null);
                        } else {
                            removePeople((Character) p, Const.KICKOUT, Const.DROP_ITEMS, null);
                        }
                    }
                }
            }

            // For lost players
            senseLogoff();
        }

    }

    /**
     * @return
     */
    public String toString() {
        return this.getName();
    }

    /**
     * World constructor comment.
     */
    public World(multiplayer aserver, Messages amsgs, String aName, String clusterid, String aImagesFolder, Logger aLogger, String aHelpUrl) throws DimxException {
        super();
        world = this;
        server = aserver;
        nextTick = Calendar.getInstance();
        nextTick.add(Calendar.SECOND, tickLength);
        Random rnd = new Random();
        long uniqueid = ((System.currentTimeMillis() >>> 16) << 16) + rnd.nextInt();
        id = aName;
        instanceid = aName + "/" + uniqueid;
        msgs = amsgs;
        urlHelp = aHelpUrl;

        buildPanelDefault();
        buildPanelExiting();
        buildPanelChat();
        buildPanelConnecting();
        buildPanelConnect();

        defaultClient = new Client(this, 3, false, false, 800, 600, "No Browser");
        name = new Token(aName);
        imagesFolder = aImagesFolder;
        logger = aLogger;
        logger.debug("World created with id=" + id);

        this.varGet("__optiFrequency", Const.GETREF).assign(new Token(new DictSorted()), this);
        this.varGet("__optiConsumed", Const.GETREF).assign(new Token(new DictSorted()), this);

        cluster = clusterid;
        if (cluster.equals("")) {
            cluster = id;
        }
        // Skin vector must be filled externally
    }

    /**
     * @param c
     * @throws DimxException
     */
    protected void addCharacter(Character c) throws DimxException {
        //remember to set world field in character obj
        if (objects.put(c.id, c)) {
            peopleCount++;
        } else {
            throw new DimxException("Duplicate definition of: " + c.id);
        }
    }

    /**
     * @param i
     * @param define_id
     * @throws DimxException
     */
    protected void addItem(AdvObject i, boolean define_id) throws DimxException {
        // Try a cast to check class type compatibility
        Item i2 = (Item) i;
        i.world = this;
        if (define_id) {
            i.id = getNextId();
        }
        if (objects.put(i.id, i)) {
        } else {
            throw new DimxException("Duplicate definition of: " + i.id);
        }
    }

    /**
     * @param w
     * @param define_id
     * @throws DimxException
     * @return
     */
    protected boolean addLink(Link w, boolean define_id) throws DimxException {
        if (define_id) {
            w.id = getNextId();
        }
        if (objects.put(w.id, w)) {
            w.getRoom().addLink(w);
            if (w.isBidirectional) {
                w.getTarget(w.getRoom().id).addLink(w);
            }
            waysCount++;
            return true;
        } else {
            throw new DimxException("Duplicate definition of: " + w.id);
        }
    }

    /**
     * @param thisPlayer
     * @param startingRoomid
     * @param panelId
     * @param listProperties
     * @param listContents
     * @throws DimxException
     * @return
     */
    public Player addPlayer(Player thisPlayer, String startingRoomid, String panelId, String listProperties, String listContents, String remoteAddr) throws DimxException {
        // To do: Merge this one with addCharacter so that the player/character is created
        // Inside here and not externally

        if (startingRoomid == null || startingRoomid.equals("")) {
            startingRoomid = defaultRoom;
        }
        thisPlayer.defContainer = startingRoomid;
        thisPlayer.world = this;
        thisPlayer.setPanel(panelId);
        if (thisPlayer.id == null) {
            thisPlayer.id = getNextPeopleId();
        }
        addCharacter(thisPlayer);
        players.put(thisPlayer.id, thisPlayer);

        // Restore properties
        thisPlayer.restoreProperties(listProperties);
        if (remoteAddr != null) {
            thisPlayer.varGet("remoteAddr", true).assign(new Token(remoteAddr), this);
        }

        //logger.debug("Before onNew " + thisPlayer.name + " is in " +thisPlayer.getRoom());
        // onNew event
        fireEvent("onNew", null, thisPlayer.id, "", false);

        //logger.debug("After onNew " + thisPlayer.name + " is in " +thisPlayer.getRoom());

        if (thisPlayer.getRoom() == null) {
            // Restore location
            thisPlayer.reset();
        }

        return thisPlayer;
    }

    /**
     * @param username
     * @param myProfile
     * @param aClient
     * @throws DimxException
     * @return
     */
    public Player addPlayer(String username, Dict myProfile, Client aClient, String remoteAddr) throws DimxException {
        // To do: Merge this one with addCharacter so that the player/character is created
        // Inside here and not externally

        String startingRoomid = myProfile.getS("location");
        String panelId = myProfile.getS("panel");
        String listProperties = myProfile.getS("properties");
        String listEvents = myProfile.getS("events");
        String listContents = myProfile.getS("items");
        String skinId = myProfile.getS("skin");

        if (startingRoomid == null || startingRoomid.equals("")) {
            startingRoomid = defaultRoom;
        }

        Player thisPlayer = new Player(this, username, getNextPeopleId(), getSkin(skinId), null /* icon */, defaultCharacter.capacity.intVal(),
                charactersDefaultAttrlist, startingRoomid, aClient);
        thisPlayer.restoreEvents(listEvents);
        // Avatar image restore
        if (myProfile.getS("image") != null) {
            String imagesrc = myProfile.getS("image");
            Image im = null;
            if (imagesrc.substring(0, 5).equals("NewIm")) {
                DimxParser p = new DimxParser(this, Const.WORLDVARS, 0, null);
                p.feed(imagesrc);
                Token t = p.evalExpression(p.lookupToken(), 0);
                im = t.imageVal();
            } else if (imagesrc.substring(0, 5).equals("IMAGE")) {
                // Compatibility towards older profiles
                WorldLoader wl = new WorldLoader(this, imagesrc);
                im = wl.parseImage();
            }
            thisPlayer.setImage("N", im);
        } else {
            thisPlayer.setImage("N", (Image) thisPlayer.skin.picPlayer.clone());
        }
        // Actually add to world
        thisPlayer = addPlayer(thisPlayer, startingRoomid, panelId, listProperties, listContents, remoteAddr);

        // Restore items
        String listContentsAfter = thisPlayer.restoreContents(listContents);
        world.recordLogin(thisPlayer.getName(), listContentsAfter);

        if (thisPlayer != null) {
            thisPlayer.password = Utils.cStr(myProfile.get("pass"));
        }
        return thisPlayer;
    }

    /**
     * @param r
     * @param isDefault
     * @throws DimxException
     */
    protected void addRoom(Room r, boolean isDefault) throws DimxException {
        if (objects.put(r.id, r)) {
            //rooms.put(aRoom.id,aRoom);
            if (isDefault || defaultRoom == null) {
                defaultRoom = r.id;
            }
            roomsCount++;
        } else {
            throw new DimxException("Duplicate definition of: " + r.id);
        }
    }

    /**
     * Insert the method's description here.
     * Creation date: (24/09/2003 16.58.27)
     */
    private void buildPanelDefault() throws DimxException {
        Panel myPanel = new Panel(this, "default");

        Ctrl myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "use", msgs.cmd[3], msgs.cmd[3] + " ...", null, "O", null);
        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "use2", msgs.cmd[4], "Use ... With ...", null, "OO", null);
        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "look", msgs.cmd[5], msgs.cmd[5] + " ... ", null, "O", null);
        myBtn = new Ctrl(myPanel, Const.CTRL_GHOST, "click", "Click", "Click" + " ... ", null, "O", null);

        myBtn = new Ctrl(myPanel, "-");

        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "open", msgs.cmd[6], msgs.cmd[6] + " ... ", null, "O", null);
        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "close", msgs.cmd[7], msgs.cmd[7] + " ... ", null, "O", null);
        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "pick", msgs.cmd[8], msgs.cmd[8] + " ... ", null, "O", null);

        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "drop", msgs.cmd[9], msgs.cmd[9] + " ... ", null, "O", null);

        myBtn = new Ctrl(myPanel, "-");

        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "search", msgs.cmd[10], msgs.cmd[10], null, null, null);
        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "hide", msgs.cmd[11], msgs.cmd[11] + " ... ", null, "O", null);
        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "put", msgs.cmd[12], msgs.cmd[12] + " ... into ...", null, "OO", null);
        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "give", msgs.cmd[13], msgs.cmd[13] + " ... to ...", null, "OO", null);


        myBtn = new Ctrl(myPanel, "-");

        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "enter", msgs.cmd[22], msgs.cmd[22] + " ... ", null, "O", null);
        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "exit", msgs.cmd[23], msgs.cmd[23], null, null, null);

        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, "-");

        myBtn = new Ctrl(myPanel, Const.CTRL_TEXTBOX, "txtBox", null, "15", null, null, null);
        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "say", msgs.cmd[14], msgs.cmd[14] + " ... to ...", null, "TO", null);

        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, "-");

        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "clear", msgs.cmd[15], null, null, null, null);
        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "help", msgs.cmd[26], this.urlHelp, null, null, null);

        myBtn = new Ctrl(myPanel, "-");

        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "save", msgs.cmd[24], msgs.cmd[24], null, "T", null);
        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "savexit", msgs.cmd[17], msgs.cmd[17], null, "T", null);

        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "logout", msgs.cmd[2], msgs.cmd[2], null, null, null);

        panels.put(myPanel.id, myPanel);
        panel = myPanel; // Set as default
    }

    /*
     * Bans the specified ip
     */
    public void banIp(String ip) throws DimxException {
        Token t = this.varGet("_bannedClients", Const.GETREF);
        t.setVal(t.strVal() + ip + "|");
        t.setLiteral();
    }

    /**
     * Insert the method's description here.
     * Creation date: (24/09/2003 16.58.27)
     */
    private void buildPanelChat() throws DimxException {
        Panel myPanel = new Panel(this, "chat");

        Ctrl myBtn = null;

        myBtn = new Ctrl(myPanel, Const.CTRL_LABEL, null, msgs.msg[10], null, null, null, null);
        myBtn = new Ctrl(myPanel, "-");

        myBtn = new Ctrl(myPanel, Const.CTRL_TEXTBOX, "txtBox", null, null, null, null, null);

        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, "-");

        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "use", msgs.cmd[3], msgs.cmd[3] + " ...", null, "O", null);

        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, "-");

        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "help", msgs.cmd[26], this.urlHelp, null, null, null);
        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "savexit", msgs.cmd[17], msgs.cmd[17], null, "T", null);
        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "logout", msgs.cmd[2], msgs.cmd[2], null, null, null);

        panels.put(myPanel.id, myPanel);
    }

    private void buildPanelExiting() throws DimxException {
        Panel myPanel = new Panel(this, "exiting");

        Ctrl myBtn = null;

        myBtn = new Ctrl(myPanel, Const.CTRL_LABEL, "lbl1", msgs.msg[7] + ":", null, null, null, null);

        myBtn = new Ctrl(myPanel, Const.CTRL_TEXTBOX, "txtBox", null, null, null, null, null);

        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, "-");

        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "savexit", msgs.cmd[17], msgs.cmd[17], null, "T", null);
        myPanel.buttons.put(myBtn.id, myBtn);

        myBtn = new Ctrl(myPanel, "-");

        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "return", msgs.cmd[18], msgs.cmd[18], null, null, null);

        myBtn = new Ctrl(myPanel, "-");

        // Reuse default panel's help button
        myPanel.buttons.put("help", (Ctrl) ((Panel) getPanel("default")).buttons.getIC("help"));

        panels.put(myPanel.id, myPanel);
    }

    private void buildPanelConnect() throws DimxException {
        Panel myPanel = new Panel(this, "connect");

        Ctrl myBtn = null;
        myBtn = new Ctrl(myPanel, Const.CTRL_LABEL, "lbl0",/* msgs.msg[#] */ "Nickname" + ":", null, null, null, null);
        myBtn = new Ctrl(myPanel, Const.CTRL_TEXTBOX, "username", "", "15", "nickname", "cookie", null);
        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, Const.CTRL_LABEL, "lbl1",/* msgs.msg[#] */ "Skin" + ":", null, null, null, null);

        String values = "";
        myBtn = new Ctrl(myPanel, Const.CTRL_DROPDOWN, "skin",/* def from cookie */ null, values, "skin", "cookie", null);
        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, Const.CTRL_LABEL, "lbl2", msgs.msg[3] + ":", null, null, null, null);
        myBtn = new Ctrl(myPanel, Const.CTRL_CHECKBOX, "audio", "ON", "Left(\"$1\",1)=1", "sounds", "cookie", null);
        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, Const.CTRL_LABEL, "lbl3", msgs.msg[12] + ":", null, null, null, null);
        myBtn = new Ctrl(myPanel, Const.CTRL_CHECKBOX, "music", "ON", "Right(\"$1\",1)=1", "sounds", "cookie", null);
        myBtn = new Ctrl(myPanel, Const.CTRL_LABEL, "lbl3a", "<FONT SIZE=\"1\">[<A TARGET=\"_blank\" HREF=\"" + msgs.msg[14] + "\">" + msgs.msg[13] + "</A>]</FONT>", null, null, null, null);
        myBtn = new Ctrl(myPanel, "-");

        myBtn = new Ctrl(myPanel, Const.CTRL_LABEL, "lbl4", /* msgs.msg[#] */ "Screen size" + ":", null, null, null, null);
        // Builds values string
        values = "";
        for (int i = 0; i < Const.screenSizes.length; i++) {
            if (i > 0) {
                values = values + ",";
            }
            values = values + Const.screenSizes[i];
        }
        myBtn = new Ctrl(myPanel, Const.CTRL_DROPDOWN, "screensize",/* def from cookie */ null, values, "screenmode", "cookie", null);
        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, "-");

        myBtn = new Ctrl(myPanel, Const.CTRL_BUTTON, "help", msgs.cmd[26], urlHelp, null, null, null);
        myBtn = new Ctrl(myPanel, Const.CTRL_SUBMIT, "login", msgs.cmd[1], msgs.cmd[1], null, null, null);
        myBtn = new Ctrl(myPanel, Const.CTRL_LABEL, "lbl5", "<SCRIPT TYPE=\"text/javascript\">this.document.forms[0].cmd.value='login';</SCRIPT>", null, null, null, null);
        myBtn = new Ctrl(myPanel, Const.CTRL_LABEL, "lbl6", "<HR><P ALIGN=\"CENTER\"><FONT SIZE=\"1\">" + world.msgs.msg[4] + " <B><a href=\"https://www.dimensionex.net/\" TARGET=\"_blank\">Dimensione X</A></B> v. " + this.server.myVersion + "</FONT></P>", null, null, null, null);

        panels.put(myPanel.id, myPanel);
    }

    public void fixSkins(Panel myPanel) throws DimxException {
        // Builds values string
        String values = "";
        for (int i = 0; i < this.skins.elementCount; i++) {
            if (i > 0) {
                values = values + ",";
            }
            Skin s = (Skin) this.skins.elementAt(i);
            values = values + s.id + "=" + s.name;
        }
        if (this.skins.size()==1) { // no asking
            Ctrl myBtn = new Ctrl(myPanel, Const.CTRL_LABEL, "lbl1","", null, null, null, null);
            myBtn = new Ctrl(myPanel, Const.CTRL_LABEL, "skin","", null, null, null, null);
        } else {
            Ctrl myBtn = new Ctrl(myPanel, Const.CTRL_DROPDOWN, "skin",/* def from cookie */ null, values, "skin", "cookie", null);
        }
    }

    private void buildPanelConnecting() throws DimxException {
        Panel myPanel = new Panel(this, "connecting");

        Ctrl myBtn = null;
        myBtn = new Ctrl(myPanel, Const.CTRL_LABEL, "lbl0", msgs.msg[9] + ":", null, null, null, null);
        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, Const.CTRL_PASSWORD, "pass", "", "15", null, null, null);
        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, Const.CTRL_SUBMIT, "login", msgs.cmd[1], msgs.cmd[1], null, null, null);
        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, Const.CTRL_SUBMIT, "startscratch", msgs.cmd[21], msgs.cmd[21], null, null, null);
        myBtn = new Ctrl(myPanel, Const.CTRL_HIDDEN, "cmd", "login", null, null, "O", null);
        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, Const.CTRL_SUBMIT, "return", msgs.cmd[19], msgs.cmd[19], null, null, null);
        myBtn = new Ctrl(myPanel, "-");
        myBtn = new Ctrl(myPanel, "-");
        myPanel.hasFocus = "pass";

        // Reuse default panel's help button
        myPanel.buttons.put("help", (Ctrl) ((Panel) getPanel("default")).buttons.getIC("help"));

        panels.put(myPanel.id, myPanel);
    }

    /**
     * @param aPanel
     */
    public void definePanel(Panel aPanel) {
        panels.put(aPanel.id, aPanel);
        if (aPanel.id.equalsIgnoreCase("default")) {
            panel = aPanel;
        }
    }

    /**
     * @param msg
     * @throws DimxException
     * @return
     */
    public boolean display(String msg) throws DimxException {
        // Sets the specified panel for all the players in that room

        logger.debug("Displaying: " + msg + " in whole world");

        Player x;
        for (int i = 0; i < objects.size(); i++) {
            try {
                x = (Player) objects.advObjectAt(i);
                if (x != null) {
                    x.display(msg);
                }
            } catch (ClassCastException e) {
                // Skip this - not a character
            }

        }
        return true;
    }

    /**
     * Creates on-the-fly a collection of ALL buttons of ALL panels
     * Creation date: (24/09/2003 17.17.57)
     * @return cleoni.adv.Dict
     */
    public Dict getAllPanelButtons() {
        Dict result = new Dict();

        for (int p = 0; p < panels.size(); p++) {
            Panel panel = (Panel) panels.elementAt(p);

            for (int b = 0; b < panel.buttons.size(); b++) {
                Ctrl button = (Ctrl) panel.buttons.elementAt(b);

                if (button.type == Const.CTRL_BUTTON || button.type == Const.CTRL_IMAGEBUTTON || button.type == Const.CTRL_GHOST) {
                    result.put(button.id, button);
                }
            }
        }

        return result;
    }

    public Cluster getCluster() {
        return server.getCluster(cluster);
    }

    public Dict getContents() {
        return objects.getElements();
    }

    /**
     * @param o
     * @param skin
     * @param thisRoomId
     * @param facing
     * @return
     */
    public String getCustomIcon(AdvObject o, Skin skin, String thisRoomId, String facing) {
        // Gets custom icon according to the specified skin
        String ico = o.getIcon();
        if (ico == null || ico.equals("")) {
            if (o.isanItem()) {
                ico = skin.icoItem;
            } else if (o.isaCharacter()) {
                if (!o.isRobot()) {
                    ico = skin.icoPlayer;
                } else {
                    ico = skin.icoCharacter;
                }
            } else if (o.isLink()) {
                ico = ((Link) o).getIcon(thisRoomId, facing, skin);
                if (ico == null) {
                    ico = skin.icoWay;
                }
            }
        }
        return ico;
    }

    /**
     * @param face
     * @return
     */
    protected String getDirectionStr(String face) {
        if (face.equals("N")) {
            return msgs.msg[167];
        } else if (face.equals("S")) {
            return msgs.msg[168];
        } else if (face.equals("E")) {
            return msgs.msg[169];
        } else if (face.equals("W")) {
            return msgs.msg[170];
        } else if (face.equals("U")) {
            return msgs.msg[171];
        } else if (face.equals("D")) {
            return msgs.msg[172];
        } else {
            return "?";
        }
    }

    /**
     * @return
     */
    public Dict getObjects() {
        Dict objectsCopy = new Dict();
        for (int i = 0; i < objects.size(); i++) {
            AdvObject x = objects.advObjectAt(i);
            if (x != null) {
                objectsCopy.put(x.id, x);
            }
        }
        return objectsCopy;
    }

    /**
     * @param id
     * @return
     */
    public DimxObject getObjectExt(String id) {
        if (id == null || id.equals("")) {
            return null;
        } else {
            DimxObject x = objects.getAdvObject(id);
            if (x == null || x.world == this) {
                return x;
            } else {
                return null;
            }
        }
    }

    /**
     * @param id
     * @return
     */
    public DimxObject getObjectExt(Token t) {
        if (t.isObject()) {
            DimxObject x = t.dimxobjVal();
            if (x == null) {
                return null;
            }
            if (x.world == this) {
                return x;
            } else {
                return null;
            }
        } else {
            return getObjectExt(t.strVal());
        }
    }

    /**
     * @return
     */
    public Panel getPanel() {
        return panel;
    }

    /**
     * @param id
     * @return
     */
    public Panel getPanel(String id) {
        return (Panel) panels.getIC(id);
    }

    /**
     * @param id
     * @return
     */
    public Character getPeople(String id) {
        return objects.getCharacter(id);
    }

    /**
     * @param name
     * @return
     */
    public AdvObject getPlayer(String name) {
        AdvObject o = null;
        name = name.toLowerCase();
        for (int i = 0; i < players.elementCount; i++) {
            o = (AdvObject) players.elementAt(i);
            if (o.name.strVal().toLowerCase().equals(name)) {
                return o;
            }
        }
        return null;
    }

    /**
     * @param imageUrl
     * @return
     */
    public Room getRoomFromImageUrl(String imageUrl) {
        // Identifies a room by one of its' scene image URL
        for (int i = 0; i < objects.size(); i++) {
            AdvObject x = objects.advObjectAt(i);
            if (x != null && x.isaRoom()) {
                Room k = ((Room) x);
                if (k.hasImageUrl(imageUrl)) {
                    return k;
                }
            }
        }
        return null;
    }

    /**
     * @return
     */
    public String getHofFile() {
        if (cluster == null || cluster.equals("")) {
            return systemDir + Utils.stringReplace(this.getName().toLowerCase(), " ", "_", false) + ".hof";
        } else {
            return systemDir + Utils.stringReplace(cluster.toLowerCase(), " ", "_", false) + ".hof";
        }
    }

    /**
     * @return
     */
    public String getSavegameFile() {
        if (cluster == null || cluster.equals("")) {
            return systemDir + Utils.stringReplace(this.getName().toLowerCase(), " ", "_", false) + ".sav";
        } else {
            return systemDir + Utils.stringReplace(cluster.toLowerCase(), " ", "_", false) + ".sav";
        }
    }

    /**
     * @return
     */
    public int getSceneHeight() {
        return sceneHeight;
    }

    /**
     * @return
     */
    public int getSceneWidth() {
        return sceneWidth;
    }

    /**
     * @param agentname
     * @param message
     * @throws DimxException
     * @return
     */
    public boolean goal(String agentname, String message) throws DimxException {
        String finalmsg = message;
        if (finalmsg == null || finalmsg.equals("")) { // Create message
            finalmsg = msgs.actualize(msgs.msg[151], agentname, Utils.now()); // Default message
        }
        hear(null, finalmsg);

        this.varGet("mode", Const.GETREF).assign(new Token(0), this);

        // Save result on Hall of Fame
        java.util.Properties p = new java.util.Properties();
        int winnersCount = 0;
        try {
            p.load(new java.io.FileInputStream(getHofFile()));
            winnersCount = Utils.cInt(p.getProperty("winnersCount"));
        } catch (java.io.FileNotFoundException e) {
            logger.debug("Creating Hall of Fame database");
        } catch (Exception e) {
            throw new DimxException(e.getMessage());
        }


        p.setProperty("winner" + winnersCount + "_name", agentname);
        p.setProperty("winner" + winnersCount + "_when", Utils.now());
        p.setProperty("winner" + winnersCount + "_worldVersion", version);
        if (message != null) {
            p.setProperty("winner" + winnersCount + "_message", finalmsg);
        }
        winnersCount++;
        p.setProperty("winnersCount", Utils.cStr(winnersCount));

        // Store
        try {
            java.io.FileOutputStream out = new java.io.FileOutputStream(getHofFile());
            p.store(out, "--- This is a generated file. Do not edit ---");
            out.close();
        } catch (Exception e) {
            throw new DimxException(e.getMessage());
        }

        return true;
    }

    /**
     * @param from
     * @param msg
     * @throws DimxException
     * @return
     */
    public boolean hear(DimxObject from, String msg) throws DimxException {
        boolean res = true;
        // Tries to hear from the specified object, returns true if successful
        if (from == null) {
            from = defaultCharacter;
        }
        // The following is not posible for WORLD - owner cannot be world
        //this.fireEvent("onHear",this,from.id,msg,false);

        if (!msg.equals("")) {
            logger.debug("Speaking: " + msg + " to all players");

            Character x;
            for (int i = 0; i < objects.size(); i++) {
                try {
                    x = (Player) objects.advObjectAt(i);
                    if (x != null) {
                        x.hear(from, msg);
                    }
                } catch (ClassCastException e) {
                    // Skip this - not a character
                }
            }

        }
        return res;
    }

    /**
     * @return
     */
    public String htmlAdminSnapshot() throws DimxException {
        StringBuffer sb = new StringBuffer("");

        sb.append("<A NAME=\"top\">\n");
        sb.append("<DIV CLASS=text><H1>" + getName() + "</H1>\n");
        sb.append("World id: " + id + "<BR>\n");
        sb.append("Images folder: " + imagesFolder + "<BR>\n");
        sb.append("Server time is now: " + new java.util.Date() + "<BR>\n");
        sb.append("Interphone: " + interphone + "<BR>\n");
        sb.append("Current panel: " + getPanel().id + "<BR>\n");
        sb.append("Players:\n");
        for (int i = 0; i < players.size(); i++) {
            sb.append("<A HREF=\"#" + ((AdvObject) players.elementAt(i)).id + "\">" + ((AdvObject) players.elementAt(i)) + "</A>,");
        }
        sb.append("<BR>\n");


        sb.append("<UL><LI><A HREF=\"#rooms\">" + roomsCount + " rooms</A><br>");
        sb.append("<LI><A HREF=\"#characters\">" + peopleCount + " characters</A><br>");
        sb.append("<LI><A HREF=\"#events\">events</A><br>");
        sb.append("<LI><A HREF=\"#ways\">" + waysCount + " links</A><br>");
        sb.append("<LI><A HREF=\"#items\">items</A><br>");
        sb.append("<LI><A HREF=\"#properties\">properties</A><br>");
        sb.append("<LI><A HREF=\"#panels\">" + panels.size() + " panels</A><br>");
        sb.append("</UL>");


        sb.append("<p><A NAME=\"rooms\"><H1>" + roomsCount + " rooms</H1>\n");
        sb.append("<UL>");
        for (int i = 0; i < objects.size(); i++) {
            AdvObject x = objects.advObjectAt(i);
            if (x != null && x.isaRoom()) {
                Room k = ((Room) x);
                sb.append("<LI><B><A NAME=\"" + k.id + "\">" + k.toString() + "</A></B>\n");
                sb.append("<UL>");
                sb.append("<LI>properties: " + "\n<UL>\n");
                for (int j = 0; j < k.varsCount(); j++) {
                    sb.append("<LI><B>" + k.varGetIdAt(j) + "</B>=" + k.varGet(k.varGetIdAt(j), false) + "\n");
                }

                sb.append("<LI><B>image</B>=" + k.listImages());
                sb.append("<LI><B>description</B>=" + k.getDescription(null, null));

                sb.append("</UL>\n");
                if (!k.getPanel().id.equalsIgnoreCase("default")) {
                    sb.append("<LI>panel: " + k.getPanel());
                }
                sb.append("</UL>\n");
            }
        }
        sb.append("</UL><P>\n");
        sb.append("<A HREF=\"#top\">Summary</A>\n");

        sb.append("<p><A NAME=\"characters\"><H1>" + peopleCount + " characters</H1><UL>\n");
        for (int i = 0; i < objects.size(); i++) {
            AdvObject x = objects.advObjectAt(i);
            if (x != null && x.isaCharacter()) {
                Character k = ((Character) x);
                sb.append("<LI><B><A NAME=\"" + k.id + "\">" + k.toString() + "</A></B><UL>\n");
                sb.append("<LI>in " + k.container + "\n");
                sb.append("<LI>properties: " + "\n<UL>\n");
                for (int j = 0; j < k.varsCount(); j++) {
                    sb.append("<LI><B>" + k.varGetIdAt(j) + "</B>=" + k.varGet(k.varGetIdAt(j), false) + "\n");
                }
                sb.append("<LI><B>image</B>=" + k.listImages());
                sb.append("<LI><B>showmode</B>=" + k.showmode + "\n");
                sb.append("<LI><B>accepts</B>=" + k.accepting + "\n");
                sb.append("<LI><B>facing</B>=" + k.facing.strVal() + "\n");
                sb.append("</UL>\n");
                sb.append("<LI>contents: " + "\n<UL>\n");
                for (int j = 0; j < k.contents.size(); j++) {
                    sb.append("<LI>" + "<A HREF=\"#" + ((AdvObject) k.contents.elementAt(j)).id + "\">" + k.contents.elementAt(j) + "</A>\n");
                }
                sb.append("</UL>\n");
                sb.append("<LI>space: free=" + k.getFreeSpace() + "/used=" + k.getUsedSpace() + "\n");
                if (k.isPlayer()) {
                    Player p = (Player) k;
                    sb.append("<LI>" + p.getClient().getMessageCount() + " messages\n");
                    sb.append("<LI>latest contact: " + p.getClient().lastContact.getTime());
                    sb.append("<LI>panel: " + p.getPanel());
                    sb.append("<LI>prevpanelids: " + p.prevPanelIds);
                }
                sb.append("</UL>\n");

            }
        }
        sb.append("</UL>");
        sb.append("<A HREF=\"#top\">Summary</A>\n");


        sb.append("<p><A NAME=\"events\"><H1>Events</H1><UL>\n");
        for (int i = 0; i < properties.size(); i++) {
            try {
                ManageableObject x = ((Token) properties.elementAt(i)).objVal();
                Event ev = (Event) x;
                sb.append("<LI> " + ev + "\n");
            } catch (ClassCastException e) {
            }
        }
        for (int i = 0; i < objects.size(); i++) {
            AdvObject o = objects.advObjectAt(i);
            if (o != null) {
                DictSorted p = o.properties;
                for (int j = 0; j < p.size(); j++) {
                    try {
                        ManageableObject x = ((Token) p.elementAt(j)).objVal();
                        Event ev = (Event) x;
                        sb.append("<LI> " + ev + "\n");
                    } catch (ClassCastException e) {
                    }
                }
            }
        }
        sb.append("</UL>\n");
        sb.append("<A HREF=\"#top\">Summary</A>\n");

        sb.append("<p><A NAME=\"ways\"><H1>" + waysCount + " links</H1><UL>\n");
        for (int i = 0; i < objects.size(); i++) {
            AdvObject x = objects.advObjectAt(i);
            if (x != null && x.isLink()) {
                Link k = ((Link) x);

                sb.append("<LI> " + k + "<UL>\n");
                sb.append("<LI>properties: <UL>\n");
                for (int j = 0; j < k.varsCount(); j++) {
                    sb.append("<LI><B>" + k.varGetIdAt(j) + "</B>=" + k.varGet(k.varGetIdAt(j), false) + "\n");
                }
                sb.append("<LI><B>image</B>=" + k.listImages());
                sb.append("<LI><B>showmode</B>=" + k.showmode + "\n");
                sb.append("</UL>\n");

                sb.append("</UL>\n");
            }
        }
        sb.append("</UL>\n");
        sb.append("<A HREF=\"#top\">Summary</A>\n");

        sb.append("<p><A NAME=\"items\"><H1>Items</H1>\n");
        sb.append("<UL>\n");

        int count = 0;
        for (int i = 0; i < objects.size(); i++) {
            AdvObject x = objects.advObjectAt(i);
            if (x != null && x.isanItem()) {
                Item k = ((Item) x);
                count++;
                sb.append("<LI>");
                if (k.isVehicle()) {
                    sb.append("VEHICLE ");
                }
                String htmlcont = null;
                if (k.container != null) {
                    htmlcont = "<A HREF=\"#" + k.container.id + "\">" + k.container + "</A>";
                } else {
                    htmlcont = "NULL";
                }
                sb.append("<A NAME=\"" + k.id + "\"><B>" + k.toString() + "</B></A> in " + htmlcont);
                sb.append("<UL>\n");
                sb.append("<LI>properties: <UL>\n");
                for (int j = 0; j < k.varsCount(); j++) {
                    sb.append("<LI><B>" + k.varGetIdAt(j) + "</B>=" + k.varGet(k.varGetIdAt(j), false) + "\n");
                }
                sb.append("<LI><B>capacity</B>=" + k.capacity + "\n");
                sb.append("<LI><B>volume</B>=" + k.volume + "\n");
                sb.append("<LI><B>container</B>=" + htmlcont);
                sb.append("<LI><B>showmode</B>=" + k.showmode + "\n");
                sb.append("<LI><B>icon</B>=" + k.getIcon() + "\n");

                sb.append("<LI><B>image</B>=" + k.listImages());

                sb.append("<LI><B>world</B>=" + k.world + "\n");
                sb.append("</UL>\n"); // Closes properties
                sb.append("</UL>\n");
            }
        }

        sb.append("Items count=" + count);
        sb.append("</UL>");
        sb.append("<A HREF=\"#top\">Summary</A>\n");

        sb.append("<p><A NAME=\"properties\"><H1>Properties</H1><UL>\n");
        for (int i = 0; i < properties.size(); i++) {
            Object o = properties.elementAt(i);
            String key = properties.keyAt(i);
            if (!key.startsWith("__")) { // Hide __variables
                if (o == null) {
                    sb.append("<LI><B>" + key + "</B>");
                    sb.append("null");
                } else {
                    if (!((Token) o).isEvent()) {
                        sb.append("<LI><B>" + key + "</B>");
                        sb.append("=" + o);
                    }
                }
            }
        }
        sb.append("</UL>\n");
        sb.append("<A HREF=\"#top\">Summary</A>\n");

        sb.append("<p><A NAME=\"panels\"><H1>Panels</H1><UL>\n");
        for (int i = 0; i < panels.size(); i++) {
            Panel x = ((Panel) panels.elementAt(i));
            sb.append("<LI> " + Utils.stringReplace(x.toString(), "\n", "<BR>", false) + "\n");
        }
        sb.append("</UL>\n");
        sb.append("<A HREF=\"#top\">Summary</A>\n");

        sb.append("</DIV>");

        return sb.toString();
    }

    /**
     * @return
     */
    public String htmlCharset() {
        return "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + msgs.charset + "\">";
    }

    /**
     * @param o
     * @param skin
     * @param thisRoomId
     * @param facing
     * @return
     */
    public String htmlIcon(AdvObject o, Skin skin, String thisRoomId, String facing) {
        if (o == null) {
            return "<IMG SRC=\"" + skin.picSpacer + "\" HEIGHT=" + iconSize + " WIDTH=" + iconSize + " ALT=\"\">";
        } else {
            // Fixup icon
            String ico = getCustomIcon(o, skin, thisRoomId, facing);

            // Fixup name
            String escpdName = Utils.stringReplace(o.getName(), "'", "\\'", false);
            String name = o.getName();
            if (name.equals("*") && o.isLink()) {
                name = getDirectionStr(((Link) o).getDirection(thisRoomId));
                escpdName = name;
            }

            return ("<A HREF=\"#\" onMouseOver=\"window.status=window.defaultStatus;return true;\" onClick=\"parent.command('" + o.getTypePrefix() + o.id + "|" + escpdName + "');return false;\"><IMG BORDER=0 WIDTH=" + iconSize + " HEIGHT=" + iconSize + " SRC=\"" + ico + "\" ALT=\"" + name + "\" TITLE=\"" + name + "\"></A>");
        }
    }

    //
    // List AdvObj collection
    //
    /**
     * @param things
     * @param sb
     * @param skin
     * @param listName
     * @param thisRoomId ID of container room - use it only if things are LINKs
     * @param focus
     * @param facing
     * @param sortWays
     * @param forceShowIcons
     */
    public String htmlTable(Dict things, Skin skin,
            String listName, String thisRoomId, AdvObject focus,
            String facing, // Only if sorted ways table is desired
            boolean sortWays, // Only if sorted ways table is desired
            boolean forceShowIcons // True if icons must be always shown regardless of show mode
            ) {

        StringBuffer sb = new StringBuffer();

        if (things != null) {
            int thingsCount = things.size();

            if (!forceShowIcons) {
                // Adjust count if needed
                for (int i = 0; i < things.size(); i++) {
                    AdvObject o = (AdvObject) things.elementAt(i);

                    if (o.showmode.intVal() != Const.OFFSCREEN) {
                        thingsCount--;
                    }
                }
            }

            if (thingsCount > 0) {
                try {
                    sb.append("<TABLE BORDER=0 CELLPADDING=2 CELLSPACING=0>");
                    if (listName != null) {
                        sb.append("<TR VALIGN=TOP><TD COLSPAN=2>" + listName + "</TD></TR>");
                    }
                    if (sortWays) {
                        things = Utils.getWaysSorted(things, thisRoomId, facing);
                    }
                    for (int i = 0; i < things.size(); i++) {
                        AdvObject o = (AdvObject) things.elementAt(i);
                        if (o.showmode.intVal() == Const.OFFSCREEN || forceShowIcons) {
                            // Display Item
                            // Find out name for label
                            String name = o.getName();
                            if (name.equals("*") && o.isLink()) {
                                name = getDirectionStr(((Link) o).getDirection(thisRoomId));
                            }

                            sb.append("<TR VALIGN=TOP><TD WIDTH=\"10%\">");
                            sb.append(htmlIcon(o, skin, thisRoomId, facing));
                            sb.append("</TD><TD WIDTH=\"90%\" CLASS=iconlabel>");
                            sb.append(name);
                            sb.append("</TD></TR>");
                        }
                    }
                    sb.append("</TABLE>");
                } catch (Exception e) {
                    sb.append("ERROR: Exception in webTable");
                    logger.debug("ERROR: Exception in webTable: " + e);
                    logger.debug(e);
                }
            }
        }
        return sb.toString();
    }

    /**
     * @throws DimxException
     */
    public void init() throws DimxException {
        logger.debug("Initializing world...");
        startDate = Calendar.getInstance();

        // Initializing objects' positions
        for (int i = 0; i < objects.size(); i++) {
            AdvObject x = objects.advObjectAt(i);
            if (x != null) {
                //logger.debug("Init " + x + "...");
                try {
                    x.reset();
                } catch (DimxException e) {
                    String s = "Problem initializing " + x + ": " + e.getMessage();
                    throw new DimxException(s);
                }
            }
        }
    }

    /** Puts an object and all its inner objects into the transfer queue.
     * The object is transferred within the current player service cycle (see
     * mulmultiplayer.service)
     * @param o
     * @param areaid
     * @throws DimxException
     * @return null if accepted, non-null (error message) otherwise.
     */
    public String moveOutside(AdvObject o, String areaid) throws DimxException {
        String result = server.requestMovement(o, areaid);
        if (result == null) {
            // transfer accepted

            /*

            if (o.isanItem()) {
            boolean prevee = eventsEnabled;
            eventsEnabled = false;
            ((Item) o).die(false,false);
            eventsEnabled = prevee;
            } else if (o.isPlayer()) {
            Player p = (Player) o;
            removePeople(p,Const.KICKOUT,Const.KEEP_ITEMS,msgs.actualize(msgs.msg[161],o.getName(), areaid));
            logger.debug("Unlinking all carried items " + p + " from current world");
            for (int i=p.contents.size()-1; i >= 0; i--) {
            Item x = (Item) p.contents.elementAt(i);
            boolean prevee = eventsEnabled;
            eventsEnabled = false;
            x.die(false,true);
            eventsEnabled = prevee;
            }
            logger.debug("Player " + o + " now in " + o.container + " world " + o.world + " carried items: " + o.contents.size());
            for (int i=o.contents.size()-1; i >= 0; i--) {
            Item x = (Item) o.contents.elementAt(i);
            logger.debug("Item " + x + " now in " + x.container + " world " + x.world + " inner items: " + x.contents.size());
            }
            logger.debug("World vars= " + vars.getElements());

            } else if (o.isaCharacter()) {
            Character p = (Character) o;
            removePeople(p,Const.KICKOUT,Const.KEEP_ITEMS,msgs.actualize(msgs.msg[161],o.getName(), areaid));
            for (int i=p.contents.size()-1; i >= 0; i--) {
            Item x = (Item) p.contents.elementAt(i);
            if (!x.world.removeObject(x)) this.logger.log("ERROR - moveOutside - remove player contents failed for: "+ x);
            }
            } else {
            throw new DimxException("Could not move object: >" + o + "< - invalid type");
            }
             */
            synchronized (moveQueueLock) {
                moveQueue.put(o.id, o);
                moveQueueDest.put(o.id, areaid);
            }
        }
        return result;
    }

    /** Performs and controls the movement all objects in the moveQueue outside current world to their
     * destination. Verifies consistency of the current world when finished.
     * Relies on multiplayer.movement for actual object movement.
     * @throws DimxException in case of problems
     */
    public void moveOutsideNow() throws DimxException {
        String errormessage = null;
        synchronized (moveQueueLock) {
            for (int i = 0; i < moveQueue.size(); i++) {
                AdvObject o = (AdvObject) moveQueue.elementAt(i);
                String areaid = (String) moveQueueDest.elementAt(i);
                logger.debug("Actually moving outside: " + o + " in " + areaid);
                String result = server.movement(this, o, areaid);
                if (result != null) {
                    errormessage = "Problem moving " + o + " outside to " + areaid + ": " + result;
                }
            }
            moveQueue = new Dict(); // Reset
            moveQueueDest = new Dict(); // Reset
        }
        verifyConsistency();
        if (errormessage != null) {
            throw new DimxException(errormessage);
        }
    }

    public void optiCount(String id) throws DimxException {
        DictSorted d = this.varGet("__optiFrequency", Const.GETREF).dictsortedVal();
        Token freqval = (Token) d.get(id);
        if (freqval == null) {
            freqval = new Token(1);
            d.put(id, freqval);
        } else {
            freqval.setVal(freqval.numVal() + 1.0);
        }
    }

    public void optiConsumed(String id, long millis) throws DimxException {
        DictSorted d = this.varGet("__optiConsumed", Const.GETREF).dictsortedVal();
        Token freqval = (Token) d.get(id);
        if (freqval == null) {
            freqval = new Token(1);
            d.put(id, freqval);
        } else {
            freqval.setVal(freqval.numVal() + Utils.cDbl(millis));
        }
    }

    /**
     * @param soundfile
     * @param loop
     * @throws DimxException
     * @return
     */
    public boolean playBackground(String soundfile, boolean loop) throws DimxException {
        // plays the specified sound
        logger.debug("Playing background sound: " + soundfile + " in the whole world");

        Player x;
        for (int i = 0; i < objects.size(); i++) {
            try {
                x = (Player) objects.elementAt(i);
                x.playBackground(soundfile, loop);
            } catch (ClassCastException e) {
                // Skip this - not a player
            }

        }
        return true;
    }

    /**
     * @param aName
     * @return
     */
    public boolean playerExists(String aName) {
        boolean ret = false;

        for (int i = 0; !ret && i < players.size(); i++) {
            if (((Player) players.elementAt(i)).getName().equalsIgnoreCase(aName)) {
                ret = true;
            }
        }
        return ret;
    }

    public String printContents() {
        return objects.toString();
    }

    /**
     * @param anIP
     * @return true if it is valid, or false if not
     */
    public boolean isValidIP(String anIP) {
        Vector bannedips = Utils.stringSplit(world.varGet("_bannedClients").strVal(), "|");
        for (int i = 0; i < bannedips.size(); i++) {
            if (Utils.instr(anIP, (String) bannedips.elementAt(i), Const.IGNORE_CASE) >= 0) {
                return false;
            }
        }
        return true; // Tests passed - valid
    }

    /**
     * @param aName
     * @return true if it is valid, or false if not
     */
    public String huntStopwords(String aName) {
        boolean ret = false;
        
        for (int i = 0; i < stopwords.size(); i++) {
            if (Utils.instr(aName, (String) stopwords.elementAt(i), Const.IGNORE_CASE) >= 0) {
                return (String) stopwords.elementAt(i);
            }
        }      
        return null; // Tests passed - valid
    }
    
     
    /**
     * @param soundfile
     * @throws DimxException
     * @return
     */
    public boolean playSound(String soundfile) throws DimxException {
        // plays the specified sound
        logger.debug("Sounding: " + soundfile + " in the whole world");

        Player x;
        for (int i = 0; i < objects.size(); i++) {
            try {
                x = (Player) objects.elementAt(i);
                x.playSound(soundfile);
            } catch (ClassCastException e) {
                // Skip this - not a player
            }

        }
        return true;
    }

    /**
     * @param c
     * @param stayZombie
     * @param dropItems
     * @param savePassword
     * @param altMessage   alternate message to be sent instead of msg[138]
     * @throws DimxException
     * @return
     */
    public boolean removePeople(Character c, boolean stayZombie, boolean dropItems, String altMessage) throws DimxException {
        boolean result = true;
        if (c != null) {
            if (dropItems) {
                // Drop or die all carried items
                Dict contents = c.getContents();
                for (int i = contents.size() - 1; i >= 0; i--) {
                    AdvObject what = (AdvObject) contents.elementAt(i);
                    what.moveTo(c.container, c, Const.DONT_CHECK_OPEN, Const.FORCE_REMOVE);
                }
            }

            if (!stayZombie) {
                // KICK OUT
                logger.debug("Character " + c.id + " definitely dies.");
                c.die(true, dropItems ? 1 : 2);
                peopleCount--;
                objects.remove(c.id);
                if (c.isPlayer()) {
                    // Message to the other players
                    String message = altMessage;
                    if (message == null) {
                        message = c.name.strVal() + " " + msgs.msg[138];
                    }
                    hear(null, message);
                    // Remove from players list
                    players.remove(c.id);
                }
            } else {
                // STAY ZOMBIE
                logger.debug("Character " + c.id + " goes to limbo.");
                c.sendCmd(Const.CMDE_REFRCTRLS);
                c.sendCmd(Const.CMDE_REFRSCENE);

                // The following are done also by c.die();
                AdvObject cont = c.container;
                if (cont != null) {
                    cont.objRemove(c, null, cont, null, Const.DONT_CHECK_OPEN, Const.FORCE_REMOVE);
                }
            }
        }
        return result;
    }

    /**
     * @param r
     * @throws DimxException
     * @return
     */
    public boolean removeRoom(Room r) throws DimxException {
        r.die(true, 0); // Try removing from container (useless), and DO NOT destroy all inner objects
        roomsCount--;
        return true;
    }

    /**
     * @param panelId
     * @throws DimxException
     * @return
     */
    public boolean setPanel(String panelId) throws DimxException {
        // Sets the default panel for the current world
        logger.debug("Setting panel: " + panelId + " in whole world");


        Panel myPanel = (Panel) getPanel(panelId);

        if (myPanel == null) {
            throw new DimxException("Unexistent panel: " + panelId);
        }

        panel = myPanel;



        Player x;
        for (int i = 0; i < objects.size(); i++) {
            try {
                x = (Player) objects.advObjectAt(i);
                x.prevPanelIds = x.getClient().getPanelIds();
                x.getClient().cmdRefrCtrls();
            } catch (ClassCastException e) {
                // Skip this - not a character
            }

        }


        return true;
    }

    /**
     * @param w
     * @param h
     */
    public void setSceneDimensions(int w, int h) {
        sceneWidth = w;
        sceneHeight = h;
    }

    /**
     * @throws DimxException
     */
    public void start() throws DimxException {
        fireEvent("onStart", null, null, null, false);
    }

    /**
     * @throws DimxException
     */
    public boolean useView(Page aView) throws DimxException {
        sceneTemplate = aView;
        return true;
    }

    /**
     * @param varId
     * @return result incapsulated in a Token. Null if not found
     */
    public Token varGet(String varId, boolean getReference) throws DimxException {
        if (varId.equalsIgnoreCase("interphone")) {
            if (getReference) {
                return interphone;
            } else {
                return interphone.getClone();
            }
        } else if (varId.equalsIgnoreCase("null")) {
            return new Token();
        } else if (varId.equalsIgnoreCase("false")) {
            return new Token(0);
        } else if (varId.equalsIgnoreCase("true")) {
            return new Token(1);
        } else if (varId.equalsIgnoreCase("instanceid")) {
            return new Token(instanceid);
        } else if (varId.equalsIgnoreCase("debugmode")) {
            return new Token(debugging);
        } else if (varId.equalsIgnoreCase("$WORLD")) {
            if (getReference) {
                throw new DimxException("Cannot assign to system constant: " + varId);
            }
            return new Token(this);
        }

        return super.varGet(varId, getReference);
    }

    public void verifyConsistency() {
        Dict cont = world.getContents();
        Dict contclone = (Dict) cont.clone();
        int n = contclone.size();
        for (int j = 0; j < n; j++) {
            AdvObject dxo = (AdvObject) contclone.elementAt(j);
            if (dxo.world == null) {
                this.logger.log("CONSISTENCY CHECK ON WORLD=" + world);
                this.logger.log("WARNING! Inconsistency found: object " + dxo + " type " + dxo.varGet("type").strVal() + " has world=null - trying to remove it");
                cont.remove(contclone.keyAt(j));
            } else if (dxo.world != this.world) {
                this.logger.log("CONSISTENCY CHECK ON WORLD=" + world);
                this.logger.log("WARNING! Inconsistency found: object " + dxo + " type " + dxo.varGet("type").strVal() + " has world= " + dxo.world + "<>current one: " + this + " - trying to remove it");
                cont.remove(contclone.keyAt(j));
            } else if (!dxo.id.equals(contclone.keyAt(j))) {
                this.logger.log("CONSISTENCY CHECK ON WORLD=" + world);
                this.logger.log("WARNING! Inconsistency found: object " + dxo + " type " + dxo.varGet("type").strVal() + " has id= " + dxo.id + "<>key: " + cont.keyAt(j) + "  - trying to remove it");
                cont.remove(contclone.keyAt(j));
            }
        }
    }

    /**
     * @param username
     * @throws DimxException
     * @return
     */
    public Dict gameLoad(String username) throws DimxException {
        int usersCount = 0;
        Dict myProfile = new Dict();
        String uname = Utils.stringFlatten(username);

        Cluster mycluster = world.getCluster();
        java.sql.Connection dbc = null;
        if (mycluster.dbConnected) {
            dbc = mycluster.dbConn(null);
        }
        if (dbc == null) { // Normal save
            try {
                java.util.Properties p = new java.util.Properties();
                try {
                    p.load(new java.io.FileInputStream(getSavegameFile()));
                } catch (java.io.FileNotFoundException e) {
                    return null;
                }

                // logger.debug("Player's data may be " + p.getProperty(Utils.stringFlatten(username) + "_name"));
                if (p.getProperty(uname + "_name") == null) {
                    return null;
                } else {
                    myProfile.put("name", p.getProperty(uname + "_name"));
                    myProfile.put("location", p.getProperty(uname + "_location"));
                    myProfile.put("pass", p.getProperty(uname + "_pass"));
                    myProfile.put("properties", p.getProperty(uname + "_properties"));
                    myProfile.put("events", p.getProperty(uname + "_events"));
                    myProfile.put("items", p.getProperty(uname + "_items"));
                    myProfile.put("image", p.getProperty(uname + "_image"));
                    myProfile.put("worldVersion", p.getProperty(uname + "_worldVersion"));
                    myProfile.put("worldId", p.getProperty(uname + "_worldId"));
                    myProfile.put("worldInstanceId", p.getProperty(uname + "_worldInstanceId"));
                    myProfile.put("cluster", p.getProperty(uname + "_cluster"));
                    myProfile.put("panel", p.getProperty(uname + "_panel"));
                    return myProfile;
                }
            } catch (Exception e) {
                throw new DimxException(e.getMessage());
            }
        } else {
            if (Utils.getSettingDB(uname + "_name", null, dbc, mycluster.id) == null) {
                return null;
            } else {
                myProfile.put("name", Utils.getSettingDB(uname + "_name", null, dbc, mycluster.id));
                myProfile.put("location", Utils.getSettingDB(uname + "_location", null, dbc, mycluster.id));
                myProfile.put("pass", Utils.getSettingDB(uname + "_pass", null, dbc, mycluster.id));
                myProfile.put("properties", Utils.getSettingDB(uname + "_properties", null, dbc, mycluster.id));
                myProfile.put("events", Utils.getSettingDB(uname + "_events", null, dbc, mycluster.id));
                myProfile.put("items", Utils.getSettingDB(uname + "_items", null, dbc, mycluster.id));
                myProfile.put("image", Utils.getSettingDB(uname + "_image", null, dbc, mycluster.id));
                myProfile.put("worldVersion", Utils.getSettingDB(uname + "_worldVersion", null, dbc, mycluster.id));
                myProfile.put("worldId", Utils.getSettingDB(uname + "_worldId", null, dbc, mycluster.id));
                myProfile.put("worldInstanceId", Utils.getSettingDB(uname + "_worldInstanceId", null, dbc, mycluster.id));
                myProfile.put("cluster", Utils.getSettingDB(uname + "_cluster", null, dbc, mycluster.id));
                myProfile.put("panel", Utils.getSettingDB(uname + "_panel", null, dbc, mycluster.id));

                try {
                    dbc.close(); dbc=null;
                } catch (Exception e) {}

                return myProfile;
            }
        }
    }

    /**
     * @param username
     * @throws DimxException
     * @return
     */
    public boolean profileExists(String username) {
        String uname = Utils.stringFlatten(username);

        try {

            Cluster mycluster = world.getCluster();
            java.sql.Connection dbc = null;
            if (mycluster.dbConnected) {
                dbc = mycluster.dbConn(null);
            }
            if (dbc == null) { // Normal save
                try {
                    java.util.Properties p = new java.util.Properties();
                    try {
                        p.load(new java.io.FileInputStream(getSavegameFile()));
                    } catch (java.io.FileNotFoundException e) {
                        return false;
                    }

                    // logger.debug("Player's data may be " + p.getProperty(Utils.stringFlatten(username) + "_name"));
                    if (p.getProperty(uname + "_name") == null) {
                        return false;
                    } else {
                        return true;
                    }
                } catch (Exception e) {
                    logger.log(e);
                    //throw new DimxException(e.getMessage());
                    return false;
                }
            } else {
                if (Utils.getSettingDB(uname + "_name", null, dbc, mycluster.id) == null) {
                try {
                    dbc.close(); dbc=null;
                } catch (Exception e) {}
                    return false;
                } else {
                try {
                    dbc.close(); dbc=null;
                } catch (Exception e) {}
                    return true;
                }
            }

        } catch (Exception e) {
            logger.log(e);
            //throw new DimxException(e.getMessage());
            return false;
        }
    }

    /**
     * @param username
     * @throws DimxException
     * @return
     */
    public String getProfileAvatar(String username) {
        String uname = Utils.stringFlatten(username);

        try {

            Cluster mycluster = world.getCluster();
            java.sql.Connection dbc = null;
            if (mycluster.dbConnected) {
                dbc = mycluster.dbConn(null);
            }
            if (dbc == null) { // Normal save
                try {
                    java.util.Properties p = new java.util.Properties();
                    try {
                        p.load(new java.io.FileInputStream(getSavegameFile()));
                    } catch (java.io.FileNotFoundException e) {
                        return null;
                    }

                    // logger.debug("Player's data may be " + p.getProperty(Utils.stringFlatten(username) + "_name"));
                    return p.getProperty(uname + "_image");
                } catch (Exception e) {
                    logger.log(e);
                    //throw new DimxException(e.getMessage());
                    return null;
                }
            } else {
                String x = Utils.getSettingDB(uname + "_name", null, dbc, mycluster.id);
                try {
                    dbc.close(); dbc=null;
                } catch (Exception e) {}
                return x;
            }

        } catch (Exception e) {
            logger.log(e);
            //throw new DimxException(e.getMessage());
            return null;
        }
    }

    /**
     * Checks for password correctness
     *
     * @param username
     * @param password
     * @throws DimxException
     * @return
     */
    public boolean profileCredentials(String username, String password) {
        String uname = Utils.stringFlatten(username);

        try {

            Cluster mycluster = world.getCluster();
            java.sql.Connection dbc = null;
            if (mycluster.dbConnected) {
                dbc = mycluster.dbConn(null);
            }
            if (dbc == null) { // Normal save
                try {
                    java.util.Properties p = new java.util.Properties();
                    try {
                        p.load(new java.io.FileInputStream(getSavegameFile()));
                    } catch (java.io.FileNotFoundException e) {
                        return false;
                    }

                    // logger.debug("Player's data may be " + p.getProperty(Utils.stringFlatten(username) + "_name"));
                    if (p.getProperty(uname + "_name") != null && p.getProperty(uname + "_pass").equals(password)) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    logger.log(e);
                    //throw new DimxException(e.getMessage());
                    return false;
                }
            } else {
                if (Utils.getSettingDB(uname + "_name", null, dbc, mycluster.id) != null
                        && Utils.getSettingDB(uname + "_pass", null, dbc, mycluster.id).equals(password)) {
                try {
                    dbc.close(); dbc=null;
                } catch (Exception e) {}
                    return true;
                } else {
                try {
                    dbc.close(); dbc=null;
                } catch (Exception e) {}
                    return false;
                }
            }
        } catch (Exception e) {
            logger.log(e);
            //throw new DimxException(e.getMessage());
            return false;
        }
    }

    /**
     * @param username assume not empty
     * @param username assume not empty
     * @throws DimxException
     * @return
     */
    public String createProfile(String username, String password, String fbid) {

        if (profileExists(username)) {
            return "Profile exists";
        }

        String uname = Utils.stringFlatten(username);

        try {

            Cluster mycluster = world.getCluster();
            java.sql.Connection dbc = null;
            if (mycluster.dbConnected) {
                dbc = mycluster.dbConn(null);
            }
            String ukey = uname; // Recycled some code
            if (dbc == null) { // Normal save
                try {
                    java.util.Properties p = new java.util.Properties();
                    try {
                        p.load(new java.io.FileInputStream(getSavegameFile()));
                    } catch (java.io.FileNotFoundException e) {
                        logger.debug("Creating users database");
                    }
                    synchronized (mycluster.saveFileLock) {

                        p.setProperty(ukey + "_name", username);
                        p.setProperty(ukey + "_pass", password);
                        if (fbid != null && !fbid.equals("")) {
                            p.setProperty(ukey + "_fbid", fbid);
                        }
                        p.setProperty(ukey + "_when", Utils.cTimeStamp(java.util.Calendar.getInstance().getTime()));
                        p.setProperty(ukey + "_cluster", world.cluster);
                        p.setProperty(ukey + "_worldId", world.id);
                        p.setProperty(ukey + "_worldInstanceId", world.instanceid);
                        p.setProperty(ukey + "_worldVersion", world.version);

                        /*
                        p.setProperty(ukey + "_panel",panelstr);
                        p.setProperty(ukey + "_image",imagestr);
                        p.setProperty(ukey+"_items",itemslist);
                        p.setProperty(ukey+"_properties",propstr.toString());
                        p.setProperty(ukey+"_events",attachedEvents.toString());
                         */

                        java.io.FileOutputStream out = new java.io.FileOutputStream(getSavegameFile());
                        p.store(out, "--- This is a generated file. Do not edit ---");
                        out.close();
                        return null; // OKAY
                    }

                } catch (Exception e) {
                    logger.log(e);
                    //throw new DimxException(e.getMessage());
                    return "Unknown error: " + e.getMessage();
                }
            } else { // DB code
                Utils.saveSettingDB(ukey + "_name", username, dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_pass", password, dbc, mycluster.id);
                if (fbid != null && !fbid.equals("")) {
                    Utils.saveSettingDB(ukey + "_fbid", fbid, dbc, mycluster.id);
                }
                Utils.saveSettingDB(ukey + "_when", Utils.cTimeStamp(java.util.Calendar.getInstance().getTime()), dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_cluster", world.cluster, dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_worldId", world.id, dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_worldInstanceId", world.instanceid, dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_worldVersion", world.version, dbc, mycluster.id);

                /*
                Utils.saveSettingDB(ukey + "_location",container.id,dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_panel",panelstr,dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_image",imagestr,dbc, mycluster.id);
                Utils.saveSettingDB(ukey+"_items",itemslist,dbc, mycluster.id);
                Utils.saveSettingDB(ukey+"_properties",propstr.toString(),dbc, mycluster.id);
                Utils.saveSettingDB(ukey+"_events",attachedEvents.toString(),dbc, mycluster.id);
                 */
                try {
                    dbc.close(); dbc=null;
                } catch (Exception e) {}

                return null; // OKAY
            }

        } catch (Exception e) {
            logger.log(e);
            return "Unknown error: " + e.getMessage();
        }
    }

    /**
     * Binds profile to facebook account
     * Does not check for password - Always use profileCredentials to check pass before calling
     *
     * @param username
     * @param password
     * @throws DimxException
     * @return NULL if ok, or string = problem found
     */
    public String bindProfile(String username, String fbid) {
        String uname = Utils.stringFlatten(username);

        try {

            Cluster mycluster = world.getCluster();
            java.sql.Connection dbc = null;
            if (mycluster.dbConnected) {
                dbc = mycluster.dbConn(null);
            }
            if (dbc == null) { // Normal save
                try {
                    java.util.Properties p = new java.util.Properties();
                    try {
                        p.load(new java.io.FileInputStream(getSavegameFile()));
                    } catch (java.io.FileNotFoundException e) { // Theoretically impossible
                        return "Profiles database does not exist";
                    }

                    // logger.debug("Player's data may be " + p.getProperty(Utils.stringFlatten(username) + "_name"));
                    if (p.getProperty(uname + "_name") != null) {
                        if (fbid != null && !fbid.equals("")) {
                            p.setProperty(uname + "_fbid", fbid);
                            return null; // OK!
                        }
                        return "Facebook ID cannot be null"; // Theoretically impossible
                    } else { // Theoretically impossible
                        return "Profile was not found";
                    }
                } catch (Exception e) {
                    logger.log(e);
                    //throw new DimxException(e.getMessage());
                    return "Unknown error: " + e.getMessage();
                }
            } else {
                if (Utils.getSettingDB(uname + "_name", null, dbc, mycluster.id) != null) {
                    Utils.saveSettingDB(uname + "_fbid", fbid, dbc, mycluster.id);
                try {
                    dbc.close(); dbc=null;
                } catch (Exception e) {}
                    return null; //OK!
                } else {
                try {
                    dbc.close(); dbc=null;
                } catch (Exception e) {}
                    // Theoretically impossible
                    return "Profile was not found";
                }
            }

        } catch (Exception e) {
            logger.log(e);
            // throw new DimxException(e.getMessage());
            return "Unknown error: " + e.getMessage();
        }
    }
}
