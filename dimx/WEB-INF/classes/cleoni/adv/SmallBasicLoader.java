package cleoni.adv;

/**
 * World Loader and parser (DXW format) for the DimensioneX engine
 * Creation date: (03/01/2003)
 * @author: Cristiano Leoni
 */
import java.util.Vector;
import java.io.*;
import java.net.*;
/** This parser is capable of reading and parsing SmallBasic code, adding it to the
 * current world.
 */
public class SmallBasicLoader extends DimxParser {
	private Messages	msgs;
        private String encoding = "UTF-8";
        private String systemDir = null;
        private String fileName = null;
        private int startLine = 0;
	private static final String[] setEventsTags = {"EVENT","SUB","FUNCTION","Include"};
	private static final String[] setEventClosers = {"EVENT","SUB","FUNCTION","END_EVENT","END_SUB","END_FUNCTION","END_EVENTS",EOF};


        public SmallBasicLoader(World aWorld, String aBuf, String aFileName, int startingLine, String anEncoding) {
	super(null,null,0,0);
        world = aWorld;
        buf = aBuf;
        startLine = startingLine;
        currLine = startingLine;
	logger = world.logger;
	msgs = world.msgs;
        fileName = aFileName;
        encoding = anEncoding;
}
        
public World load (String aSystemDir,boolean tolerateDups) throws DimxException, Exception { 
// Parses SmallBasic code (EVENTS section)
        systemDir = aSystemDir;
	parseEvents(tolerateDups);
        return world;
}

private void parseActions(Event newEvent) throws DimxException {
	getLine(); // Eats initial CR
        int rememberLine = this.currLine;
	StringBuffer act = new StringBuffer();
        String s = lookupToken().strVal();
	while (!Utils.isIn(s,setEventClosers)) {
		act.append(getLine());
                s = lookupToken().strVal();
	}
	newEvent.setActions(act.toString(),rememberLine,fileName);
}

private Vector parseEventQual(boolean tolerateDups) throws DimxException {
	DimxObject ownerObj = world; // WORLD is the default owner object
	String eventId = null;
        
	String qual = nextToken().strVal();

	//logger.debug("Event qualifier encountered: " + qual);
        if (lookupToken().strVal().equals(".")) {
            eat();
            ownerObj = (AdvObject) world.getObject(qual);
            eventId = nextToken().strVal();
            qual = qual + "." + eventId;

            if (ownerObj == null) throw new DimxException("Undefined owner object: " + qual);

	} else {
            eventId = qual;
        }
        
        // System functions check
        if (Utils.isIn(qual,setFunctions)) {
            throw new DimxException("Function/Event/Sub named " + qual + ": duplicates system function");
        }

        // System instructions check
        if (Utils.isIn(qual,ActionsRunner.actKeywords)) {
            throw new DimxException("Function/Event/Sub named " + qual + ": duplicates system instruction");
        }
        
        if (!tolerateDups) {
            // Custom functions check
            Vector res = identifyFunProc(qual,setFunctions,setFunctArgs);
            if (((Integer) res.elementAt(0)).intValue() != Const.NOTFOUND) {
                throw new DimxException("Function/Event/Sub named " + qual + ": name already taken by custom script");
            }
        }

        Vector v = new Vector();
        
        v.add(ownerObj);
        v.add(eventId);
        v.add(qual);

        return v;
}
private void parseEvents(boolean tolerateDups) throws Exception {
	try {
                String s = lookupToken().strVal();
                while (Utils.isIn(s,setEventsTags)) {
                    if (s.equalsIgnoreCase("SUB") || s.equalsIgnoreCase("FUNCTION") || s.equalsIgnoreCase("EVENT")) {
                            parseSubFunctionEvent(s,tolerateDups);
                    } else if (s.equalsIgnoreCase("Include")) {
                            parseInclude();
                    }
                    s = lookupToken().strVal();
		} 
                if (!s.equals(EOF)) {
                    nextToken();
                    throw new DimxException("Unknown scripting object: " + s);
                }
            } catch (Exception e) {
		String errmsg = "ERROR loading EVENTS block\n** " + e.getMessage() + "\nnear line: " + currLine + "\n-----------------\n" + identifyLine();
                throw new NestedException(errmsg);
            }
}
private void parseInclude() throws Exception {
	parseToken("Include");

	String fileName = this.nextToken().strVal();

        if (!(fileName.startsWith("https:") || fileName.startsWith("http:"))) fileName = Utils.getParentFolder(this.fileName) + fileName;
        String buf = Utils.fetch(fileName,encoding);        
        
        SmallBasicLoader myLoader = new SmallBasicLoader(world,buf,fileName,1, encoding);

        myLoader.load(systemDir,false);
}
private Vector parseParamList() throws DimxException {
    Vector ret = new Vector();
    
    parseToken("(");
    
    String s = lookupToken().strVal();
    while(!s.equals(")") || s.equals(EOF)) {
        s = nextToken().strVal();
        ret.add(s);
        s = lookupToken().strVal();
        if (s.equals(",")) {
            parseToken(",");
            s = lookupToken().strVal();
        }
    }
    parseToken(")");
    if (ret.size() == 0) ret = null; // avoid returning empty vector
    return ret;
}
private Event parseSubFunctionEvent(String aType, boolean tolerateDups) throws DimxException {
	parseToken(aType);
        Vector v = parseEventQual(tolerateDups);
	DimxObject ownerObj = (DimxObject) v.elementAt(0);
        String eventId = (String) v.elementAt(1);
        String qual = (String) v.elementAt(2);

        Vector params = null;

        if (lookupToken().strVal().equals("(")) {
            params = parseParamList();
        }
        Event newEvent = new Event(ownerObj,qual,ownerObj,aType.toUpperCase(),params);
        ownerObj.varGet(eventId,Const.GETREF).assign(new Token(newEvent),world);
        
        // Actions	
        if (lookupToken().strVal().equalsIgnoreCase("ACTIONS") ) {
                readToCR();
                parseToken("ACTIONS");
        }

        parseActions(newEvent);

        String s = lookupToken().strVal();
        if (s.substring(0,3).equalsIgnoreCase("END") && !s.equalsIgnoreCase("END_EVENTS")) {
            parseToken("End_" + aType);
        }

        return newEvent;
}


}
