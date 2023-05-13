package cleoni.adv;

/** Represents *any* DimensioneX object:
 * WORLD, ITEM, CHARACTER, LINK, ROOM
 * excludes Dictionary objects and Images
 *
 * Base of object of DimensioneX.
 * Like AdvObject, but includes also WORLD itself.
 * @author: Cristiano Leoni
 */
public class DimxObject extends ManageableObject {
    public	String id;
    public 	World world = null;
    public 	Token name = new Token();
    public	DictSorted properties = new DictSorted(); // For the moment still using this instead or varspace for values
    // If possible use varSet varsGet to handle
    public	Dict contents = new Dict();
    
    public static String[] methods =
    {"getProperty","setProperty"};
    
    // Number of expected parameter for each instruction. -1 means "can vary"
    public static int[] methodArgs =
    {1,2};
    
    /**
     * DimxObject constructor comment.
     */
    public DimxObject() {
        super();
    }
    public void debug(String s) {}
    public boolean display(String msg) throws DimxException {
        // No display unless World, Player or Room
        return false;
    }
    
    public boolean displayRight(String msg) throws DimxException {
        // No display unless World, Player or Room
        return false;
    }
    
   /**
     * @param name
     * @return
     */    
    public Dict getObjectsByName(String name, Dict found) {
        Dict list;
        if (found != null) {
            list = found;
        } else {
            list = new DictSorted();
        }
        AdvObject o = null;
        name = name.toLowerCase();
        for (int i=0; i < contents.elementCount; i++) {
            o = (AdvObject) contents.elementAt(i);
            if (o.name.strVal().toLowerCase().equals(name))
                list.put(o.id, new Token(o));
            
            // Search and inner objects
            Dict innerlist = o.getObjectsByName(name, list);
            for (int j=0; j < innerlist.elementCount; j++) {
                list.put(innerlist.keyAt(j), innerlist.elementAt(j));
            }
        }
        return list;
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

    public boolean hasMethod(String mname) {
        return Utils.isIn(mname,getMethods());
    }
    
    /*
     * Management of heared voice
     * MUST BE OVERRIDDEN
     *
     */
    public boolean hear(DimxObject from, String msg) throws DimxException  {
        return true;
    }
    
    public boolean isanItem() {
        return false;
    }
    public boolean isaCharacter() {
        return false;
    }
    public boolean isLink() {
        return false;
    }
    public boolean isaRoom() {
        return false;
    }

    private Token metGetProperty(Dict params) throws DimxException {
        Token p0 = (Token) params.elementAt(0);
        Token x = this.varGet(p0.strVal(), false);
        return x;
    }

    private Token metSetProperty(Dict params) throws DimxException {
        Token p0 = (Token) params.elementAt(0);
        Token p1 = (Token) params.elementAt(1);
        Token x = this.varGet(p0.strVal(), true);
        x.assign(p1, this.world);
        return p1;
    }

    public boolean playBackground(String soundfile,boolean loop) throws DimxException {
        // No sound unless World, Player or Room - needs to be overridden
        return false;
    }
    public boolean playSound(String soundfile) throws DimxException {
        // No sound unless World, Player or Room - needs to be overridden
        return false;
    }
    public boolean sendCmd(String cmd) throws DimxException {
        // No commands unless  World, Player or Room - needs to be overridden
        return false;
    }
    public boolean setPanel(String panelId) throws DimxException  {
        // Tries to set the specified controls panel, returns true if successful
        // To be overridden
        
        return true;
    }
    public boolean useView(Page view) throws DimxException {
        // No view unless  World, Player or Room - needs to be overridden
        return false;
    }
    public boolean varExists(String feature) {
        Token t;
        try {
            t = varGet(feature,false);
        } catch (DimxException e) {
            Logger.echo(e.getMessage());
            t = null;
        }
        if (t == null) return false;
        
        if (t.isObject())
            return t.objVal() != null;
        else
            return !t.strVal().equals("");
    }
    /*
     */
    /** Gets a property
     * @param varId Id of the property to be found
     * @param getReference Do you need its reference?
     * @throws DimxException
     * @return always returns a valid Token
     */    
    public Token varGet(String varId, boolean getReference) throws DimxException {
        if (varId.equalsIgnoreCase("name")) {
            if (getReference) return name;
            else return name.getClone();
        } else if (varId.equalsIgnoreCase("id")) {
            if (getReference) {
                throw new DimxException("Cannot change object id of: " + id);
            } else {
                return new Token(id);
            }
        }
        // Else
        
        Token res = (Token) properties.get(varId);
        if (res != null) {
            if (res.isDimxObject()) {
                DimxObject x = res.dimxobjVal();
                if (x.world != this.world) {
                    res = null; // We do not allow references from other areas
                }
            }
        }
        
        if (res != null) {
            if (getReference) return res;
            else return res.getClone();
        }

        if (res == null) {
            //debug("WARNING: " + this.id + "." + varId + " = null");
            res = new Token(); // Initialize to NULL
            if (getReference) {
                properties.put(varId,res);
            }
        }
        
        return res;
    }
    public Dict getContents() {
        return contents;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name.strVal();
    }
    
    public Token execMethod(String mname,Dict params) throws DimxException {
        String mnamel = mname.toLowerCase();
        if (mnamel.equals("getproperty")) {
            return metGetProperty(params);
        } else if (mnamel.equals("setproperty")) {
            return metSetProperty(params);
        } 
        
        return super.execMethod(mname, params);
    }
}
