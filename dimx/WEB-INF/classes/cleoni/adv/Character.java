package cleoni.adv;

/** Implements CHARACTERs */
public class Character extends AdvObject {
    public	Token accepting = null;// null means will accept everything
    public static String[] methods =
    {"go"};
    
    // Number of expected parameter for each instruction. -1 means "can vary"
    public static int[] methodArgs =
    {1};
    
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

    public Token execMethod(String mname,Dict params) throws DimxException {
        String mnamel = mname.toLowerCase();
        if (mnamel.equals("go")) {
            return metGo(params);
        } 
        
        return super.execMethod(mname, params);
    }

    private Token metGo(Dict params) throws DimxException {
        Token p0 = (Token) params.elementAt(0);
        AdvObject cont = this.container;
        if (cont != null && cont.isaRoom()) {
            String dir = p0.strVal().toUpperCase();
            Dict links = ((Room) cont).getLinks();
            for (int i=0; i<links.size(); i++) {
                Link ln = (Link) links.elementAt(i);
                if (ln.getDirection(cont.id).equals(dir)) {
                    boolean res = go(ln.id);
                    return new Token(res);
                }
            }
        }
        return new Token(false);
    }

    public String getTypePrefix() {
        return "c!";
    }
    
    public boolean isaCharacter() {
        return true;
    }
    public boolean isRobot() {
        return true;
    }
    

    protected boolean itemDrop(AdvObject what) throws DimxException {
        if (what != null) { // Do the object exist?
            AdvObject cont = what.container;
            if (cont != null) { // Do the container exist?
                if (what.volume.intVal() <= container.getFreeSpace()) { // Is there enough room?
                    if (this.contains(what.id)) { // Do I own the object?
                        AdvObject it = cont.objRemove(what,this,cont,this.container,Const.DONT_CHECK_OPEN,Const.CHECK_EVENTS);
                        if (it != null) {
                            if (container.objPlace(it,false,this,Const.CHECK_EVENTS) || what.world == null) {
                                display(world.msgs.msg[113] + " " + it.getName());
                                return true;
                            } else {
                                display(world.msgs.msg[114] + " " + it.getName());
                            }
                        } else {
                            world.logger.debug("Warning: " + world.msgs.msg[115] + " " +  what.getName());
                        }
                    } else {
                        display(world.msgs.msg[116]);
                    }
                } else {
                    display(world.msgs.msg[108]);
                }
            } else {
                display(world.msgs.msg[117] + " " + what.getName() + "?!?");
            }
        } else {
            display(world.msgs.msg[118]);
        }
        return false;
    }
    protected boolean itemGive(String fullId, String toWhoId) throws DimxException {
        // Refers always to a CHARACTER giving to a CHARACTER
        AdvObject what = world.getObject(fullId);
        if (what != null) {
            if (what.isAccessibleFrom(this) && this.isNear(toWhoId)) {
                Character where = world.getPeople(toWhoId);
                if (where != null &&
                what.isanItem()) {
                    if (what.isPickable()) {
                        if (where.accepts(what)) {
                            AdvObject cont = what.container;
                            AdvObject it = cont.objRemove(what,this,this,where,Const.DONT_CHECK_OPEN,Const.CHECK_EVENTS);
                            if (it != null) {
                                if (where.objPlace(it,Const.DONT_CHECK_OPEN,this,Const.CHECK_EVENTS)) {
                                    // Successful
                                    where.display(Utils.stringReplace(Utils.stringReplace(world.msgs.msg[146],"$1",this.getName(),false),"$2",what.getName(),false));
                                    display(Utils.stringReplace(Utils.stringReplace(world.msgs.msg[122],"$1",it.getName(),false),"$2",where.getName(),false));
                                    
                                    where.sendCmd(Const.CMDE_REFRSCENE);
                                    return true;
                                } else { // Else say nothing
                                    if (it.container == null && it.world != null) {
                                        // Avoid item to be dropped in null - place into character's container
                                        // The second condition is to avoid this being done for items being self-destroyed
                                        this.container.objPlace(it,Const.DONT_CHECK_OPEN, this, Const.CHECK_EVENTS);
                                    }
                                }
                            } else { // Remove failed - cause unknown
                                world.logger.debug("Warning: removal object " + fullId + " from its container results to have failed.");
                            }
                        } else {
                            display(world.msgs.msg[121]);
                        }
                    } else {
                        display(world.msgs.msg[115] + " " + what.getName());
                    }
                } else {
                    display(world.msgs.msg[120]);
                }
            } else {
                display(world.msgs.msg[119]);
            }
        } else {
            world.logger.debug("The selected item does not exist");
            display(world.msgs.msg[118]);
        }
        return false;
    }
    protected boolean itemPick(AdvObject i1)  throws DimxException {
        // Must refer to a CHARACTER picking from a room or item
        if (i1!=null) {
            String what = i1.toString();
            if (!i1.isHidden()) {
                AdvObject cont = i1.container;
                AdvObject myCont = container;
                if (cont != this) {
                    if (i1.isAccessibleFrom(this)) {
                        // Se siamo nella stessa stanza o al massimo sono nella stanza del suo contenitore
                        if (i1.isPickable()) {
                            if (this.getFreeSpace() >= i1.volume.intVal()) {
                                AdvObject it = cont.objRemove(i1,this,cont,this,Const.CHECK_OPEN,Const.CHECK_EVENTS);
                                if (it != null) {
                                    if (objPlace(it,Const.DONT_CHECK_OPEN,this,Const.CHECK_EVENTS)) {
                                        displayh(world.msgs.msg[124] + " " + it.getName());
                                        return true;
                                    } else {
                                        world.logger.debug("Character.itemPick: Unable to pick up " + what);
                                        //could not pick up
                                        //display(world.msgs.msgxxx + " " + it.name);
                                    }
                                } else {
                                    world.logger.debug("Character.itemPick: Unable to move " + what);
                                    display(world.msgs.msg[115] + " " + i1.getName());
                                }
                            } else {
                                display(world.msgs.msg[108]);
                            }
                        } else {
                            display(world.msgs.msg[126] + " " + i1.getName());
                        }
                    } else {
                        display(world.msgs.msg[127] + " " + i1.getName() + "...");
                    }
                } else {
                    display(world.msgs.msg[128]);
                }
            } else {
                display(world.msgs.msg[129]);
            }
        } else {
            display(world.msgs.msg[118]);
        }
        return false;
    }
    protected boolean itemPut(String fullId, String inWhatId) throws DimxException  {
        // Must refer to a CHARACTER placing into an ITEM
        if (this.isNear(fullId) && this.isNear(inWhatId)) {
            AdvObject what = world.getObject(fullId);
            AdvObject where = world.getObject(inWhatId);
            if (what == where) {
                return false; // Cannot put object into itself!
            }
            if (where.isanItem() &&
            what.isanItem()) {
                if (what.isPickable()) {
                    AdvObject cont = what.container;
                    AdvObject it;
                    if (cont != this) {
                        it = cont.objRemove(what,this,cont,where,Const.CHECK_OPEN,Const.CHECK_EVENTS);
                    } else { // E' roba mia - bypassa ctrl antifurto
                        it = cont.objRemove(what,this,cont,where,Const.DONT_CHECK_OPEN,Const.CHECK_EVENTS);
                    }
                    if (it != null) {
                        if (where.objPlace(it,true,this,Const.CHECK_EVENTS)) {
                            display(world.msgs.actualize(world.msgs.msg[130],it.getName(),where.getName()) );
                            return true;
                        } else {
                            display(world.msgs.actualize(world.msgs.msg[131],it.getName(),where.getName()) );
                            cont.objPlace(it,false,null,Const.CHECK_EVENTS);
                        }
                    } else {
                        display(world.msgs.msg[132] + " " + what.getName());
                    }
                } else {
                    display(world.msgs.msg[133]);
                }
            } else {
                display(world.msgs.msg[134]);
            }
        } else {
            display(world.msgs.msg[135]);
        }
        return false;
    }
    protected boolean itemUse(String fullId) throws DimxException  {
        AdvObject o = world.getObject(fullId);
        if (o != null) {
            if (o.isAccessibleFrom(this)) {
                if (world.fireEvent("onUse",o,id,"",false)) {
                    displayh(world.msgs.msg[104]);
                } else {
                    displayh(world.msgs.msg[105]);
                }
            } else {
                display(world.msgs.msg[136]);
            }
        } else {
            world.logger.debug("itemUse: The item does not exist: " + fullId);
            display(world.msgs.msg[118]);
        }
        
        return false;
    }
    protected boolean itemUse(String fullId1, String fullId2) throws DimxException  {
        AdvObject o1 = world.getObject(fullId1);
        AdvObject o2 = world.getObject(fullId2);
        if (o1 != null && o2 != null) {
            if (o1.isAccessibleFrom(this) && o2.isAccessibleFrom(this)) {
                if (world.fireEvent("onUseWith",o1,id,fullId2,false)) {
                    displayh(world.msgs.msg[104]);
                } else {
                    displayh(world.msgs.msg[105]);
                }
            } else {
                display(world.msgs.msg[136]);
            }
        } else {
            world.logger.debug("One of the selected items (" + fullId1 +"," + fullId2 + ") does not exist");
            display(world.msgs.msg[137]);
        }
        return false;
    }
    
    public String look(AdvObject o, DictSorted input, Skin skin) throws DimxException  {
        StringBuffer out = new StringBuffer();
        if (o != null && world != null) {
            
            if (o.isAccessibleFrom(this)) {

                if (!o.isaRoom()) {
                    String d = o.getDescription(this, input);
                    out.append(d);
                }
                Messages msgs = world.msgs;
                
                boolean res = world.fireEvent("onLook",o,id,"",input,true, false);
                
                if (res) { // If look successful...

                    if (o.world != null) { // Looked character/object could have been destroyed as a result of onLook EVENT
                        // If player is not inside it, then show contents
                        String listName = msgs.msg[148];
                        if (!o.isaCharacter()) listName = msgs.msg[149];
                        if (!(o.isPeopleContainer() && this.container == o)) {
                            if (!o.isLink() && !o.isaCharacter() && world != null && skin != null) {
                                out.append(world.htmlTable(o.getContents(true),skin,listName,/* o.container.id */ null,o,facing.strVal(),false,true));
                            }
                        }

                        // Notify watched character
                        if (o.isPlayer())
                            o.display(Messages.actualize(msgs.msg[155],this.getName()));

                        // Optionally show attributes
                        if (o.isaCharacter() || o.isanItem()) {
                            
                            boolean skip = false;
                            
                            if (o.varGet("__clearinfo").boolVal()) {
                                skip = true;
                                o.varGet("__clearinfo", Const.GETREF).assign(new Token(),world); // Clear flag
                            }

                            if (!skip) {
                                StringBuffer propSb = new StringBuffer();
                                int attrcount = o.varsToHtmlTable(propSb,msgs.msg[24]);
                                if (attrcount>0) {
                                    out.append(propSb.toString());
                                }
                                if (o.isaCharacter() && world != null) {
                                    out.append(msgs.msg[163] + ": " + o.world.getDirectionStr(facing.strVal())+"\n");
                                    if (skin != null) {
                                        out.append(world.htmlTable(o.getContents(true),skin,listName,/* o.container.id */ null,o,facing.strVal(),false,true));
                                    }
                                }
                            }
                        }

                        // Optionally show extras
                        /* disab since ver 7.0.6
                        if (o.isPlayer()) {
                            out.append(((Player) o).getExtras());
                        }
                        */
                    }
                } else { // onLook returned FALSE (cancel action) - revert
                    return null; // Clears it
                }
            } else { // not accessible
                out.append(world.msgs.msg[139]);
            }
        }
        return out.toString();
    }
        
    public boolean objectClose(AdvObject o) throws DimxException  {
        StringBuffer errmsg = new StringBuffer("");
        if (o != null) {
            if (o.isAccessibleFrom(this)) {
                if (o.close(this)) {
                    if (world != null) displayh(world.msgs.actualize(world.msgs.msg[140],o.getName()));
                    return true;
                }
            } else {
                display(world.msgs.msg[141]);
            }
        } else {
            world.logger.debug("The selected item does not exist anymore.");
            display(world.msgs.msg[118]);
        }
        return false;
    }
    /** Hides the specified object.
     * @param o object to be hidden
     * @throws DimxException
     * @return true if successful
     */    
    protected boolean objectHide(AdvObject o)  throws DimxException {
        StringBuffer errmsg = new StringBuffer("");
        if (o.isanItem()) {
            if (o.container == this) { // Player owns the object - must drop it first
                this.itemDrop(o);
            }
            if (o.container == this.container || o.world == null) {
                if (o.world != null && o.hide(errmsg)) {
                    display(world.msgs.actualize(world.msgs.msg[142],o.getName()));
                    return true;
                } else {
                    String msg = world.msgs.msg[141];
                    if (!errmsg.toString().equals("")) msg = msg + " (" + errmsg + ")";
                    display(msg);
                }
            } else {
                display(world.msgs.msg[141]);
            }
        } else {
            display(world.msgs.msg[106]);
        }
        return false;
    }
    /** Hides the specified object.
     * @param fullId ID of the object to be hidden, "" means "all objects"
     * @throws DimxException
     * @return true upon success
     */    
    protected boolean objectHide(String fullId)  throws DimxException {
        boolean result = true;
        if (fullId.equals("")) {
            // Hide ALL items
            Dict x = this.getContents();
            Dict objects = (Dict) x.clone();
            for (int i=objects.size()-1; i >= 0 ; i--) {
                AdvObject o = (AdvObject) objects.elementAt(i);
                result = objectHide(o) && result; // NOT short-circuit
            }
        } else {
            AdvObject o = world.getObject(fullId);
            if (o != null) {
                result = objectHide(o);
            } else {
                world.logger.debug("The selected item does not exist: " + fullId);
                display(world.msgs.msg[118]);
                result = false;
            }
        }
        return result;
    }
    public boolean objectOpen(AdvObject o) throws DimxException  {
        StringBuffer errmsg = new StringBuffer("");
        
        if (o != null) {
            if (o.isAccessibleFrom(this)) {
                if (o.open(this)) {
                    if (world != null) displayh(world.msgs.actualize(world.msgs.msg[143],o.getName()));
                    return true;
                }
            } else {
                display(world.msgs.msg[141]);
            }
        } else {
            world.logger.debug("The selected item does not exist anymore");
            display(world.msgs.msg[118]);
        }
        return false;
    }

    public boolean objectSearch(String fullId)  throws DimxException {
        AdvObject o = world.getObject(fullId);
        int found = 0;
        if (o != null) {
            if (o.isAccessibleFrom(this)) {
                Dict cont = o.getContents();
                for (int i=0; i < cont.size(); i++) {
                    AdvObject x = (AdvObject) cont.elementAt(i);
                    if (x.isHidden()) {
                        if (x.isAccessibleFrom(this)) {
                            boolean ret = world.fireEvent("onSearch",x,id,"",true);
                            if (ret) {
                                x.varGet("hidden",Const.GETREF).assign(new Token(0.0),world);
                                display(world.msgs.actualize(world.msgs.msg[144],x.getName()));
                                found++;
                            }
                        }
                    }
                }
                if (found == 0) {
                    display(world.msgs.msg[145]);
                }
                return true;
            } else {
                display(world.msgs.msg[141]);
            }
        } else {
            world.logger.debug("The selected item does not exist: " + fullId);
            display(world.msgs.msg[118]);
        }
        return false;
    }
    
    
    /**
     * Character constructor comment.
     */
    public Character(World aWorld, String aName,
    String anId, String aDescription, String anIcon,
    int aCapacity, String attrList,
    String aDefContainer,
    String acceptList) throws DimxException {
        super(aWorld,aName,anId,aDescription, anIcon,null, aCapacity, 10, true, aDefContainer);
        accepting = new Token(acceptList);
        showmode = new Token(Const.ONSCREEN_IMAGE);
        this.varsSet(attrList); // These are the default attributes
    }
    
    protected boolean accepts(AdvObject what) {
        String acceptList = accepting.strVal();
        if (acceptList.equals(Const.ACCEPT_ALL) || acceptList.indexOf(what.id)>=0) {
            return true;
        } else {
            String type = what.varGet("type").strVal();
            String maintype = "";
            if (type.indexOf(".") > 0) { // Complex type - extract maintype
                maintype = type.substring(0, type.indexOf("."));
            }
            if ((!type.equals("") && acceptList.indexOf(type)>=0) || (!maintype.equals("") && acceptList.indexOf(maintype)>=0)) {
                return true;
            }
            return false;
        }
    }
    
    public boolean display(String msg) throws DimxException {
        return false;
    }
    
    public boolean displayh(String msg) throws DimxException {
        return false;
    }
    
  
    protected void rotate(String direction) {
        AdvObject cont = this.container;
        if (cont != null && cont.isanItem()) {
            cont = cont.container;
        }
        if (cont != null) {
            facing = new Token(Utils.rotate(cont,facing.strVal(),direction));
        }
    }
    
    public Token varGet(String varId, boolean getReference)  throws DimxException {
        Token res = null;
    
        if (varId.equalsIgnoreCase("accepts")) {
            if (getReference) return accepting;
            else return accepting.getClone();
        } else {
            res = super.varGet(varId, getReference); 
        }
        
        return res;
    }
   
    /**
     * See AdvObject.WorldChange
     * @param See AdvObject.WorldChange
     * @param See AdvObject.WorldChange
     * @throws cleoni.adv.DimxException in case of problems
     */
    public void worldChange(World toWorld, String newid, String defContainer)  throws DimxException {
        boolean removed = this.world.removePeople(this,Const.KICKOUT,Const.KEEP_ITEMS,this.world.msgs.actualize(this.world.msgs.msg[161],this.getName(), toWorld.id));
        if (newid == null) newid = toWorld.getNextPeopleId();
        super.worldChange(toWorld,newid, defContainer);
        if (!isPlayer()) {
            toWorld.addCharacter(this);
        }
   }

}