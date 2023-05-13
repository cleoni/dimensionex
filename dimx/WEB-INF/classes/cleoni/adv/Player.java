package cleoni.adv;

import java.util.*;

/** data and functions for the human player. It is a subclass of  CHARACTER - see class hierarchy in the JavaDoc */
public class Player extends cleoni.adv.Character {
    public   Skin skin = null;
    private	    Client msgBoard = null;
    private     Panel	panel = null;
    public      String password = "";
    public      String prevPanelIds = "";
    private  boolean listening = true;
    public      AdvObject focus = null;
    public  Page view = null;
    
    public static String[] methods =
    {"getCookie","saveCookie","printXY","getPanel"};
    
    // Number of expected parameter for each instruction. -1 means "can vary"
    public static int[] methodArgs =
    {1,2,3,0};
    
    public String[] getMethods() {
        return Utils.array_merge(this.methods,super.getMethods());
    }
    
    public Image getImageAndCorrectFacing(PeopleContainer container) throws DimxException {
        Image im = null;
        String newface = this.facing.strVal();
        if (container != null) {
            if (container.isaRoom()) { // Room
                Object[] res = container.getNearestImage(newface);
                if (res != null) {
                    im = (Image) res[0];
                    newface = (String) res[1];
                    newface = newface + "";
                }
            } else { // Item or vehicle
                if (!container.isaRoom()) { // Vehicle
                    Object[] res = this.getRoom().getNearestImage(newface);
                    im = (Image) res[0];
                    newface = (String) res[1];
                } else { // Item
                    im = container.varGet("innerImage").imageVal();
                }
            }
        }

        // Facing correction
        if (im != null && !newface.equals(this.facing.strVal())) {
            // Player changes facing
            this.varGet("facing",Const.GETREF).assign(new Token(newface),this.world);
        }
        return im;
    }

    public int getMethodArgs(String mname) {
        int i = Utils.indexOf(mname, this.methods);
        if (i >= 0) //Method is in this class
            return this.methodArgs[i];
        else
            return super.getMethodArgs(mname);
    }
    
    public Token execMethod(String mname,Dict params) throws DimxException {
        String mnamel = mname.toLowerCase();
        if (mnamel.equals("getcookie")) {
            return metGetCookie(params);
        } else if (mnamel.equals("savecookie")) {
            return metSaveCookie(params);
        } else if (mnamel.equals("printxy")) {
            return metPrintXY(params);
        } else if (mnamel.equals("getpanel")) {
            return metGetPanel(params);
        } 
        
        return super.execMethod(mname, params);
    }
    
    private Token metGetPanel(Dict params) throws DimxException {
        Panel panel = this.getPanel();
        if (panel != null) {
            return new Token(this.getPanel().id);
        } else {
            return new Token("default");
        }
    }

    private Token metGetCookie(Dict params) throws DimxException {
        Token p0 = (Token) params.elementAt(0);
        String x = msgBoard.session.getCookie(p0.strVal());
        return new Token(x);
    }
    
    private Token metSaveCookie(Dict params) throws DimxException  {
        Token ret = new Token();
        
        Token key = (Token) params.get("par0");
        Token val = (Token) params.get("par1");
        
        try {
            msgBoard.session.setCookie(key.strVal(), val.strVal()); // Remembers banning via a cookie
        } catch (java.lang.IllegalArgumentException e) {
            this.world.logger.log("ERROR: Cannot save cookie with key: " + key + " val: " + val + " to player: " + this);
        }
        return ret;
    }
    
    private Token metPrintXY(Dict params) throws DimxException  {
        Token ret = new Token();
        
        Token tstuff = (Token) params.get("par0");
        Token xt = (Token) params.get("par1");
        int x = xt.intVal();
        Token yt = (Token) params.get("par2");
        int y = yt.intVal();
        int stuffheight=10;
        String stuff = null;
        
        if (tstuff.isImage()) {
            Image im = tstuff.imageVal();
            stuff = im.toHTML("", this.msgBoard.factorx);
            stuffheight = im.getHeight();
        } else {
            stuff = tstuff.strVal();
        }
        printXYZ(x,y,-1,stuff,stuffheight);
        
        return ret;
    }
    
    public void printXYZ(int x,int y,int z,String stuff,int stuffheight) throws DimxException  {

        PeopleContainer container = (PeopleContainer) this.container;
        Image im = getImageAndCorrectFacing(container);

        // Reproportion Y
        if (z == -1) { // Auto
            z = im.getHeight()-y;
        }
        z = Utils.proportion(z, 0, im.getHeight(), 15, 204);
        
        // Actual visualisation
        msgBoard.displayJust("<span style=\"left:" + Utils.cInt(msgBoard.factorx*im.getScreenX(x)) + "; top:" + Utils.cInt(msgBoard.factorx*im.getScreenY(y,stuffheight)+21) + "; position: absolute; z-index: " + z + "\">");
        msgBoard.display(stuff + "</span>");
    }

    public Client getClient() {
        return msgBoard;
    }
    
    public String getDescription() {
        StringBuffer s = new StringBuffer("");
        return s.toString();
    }
    
    /*
    * obsolete - unused
    */
    public String getExtras() {
        StringBuffer s = new StringBuffer("");
        
        s.append(world.msgs.msg[162] + ": " + (getClient().audioSupport?world.msgs.msg[165]:world.msgs.msg[166]) + "\n");
        s.append(world.msgs.msg[12] + ": " + (getClient().audioSupport?world.msgs.msg[165]:world.msgs.msg[166]) + "\n");
        s.append("Browser: " + getClient().browser + "\n");
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        
        s.append(world.msgs.msg[164] + ": " + sdf.format(getClient().lastContact.getTime()));
        return s.toString();
    }
    
    
    /**
     * Player constructor comment.
     * @param aWorld leoni.adv.World
     * @param aName java.lang.String
     * @param anId java.lang.String
     * @param isRobot boolean
     * @param msgListSize int
     * @param aDescription java.lang.String
     * @param anIcon java.lang.String
     * @param aCapacity int
     * @param position java.lang.String
     */
    public Player(World aWorld, String aName, String anId, Skin aSkin, String anIcon, int aCapacity,
            String attrList,
            String aDefContainer,
            Client aClient
            ) throws DimxException  {
        super(aWorld, aName, anId, null, anIcon, aCapacity, attrList, aDefContainer, Const.ACCEPT_ALL);
        msgBoard = aClient;
        aClient.setOwner(this);
        //msgBoard = new Client(this,aWorld.msgListSize,audioSupport,aWorld.screenwidth,aWorld.screenheight);
        skin = aSkin;
        panel = null;
        // Welcome broadcast (may become optional in future releases)
        if (world != null) 
            world.hear(null,aName  + " " + world.msgs.msg[112]);
    }
    
    public boolean display(String msg) throws DimxException {
        if (listening) getClient().display(msg);
        return true;
    }
    
    public boolean displayRight(String msg) throws DimxException {
        if (listening) getClient().displayRight(msg);
        return true;
    }
    
    public boolean displayh(String msg) throws DimxException {
        if (listening) getClient().displayh(msg);
        return true;
    }
    
    public Panel getPanel() {
        return panel;
    }
    
    public boolean go(String wayId) throws DimxException {
        boolean ret = super.go(wayId);
        if (ret) updMapPos();
        return ret;
    }
    
    public boolean hear(DimxObject from, String msg) throws DimxException  {
        // Tries to hear from the specified object, returns true if successful
        boolean res = super.hear(from,msg);
        
        if (from == null) from = world.defaultCharacter;
        
        if (!msg.equals("") && from != this && listening) {
            msgBoard.receive(new Message(from,this.id,msg));
        }
        return res;
    }
    
    public boolean isaCharacter() {
        return true;
    }
    
    public boolean isPlayer() {
        return true;
    }
    
    public boolean isRobot() {
        return false;
    }
    
    public boolean look(AdvObject o, DictSorted input) throws DimxException  {
        String out=super.look(o,input, skin);
        if (out != null && o != null) {
            if (o.showmode.intVal() != Const.OFFSCREEN) {
                out="-- " + o.getName() + " --\n"+out;
            }
            displayh(out);
            return true;
        }
        return false;
    }

    public boolean objectOpen(AdvObject o) throws DimxException  {
        boolean ret = super.objectOpen(o);
        
        if (ret) { // If opening succeeded...
            if (o != null && o.isAccessibleFrom(this) && !o.isLink() && o.showmode.intVal() != Const.OFFSCREEN) {
                // If object still exists, is accessible, not a link and onscreen...
                String out = super.look(o, null, skin); // look at it
                if (out != null) display(out);
                focus = o;
            }
        }
        return ret;
    }
    
    public boolean playBackground(String soundfile,boolean loop) throws DimxException {
        // plays the specified background sound
        getClient().setBackgroundSound(soundfile,loop);
        return true;
    }
    
    public boolean playSound(String soundfile) throws DimxException {
        // plays the specified sound
        //world.logger.debug("Sounding: " + soundfile + " to " + id);
        getClient().setSound(soundfile);
        return true;
    }
    
    /**
     * Restores player's inventory at login
     * @param contentsString List of items to be restored
     * @throws cleoni.adv.DimxException 
     * @return List of items to be restored: it will be the same list as input, in which the saveInfo/restoreInfo part will be cleared if the item has been reconstructed
     */
    public String restoreContents(String contentsString) throws DimxException {
        StringBuffer alerts = new StringBuffer();
        StringBuffer returnList = new StringBuffer();
        if (contentsString != null) {
            Vector kv = Utils.stringSplit(contentsString,",");
            
            this.listening = false; // Ignore messages resulting from restore attempts
            for (int i=0; i < kv.size(); i++) {
                String oid = (String) kv.elementAt(i);
                world.logger.debug("Trying to restore item: " + oid);
                Vector vitem = Utils.stringSplit(oid,"|");
                AdvObject o = (AdvObject) world.getObject((String) vitem.elementAt(0));
                if (i>0) returnList.append(",");
                returnList.append((String) vitem.elementAt(0));
                String otype = "";
                String restoreinfo = null;
                if (vitem.size()>1) {
                    otype = (String) vitem.elementAt(1);
                    returnList.append("|"+otype);
                }
                if (vitem.size()>2) {
                    restoreinfo = (String) vitem.elementAt(2);
                }
                
                DictSorted actualpar = new DictSorted();
                actualpar.put("type", new Token(otype));
                actualpar.put("restoreinfo", new Token(restoreinfo));
                actualpar.put("player", new Token(this));
                boolean done = false;
                if (o != null && o.varGet("type").strVal().equals(otype)) {
                    done = ((Item) o).restoreTo(this, alerts, world);
                }
                if (done && restoreinfo != null) { // Restored with existing items - keep restoreinfo
                    returnList.append("|"+restoreinfo);
                }
                if (!done) {
                    //  object doesn't exist any more
                    world.logger.debug(oid + " doesn't exist anymore");
                    if (!otype.equals("")) {
                        this.listening = true;
                        Token restore_result = world.execute("restore", world, null, actualpar, new Token(false), false);
                        if (restoreinfo != null && (restore_result.isNull() || restore_result.boolVal()==false)) {
                            // Item was NOT restored - keep restoreInfo
                            returnList.append("|"+restoreinfo);
                        }
                        this.listening = false;
                        
                        if (restore_result == null || (restore_result != null && restore_result.boolVal()==false) ) {
                            // Search world for same type - trying AutoRestore
                            if (world.disableAutoRestore) {
                                world.logger.debug("AutoRestore is disabled");
                                alerts.append(Messages.actualize(world.msgs.msg[125],otype) + "\n");
                            } else {
                                Dict cont = world.getContents();
                                for (int j=cont.size()-1; (j >= 0 && !done); j--) {
                                    AdvObject dxo = (AdvObject) cont.elementAt(j);
                                    if (dxo.world == null) {
                                        world.logger.log("WARNING! Inconsistency found: object " + dxo + " type " + dxo.varGet("type").strVal() + " has world=null - trying to remove it");
                                        cont.removeAt(j);
                                    } else if (dxo.world != this.world) {
                                        world.logger.log("WARNING! Inconsistency found: object " + dxo + " type " + dxo.varGet("type").strVal() + " has world= " + dxo.world + "<>current one: " + this.world + " - trying to remove it");
                                        cont.removeAt(j);
                                    } else if (!dxo.id.equals(cont.keyAt(j))) {
                                        world.logger.log("WARNING! Inconsistency found: object " + dxo + " type " + dxo.varGet("type").strVal() + " has id= " + dxo.id + "<>key: " + cont.keyAt(j) + "  - trying to remove it");
                                        cont.removeAt(j);
                                    } else if (dxo.isanItem() && otype.equals(dxo.varGet("type").strVal())) {
                                        done = ((Item) dxo).restoreTo(this,null,world); // No alerts please
                                    }
                                }
                                if (!done) { // Try AutoRestore using main type
                                    String maintype = otype;
                                    int x = otype.indexOf(".");
                                    if (x > 0) maintype = otype.substring(0, x);
                                    x = maintype.length();
                                    cont = world.getContents();
                                    for (int j=cont.size()-1; (j >= 0 && !done); j--) {
                                        AdvObject dxo = (AdvObject) cont.elementAt(j);
                                        if (dxo.isanItem()) {
                                            String thistype = dxo.varGet("type").strVal();
                                            if (thistype.length() >= x && maintype.equals(thistype.substring(0,x))) {
                                                done = ((Item) dxo).restoreTo(this,null,world); // No alerts please
                                            }
                                        }
                                    }
                                }
                                if (!done) {
                                    alerts.append(Messages.actualize(world.msgs.msg[125],otype) + "\n");
                                } else {
                                    alerts.append("AutoRestored: " + otype + "\n");
                                }
                            }
                        }
                    }
                }
            }
            this.listening = true; // Restores listening messages
            //this.getClient().getConsole(); // Empty console
            display(alerts.toString());
        }
        return returnList.toString();
    }
    
    public void restoreEvents(String propsString)  throws DimxException
// Restores players' attached events (for Load Game)
    {
        if (propsString != null) {
            Vector kv = Utils.stringSplit(propsString,",");
            
            for (int i=0; i < kv.size(); i++) {
                String pair = (String) kv.elementAt(i);
                
                Vector ke = Utils.stringSplit(pair,"=");
                if (ke.size() < 2) {
                    world.logger.debug("ERROR restoring players' events - " + pair + " is not a key/value pair");
                } else {
                    String eventid = (String) ke.elementAt(0);
                    String copyfromid = (String) ke.elementAt(1);
                    Token tcopyfrom = world.varGet(copyfromid);
                    
                    this.attachEvent(eventid,copyfromid,tcopyfrom);
                    world.logger.debug("Re-attached: " + eventid + "=" + copyfromid);
                }
            }
        }
    }
    
    public void restoreProperties(String propsString)  throws DimxException
// Restores players' properties (for Load Game)
    {
        if (propsString != null) {
            Vector kv = Utils.stringSplit(propsString,",");
            
            for (int i=0; i < kv.size(); i++) {
                String pair = (String) kv.elementAt(i);
                
                Vector ke = Utils.stringSplitPair(pair,"=");
                if (ke.size() < 2) {
                    world.logger.debug("ERROR restoring players' properties - " + pair + " is not a key/value pair");
                } else {
                    String expr = (String) ke.elementAt(1);
                    Token t = null;
                    if (Utils.instr(expr,"NewImage(",Const.IGNORE_CASE) == 0) {
                        // It is a function - let's parse it
                        expr = Utils.stringReplace(expr, "*", ",",false); // For compatibility from past
                        expr = Utils.unescapeChars(expr,",="); // Regular unescape
                        DimxParser p = new DimxParser(world,Const.WORLDVARS,0,this.id);
                        p.feed(expr);
                        try {
                            t = p.evalExpression(p.lookupToken(),0);
                        } catch (DimxException e) {
                            world.logger.log("Problem in restoring image of player: " + this + " expr='" + expr + "'");
                            t = new Token(world.getSkin("").picPlayer);
                        }
                    } else if (expr.startsWith("!set!")) {
                        expr = Utils.stringReplace(expr, "!3!", "*",false); // For compatibility from past
                        DictSorted d = Utils.string2setTokens(expr.substring(5),"*","=",true);
                        t = new Token(d);
                    } else {
                        // Not a function - treat as simple value!
                        t = new Token(expr,true); // Guess type!
                    }
                    this.varGet((String) ke.elementAt(0),Const.GETREF).assign(t,world);
                    
                    world.logger.debug("Restored: " + (String) ke.elementAt(0) + "=" + (new Token((String) ke.elementAt(1))).strVal() + " now: " + varGet((String) ke.elementAt(0)).strVal());
                }
            }
        }
    }
    
    public boolean saveGame(boolean exiting) throws DimxException {
        String itemslist = null;
        String savegameFile = world.getSavegameFile();
        String target;
        StringBuffer attachedEvents = new StringBuffer();
        
        if (exiting) {  // Save and exit - no target
            target = "";
        } else {// Save and continue
            target = this.id;
        }
        boolean result = (container != null) && world.fireEvent("onSave",this,this.id,target,true);
        if (result == false) return false;
        try {
            world.logger.debug("Saving player's data...");
            
            //Dict objects = (Dict) contents.clone(); // Not needed by now

            StringBuffer items = new StringBuffer("");
            boolean commas = false;

            // The following for the call to saveInfo
            DictSorted actualpar = new DictSorted();
            actualpar.put("exiting", new Token(exiting));
            
            for (int i=contents.size()-1; i >= 0 ; i--) {
                if (commas) items.append(",");
                items.append(contents.keyAt(i));
                commas = true;
                
                Item it = (Item) contents.elementAt(i);
                String type = it.varGet("type").strVal();
                if (!type.equals("")) {
                    items.append("|" + type);

                    if (exiting) { // Call saveInfo in this case
                        Token eventres = world.fireEvent_t("saveInfo",it,this.id,target,actualpar,new Token(),false);
                        //The following line is for using saveInfo() also as generic event - disabled
                        //if (eventres == null) eventres = world.execute("saveInfo", world, actualpar,null,false);
                        if (!eventres.isNull()) {
                            items.append("|" + eventres.strVal());
                            world.removeObject(it); // Restore info saved - remove object because it will be restored upon re-enter
                        } else { // saveInfo returned NULL so it will not be possible to restore object - just drop it
                            it.moveTo(this.container,this,Const.DONT_CHECK_OPEN,Const.FORCE_REMOVE);
                        }
                    }
                }
            }
                
            itemslist = items.toString();
            
            String ukey = new String(this.getName());
            ukey = Utils.stringReplace(ukey.toLowerCase()," ","_",false);
            // Save properties
            StringBuffer propstr = new StringBuffer("");
            // Add automatic ones
            propstr.append("facing="+facing.strVal());
            if (capacity.intVal() != world.defaultCharacter.capacity.intVal()) {
                propstr.append(",capacity="+capacity.strVal());
            }
            // Add normal ones
            for (int i=0; i < varsCount(); i++) {
                String key = varGetIdAt(i);
                String val = "";
                Event e = null;
                if (!key.substring(0,2).equals("__")) { // Skip system temp variables
                    Token t = varGet(varGetIdAt(i));
                    if (t.isEvent() && (e = t.eventVal()).attachedEventsId != null) {
                        attachedEvents.append(e.id+"="+e.attachedEventsId);
                        attachedEvents.append(",");
                    } else if (t.isObject()) {
                        if (t.isImage()) {
                            val = t.imageVal().toString();
                            val = Utils.stringReplace(val, ",", "*",false);
                            //val = Utils.escapeChars(val,",="); less efficient
                        } else if (t.isDict()) {
                            val = t.dictVal().toSettingsPair("*");
                        } else {
                            DimxObject o = t.dimxobjVal();
                            val = o.id;
                        /* This part is not yet enabled - type checking only on owned ITEMS
                        String type = o.varGet("type").strVal();
                        if (!type.equals("")) {
                            // Trigger restoreinfo = o.onSave() event here
                            val = val + "|" + type; // + "|" + restoreinfo
                        }
                         */
                        }
                    } else { // Scalar object
                        val = t.strVal();
                        val = Utils.escapeChars(val,",=");
                    }
                    if (!val.equals("")) { // Avoid writing null vars
                        propstr.append("," + key + "=" + val);
                    }
                }
            }
            
            String panelstr = "default";
            Panel pan = getPanel();
            if (pan != null) {
                panelstr = pan.id;
            }
            
            String imagestr = "";
            Object[] ims = this.getNearestImage("N");
            if (ims[0] != null) {
                imagestr = ims[0].toString();
            } else {
                world.logger.log("WARNING: No images[0] for " + this + " - won't save it to player profile");
            }
            Cluster mycluster = world.getCluster();
            java.sql.Connection dbc = null;
            if (mycluster.dbConnected) dbc = mycluster.dbConn(null);
            
            if (dbc == null) { // Normal save
                java.util.Properties p = new java.util.Properties();
                synchronized (mycluster.saveFileLock) {
                    try {
                        p.load(new java.io.FileInputStream(savegameFile));
                    } catch (java.io.FileNotFoundException e) {
                        world.logger.debug("Creating users database");
                    }
                    
                    p.setProperty(ukey + "_name",this.getName());
                    p.setProperty(ukey + "_pass",password);
                    p.setProperty(ukey + "_location",container.id);
                    p.setProperty(ukey + "_when",Utils.cTimeStamp(java.util.Calendar.getInstance().getTime()));
                    p.setProperty(ukey + "_cluster",world.cluster);
                    p.setProperty(ukey + "_worldId",world.id);
                    p.setProperty(ukey + "_worldInstanceId",world.instanceid);
                    p.setProperty(ukey + "_worldVersion",world.version);
                    p.setProperty(ukey + "_panel",panelstr);
                    p.setProperty(ukey + "_image",imagestr);
                    p.setProperty(ukey+"_items",itemslist);
                    p.setProperty(ukey+"_properties",propstr.toString());
                    p.setProperty(ukey+"_events",attachedEvents.toString());
                    
                    java.io.FileOutputStream out = new java.io.FileOutputStream(savegameFile);
                    p.store(out, "--- This is a generated file. Do not edit ---");
                    out.close();
                }
            } else { // DB supported
                Utils.saveSettingDB(ukey + "_name",this.getName(),dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_pass",password,dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_location",container.id,dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_when",Utils.cTimeStamp(java.util.Calendar.getInstance().getTime()),dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_cluster",world.cluster,dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_worldId",world.id,dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_worldInstanceId",world.instanceid,dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_worldVersion",world.version,dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_panel",panelstr,dbc, mycluster.id);
                Utils.saveSettingDB(ukey + "_image",imagestr,dbc, mycluster.id);
                Utils.saveSettingDB(ukey+"_items",itemslist,dbc, mycluster.id);
                Utils.saveSettingDB(ukey+"_properties",propstr.toString(),dbc, mycluster.id);
                Utils.saveSettingDB(ukey+"_events",attachedEvents.toString(),dbc, mycluster.id);
                dbc.close();
                dbc = null;
            }
            // Old code, no more need to restore on normal save
            //if (!exiting) restoreContents(itemslist);
            
            getClient().display(world.msgs.msg[156]);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            world.logger.log(e);
            world.logger.log("Player: " + this);
            throw new DimxException(e.toString() + " catched in Player.saveGame.\n" + e.getStackTrace().toString());
        }
        return result;
    }
    
    public void say(String msg, DimxObject destination) throws DimxException  {
        Token v = this.varGet("mute",Const.GETREF);
        
        if (world.muting > 0) {
            if (world.muting == 2 && v.intVal() > 0) {
                display(world.msgs.actualize(world.msgs.msg[147],Utils.cStr(Utils.cInt(0.5 + v.numVal()/2.0))));
                return;
            }
            String stopword1,stopword2,stopword3;
            stopword1 = world.huntStopwords(msg);
            String deflmsg = Utils.deflate(msg);
            stopword2 = world.huntStopwords(deflmsg);
            stopword3 = world.huntStopwords(Utils.stringReplace(deflmsg,"k", "c", Const.IGNORE_CASE));
            if (stopword1 != null || stopword2 != null || stopword3 != null) {
                display("****");
                if (world.muting > 1) v.setVal(v.intVal()+1);
                return;
            }
            if (world.muting == 3 && v.intVal() > 0) { // code is identical to above
                display(world.msgs.actualize(world.msgs.msg[147],Utils.cStr(Utils.cInt(0.5 + v.numVal()/2.0))));
                return;
            }
        }
        if (destination != null) {
            Client client = this.getClient();
            display("- " + msg);
            String console = client.getConsole();
            if (destination.hear(this,msg)) {
                client.displayh(console.substring(0,console.length()-1));
            }
        }
    }
    
    public boolean sendCmd(String cmd) throws DimxException {
        if (cmd.equalsIgnoreCase("refresh!ctrls")) {
            getClient().cmdRefrCtrls();
        } else if (cmd.equalsIgnoreCase(Const.CMDE_REFRSCENE)) {
            getClient().cmdRefrScene();
        } else if (cmd.equalsIgnoreCase("silence")) {
            getClient().cmdSilence();
        } else if (cmd.length()>=6 && cmd.substring(0,7).equalsIgnoreCase("custom:")) {
            getClient().cmdCustom(cmd.substring(7));
        } else {
            throw new DimxException("Unsupported client command: " + cmd);
        }
        
        return true;
    }
    
    public void updMapPos() {
        if (world != null && world.map != null && container != null) { // If player living AND default map defined...
            int mapx = Utils.cInt(container.varGet("mapx").numVal());
            int mapy = Utils.cInt(container.varGet("mapy").numVal());
            String map;
            Room r = this.getRoom();
            if (r == null || r.varGet("map").isNull()) { // No room or no special map defined
                map = world.map; // Specify world's default map
            } else {
                map = Utils.absolutizeUrl(r.varGet("map").strVal(), world.imagesFolder); // Specify special map
            }
            this.msgBoard.cmdCustom("map!"+mapx+","+mapy+","+map);
        }
    }
    public boolean useView(Page aView) throws DimxException {
        view = aView;
        return true;
    }
    
    /**
     * See AdvObject.WorldChange
     * @param See AdvObject.WorldChange
     * @param See AdvObject.WorldChange
     * @throws cleoni.adv.DimxException in case of problems
     */
    public void worldChange(World toWorld, String newid, String defContainer)  throws DimxException {
        String fromWorldName = this.world.id;
        // Remember panel ID
        String panelid = null;
        Panel currpanel = this.getPanel();
        if (currpanel != null) panelid = currpanel.id;
        // Update Session
        Utils utils = this.getClient().session;
        utils.setSession("worldinstanceid",toWorld.instanceid);
        utils.setSession("worldid",toWorld.id);
        utils.setSession("game",toWorld.slot);
        // Message world
        toWorld.hear(toWorld.defaultCharacter, toWorld.msgs.actualize(toWorld.msgs.msg[160],this.name.strVal(), fromWorldName));
        super.worldChange(toWorld,newid,defContainer);
        utils.setSession("userid",this.id);
        //o.id = null; // Clear up id so that it is redefined
        toWorld.addPlayer(this,null,panelid,"","", null);
        this.updMapPos(); // map changed
   }

    public void afterWorldChange() throws DimxException {
        super.afterWorldChange();
        this.world.logger.debug("player is now in: " + this.container);
    }
    
    public boolean setPanel(String panelId) throws DimxException {
        // Sets the specified panel for this Character
        if (panelId == null || panelId.equals("")) panelId = "default";
        world.logger.debug("Setting panel: " + panelId + " for player: " + this.id);
        
        Panel myPanel = (Panel) world.getPanel(panelId);
        
        if (myPanel == null)
            throw new DimxException("Unexistent panel: " + panelId);
        
        if (panelId.equalsIgnoreCase("default")) myPanel = null;
        
        panel = myPanel;
        
        if (!getClient().getPanelIds().equalsIgnoreCase(prevPanelIds)) {
            prevPanelIds = getClient().getPanelIds();
            getClient().cmdRefrCtrls();
        }
        
        return true;
        
    }
    
}