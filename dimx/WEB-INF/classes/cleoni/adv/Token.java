package cleoni.adv;

/** Another wrapper class for all values.
 * This is necessary for all values to be managed via scripting.
 * @author: Cristiano Leoni
 */
public class Token extends Object implements Cloneable {
    public char type = 'N'; // S = String/Identifier P=Punctuation L=Literal string N=number D=Dict/Set O=DimxObject V=Page(was:View)
    private Object sVal = "";
    private double nVal = 0.0;
    /** Creates a null token */
    public Token() {
        super();
    }
    /**
     * Token constructor comment.
     */
    public Token(char aType, double anVal,Object asVal) {
// Useful for getClone()
        super();
        nVal = anVal;
        sVal = asVal;
        type = aType;
    }
    
    /**
     * Token constructor comment.
     */
    public Token(double initVal) {
        super();
        setVal(initVal);
    }
    
    /**
     * Token constructor comment.
     */
    public Token(String initVal) {
        super();
        if (initVal == null) setVal("");
        else setVal(initVal);
        type = 'S';
    }
    
    /**
     * Token constructor comment.
     */
    public Token(String initVal,boolean guessType) {
        super();
        if (initVal == null) setVal("");
        else setVal(initVal);
        if (guessType) {
            type = getLikelyType();
        } else {
            type = 'S';
        }
    }
    
    /**
     * Token constructor comment.
     */
    public Token(boolean initVal) {
        super();
        setVal(initVal);
        type = 'N';
    }
    
    public Token(DimxObject initObj) {
        super();
        if (initObj != null) {
            sVal = initObj;
            //nVal = 0.0; // already in Super
            type = 'O';
        } // Else it is a regular NULL
    }
    
    public Token(Event initObj) {
        super();
        sVal = initObj;
        //nVal = 0.0; // already in Super
        type = 'E';
    }
    
    public Token(Image initObj) {
        super();
        sVal = initObj;
        nVal = 0.0;
        type = 'I';
    }
    
    public Token(Page initObj) {
        super();
        sVal = initObj;
        nVal = 0.0;
        type = 'V';
    }
    
    public Token(Dict dictObj) {
        super();
        sVal = dictObj;
        nVal = 0.0;
        type = 'D';
    }
    
    
/*
 *
 * assigns res to the token
 *
 * World is current world (for image resolution)
 */
    public void assign(Token res, World world) throws DimxException {
        if (this.isDict()) {
            if (!res.isDict()) {
                // First we check if it could be an image array
                Dict d = this.dictVal();
                if (d.size() == 1 && ((Token)d.elementAt(0)).isImage()) { // OK assign it to the first value
                    if (res.isImage()) {
                        d.setElementAt(res,0);
                    } else {
                        // Old code
                        ((Token)d.elementAt(0)).imageVal().src = new Token(Utils.absolutizeUrl(res.strVal(),world.imagesFolder));
                        // New code
                        //Image imold = ((Token)d.elementAt(0)).imageVal();
                        //Image imnew = new Image(Utils.absolutizeUrl(res.strVal(),world.imagesFolder), imold.getWidth(), imold.getHeight());
                        //d.setElementAt(new Token(imnew),0);
                    }
                    return;
                } else {
                    throw new DimxException("It is not correct to assign a scalar to a vectorial variable. Please specify an index.");
                }
            }
            this.setVal(res.dictVal());
        } else if (res.isNumber()) {
            this.setVal(res.strVal(),res.numVal());
        } else if (this.isImage()) {
            if (!res.isImage()) { // Assume its an url
                // Old code
                this.imageVal().src = new Token(Utils.absolutizeUrl(res.strVal(),world.imagesFolder));
                // New code
                //Image imold = this.imageVal();
                //Image imnew = new Image(Utils.absolutizeUrl(res.strVal(),world.imagesFolder), imold.getWidth(), imold.getHeight());
                //this.setVal(imnew);
                return;
            }
            this.setVal(res.imageVal());
        } else if (res.isDimxObject()) {
            this.setVal(res.dimxobjVal());
        } else if (res.isImage()) {
            this.setVal(res.imageVal());
        } else if (res.isDict()) {
            this.setVal(res.dictVal());
        } else if (res.isEvent()) {
            this.setVal(res.eventVal());
        } else if (res.isPage()) {
            this.setVal(res.pageVal());
        } else {
            this.setVal(res.strVal());
        }
        this.type = res.type;
    }
    
    public boolean boolVal() {
        return nVal != 0.0;
    }
    public Object clone() {
        // Creates an exact copy
        Object o = sVal;
        try {
            if (this.isImage()) {
                o = imageVal().clone();
//            } else if (this.isIdentifier() || this.isLiteral()) {
//                o = sVal.toString();
            }
        } catch (Exception e) {
        }
        Token t = new Token(type,nVal,o);
        return t;
    }
    public Token getClone() {
        // Creates an exact copy
        Token t = new Token(type,nVal,sVal);
        return t;
    }
    public char getLikelyType() {
// Decides if the value is most likely a number or a string
        //String s = new PrintfFormat(java.util.Locale.ENGLISH,"%1.1g").sprintf(nVal);
        String s = compactNval(nVal);
        if (s.equals(sVal)) return 'N';
        else return 'S';
    }
    
    public boolean isIdentifier() {
        return type=='S';
    }
    public boolean isLiteral() {
        return type=='L';
    }
    public boolean isNumber() {
        return type=='N';
    }
    public boolean isPunctuation() {
        return type=='P';
    }
    
    public boolean isImage() {
        return type=='I';
    }
    
    public boolean isDict() {
        return type=='D';
    }
 
    public boolean isDictSorted() {
        if (isDict()) {
            try {
                DictSorted d = (DictSorted) sVal;
                return true;
            } catch (ClassCastException e) {
                return false;
            }
        }
        return false;
    }    
    
    public boolean isObject() {
        return type=='O' || type=='D' || type=='I' || type =='P' || type == 'E';
    }
    
    public boolean isDimxObject() {
        return type=='O';
    }
    
    public boolean isEvent() {
        return (type == 'E');
    }
    
    public boolean isPage() {
        return (type == 'V');
    }
    
    public boolean isNull() {
        return ((type == 'S' || type == 'N') && sVal.equals(""));
    }
    
    public double numVal() {
        return nVal;
    }
    public int intVal() {
        return new Double(nVal).intValue(); // ... and using (int) nVal ?
    }
    public void setIdentifier() {
        type = 'S';
    }
    public void setLiteral() {
        type = 'L';
    }
    public void setNumber() {
        type = 'N';
    }
    public void setPunctuation(char c) {
        type = 'P';
        setVal("" + c);
    }
    public void setPunctuation(String c) {
        type = 'P';
        setVal(c);
    }
    
/*
 * compcts the specified double value
 * by removing useless decimals
 */
    private String compactNval(double v) {
        //sVal = new PrintfFormat(java.util.Locale.ENGLISH,"%1.1g").sprintf(v);
        int intval = Utils.cInt(v);
        if (v == (double) intval)
            return Utils.cStr(intval);
        else
            return Utils.cStr(v);
    }
    public void setVal(double v) {
        sVal = compactNval(v);
        nVal = v;
        type = 'N';
    }
    public void setVal(int v) {
        sVal = Utils.cStr(v);
        nVal = Utils.cDbl(v);
        type = 'N';
    }
    public void setVal(String v) {
        sVal = v;
        nVal = Utils.cDbl(v);
    }
    public void setVal(ManageableObject v) {
        sVal = v;
        nVal = 0.0;
    }
    public void setVal(boolean initVal) {
        double iVal = 0.0;
        if (initVal) iVal = 1.0;
        setVal(iVal);
    }
    public void setVal(String s, double n) {
        sVal = s;
        nVal = n;
    }
    public String strVal() {
        if (type == 'S')
            return (String) sVal;
        else
            return sVal.toString();
    }
    
    public DimxObject dimxobjVal() throws ClassCastException {
        try {
            return (DimxObject) sVal;
        } catch (ClassCastException e) { // Say nothing
            return null;
        }
    }
    
    public ManageableObject objVal() throws ClassCastException {
        return (ManageableObject) sVal;
    }
    public Event eventVal() throws ClassCastException {
        try {
            return (Event) sVal;
        } catch (ClassCastException e) { // Say nothing
            return null;
        }
    }
    
    public Page pageVal() throws ClassCastException {
        try {
            return (Page) sVal;
        } catch (ClassCastException e) {
            // Say nothing
            return null;
        }
    }
    
    public Dict dictVal() {
        try {
            return (Dict) sVal;
        } catch (ClassCastException e) {
            // Empty Set
            return new Dict();
        }
    }
    
    public Image imageValSafe() {
        try {
            return (Image) sVal;
        } catch (ClassCastException e) {
            // return
            return new Image(e.getMessage(),100,100);
        }
    }
    public Image imageVal() {
        try {
            return (Image) sVal;
        } catch (ClassCastException e) {
            // return
            return null;
        }
    }
    public DictSorted dictsortedVal() {
        try {
            return (DictSorted) sVal;
        } catch (ClassCastException e) {
            // Say nothing
            return null;
        }
    }
    public Page valPage() throws DimxException {
        try {
            return (Page) sVal;
        } catch (ClassCastException e) {
            throw new DimxException("Type mismatch: expected a PAGE object");
        }
    }
    public String toString() {
        if (type == 'D')
            return Utils.cStr(sVal);
        else
            return "(" + type + "|" + sVal + "|" + nVal + ")";
    }
}
