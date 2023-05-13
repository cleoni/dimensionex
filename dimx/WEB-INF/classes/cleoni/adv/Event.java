package cleoni.adv;

import java.util.*;

/** Represents an EVENT, a FUNCTION or a SUB. Contains the associated script code. */
public class Event extends ManageableObject {
	public String id;
	private DimxObject parent;
        public String type;
        private Varspace varspace = null;
        private Vector formalparams = null;
        public String attachedEventsId = null; //For attached events, source Event's ID
	private String actions = "";
	public int actionsStartLine = 0;
        public String fileName = null;
        public String fileShort = null;
	public DimxObject owner = null;

/**
 * Event constructor comment.
 */
public Event(DimxObject aParent, String anId, DimxObject anOwner, String aType, Vector vParams) throws DimxException {
	super();
        if (aType.equals("EVENT") || aType.equals("FUNCTION") || aType.equals("SUB"))
            type = aType;
        else
            throw new DimxException("Unknown event type: " + aType);
        
	id = anId;
	parent = aParent;
        formalparams = vParams;
        varspace = new Varspace();
}

/**
 * Fires an event - first searching for object-specific, the searching for generic.
 * This version returns a Token result
 * NOTE - This function is almost equal to fire() - please keep them aligned or merge them
 * @param owner 
 * @param agent 
 * @param target 
 * @param params Must be a DictSorted of values to be passed to the event as parameters. Could contain also the "input" parameter which in turn must be a DictSorted.
 * @param defaultResult 
 * @throws cleoni.adv.DimxException 
 * @return Token result
 */
public Token fire_t(DimxObject owner, String agent,String target, DictSorted params, Token defaultResult) throws DimxException {
        World world = getWorld();
        if (world == null) {
            throw new DimxException("Found NULL world for Event: " + this.id);
        }
        world.logger.debug(type + " " + id);
        for (int i=0; i < params.elementCount; i++) {
            varspace.varSet(params.keyAt(i),(Token) params.elementAt(i));
        }
        Token result = executeActions(owner,agent,target,defaultResult);
	return result;
}
/*
 *
 * NOTE - This function is almost equal to fire_t() which is a modified copy of this one 
 * - please keep them aligned or merge them
 */
public boolean fire(DimxObject owner, String agent,String target, DictSorted input, boolean defaultResult) throws DimxException {
        World world = getWorld();
        if (world == null) {
            throw new DimxException("Found NULL world for Event: " + this.id);
        }
        world.logger.debug(type + " " + id);
        varspace.varSet("input", new Token(input));
	boolean result =executeActions(owner,agent,target,new Token(defaultResult)).boolVal();
	return result;
}




public String toString() {
	String ret = type + " " + id ;
        if (formalparams != null) {
            ret = ret + "(";
            for (int i=0; i<formalparams.size(); i++) {
                if (i>0) ret = ret + ",";
                ret = ret + (String) formalparams.elementAt(i);
            }
            ret = ret + ")";
        }
        ret = ret + " - " + fileShort + "(" + actionsStartLine + ")";
	return ret; // + "ACTIONS\n" + actions;
}


public Token executeActions(DimxObject owner, String agent,String target,Token defaultResult) throws DimxException {
        World w = getWorld();
	w.logger.debug("EVENT " + id + " executing");

	String ownerId = null;
	if (owner != null) ownerId = owner.id;
	
	ActionsRunner runner = new ActionsRunner(getWorld(),varspace,actions,actionsStartLine, fileName, fileShort, ownerId, (AdvObject) w.getObjectExt(agent), target, 0);
        
        Calendar now = Calendar.getInstance();

        Token result = runner.run();

        if (w != null) { // Because object might have been destroyed by event
            Calendar then = Calendar.getInstance();
            String myid = fileShort+":"+actionsStartLine;
            w.optiCount(myid);
            w.optiConsumed(myid,then.getTimeInMillis() - now.getTimeInMillis());
        }
	return result;
}

public String getActions()
{	return actions;
}

public String getId() {
    return id;
}

public int getNoFormalParams() {
    if (formalparams == null) return 0;
    else return formalparams.size();
}
public Vector getFormalParams() {
    return formalparams;
}
public void setActions(String actionsBlock, int startLine, String aFileName) {
	actions = actionsBlock;
	actionsStartLine = startLine;
        fileName = aFileName;
        fileShort = Utils.getRidOfParentFolder(aFileName);
}

private World getWorld() {
    try {
        World w = (World) parent;
        return w;
    } catch (ClassCastException e) {
        return ((AdvObject) parent).world;
    }
}
}