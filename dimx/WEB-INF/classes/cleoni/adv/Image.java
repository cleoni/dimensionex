package cleoni.adv;

/** Represents an image to be used in the game. */
public class Image extends ManageableObject implements Cloneable {
    protected   Token src = null;
    protected   Token width = null;
    protected   Token height = null;
    protected	int	baseline = -1;
    protected 	int	showareax1 = 0;
    protected 	int	showareax2 = -1;
    public static String[] methods =
    {"html"};
     
     // Number of expected parameter for each instruction. -1 means "can vary"
    public static int[] methodArgs =
    {-1};
    
    /**
     * Image constructor comment.
     */
    public Image(String aSrc, int aWidth,int aHeight) {
        super();
        src=new Token(aSrc);
        width=new Token(aWidth);
        height=new Token(aHeight);
    }
    
    public Object clone() {
        try {
	    Image i = (Image)super.clone();
	    i.src = new Token(src.strVal());
	    i.width = new Token(width.numVal());
	    i.height = new Token(height.numVal());
            i.baseline = baseline;
            i.showareax1 = showareax1;
            i.showareax2 = showareax2;
            return i;
	} catch (CloneNotSupportedException e) { 
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}
    }

    public Token varGet(String varId,boolean getReference) throws DimxException {
        if (varId.equalsIgnoreCase("url")) {
            if (getReference) return src;
            else return src.getClone();
        } else if (varId.equalsIgnoreCase("width")) {
            if (getReference) return width;
            else return width.getClone();
        } else if (varId.equalsIgnoreCase("height")) {
            if (getReference) return height;
            else return height.getClone();
        }
        // Else
        
        return super.varGet(varId,getReference);
    }
    
    public String toHTML(String title, double factorx) {
        return "<IMG BORDER=0 SRC=\""+ getSrc() + "\" WIDTH=\"" + Utils.cInt(factorx*getWidth()) + "\" HEIGHT=\""+ Utils.cInt(factorx*getHeight()) + "\" ALT=\"" + title + "\" TITLE=\"" + title + "\">";
    }
    
    public String toDXW() {
        return "IMAGE " + getWidth() + "x" + getHeight() + " SHOWAREA " + showareax1 + "," + showareax2 + "," + baseline + " " + getSrc();
    }
    public String toString() {
        return "NewImage(\"" + getSrc() + "\"," + getWidth() + "," + getHeight() + ")";
    }
    
    public int getScreenX(int cartX) {
        // Transforms cartesian coordinates into image's relative coordinates
        return 1 + cartX;
    }
    
    
    public int getScreenY(int cartY, int objectsHeight) {
        // Transforms cartesian coordinates into image's relative coordinates
        return getHeight() - cartY - objectsHeight;
    }

    public String getSrc() {
        return src.strVal();
    }

    public int getWidth() {
        return width.intVal();
    }

    public int getHeight() {
        return height.intVal();
    }
    
    public String[] getMethods() {
        return this.methods;
    }
    
    public int getMethodArgs(String mname) {
        return this.methodArgs[Utils.indexOf(mname, this.methods)];
    }

    public Token execMethod(String mname,Dict params) throws DimxException {
        String mnamel = mname.toLowerCase();
        if (mnamel.equals("html")) {
            return metHtml(params);
        } 
        return super.execMethod(mname, params);
    }
        
    private Token metHtml(Dict params) {
        Token p0 = (Token) params.get("par0");
        Token p1 = (Token) params.get("par1");
        Token tworld = (Token) params.get("$WORLD");
        double factorx = 1.0;
        if (p1 != null && tworld != null) {
            World world = (World) tworld.objVal();
            if (world != null) {
                DimxObject o = world.getObjectExt(p1);
                if (o != null && o.isaCharacter()) {
                    Character c = (Character) o;
                    if (c.isPlayer()) {
                        Player p = (Player) c;
                        factorx = p.getClient().factorx; // Yee!
                    }
                }
            }
        }
        String title = "";
        if (p0 != null) title = p0.strVal();
        return new Token(this.toHTML(title,factorx));
    }
}