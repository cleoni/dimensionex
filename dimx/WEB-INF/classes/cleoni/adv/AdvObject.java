package cleoni.adv;

import java.util.Vector;

/** Game's base object. CHARACTER, ITEM, LINK and ROOM are AdvObjects
 * All the common tasks and properties are here.
 * Most of the DimX game code is here.
 * Please see Class hierarchy of the Javadoc.
 */
public class AdvObject extends DimxObject {


    private	Token icon = null;
    public	Token capacity = null;
    public	Token volume = null;
    private	Token description = null;

    public	String defContainer; 	// Defult container: Id or expression yielding an Id
    public	Token images = new Token(new Dict());
    public	Token showfor = new Token();
    public	Token showmode = new Token(Const.OFFSCREEN);		// Showing mode
    public	Token facing = new Token("N"); // by default all objects are facing North
    public	AdvObject container = null;

    public static String[] methods =
    {};

    // Number of expected parameter for each instruction. -1 means "can vary"
    public static int[] methodArgs =
    {};

    /**
     * AdvObject constructor comment.
     */
    public AdvObject(World aWorld, String aName, String anId,
    String aDescription, String anIcon,
    String attrList,
    int aCapacity,
    int aVolume,
    boolean setDefShowpos,
    String aDefContainer) throws DimxException {
        super();
        world = aWorld;
        id = anId;
        name = new Token(aName);
        description = new Token(aDescription);
        if (world != null)
            icon = new Token(Utils.absolutizeUrl(anIcon,world.imagesFolder));
        capacity = new Token(aCapacity);
        volume = new Token(aVolume);
        defContainer = aDefContainer;
        if (setDefShowpos) varsSet("showx=-1,showy=-1");
        varsSet(attrList);
    }



    protected boolean contains(String whatId) {
        boolean res = false;
        if (contents.get(whatId) != null) {
            res = true;
        } else {
            int i=0;
            while (i<contents.size() && !res) {
                res = ((AdvObject)contents.elementAt(i)).contains(whatId);
                i++;
            }
        }
        return res;
    }





    public Room getRoom() {
        Room ret = null;
        if (container != null) {
            if (container.isaRoom())
                ret = (Room) container;
            else
                ret = container.getRoom();
        }
        return ret;
    }

    /** Gets the current container but checks if it is a regular "people container",
     * that is, a Container object type.
     * @return The reference, or null if it i not a Container object
     */
    public PeopleContainer getPeopleContainer() {
        try {
            return (PeopleContainer) container;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public Dict getContents(boolean checkOpen) {
        Dict contents = getContents();
        if (checkOpen) {
            if (isanItem()) {
                if (contents.size() == 0 || !isOpen()) {
                    contents = null;
                }
            }
        }
        return contents;
    }
    public String getIcon() {
        String s = icon.strVal();
        if (!(s.startsWith("https:") || s.startsWith("http:"))) {
            s = Utils.absolutizeUrl(s, world.imagesFolder);
            icon.setVal(s);
        }
        return s;
    }
    public String getDescription(AdvObject agent,DictSorted input) throws DimxException {
        String s = description.strVal();
        if (s == null)
            s = "";
        else {
            if (s.startsWith("@") && agent != null && input != null) {
                s = world.evaluateExpression(s.substring(1), this, agent, null, input).strVal();
            }
        }
        // Open/Closed?
        if (isOpenable()) {
            if (!s.equals("")) {
                s = s + "\n";
            }
            s = s + this.getName() + " ";
            if (isOpen()) {
                s = s + world.msgs.msg[102] + ".";
            } else {
                s = s + world.msgs.msg[103] + ".";
            }
        }
        return s;
    }
    protected int getFreeSpace() {
        return capacity.intVal() - getUsedSpace();
    }

    public String getTypePrefix() {
        return "?!";
    }
    protected int getUsedSpace() {
        int used =0;
        for (int i=0; i < contents.size(); i++) {
            used += ((AdvObject) contents.elementAt(i)).volume.intVal();
        }
        return used;
    }

    /*
     * Manages hearing voice (event firing) for all object
     */
    public boolean hear(DimxObject from, String msg) throws DimxException  {
        boolean res = true;
        if (world != null) {
            if (from == null) from = world.defaultCharacter;

            if (!msg.equals("") && from != this) {
                res = world.fireEvent("onHear",this,from.id,msg,true);
              }
        }
        return res;
    }
        public boolean hide(StringBuffer msg)  throws DimxException {
        if (world == null) throw new DimxException("Tried to hide non-existent object: " + this.toString() + " type='" + this.varGet("type") + "'");
        if (!isHidden()) {
            if (isHideable()) {
                if (container.isaRoom() || container.isanItem()) {
                    this.varGet("hidden",Const.GETREF).assign(new Token(1.0),world);
                    return true;
                } else { // Can't hide inside a character
                    msg.append(world.msgs.msg[106]);
                }
            } else {
                msg.append(world.msgs.msg[106]);
            }
        } else {
            msg.append(world.msgs.msg[107]);
        }
        return false;
    }
/*
 * L'oggetto corrente ? quello da raggiungere
 * from ? il punto di partenza
 */
    protected boolean isAccessibleFrom(AdvObject from) {
        if (from.getContents().get(id) != null) // If contained inside, then OK
            return true;
        AdvObject myCont = from.container;
        AdvObject cont = container;
        //world.logger.debug("I am in: " + myCont + " the object to be accessed is in: " + cont );
        if (myCont == cont || myCont == this) // Se siamo contenuti nello stesso oggetto OK
            return true;
        if (cont != null) {
            //world.logger.debug("...which in turn is in: " + cont.container );
            if (cont.isanItem()) {
                // Si trova in qualcosa e..
                if (cont.isOpen() && (cont.container == myCont || cont.container == from)) {
                    // il contenitore ? aperto e sta sul pavimento oppure ce l'ho io
                    return true;
                }
            }
        } else {
            // cont == null that's to say: the object to be accessed, either is a ROOM or a LINK or it is nowhere
            if (this.isaRoom() && myCont == this) {
                // If it is a ROOM and I am there OK
                return true;
            } else if (this.isLink()) {
                Link w = (Link) this;
                if (w.getRoom() == myCont || (w.isBidirectional && w.leadsTo(myCont.id))) {
                    // If it is a LINK and I am in the source or arrival, room OK
                    return true;
                }
            }
            //Logger.echo("AdvObject.isAccessibleFrom: ERROR: How is it possible that container of " + this + " is NULL?");
        }
        return false;
    }
    public boolean isRobot() {
        return false;
    }
    public boolean isVehicle() {
        return false;
    }
    public boolean isPeopleContainer() {
        return false;
    }
    public boolean isPlayer() {
        return false;
    }
    protected boolean isHidden() {
        return (varGet("hidden").numVal()>0.0);
    }
    public boolean isHideable() {
        boolean res = true;
        if (varExists("hideable") && (varGet("hideable").boolVal() == false)) res = false;
        return res;
    }
    public boolean isLink() {
        return false;
    }
    public boolean isLocked() {
        return (varGet("locked").numVal()>0.0);
    }
/*
 * Due oggetti si dicono vicini sse
 * stanno nella stessa stanza oppure quando
 * uno di essi ? la stanza in cui sta l'altro oppure
 * quando uno di essi ? una via che parte dalla stanza
 * dove sta l'altro
 * Notare che questa implementazoine rspnde false
 * nel caso di due vie bidirectional che partono
 * in un qualche modo dalla stessa stanza perch?
 * si diventa matti a fare le due combinazioni (uso startsFrom)
 */
    protected boolean isNear(String targFullId) {
        try {
            AdvObject targ = world.getObject(targFullId);
            if (targ !=null) {
                if (targ == this) {
                    // Ogni oggetto ? vicino a s? stesso
                    world.logger.debug("The two objects coincide then are near");
                    return true;
                } else if (container == targ.container) {
                    // Ogni oggetto che sta in una stanza ? vicino alla stanza e a ogni oggetto che sta in quella stanza
                    world.logger.debug("The two objects are located in the same room");
                    return true;
                } else if (targ.container == this) {
                    world.logger.debug("Object #1 contains object #2");
                    return true;
                } else if (isLink() && !targ.isLink() && ( ((Link) this).startsFrom(targ.container.id) || ((Link) this).leadsTo(targ.container.id) ) ){
                    // Se questa ? una via ? vicina a un oggetto purch? questa parta dalla stanza in cui si trova l'oggetto
                    world.logger.debug("The way is near the object");
                    return true;
                } else if (!isLink()
                && targ.isLink()
                && ( ((Link) targ).startsFrom(this.container.id) || ((Link) targ).leadsTo(targ.container.id) ) ){
                    // Se questo ? un oggetto ? vicino a una via purch? la via parta dalla stanza in cui si trova questo oggetto
                    world.logger.debug("The object is near the way");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            world.logger.debug("AdvObject.isNear(): ERROR: " + e);
            e.printStackTrace(System.out);
            return false;
        }
    }
    protected boolean isOpen() {
        return varGet("open").boolVal();

    }
    protected boolean isOpenable() {
        if (varExists("openable") && (varGet("openable").boolVal() == false)) {
            // Se specificato chiaramente che non ? apribile allora non lo ?
            return false;
        } else {
            // Se ? aperto o chiuso o se solo ? chiudibile a chiave allora lo ?
            if (varExists("open") || varExists("locked")) {
                return true;
            }
        }
        return false;
    }
    public boolean isPickable() {
        return varGet("pickable").boolVal();
    }

    /** Places an object into this one.
     * @param what Object to be placed
     * @param checkOpen Shall I check "open" property first?
     * @param agent Object causing the action
     * @throws DimxException any error
     * @return true if succeeded
     */
    public boolean objPlace(AdvObject what, boolean checkOpen, AdvObject agent, boolean checkEvents) throws DimxException {
        if (checkOpen) {
            if (!isOpen()) {
                world.logger.debug("itemPlace: Impossibile because object " + this + " is not open or not suitable as a container");
                return false;
            }
        }

        boolean res = true;

        // Trigger events
        String agentid = "";
        if (agent != null) agentid = agent.id;

        if (this.getFreeSpace() >= what.volume.intVal()) {

            AdvObject orig_container = what.container;

            // Two operations which should be always linked together
            contents.put(what.id,what);
            what.container = this;

            // Refresh players' view
            sendCmd(Const.CMDE_REFRSCENE);

            if (this.isaCharacter()) {
                if (this.id == null || this.id.equals("")) throw new DimxException("NULL target!");
                res = res && world.fireEvent("whenPicked", what, agentid, this.id, true);
            } else {
                res = res && world.fireEvent("onEnter", what, agentid, this.id, true);
            }
            if (what.isanItem()) {
                res = res && world.fireEvent("onReceiveItem",this,agentid,what.id,true);
            } else {
                res = res && world.fireEvent("onReceive",this,agentid,what.id,true);
            }

            if (!checkEvents) res = true; // Events' result cannot prevent action to be completed in selected situations

            if (!res) { // Oeration failed!
                // Check if the item should be removed (could be done by programmer in the onReceive event body)
                if (contents.remove(what.id)) { // It tolerates if contents does not contain "what" (already moved)
                    what.container = orig_container; // Restore original
                }
            }
        } else {
            world.logger.debug("AdvObject.objPlace: Not enough capacity in " + this );
            res = false;
        }

        if (res && what.isaCharacter()) {

            if (what.isPlayer()) { // Check for new commands panel
                Player thisPlayer = (Player) what;

                String panelIds = thisPlayer.getClient().getPanelIds();

                if (!panelIds.equalsIgnoreCase(thisPlayer.prevPanelIds)) {
                    thisPlayer.prevPanelIds = panelIds;
                    thisPlayer.getClient().cmdRefrCtrls();
                }
            }
        }
        return res;
    }


    protected boolean open(Character agent) throws DimxException {
        if (isOpenable()) {
            if (agent != null) {
                world.fireEvent("beforeOpen",this,agent.id,"", false);
            }
            if (!isOpen()) {
                if (!isLocked()) {
                    this.varGet("open",Const.GETREF).assign(new Token(1.0),world);
                    if (agent != null) {
                        world.fireEvent("onOpen",this,agent.id,"", false);
                    }
                    return true;
                } else {
                    if (agent != null) {
                        agent.display(world.msgs.msg[109]);
                    }
                }
            } else {
                if (agent != null) {
                    agent.display(world.msgs.msg[110]);
                }
            }
        } else {
            if (agent != null) {
                agent.display(world.msgs.msg[111]);
            }
        }
        return false;
    }




    /** sets the image for the specified face.
     *
     * You must specify a valid value (N S E W) otherwise the results may be
     * unpredictable
     * @param face
     * @param anImage
     * @return
     */
    public boolean setImage(String face, Image anImage) {
        images.dictVal().put(face,new Token(anImage));
        return true;
    }

    public String toString() {
        return id + " (" + this.getName() + ")";
    }
    public void setFacing(String aDirection) {
        facing = new Token(aDirection);
    }


    /** Attaches and event to the current object
     * @param eventid Id of the event you want the object to respond to
     * @param copyfromid ID of the event you are copying from
     * @param tcopyfrom Event you are copying from, incapsulated into a Token object
     * @throws DimxException in case of problems
     * @return true upon success, throws exception otherwise
     */
    public boolean attachEvent(String eventid, String copyfromid, Token tcopyfrom) throws DimxException {

        Event e = null;
        Event newEvent = null;

        if (tcopyfrom != null) {
            try {
                e = tcopyfrom.eventVal();
            } catch (ClassCastException ex) {
            }

            if (e == null) {
                throw new DimxException("This is not a valid event: " + copyfromid);
            }
            //world.logger.debug("OK - got event: " + e);

            newEvent = new Event(this,this.id + "." + eventid,this,"EVENT",e.getFormalParams());
            newEvent.setActions(e.getActions(),e.actionsStartLine,e.fileName);
            newEvent.attachedEventsId = copyfromid;

            this.varGet(eventid, Const.GETREF).assign(new Token(newEvent),world);
        } else {
            this.varGet(eventid, Const.GETREF).assign(new Token(),world);
        }

	return true;
    }
    public boolean close(Character agent) throws DimxException {
        if (isOpenable()) {
            if (isOpen()) {
                    this.varGet("open",Const.GETREF).assign(new Token(0.0),world);
                if (agent != null) {
                    world.fireEvent("onClose",this,agent.id,"",false);
                }
                return true;
            } else {
                if (agent != null) {
                    agent.display(world.msgs.msg[100]);
                }
            }
        } else {
            if (agent != null) {
                agent.display(world.msgs.msg[101]);
            }
        }
        return false;
    }

public void copyFacesInto(String[] res) {
    Dict ims = images.dictVal();
    for (int i=0; i<ims.size(); i++) {
        res[i] = ((Token) ims.elementAt(i)).imageVal().getSrc();
    }
}
    public void debug(String s) {
            if (world != null) {
                world.logger.debug(s);
            } else {
                Logger.echo("ERROR: for object: " + this.id + " world=NULL");
            }
    }
/*
 * Leaves current room+world
 *
 */
    /**
     * makes internal cleaning before object destruction
     * @param removeFromContainer Should the object be removed from its container?
     * @param contentsPolicy 0 = Drop them to floor
     * 1 = Recurse death
     * 2 = Keep them (warning - this may lead to inconsistent results)
     * @throws cleoni.adv.DimxException in case of problems
     */
    public void die(boolean removeFromContainer, int contentsPolicy) throws DimxException  {
        AdvObject c = container;
        Dict contents = getContents();

        if (world == null) {
            //throw new DimxException("Tried to destroy object " +  this + " - world=NULL");
        }

        if (removeFromContainer) {
            if (world != null) world.logger.debug("Removal of " + this + " from its container: " + c);
            if (c!= null) c.objRemove(this,null,c,null,Const.DONT_CHECK_OPEN,Const.FORCE_REMOVE);
        }

        DictEnumerator e = new DictEnumerator(contents);
        if (contentsPolicy == 1) { // Inner contents must also die
            if (world != null) world.logger.debug("Destroying all inner items of " + this);
            while (e.hasMoreElements ()) {
                AdvObject it = (AdvObject) e.nextElement();
                it.die(false,1); // Don't extract inner items, recurse death in inner items
            }
        } else if (contentsPolicy == 0) {
            if (!this.isaRoom()) {
                if (world != null) world.logger.debug("Dropping all inner items of " + this);
                while (e.hasMoreElements ()) {
                    AdvObject it = (AdvObject) e.nextElement();
                    this.objRemove(it, null, this, c, Const.DONT_CHECK_OPEN,Const.FORCE_REMOVE);
                    c.objPlace(it, Const.DONT_CHECK_OPEN, null, Const.DONT_CHECK_EVENTS);
                }
            }
        }
         if (world != null && !world.removeObject(this)) throw new DimxException("Could not remove object: " + this + " from world " + world);
    }


    public Image getExactImage(String face) {
        // torna l'immagine chiesta. Per avere la pi? vicina chiedere getNearestImage
        // Questa in realt? ? usata solo da Room per capire se l'osservatore deve essere
        // ruotato o no su se stesso
        Token t = (Token) images.dictVal().get(face);
        if (t != null)
            return t.imageVal();
        else
            return null;
    }

    public String[] getMethods() {
        return Utils.array_merge(this.methods,super.getMethods());
    }

    public int getMethodArgs(String mname) {
        int i = Utils.indexOf(mname, this.methods);
        if (i >= 0) //Method is in this class
            return this.methodArgs[i];
        else
            return super.getMethodArgs(mname);
    }


    /*
     * gets the nearest image to the specified face.
     * returns an object, whose element 0 is the image (Image), element 1 is the facing (String)
     */
    public Object[] getNearestImage(String face) {
        Object[] ret = new Object[2];
        Dict myimages = images.dictVal();
        Image im;
        int howmany = myimages.size();
        if (howmany == 1) {
            // Simple case - one image only
            ret[1] = myimages.keyAt(0);
            ret[0] = ((Token) myimages.elementAt(0)).imageVal();
            return ret;
        }
        if (howmany == 0) return null;
        // Else many images
        face = Utils.getRelativeDirection(face, this.facing.strVal());
        im = getExactImage(face);
        if (im != null) { // If found exit
            ret[1] = face;
            ret[0] = im;
            return ret;
        }

        // Try to find the nearest one
        Token t;
        if (face.equals("N")) {
            if ((t = (Token) myimages.get("W")) != null) {
                ret[0] = t.imageVal();
                ret[1] = "W";
            } else	if ((t = (Token) myimages.get("E")) != null) {
                ret[0] = t.imageVal();
                ret[1] = "E";
            } else {
                ret[0] = ((Token) myimages.get("S")).imageVal();
                ret[1] = "S";
            }
        } else if (face.equals("S")) {
            if ((t = (Token) myimages.get("E")) != null) {
                ret[0] = t.imageVal();
                ret[1] = "E";
            } else	if ((t = (Token) myimages.get("W")) != null) {
                ret[0] = t.imageVal();
                ret[1] = "W";
            } else {
                ret[0] = ((Token) myimages.get("N")).imageVal();
                ret[1] = "N";
            }
        } else if (face.equals("W")) {
            if ((t = (Token) myimages.get("N")) != null) {
                ret[0] = t.imageVal();
                ret[1] = "N";
            } else if ((t = (Token) myimages.get("S")) != null) {
                ret[0] = t.imageVal();
                ret[1] = "S";
            } else {
                ret[0] = ((Token) myimages.get("N")).imageVal();
                ret[1] = "E";
            }
        } else if (face.equals("E")) {
            if ((t = (Token) myimages.get("S")) != null) {
                ret[0] = t.imageVal();
                ret[1] = "S";
            } else	if ((t = (Token) myimages.get("N")) != null) {
                ret[0] = t.imageVal();
                ret[1] = "N";
            } else {
                ret[0] = ((Token) myimages.get("W")).imageVal();
                ret[1] = "W";
            }
        } else {
            return null;
        }
        return ret;
    }

    public Panel getPanel() {
        // Player and Room will override this
        return null;
    }

    /** Moves the object along a Link.
     * Should be used only on Character and Vehicle objects.
     * @param wayId
     * @throws DimxException
     * @return
     */
    public boolean go(String wayId) throws DimxException {
        PeopleContainer container = getPeopleContainer();
        World wasworld = this.world;
        if (container != null) { // OPTIMIZE
            Link w = container.getWay(wayId);
            if (w != null) {
                if (w.isOpen()) {
                    AdvObject target = w.getTarget(container.id);
                    if (target != null) {
                        String contname = container.getName();
                        if (container.objRemove(this,this,container,w,Const.CHECK_OPEN,Const.CHECK_EVENTS)!=null) {
                            if ((this.container) == null) {
                                target.objPlace(this,Const.DONT_CHECK_OPEN,this,Const.CHECK_EVENTS);
                                // Adjust facing - is there an image in the Link direction?
                                String dir = w.getDirection(container.id);
                                if (target.getExactImage(dir) != null) {
                                    this.setFacing(dir);
                                    if (this.isVehicle()) { // Update passengers
                                        Dict contents = this.getContents();
                                        for (int i=0; i < contents.size(); i++) {
                                            AdvObject o = (AdvObject) contents.elementAt(i);
                                            if (o.isPlayer()) o.setFacing(dir);
                                        }
                                    }
                                }
                                // Otherwise keep current facing
                                return true;
                            }
                        } else {
                            wasworld.logger.debug("Could not move " + this.getName() + " away from " + contname + ": action cancelled.");
                        }
                    } else {
                        display("?");
                        world.logger.debug("ERROR: LINK " + wayId + " leads to NULL");
                    }
                } else {
                    display(w.getName() + " " + world.msgs.msg[103]);
                }
            } else {
                display("?");
                world.logger.debug("ERROR: LINK " + wayId + " does not exist - (Player used REFRESH?)");
            }
        } else {
            display("?");
            world.logger.debug("ERROR: container is NULL");
        }
        return false;
    }

    /*
     * Decides whether the specified URL represents an image of this object
     */
    public boolean hasImageUrl(String imageUrl) {
        Dict myimages = images.dictVal();
        for (int i=0; i < myimages.size(); i++) {
            Image im = ((Token) myimages.elementAt(i)).imageVal();
            if (im.getSrc().endsWith(imageUrl)) return true;
        }
        return false;
    }

    protected boolean hasSeveralFaces() {
        return (images.dictVal().size() > 1);
    }

    public AdvObject objRemove(AdvObject what, AdvObject agent, AdvObject source, AdvObject dest, boolean checkOpen, boolean checkEvents) throws DimxException {
        //AdvObject what = (AdvObject) contents.get(owner);
        if (this.world != null && what.world == null) {
            this.world.logger.log("Tried to remove un-existent object: " + what.toString() + " from: " + this.toString());
            return null;
        }
        if (this.world == null && what.world != null) {
            what.world.logger.log("Tried to remove valid object :" + what.toString() + " from null world container: " + this.toString() + " skipping events");
            return what;
        }
        if (checkOpen) {
            if (varExists("open") && !isOpen()) {
                // not open!
                return null;
            }
            if (isaCharacter()) {
                // stealing not allowed
                return null;
            }
        }

        if (what != null) {
            String agentid = "";
            if (agent != null) agentid = agent.id;

            String sourceid = "";
            if (source != null) sourceid = source.id;

            String destid = "";
            if (dest != null) destid = dest.id;

            String whatid = "";
            if (what != null) whatid = what.id;

            boolean res = true;

            if (this.isaCharacter()) {
                res = res && world.fireEvent("whenDropped",what,agentid,destid,true);
            } else {
                if (world == null) throw new DimxException("WORLD = NULL: " + this.toString() + " type='" + this.varGet("type") + "'");
                res = res && world.fireEvent("onExit",what,agentid,destid,true);
            }
            if (what.isanItem()) {
                res = res && world.fireEvent("onLooseItem",this,agentid,whatid,true);
            } else {
                String target = destid;
                if (agentid.equals("")) {
                    target = whatid;
                }
                res = res && world.fireEvent("onLoose",this,agentid,target,true);
            }

            if (!checkEvents) res = true; // Events' result cannot prevent action to be completed in selected situations

            if (res && contents.remove(what.id)) {
                what.container = null;

                // Refresh players' view
                sendCmd(Const.CMDE_REFRSCENE);

                return what;
            }
        }
        return null;

    }

    /** Resets the object in its initial position
     * @throws DimxException in case of problems
     */
    public void reset() throws DimxException {
        AdvObject cont;

        if (defContainer != null && defContainer != "") {
            cont = world.getObject(defContainer);
            if (cont == null) {	// Maybe it's an expression - try to parse

                DimxParser p = new DimxParser(world,Const.WORLDVARS,0,this.id);
                p.feed(defContainer);
                cont = world.getObject(p.evalExpression(p.lookupToken(),0));
            }
            if (cont != null)  {
                cont.objPlace(this,Const.DONT_CHECK_OPEN,this,Const.DONT_CHECK_EVENTS);
            }

            if (this.container == null && world != null) { // If world == null then the object have been destroyed by events triggered in objPlace
                world.logger.debug("WARNING: Can't reposition '" + id + "' in: " + defContainer + " - Trying default place");
                world.getObject(world.defaultRoom).objPlace(this,Const.DONT_CHECK_OPEN,null,Const.DONT_CHECK_EVENTS);
                if (this.isPlayer()) ((Player) this).getClient().displayh("Cannot restore saved position - using default");
            }
        }
    }

    /*
     * gets a property
     * always returns a valid token
     */
    public Token varGet(String varId, boolean getReference)  throws DimxException {
        Token res = null;

        if (varId.equalsIgnoreCase("description")) {
            if (getReference) return description;
            else return description.getClone();
        } else if (varId.equalsIgnoreCase("container")) {
            //if (getReference) {
            //    world.logger.log("WARNING: Suspect attempt to assign to container property. Use Move instruction instead.");
            //}
            if (container != null) {
                res = new Token(container);
            } else {
                res = null;
            }
        } else if (varId.equalsIgnoreCase("image")) {
            // For the moment we return image facing North or nearest
            if (getReference) {
                res = images;
            } else { // read only - get north side
                res = new Token((Dict) images.dictVal().clone());
            }
        } else if (varId.equalsIgnoreCase("icon")) {
            if (getReference) return icon;
            else return icon.getClone();
        } else if (varId.equalsIgnoreCase("showFor")) {
            if (getReference) return showfor;
            else return showfor.getClone();
        } else if (varId.equalsIgnoreCase("capacity")) {
            if (getReference) return capacity;
            else return capacity.getClone();
        } else if (varId.equalsIgnoreCase("volume")) {
            if (getReference) return volume;
            else return volume.getClone();
        } else if (varId.equalsIgnoreCase("showmode")) {
            if (getReference) return showmode;
            else return showmode.getClone();
        } else if (varId.equalsIgnoreCase("facing")) {
            if (getReference) return facing;
            else return facing.getClone();
        } else {
            res = super.varGet(varId, getReference);
        }

        if (res == null) {
            // debug("WARNING: " + this.id + "." + varId + " = null");
            res = new Token(""); // NULL
        }
        return res;
    }

    protected String varGetIdAt(int i) {
        return properties.keyAt(i);
    }

    protected int varsCount() {
        return properties.size();
    }

   public int varsToHtmlTable(StringBuffer sb, String title)
    {
        int attrcount = 0;
        //sb.append("<DIV CLASS=text>" + title + "</DIV>");
        sb.append("<TABLE BORDER=0 CELLPADDING=2 CELLSPACING=0>");
        sb.append("<TR VALIGN=TOP><TD CLASS=text>" + title + "</TD></TR>");
        for (int i=0; i < properties.size(); i++) {
            String k = properties.keyAt(i);
            Token t = (Token) properties.elementAt(i);
            if (!t.isEvent()) {
                boolean displayit = false;

                if (world.showproperties == null) {
                    // only Vars starting with Caps letters are shown
                    char c = k.charAt(0);
                    if (c >= 'A' && c <= 'Z') {
                        displayit = true;
                    }
                } else {
                    // check if the property "k" is in the showproperties list
                    displayit = Utils.isIn(k,world.showproperties);
                }

                if (displayit) {
                        sb.append("<TR VALIGN=TOP><TD CLASS=iconlabel>" + k + ": ");
                        sb.append(((Token) properties.elementAt(i)).strVal());
                        attrcount++;
                        sb.append("</TD></TR>");
                }
            }
        }
        sb.append("</TABLE>");
        return attrcount;
    }

    protected boolean varsSet(String attrlist) throws DimxException {
        Token t = null;
        if (attrlist != null) {
            // Tutto sugli attributi di default
            Vector list = Utils.stringSplit(attrlist,",");
            for (int i=0; i < list.size(); i++) {
                String s1 = (String) list.elementAt(i);
                Vector couple = Utils.stringSplit_tolerant(s1,"=");
                if (couple.size() > 0) {
                    String k = (String) couple.elementAt(0);
                    if (couple.size() > 1) {
                        // Value specified
                        // Decide if string or number
                        String v = (String) couple.elementAt(1);
                        if (Utils.cDbl(v) > 0 || v.equals("0")) {
                            t = new Token(Utils.cDbl(v));
                        } else {
                            t = new Token(v);
                        }
                    } else {
                        // No value specified - zero default
                        t = new Token(1.0);
                    }
                    if (k.equalsIgnoreCase("icon")) { // Special case - fixup
                        t.setVal(Utils.absolutizeUrl(t.strVal(),world.imagesFolder));
                    }
                    this.varGet(k, Const.GETREF).assign(t,world);
                } else {
                    this.world.logger.log("Warning: Incorrect attribute list '"+attrlist+"' for object: "+this.id);
                }
            }
        }
        return true; // Normalmente ritorna OK
    }

    public String listImages() {
        StringBuffer sb = new StringBuffer();
        Dict imm = this.images.dictVal();
        for (int im=0; im< imm.size(); im++) {
            Image xim = ((Token) imm.elementAt(im)).imageVal();
            sb.append("" + imm.keyAt(im) + "=");
            if (xim != null) {
                sb.append("" + xim.toDXW() + "<BR>");
            }
        }
        return sb.toString();
    }
    /**
     * @return
     * @param agent
     * @param wheres
     * @param checkOpen
     * @throws DimxException
     */
    public boolean moveTo(AdvObject where, AdvObject agent, boolean checkOpen, boolean checkEvents) throws DimxException {
        if (world == null && where.world != null) {
            where.world.logger.log("Tried to move non-existent object: " + this.toString() + " type='" + this.varGet("type").strVal() + "' in: " + where);
        }
        boolean ret = true;

        if (where != null) {
            if (!this.isaRoom()) {
                if (checkOpen) {
                    // Check before - so that we don't take the object out in vain
                    if (where.varExists("open") && !where.isOpen()) {
                        world.logger.debug("Cannot move " + this + ": destination closed");
                        return false;
                    }
                }
                if (this.volume.intVal() <= where.getFreeSpace()) {
                    // Get object...
                    AdvObject o = container;
                    if (o != null) {
                        ret = (o.objRemove(this,agent,o,where,checkOpen,checkEvents) != null);
                        if (ret == false)
                            if (world != null)
                                world.logger.debug("WARNING: Move of " + this + " from its container failed");
                            else
                                Logger.echo("WARNING: Move of " + this + " from its container failed");
                    }
                    if (ret) {
                        // ...Put object
                        ret = where.objPlace(this,Const.DONT_CHECK_OPEN,agent,checkEvents);
                        if (ret == false && world != null)
                                // world != null check is necessary because this object could have been replaced by EVENTs and no more existing
                                world.logger.debug("WARNING: Placing " + this + " into " + where + " failed.");
                    }
                } else {
                    ret = false;
                    if (world != null) world.logger.debug("Not enough room in " + where);
                }
            } else {
                    world.logger.debug("ERROR: ROOM objects cannot be moved.");
            }
        } else {
            if (world == null) throw new DimxException("Problem: " + this + " has world = null");
            if (world.logger == null) throw new DimxException("Problem: " + this + " has world.LOGGER = null");
            world.logger.debug("ERROR: Destination is NULL - cannot move.");
        }
        return ret;
    }

    /**
     * Changes references to the world for the current object, and all its inner objects.
     * @param toWorld reference to the new world this object should belong to
     * @param newid new id that the object should have. null = get new one automatically
     * @throws cleoni.adv.DimxException in case of problems
     */
    public void worldChange(World toWorld, String newid, String defContainer)  throws DimxException {
        World fromWorld = this.world;
        if (fromWorld != null) {
            if (!fromWorld.removeObject(this)) {
                fromWorld.logger.debug("INTERNAL ERROR: Impossible to remove: "+this+" from "+fromWorld);
                throw new DimxException("World change error: Impossible to remove: "+this+" from "+fromWorld);
            }
        }
        if (newid == null) newid = toWorld.getNextId();
        this.id = newid;
        this.world = toWorld;
        if (defContainer == null) defContainer = toWorld.defaultRoom; // Patch default container if needed
        this.defContainer = defContainer;
        Dict prevcontents = (Dict) this.contents.clone();
        int n = prevcontents.size();
        for (int i=0; i < n; i++) {
            AdvObject x = (AdvObject) prevcontents.elementAt(i);
            String old_id = x.id;
            x.worldChange(toWorld,null, newid);
            this.contents.put(x.id,x);
        }
   }

   /**
    * Does any final operations after world change (typically calls reset() )
    * @throws cleoni.adv.DimxException if problems
    */
   public void afterWorldChange() throws DimxException {
        if (this.container == null) this.reset();
        for (int i=this.contents.size()-1; i >= 0; i--) {
            AdvObject x = (AdvObject) this.contents.elementAt(i);
            x.afterWorldChange();
        }
   }
}