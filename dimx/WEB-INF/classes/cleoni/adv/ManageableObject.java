/*
 * ManageableObject.java
 *
 * Created on 27 gennaio 2004, 13.15
 */

package cleoni.adv;

/** Base class for all things. This is for code optimisation. See class hierarchy of the JavaDoc.
 * @author CrLeoni
 */
public class ManageableObject {
    
    public static String[] methods =
    {};
     
     // Number of expected parameter for each instruction. -1 means "can vary"
    public static int[] methodArgs =
    {};
     
    /** Creates a new instance of ManageableObject */
    public ManageableObject() {
    }
    
    public String[] getMethods() {
        return this.methods;
    }
    
    public int getMethodArgs(String mname) {
        int i = Utils.indexOf(mname, this.methods);
        return this.methodArgs[i];
    }


    public boolean hasMethod(String mname) {
        return Utils.isIn(mname,getMethods());
    }

    public Token execMethod(String mname,Dict params) throws DimxException {
        throw new DimxException("The game engine cannot handle this method: " + mname);
    }
    public boolean varExists(String feature) {
        return false;
    }
    public Token varGet(String varId) {
        Token t;
        try {
            return varGet(varId, false);
        } catch (DimxException e) {
            Logger.echo(e.getMessage());
            return null;
        }
    }
    public Token varGet(String varId, boolean getReference)  throws DimxException {
        // Create a nullstring token
        if (getReference) {
            throw new DimxException("Cannot get reference for: " + varId);
        }
	Token x = new Token("");
	x.setNumber();
	return x;
    }

    public String getName() {
    return "(no name)";
    }
    public String getId() {
    return "(no id)";
    }
    public String toString() {
        return "(object.toString)" + this.getClass().getName();
    }
}
