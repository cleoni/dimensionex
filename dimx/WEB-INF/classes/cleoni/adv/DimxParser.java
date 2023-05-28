package cleoni.adv;

/**
 * Parser for the DimensioneX engine
 * Creation date: (04/08/2002 16.24.17)
 * @author: Cristiano Leoni
 */
import java.util.*;
/** Text parser. This is capable of evaluating expressions and functions. Does not deal with SmallBasic script. Main method is "evalExpression" */
public class DimxParser {
    public boolean verbose = false;
    private static java.util.Random rndGen = new java.util.Random();
    
    public World world = null;
    public Varspace varspace = null; // If null the use world's
    protected Logger logger = null;
    public String buf = "";
    public int startToken = 0;
    public int startLine = 0;
    public int currLine = 0;
    protected int stackLevel = 0; //Initially 0, incremented for each nested call
    
    //For vars
    public String owner;
    public AdvObject agent;
    public String target;
    
    // Util Constants
    public static final String EOF = "(EOF)";
    public static final String CR = "\n";
    protected static final boolean CONSUME = false;
    protected static final boolean PRESERVE = true;
    
    // Util Vectors
    protected static final String[] setCr = {"\n","\r"};
    protected static final String[] setTabSpace = {"\t"," "};
    public static final String[] setFunctClosers = {")"};
    public static final String[] setCrTabSpace = {"\n","\t"," ","\r"};
    public static final String[] setQuotes =		{"\""};
    
    // Attention - the last string MUST be a dot
    protected static final String[] setOperators = 		{"+","-","*","/","=","<",">","<=",">=","<>","OR","AND","^","&","MOD","."};
    protected static final String[] setPunctuation = 	{"+","-","*","/","=","<",">","(",")",",","\n",";","^","&","."};
    protected static final String[] tokenTerminators = 	{" ","\t","\n","'","+","-","*","/","=","<",">","(",")",",",";","^","\r","&","."};
    
    // Reckognized SmallBasic functions
    protected static final String[] setFunctions = 		{"NOT","IsPlayer","IsCharacter","Rnd",
    "Int","RndInt","RndSet","Abs","getRoomsFrom","getPlayersIn","Round","Left","Right","Len","Mid","InStrCount",
    "NewCharacter","getItemsIn","NewItem","NewImage","getCharactersIn",
    "Split","SetLen","IsRoom","SetContainsKey","Exists",
    "getTime","getSetting","SetKeys","ExistScript","SetIndexOf",
    "NewSet","SetKey","Replace","Chr","LCase",
    "getRooms","InStr","getObjectsType","IsItem","Sqr",
    "MainType","PanelHtml","UCase","gameInfo","getObject",
    "NewArray","NewLink","getPlayerProperties","HttpFetch","Copy",
    "getObjectsSubtype","getPlayer","getLinksFrom","Log","Asc",
    "Urlencode"};
    
    // Expected parameters for each function
    // Function code needs to be hooked into evalFunction() method.
    protected static final int[] setFunctArgs =             {1,1,1,1,
    1,1,1,1,1,1,2,2,2,1,3,2,
    5,1,5,3,1,
    2,1,1,2,1,
    1,-1,1,1,2,
    -1,2,3,1,1,
    1,-1,2,1,1,
    1,1,1,1,1,
    -1,8,1,1,1,
    2,1,1,1,1,
    1};
    
    public DimxParser(World aWorld, Varspace aVarspace, int aStackLevel, int aStartLine) {
        super();
        
        stackLevel = aStackLevel;
        world = aWorld;
        varspace = aVarspace;
        startLine = aStartLine;
        if (world != null) {
            logger = world.logger;
        }
    }
    public DimxParser(World aWorld, Varspace aVarspace, int aStackLevel, int aStartLine, String anOwner, AdvObject anAgent, String aTarget) {
        this(aWorld, aVarspace, aStackLevel, aStartLine);
        
        owner = anOwner;
        agent = anAgent;
        target = aTarget;
    }
    public DimxParser(World aWorld, Varspace aVarspace, int aStackLevel, String anOwner) {
        this(aWorld, aVarspace, aStackLevel, 0);
        
        owner = anOwner;
    }
    public void eat() throws DimxException {
        // Consume (eats) the next element skipping any Tab/Space
        if (verbose) logger.debug("eat!");
        nextElement(setTabSpace);
        
    }
    public void eat_extended() throws DimxException {
        // Consume (eats) the next element skipping any Tab/Space/Cr
        if (verbose) logger.debug("eat!");
        nextElement(setCrTabSpace);
        
    }
    
    public Token evalExpression(Token t, int prevPriority) throws DimxException {
        return evalExpression(t,prevPriority,false, "\n"); // Gets value
    }
    
    public Token evalExpression(Token t, int prevPriority, boolean getReference, String stopper) throws DimxException {
        // Parses an expression
        // t is a looked-up token (not yet eaten) of the expression
        Token result = null;
        
        if (verbose) logger.debug("evalExpression - Expression lookup: -->" + t );
        if (t.isIdentifier()) { // An identifier IS an expression
            String s = t.strVal();
            nextToken();
            result = resolve(s,getReference);
            if (verbose) logger.debug("resolved: " + result);
            
            if (result == null) { // Still unresolved??
                if (Utils.isIn(s,setOperators)) { // operator?
                    throw new DimxException("Binary operator erroneously used as unary: " + s);
                } else { // system variable?
                    throw new DimxException("Unresolved operator: " + s);
                    /*
                    Vector res = identifyFunProc(s,Const.emptySet,setFunctArgs);
                    int param_no = ((Integer) res.elementAt(0)).intValue();
                    Event e = (Event) res.elementAt(1);
                    if (e != null) {
                        logger.debug("Warning: Please use brackets () when referencing: " + e.toString());
                        //throw new DimxException("Please use brackets () when referencing: " + e.toString());
                        result = evalFunction(s,param_no,e);
                    }
                    */
                }
            }
            
            if ((result != null && result.isEvent()) || lookupToken().strVal().equals("(")) {
                // It is either a function or a SET/ARRAY!
                Vector res = identifyFunProc(s,setFunctions,setFunctArgs);
                int param_no = ((Integer) res.elementAt(0)).intValue();
                Event e = (Event) res.elementAt(1);
                if (param_no != Const.NOTFOUND) { // Function
                    result = evalFunction(s,param_no,e);
                } else { // array/set?
                    if (result.isDict()) {
                        // OK - set or array
                        parseToken("(");
                        Token expr = evalExpression(lookupToken(),0);
                        parseToken(")");
                        if (expr.getLikelyType() == 'N') {
                            Dict d = result.dictVal();
                            int n = Utils.cInt(expr.numVal());
                            if (n == (d.size()+1) && getReference) { // OK to extend
                                d.put(Utils.leadingZeroes(n,Const.ARRAYMAX),new Token());
                            }
                            if (n > 0 && n <= d.size()) {
                                if (getReference) {
                                    result = ((Token) d.elementAt(n-1));
                                } else {
                                    result = ((Token) d.elementAt(n-1)).getClone();
                                }
                            } else {
                                throw new DimxException("Index out of bounds: " + n);
                            }
                        } else { // search via key
                            DictSorted d = result.dictsortedVal();
                            Token rt;
                            if (d == null) { // unsorted SET - get anyway
                                rt = (Token) result.dictVal().get(expr.strVal());
                            } else { // sorted SET - get
                                rt = (Token) d.get(expr.strVal());
                            }
                            if (rt != null) { // Element exists
                                if (getReference) {
                                    result = rt;
                                } else {
                                    result = rt.getClone();
                                }
                            } else { // element does not exist
                                result = new Token(); // create null
                                if (getReference) { // if for write, insert it
                                    d.put(expr.strVal(),result);
                                }
                            }
                        }
                    } else {
                        throw new DimxException("Undefined Sub, Function or Event: " + s);
                    }
                    // end case array/set
                }
                // end case function /array
            }
        } else if (t.isLiteral()) {
            // a literal string IS an expression
            eat();
            result = new Token(t.strVal());
            result.setLiteral();
        } else if (t.isPunctuation()) {
            String pnt = t.strVal();
            if (pnt.equals(stopper)) {
            } else if (t.strVal().equals(",")) {
                // a null expression IS an expression
                // Keep null
            } else if (t.strVal().equals("(")) {
                // an expression between brackets also IS an expression
                eat();
                result = evalExpression(lookupElement(setTabSpace), 0);
                parseToken(")");
            } else if (t.strVal().equals("-")) {
                // an expression with the minus sign also IS an expression
                eat();
                result = evalExpression(lookupElement(setTabSpace), 2);
                result.setVal(-result.numVal());
            } else {
                throw new DimxException("Unexpected operand: " + t.strVal());
            }
        } else if (t.isNumber()) {
            eat();
            result = new Token(t.numVal());
        } else {
            throw new DimxException("Cannot understand expression: " + t.strVal());
        }
        
        if (verbose) logger.debug("evalExpression: Result before operator is: " + result);
        
        // Lookup for operator and operate numerically
        Token op = lookupElement(setTabSpace);
        String ops = op.strVal();
        
        while (!ops.equals(stopper) && Utils.isIn(ops,setOperators) ) {
            // Operator found!
            // current priority is: prevPriority
            if (verbose) {
                logger.debug("evalExpression - Operator found: " + ops + " current priority is " + prevPriority);
                logger.debug("evalExpression - Left operand is: " + result);
            }
            
            if (ops.equals(".") && prevPriority < 5) {
                eat();
                Token b = nextToken();
                ManageableObject parent = mygetObject(result);
                if (parent != null) {
                    result = parent.varGet(b.strVal(),getReference);
                    if (result.isNull() && lookupTokenCRsens().strVal().equals("(")) {
                        if ( parent.hasMethod(b.strVal()) ) {
                            int no_params = parent.getMethodArgs(b.strVal());
                            Dict mparams = new Dict();
                            if (no_params > 0 || no_params == Const.VARIABLE) { // One or more params expected
                                parseToken("(");
                                mparams = parseList(no_params,setFunctClosers);
                                parseToken(")");
                            } else { // No params expected
                                if (lookupToken().strVal().equals("(")) { // Brackets optional
                                    parseToken("(");
                                    parseToken(")");
                                }
                            }
                            mparams.put("$WORLD",new Token(world));
                            result = parent.execMethod(b.strVal(), mparams);
                        } else {
                            throw new DimxException("Object " + parent + " does not have this method: " + b.strVal() + "(...)");
                        }
                    } 
                } else {
                    if (getReference) {
                        throw new DimxException("Cannot reference property \"" + b.strVal() + "\": expression before dot: \"" + result + "\" evaluates to NULL");
                    } else {
                        logger.debug("WARNING: Cannot reference property \"" + b.strVal() + "\": object is NULL");
                        result = new Token(); 
                    }
                }
            } else if (ops.equals("+") && prevPriority < 3) {
                eat();
                Token b = evalExpression(lookupElement(setTabSpace),3);
                if (result.isNumber()) { // Number sum
                    result.setVal(result.numVal() + b.numVal());
                } else { // String concat
                    result.setVal(result.strVal() + b.strVal());
                }
                prevPriority--;
            } else if (ops.equals("-")  && prevPriority < 3) {
                eat();
                Token b = evalExpression(lookupElement(setTabSpace),3);
                if (result == null) {
                    int i=0;
                }
                result.setVal(result.numVal() - b.numVal());
                prevPriority--;
            } else if (ops.equals("&") && prevPriority < 3) {
                eat();
                Token b = evalExpression(lookupElement(setTabSpace),3);
                result.setVal(result.strVal() + b.strVal());
                result.setLiteral();
                prevPriority--;
            } else if (ops.equals("*")  && prevPriority < 4) {
                eat();
                Token b = evalExpression(lookupElement(setTabSpace),4);
                result.setVal(result.numVal() * b.numVal());
                prevPriority--;
            } else if (ops.equals("/")  && prevPriority < 4) {
                eat();
                Token b = evalExpression(lookupElement(setTabSpace),4);
                result.setVal(result.numVal() / b.numVal());
                prevPriority--;
            } else if (ops.equals("Mod")  && prevPriority < 4) {
                eat();
                Token b = evalExpression(lookupElement(setTabSpace),4);
                result.setVal(result.numVal() % b.numVal());
                prevPriority--;
            } else if (ops.equals("=") && prevPriority < 2) {
                eat();
                Token b = evalExpression(lookupElement(setTabSpace),2);
                if (result.isNumber() && b.isNumber()) { // Number compare
                    result.setVal(result.numVal() == b.numVal());
                } else if (result.isObject() && b.isObject()) {
                    result.setVal(result.objVal()==b.objVal());
                } else { // String compare
                    result.setVal(result.strVal().compareTo(b.strVal())==0);
                }
                prevPriority--;
            } else if (ops.equals(">") && prevPriority < 2) {
                eat();
                Token b = evalExpression(lookupElement(setTabSpace),2);
                if (result.isNumber() && b.isNumber()) { // Number compare
                    result.setVal(result.numVal() > b.numVal());
                } else { // String compare
                    result.setVal(result.strVal().compareTo(b.strVal())>0);
                }
                prevPriority--;
            } else if (ops.equals("<") && prevPriority < 2) {
                eat();
                Token b = evalExpression(lookupElement(setTabSpace),2);
                if (result.isNumber() && b.isNumber()) { // Number compare
                    result.setVal(result.numVal() < b.numVal());
                } else { // String compare
                    result.setVal(result.strVal().compareTo(b.strVal())<0);
                }
                prevPriority--;
            } else if (ops.equals(">=") && prevPriority < 2) {
                eat();
                Token b = evalExpression(lookupElement(setTabSpace),2);
                if (result.isNumber() && b.isNumber()) { // Number compare
                    result.setVal(result.numVal() >= b.numVal());
                } else { // String compare
                    result.setVal(result.strVal().compareTo(b.strVal())>=0);
                }
                prevPriority--;
            } else if (ops.equals("<=") && prevPriority < 2) {
                eat();
                Token b = evalExpression(lookupElement(setTabSpace),2);
                if (result.isNumber() && b.isNumber()) { // Number compare
                    result.setVal(result.numVal() <= b.numVal());
                } else { // String compare
                    result.setVal(result.strVal().compareTo(b.strVal())<=0);
                }
                prevPriority--;
            } else if ((ops.equals("<>")||ops.equals("><")) && prevPriority < 2) {
                eat();
                Token b = evalExpression(lookupElement(setTabSpace),2);
                if (result.isNumber() && b.isNumber()) { // Number compare
                    result.setVal(result.numVal() != b.numVal());
                } else if (result.isObject() && b.isObject()) {
                    result.setVal(result.objVal()!=b.objVal());
                } else { // String compare
                    result.setVal(result.strVal().compareTo(b.strVal())!=0);
                }
                prevPriority--;
            } else if (ops.equalsIgnoreCase("OR") && prevPriority < 1) {
                eat();
                Token b = evalExpression(lookupElement(setTabSpace),1);
                result.setVal(Utils.cInt(result.numVal()) | Utils.cInt(b.numVal()));
                prevPriority--;
            } else if (ops.equalsIgnoreCase("AND") && prevPriority < 1) {
                eat();
                Token b = evalExpression(lookupElement(setTabSpace),1);
                result.setVal(Utils.cInt(result.numVal()) & Utils.cInt(b.numVal()));
                prevPriority--;
            } else if (ops.equals("^")  && prevPriority < 4) {
                eat();
                Token b = evalExpression(lookupElement(setTabSpace),4);
                result.setVal(java.lang.Math.pow(result.numVal(), b.numVal()));
                prevPriority--;
            } else break; // Lower priority is on the right - consume left before advancing
            if (verbose) logger.debug("evalExpression: Done " + ops + " result is " + result);
            op = lookupElement(setTabSpace);
            ops = op.strVal();
            if (verbose) logger.debug("evalExpression: token ahead is: " + ops);

            if (result.isEvent()) {
                // Executes event
                result = evalFunction(result.eventVal().id, result.eventVal().getNoFormalParams(), result.eventVal());
                op = lookupElement(setTabSpace);
                ops = op.strVal();
            } else if (result.isDict() && ops.equals("(")) {
                parseToken("(");
                Token index = evalExpression(lookupElement(setTabSpace), 0);
                parseToken(")");
                if (!getReference) {
                    if (index.isNumber()) {
                        result = (Token) result.dictVal().elementAt(Utils.cInt(index.numVal())-1);
                    } else {
                        result = (Token) result.dictVal().get(index.strVal());
                    }
                    if (result == null) {
                        result = new Token();
                    } else {
                        result = (Token) result.getClone();
                    }
                } else {
                    Token newval;
                    if (index.isNumber()) {
                        newval = (Token) ((Dict) result.objVal()).elementAt(Utils.cInt(index.numVal())-1); // Actually may be DictSorted
                    } else {
                        newval = (Token) ((Dict) result.objVal()).get(index.strVal()); // Actually may be DictSorted
                    }
                    if (newval == null) {
                        newval = new Token();
                        ((Dict) result.objVal()).put(index.strVal(),newval); // Actually may be DictSorted
                    }
                    result = newval;
                }
                op = lookupElement(setTabSpace);
                ops = op.strVal();
            } // Case SETS/ARRAYS
        } // WHILE there are operators
        
        if (prevPriority == 0) {
            if (verbose) logger.debug("evalExpression: Result is " + result);
        } else {
            if (verbose) logger.debug("evalExpression: Intermediate result is " + result);
        }
        return result;
    }
    
    /** Evaluates a SmallBasic function
     * Function name "s" has been already eaten
     * @param s function name
     * @param no_params
     * @param e
     * @throws DimxException
     * @return result
     */
    private Token evalFunction(String s, int no_params, Event e) throws DimxException {
        Token result = null;
        
        try {
            if (verbose) logger.debug("evalFunction: expecting " + no_params + " parameters");
            Dict aparams = null;
            if (no_params > 0 || no_params == Const.VARIABLE) { // One or more params expected
                parseToken("(");
                aparams = parseList(no_params,setFunctClosers);
                parseToken(")");
            } else { // No params expected
                if (lookupToken().strVal().equals("(")) { // Brackets optional
                    parseToken("(");
                    parseToken(")");
                }
            }
            
            if (e!= null) {
                // Custom function
                if (verbose) logger.debug("evalFunction: Evaluating custom function: " + s);
                Varspace newvarsp = new Varspace(e.getFormalParams(),aparams);
                if (varspace != null && varspace.exists("input")) {
                    newvarsp.varSet("input", varspace.varGet("input", false));
                }
                logger.debug("Calling " + e.toString() + " with varspace: " + newvarsp);
                ActionsRunner runner = new ActionsRunner(world, newvarsp, e.getActions(), e.actionsStartLine, e.fileName, e.fileShort, owner, agent, target, stackLevel+1);
                try {
                    String myid = e.fileShort+":"+e.actionsStartLine;
                    world.optiCount(myid);
                    Calendar now = Calendar.getInstance();
                    result = runner.run();
                    Calendar then = Calendar.getInstance();
                    world.optiConsumed(myid,then.getTimeInMillis() - now.getTimeInMillis());
                    return result;
                } catch (DimxException exc) {
                    // Already documented - pass on as Nested
                    throw new NestedException(exc.getMessage());
                }
            }
            
            if (verbose) logger.debug("evalFunction: Evaluating system function: " + s);
            s = s.toLowerCase();
            if (s.equals("not")) {
                result = new Token(!((Token) aparams.elementAt(0)).boolVal());
            } else if (s.equals("isplayer")) {
                result = funIsPlayer(aparams);
            } else if (s.equals("ischaracter")) {
                result = funIsCharacter(aparams);
            } else if (s.equals("rnd")) {
                result = new Token(rndGen.nextFloat());
            } else if (s.equals("rndint")) {
                Token z = (Token) aparams.elementAt(0);
                result = new Token(1+rndGen.nextInt(Utils.cInt(z.numVal())));
            } else if (s.equals("int")) {
                Token z = (Token) aparams.elementAt(0);
                result = new Token(Utils.cInt(z.numVal()));
            } else if (s.equals("abs")) {
                Token z = (Token) aparams.elementAt(0);
                result = new Token(Math.abs((z.numVal())));
            } else if (s.equals("rndset")) {
                result = funRndSet(aparams);
            } else if (s.equals("newarray")) {
                result = funNewArray(aparams);
            } else if (s.equals("newlink")) {
                result = funNewLink(aparams);
            } else if (s.equals("getplayerproperties")) {
                result = funGetPlayerProperties(aparams);
            } else if (s.equals("httpfetch")) {
                result = funHttpFetch(aparams);
            } else if (s.equals("newset")) {
                result = funNewSet(aparams);
            } else if (s.equals("getroomsfrom")) {
                result = funGetRoomsFrom(aparams);
            } else if (s.equals("getrooms")) {
                result = funGetRooms(aparams);
            } else if (s.equals("round")) {
                result = funRound(aparams);
            } else if (s.equals("left")) {
                result = funLeft(aparams);
            } else if (s.equals("right")) {
                result = funRight(aparams);
            } else if (s.equals("len")) {
                result = funLen(aparams);
            } else if (s.equals("mid")) {
                result = funMid(aparams);
            } else if (s.equals("getplayersin")) {
                result = funGetPlayersIn(aparams);
            } else if (s.equals("getitemsin")) {
                result = funGetItemsIn(aparams);
            } else if (s.equals("getlinksfrom")) {
                result = funGetLinksFrom(aparams);
            } else if (s.equals("getobjectstype")) {
                result = funGetObjectsType(aparams,false); // false = DON'T Match subtypes
            } else if (s.equals("getobjectssubtype")) {
                result = funGetObjectsType(aparams,true); // true = Match subtypes
            } else if (s.equals("getobject")) {
                result = funGetObject(aparams);
            } else if (s.equals("getplayer")) {
                result = funGetPlayer(aparams);
            } else if (s.equals("instr")) {
                result = funInstr(aparams);
            } else if (s.equals("instrcount")) {
                result = funInstrCount(aparams);
            } else if (s.equals("log")) {
                result = funLog(aparams);
            } else if (s.equals("replace")) {
                result = funReplace(aparams);
            } else if (s.equals("maintype")) {
                result = funMainType(aparams);
            } else if (s.equals("newcharacter")) {
                result = funNewCharacter(aparams);
            } else if (s.equals("newitem")) {
                result = funNewItem(aparams);
            } else if (s.equals("newimage")) {
                result = funNewImage(aparams);
            } else if (s.equals("getcharactersin")) {
                result = funGetCharactersIn(aparams);
            } else if (s.equals("split")) {
                result = funSplit(aparams);
            } else if (s.equals("setlen")) {
                result = funSetLen(aparams);
            } else if (s.equals("setcontainskey")) {
                result = funSetContainsKey(aparams);
            } else if (s.equals("setindexof")) {
                result = funSetIndexOf(aparams);
            } else if (s.equals("setkey")) {
                result = funSetKey(aparams);
            } else if (s.equals("setkeys")) {
                result = funSetKeys(aparams);
            } else if (s.equals("isroom")) {
                result = funIsRoom(aparams);
            } else if (s.equals("isitem")) {
                result = funIsItem(aparams);
            } else if (s.equals("chr")) {
                result = funChr(aparams);
            } else if (s.equals("copy")) {
                result = funCopy(aparams);
            } else if (s.equals("lcase")) {
                result = funLCase(aparams);
            } else if (s.equals("ucase")) {
                result = funUCase(aparams);
            } else if (s.equals("exists")) {
                result = funExists(aparams);
            } else if (s.equals("gettime")) {
                result = funGetTime(aparams);
            } else if (s.equals("getsetting")) {
                result = funGetSetting(aparams);
            } else if (s.equals("existscript")) {
                result = funExistsScript(aparams);
            } else if (s.equals("sqr")) {
                result = funSqr(aparams);
            } else if (s.equals("panelhtml")) {
                result = funPanelHtml(aparams);
            } else if (s.equals("gameinfo")) {
                result = funGameInfo(aparams);
            } else if (s.equals("asc")) {
                result = funAsc(aparams);
            } else if (s.equals("urlencode")) {
                result = funUrlencode(aparams);
            } else {
                throw new DimxException("evalFunction: DimX engine misses function-handling code for: "+s);
            }
            return result;
        } catch (NestedException exc) {
            // It is already fully defined - simply pass it on
            throw new NestedException(exc.getMessage());
        // ver 5.7. - commenting it 'cause it seems it can handle it well in actionsRunner
        //} catch (Exception exc) {
        //    String errmsg = "ERROR evaluating function: " + exc + "\nnear line: " + currLine;
        //    errmsg = errmsg + "\n" + identifyLine();
        //    throw new NestedException(errmsg);
        }
    }
    public void feed(String str) {
        buf = buf + str;
    }
    
    protected String getLine() throws DimxException {
        return getToken(CONSUME,null,setCr).strVal();
    }
    
    private Token getToken(boolean lookupOnly, String[] leadingIgnored, String[] terminating) throws DimxException {
        Token result = new Token();
        boolean thruComment = false;
        
        // Set starting point
        int a = startToken;
        if (leadingIgnored != null) {
            // Search required
            while (a < buf.length()) {
                char c = buf.charAt(a);
                String cc = Utils.cStr(c); // For debug watch
                if (thruComment) {
                    if (c=='\n') { // End comment!
                        thruComment = false;
                        a--; // Avoid eating up the CR
                    }
                } else {
                    // Normal code
                    if (c == '\'') { // Comment found!
                        thruComment = true;
                    } else {
                        if (Utils.isInCsens(cc,leadingIgnored)) {
                            // Continue skipping
                            if (c=='\n' && !lookupOnly) currLine++;
                        } else { // Starting point found / Stop skipping
                            break;
                        }
                    }
                }
                a++;
            }
        }
        
        if (a == buf.length()) {
            // Reached EOF without encountering any token start
            result.setPunctuation(EOF);
            if (!lookupOnly) startToken = buf.length();
            // EXIT
        } else {
            // Analysis of first char
            char c = buf.charAt(a);
            String cc = Utils.cStr(c); // debug watch
            if (Utils.isInCsens(cc, setPunctuation)) { // TYPE=Punctuation
                result.setPunctuation(c);
                // Check for compound operator >= <= ...
                if (!lookupOnly) startToken = a+1;
                if (c=='\n' && !lookupOnly) currLine++;
                if (c=='<') {
                    char d = buf.charAt(a+1);
                    if ((d=='=')||(d=='>')) {
                        result.setPunctuation(""+c+d);
                        if (!lookupOnly) startToken = a+2;
                    }
                } else if (c=='>') {
                    char d = buf.charAt(a+1);
                    if (d=='='||d=='<') {
                        result.setPunctuation(""+c+d);
                        if (!lookupOnly) startToken = a+2;
                    }
                }
            } else { // NO-punctuation case
                if (c=='\"') {
                    // TYPE=Literal
                    result.setLiteral();
                    terminating = setQuotes;
                } else if (((int) c) >= 48 && ((int) c) <= 57) {
                    // TYPE=Number
                    result.setNumber();
                    //Logger.echo("Terminating first: " + Utils.arrayToString(terminating));
                    terminating = allButDot(terminating); // Remove dot from terminators
                    //Logger.echo("Terminating next: " + Utils.arrayToString(terminating));
                } else if (Utils.isInCsens("" + c, setPunctuation)) {
                    // TYPE=Punctuation
                    result.setPunctuation(c);
                } else {
                    // TYPE=String
                    result.setIdentifier();
                }
                // Setting ending point
                int z = a+1;
                if (terminating != null) {
                    // Search required
                    while (z < buf.length() && !Utils.isInCsens(Utils.cStr(buf.charAt(z)),terminating)) {
                        z++;
                    }
                } else {
                    // End at string's end
                    z = buf.length();
                }
                
                // Cutting...
                
                if (result.isLiteral())
                    result.setVal(buf.substring(a+1,z));
                else
                    result.setVal(buf.substring(a,z));
                if (result.isLiteral()) {
                    if (z == buf.length()) {
                        throw new DimxException("Missing closing quote");
                    } else {
                        z++; // Skip closing quote
                    }
                }
                if (!lookupOnly) startToken = z;
            }
        }
        
        if (verbose) {
            if (lookupOnly) {
                logger.debug("Next is:  " + result);
            } else {
                logger.debug("Consumed: " + result);
            }
        }
        return result;
    }
/*
 * Identifies specified function "s".
 * if found, returns the expected number of parameters
 * if custom function, fills e with a ref to the Event, or leaves it unchanged otherwise
 * returns (-1,null) if not found
 */
    public Vector identifyFunProc(String s,String[] setNames,int[] setNoArgs) {
        // Try to search in system functions
        Token et = null;
        Event e = null;
        Vector ret = new Vector(2,1);
        int i = 0;
        while (i < setNames.length) {
            if (s.equalsIgnoreCase(setNames[i])) {
                ret.addElement(new Integer(setNoArgs[i]));
                ret.addElement(null);
                return ret;
            }
            i++;
        }
        // Try to search custom functions
        et = world.varGet(s);
        if (et != null) {
            e = et.eventVal();
            
            if (e != null) {
                ret.addElement(new Integer(e.getNoFormalParams()));
                ret.addElement(e);
                return ret;
            }
        }
        // Else return search failed
        ret.addElement(new Integer(Const.NOTFOUND));
        ret.addElement(null);
        return ret;
    }
    
    public String identifyLine() {
        int at = startToken;
        int start = 0;
        if ((at-start) > 80)  start = at-80; // view last 80 chars
        return buf.substring(start,at) + "<-- Error here";
    }
    public Token lookupElement(String[] leadingIgnored) throws DimxException {
        return getToken(PRESERVE,leadingIgnored,tokenTerminators);
    }
    /**
     * 
     * @throws cleoni.adv.DimxException 
     * @return 
     */
    public Token lookupToken() throws DimxException {
        return getToken(PRESERVE,setCrTabSpace,tokenTerminators);
    }

    public Token lookupWord() throws DimxException {
        return getToken(PRESERVE,setTabSpace,setCrTabSpace);
    }
    /**
     * Looks up for the next token, CR is not passed over.
     * @throws cleoni.adv.DimxException 
     * @return 
     */
    public Token lookupTokenCRsens() throws DimxException {
        return getToken(PRESERVE,setTabSpace,tokenTerminators);
    }
    public Token nextElement(String[] leadingIgnored) throws DimxException {
        return getToken(CONSUME,leadingIgnored,tokenTerminators);
    }
    public Token nextToken() throws DimxException {
        return getToken(CONSUME,setCrTabSpace,tokenTerminators);
    }
    
    public Token parseCR() throws DimxException {
        if (verbose) logger.debug("DimxParser Searching for: CR ignoring TAB/SPACE");
        
        Token myToken = getToken(CONSUME,setTabSpace,tokenTerminators);
        
        String s = myToken.strVal();
        if (s.equals(CR)) {
            return myToken;
        } else {
            throw new DimxException("Unexpected: " + myToken.strVal() + " when looking for: (Carriage Return)");
        }
    }
    public Token parseCREOF() throws DimxException {
        if (verbose) logger.debug("DimxParser Searching for: CR or EOF ignoring TAB/SPACE");
        
        Token myToken = getToken(CONSUME,setTabSpace,tokenTerminators);
        
        String s = myToken.strVal();
        if (s.equals(CR) || s.equals(EOF)) {
            return myToken;
        } else {
            throw new DimxException("Unexpected: " + myToken.strVal() + " when looking for: (Carriage Return) or (EOF)");
        }
    }
    public Token parseToken(String aToken) throws DimxException {
        /* Recognises and consumes the specified token. Ignores Up/lowercase */
        /* If reaches EOF or another token it raises an exception */
        
        
        if (verbose) logger.debug("DimxParser Searching for: " + aToken + " ignoring CR/TAB/SPACE");
        
        Token myToken = nextToken();
        
        if (myToken.strVal().equalsIgnoreCase(aToken)) {
            return myToken;
        } else {
            throw new DimxException("Unexpected: " + myToken + " when looking for: " + aToken);
        }
    }
/*
 * Special version of getLine:
 * skips initial spaces and tabs, eats final CR
 * doesn't return final CR, though
 */
    public String readToCR() throws DimxException {
        
        String s=tokenizeLine().strVal();
        String x = getLine();
        return s;
    }
    
    public void reset() {
        this.currLine = 0;
        startToken = 0;
        buf = "";
    }
    
    /**
     * @param x Object or ID of the object
     * @return the object or NULL
     */    
    private ManageableObject mygetObject(Token x) {
        try {
            return (ManageableObject) x.objVal();
        } catch (ClassCastException e) {
            return (ManageableObject) world.getObjectExt(x.strVal());
        }
    }
/*
 * Resolves the specified ID by using the current varspace or world's
 * returns a token with a copy of the object inside (getReference = false)
 * or a reference to the object if getReference = tRUE
 */
    public Token resolve(String s, boolean getReference) throws DimxException {
        Token result = null;
        // First, search current varspace
        if (varspace != null) { // check necessary because during world init there is no varspace
            result = varspace.varGet(s, getReference);
        }
        if (result != null) return result;
        // Next, check specials
        if (s.charAt(0) == '$') {
            DimxObject o = null;
            if (s.equalsIgnoreCase("$AGENT")) {
                o = agent;
            } else if (s.equalsIgnoreCase("$OWNER")) {
                o = world.getObjectExt(owner);
            } else if (s.equalsIgnoreCase("$TARGET")) {
                o = world.getObjectExt(target);
                if (o == null && target != null && !target.equals("")) {
                    return new Token(target);
                }
            } else if (s.equalsIgnoreCase("$WORLD")) {
                o = world;
            }
            if (o != null) {
                return new Token(o);
            } else { // Return null
                Token tnull = new Token();
                tnull.setNumber();
                return tnull;
            }
        }
        // Next, search world OBJECTS (world contents)
        DimxObject o = world.getObjectExt(s);
        if (o != null) return new Token(o);

        // Finally, scan world properties
        result = world.varGet(s,getReference);
        if (result == null) { // Create a nullstring token
            result = new Token("");
            result.setNumber();
        }
        return result;
    }
    
    private Token tokenizeLine() throws DimxException {
        Token result = new Token();
        boolean thruComment = false;
        String[] terminating = setCr;
        String[] leadingIgnored = setTabSpace;
        
        // This block is copied from getToken - please update if it changes
        // Set starting point
        int a = startToken;
        if (leadingIgnored != null) {
            // Search required
            while (a < buf.length()) {
                char c = buf.charAt(a);
                String cc = Utils.cStr(c); // For debug watch
                if (thruComment) {
                    if (c=='\n') { // End comment!
                        thruComment = false;
                        a--; // Avoid eating up the CR
                    }
                } else {
                    // Normal code
                    if (c == '\'') { // Comment found!
                        thruComment = true;
                    } else {
                        if (Utils.isIn(Utils.cStr(c),leadingIgnored)) {
                            // Continue skipping
                            if (c=='\n') currLine++;
                        } else { // Starting point found / Stop skipping
                            break;
                        }
                    }
                }
                a++;
            }
        }
        
        if (a == buf.length()) {
            // Reached EOF without encountering any token start
            result.setPunctuation(EOF);
            startToken = buf.length();
            // EXIT
        } else {
            result.setLiteral();
            int z;
            
            if (buf.charAt(a)=='\n') {
                // Null content - line is only spaces and final CR
                z = a;
            } else {
                // Setting ending point
                z = a+1;
                // Search required
                while (z < buf.length() && !Utils.isIn(""+buf.charAt(z),terminating)) {
                    z++;
                }
            }
            
            // Cutting...
            result.setVal(buf.substring(a,z));
            startToken = z;
        }
        
        if (verbose) {
            logger.debug("Read and consumed: " + result);
        }
        return result;
    }
    
    public String toString() {
        return buf;
    }
    
    public Dict parseList(int how_many_expected,String[] setClosers) throws DimxException {
        // Read and recognize parameters
        Dict params = new Dict();
        Token parval;
        int i = 0;
        Token t = lookupElement(setTabSpace);
        String s = t.strVal();
        
        while (!(s.equals(CR) || s.equals(EOF) || s.equals(")") /* || (!t.isLiteral() && Utils.isIn(s,setOperators) )*/ ) && (i < how_many_expected || how_many_expected == Const.VARIABLE)) {
            // While not CR and something to read expected...
            // Read Expression
            parval = evalExpression(t,0);
            
            if (parval != null) {
                // Set parameter
                params.put("par" + i,parval);
            }
            
            i++;
            
            if (i < how_many_expected) { // More params expected
                parseToken(","); // Consume comma
            } else if (how_many_expected == -1) {
                // Is there any more?
                t = lookupElement(setTabSpace);
                if (t.strVal().equals(",")) {
                    parseToken(","); // YES - Consume comma
                } else break;
            }
            t = lookupElement(setTabSpace);
            s = t.strVal();
        }
        
        if (i < how_many_expected) {
            // If CR encountered and we have read less params...
            throw new DimxException("Too few parameters. Specified " + i + " expected " + how_many_expected);
        }
        
        if (Utils.isIn(t.strVal(),setClosers)) {
            return params;
        } else {
            String message = "Too many parameters.";
            if (how_many_expected >= 0) message = message + "Expected " + how_many_expected;
            throw new DimxException(message);
            //throw new DimxException("Badly formed parameters list");
        }
    }
    
    private Token funGetObject(Dict params) {
        Token p0 = (Token) params.elementAt(0);
        DimxObject o = world.getObjectExt(p0);
        return new Token(o);
    }

    private Token funGetPlayer(Dict params) {
        Token p0 = (Token) params.elementAt(0);
        AdvObject o = world.getPlayer(p0.strVal());
        return new Token(o);
    }

    private Token funGetObjectsType(Dict params,boolean match_subtype) {
        Token p0 = (Token) params.elementAt(0);
        Token p1 = (Token) params.elementAt(1);
        String type = p1.strVal();
        Dict newSet = new Dict();
        if (type.equals("")) return new Token(newSet); // Return null set
        DimxObject o = world.getObjectExt(p0);
        if (o != null) {
            Dict cont = o.getContents();
            for (int i=0; i < cont.size(); i++) {
                AdvObject dxo = (AdvObject) cont.elementAt(i);
                if (match_subtype) { // Match over type and subtypes
                    Vector v = Utils.stringSplit(dxo.varGet("type").strVal(), ".");
                    if (Utils.isIn(type,v)) { // Match found!
                        newSet.put(dxo.id,new Token(dxo));
                    }
                } else {
                    if (dxo.varGet("type").strVal().startsWith(type)) {
                        newSet.put(dxo.id,new Token(dxo));
                    }
                }
            }
        }
        return new Token(newSet);
    }
    private void innerGetItemsIn(DimxObject o, Dict newSet) {
        if (o != null) {
            Dict cont = o.getContents();
            for (int i=0; i < cont.size(); i++) {
                AdvObject dxo = (AdvObject) cont.elementAt(i);
                if (dxo.isanItem()) {
                    newSet.put(dxo.id,new Token(dxo));
                }
            }
        }
    }
    private void innerGetLinksFrom(DimxObject o, Dict newSet) {
        if (o != null) {
            if (o.isaRoom()) {
                Room r = (Room) o;
                Dict links = r.getLinks();
                for (int i=0; i < links.size(); i++) {
                    AdvObject dxo = (AdvObject) links.elementAt(i);
                    newSet.put(dxo.id,new Token(dxo));
                }
            }
        }
    }
    private Token funGetItemsIn(Dict params) {
        Token p0 = (Token) params.elementAt(0);
        Dict newSet = new Dict();
        if (!p0.isDict()) {
            DimxObject o = world.getObjectExt(p0);
            innerGetItemsIn(o,newSet);
        } else {
            Dict myset = p0.dictVal();
            for (int i=0; i < myset.size(); i++ ) {
                innerGetItemsIn(((Token) myset.elementAt(i)).dimxobjVal(),newSet);
            }
        } 
        return new Token(newSet);
    }
    private Token funGetLinksFrom(Dict params) throws DimxException {
        Token p0 = (Token) params.elementAt(0);
        Dict newSet = new Dict();
        if (!p0.isDict()) {
            DimxObject o = world.getObjectExt(p0);
            if (o == this.world) {
                Dict newparams = new Dict();
                newparams.put("param0", new Token(funGetRooms(new Dict()).dictVal()));
                return funGetLinksFrom(newparams);
            } else {
                innerGetLinksFrom(o,newSet);
            }
        } else {
            Dict myset = p0.dictVal();
            for (int i=0; i < myset.size(); i++ ) {
                innerGetLinksFrom(((Token) myset.elementAt(i)).dimxobjVal(),newSet);
            }
        } 
        return new Token(newSet);
    }
    
    private Token funAsc(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        String s = p1.strVal();
        int ret = -1;
        if (!s.equals("")) {
            ret = s.charAt(0);
        } 
        return new Token(ret);
    }

    private Token funChr(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        char c = (char) Utils.cInt(p1.numVal());
        StringBuffer s = new StringBuffer("");
        return new Token(s.append(c).toString());
    }

    private Token funCopy(Dict params) throws DimxException {
        Token p1 = (Token) params.elementAt(0);
        if (!p1.isDict()) {
            throw new DimxException("Unexpected type for: "+p1.strVal());
        }
        Dict d = null;
        if (p1.isDictSorted()) {
            d = (DictSorted) p1.dictsortedVal().clone();
        } else {
            d = (Dict) p1.dictVal().clone();
        }
        return new Token(d);
    }
    
    private Token funExists(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        DimxObject o = world.getObjectExt(p1);
        boolean result = (o != null && o.world != null);
        return new Token(result);
    }
    
    private Token funExistsScript(Dict params) throws DimxException {
        Token p1 = (Token) params.elementAt(0);
        if (!p1.isLiteral()) { 
            throw new DimxException("Literal parameter expected. Specify function name between double quotes");
        }
        Vector res = identifyFunProc(p1.strVal(),setFunctions,setFunctArgs);
        Event e = (Event) res.elementAt(1);
        boolean result = (e != null);
        return new Token(result);
    }
    
    private Token funGameInfo(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        String par1 = p1.strVal().toLowerCase();
        if (par1.equals("site")) {
            return new Token(world.site);
        } else if (par1.equals("imagesfolder")) {
            return new Token(world.imagesFolder);
        } else if (par1.equals("navigator")) {
            return new Token(world.server.navigatorUrl);
        }
        return new Token();
    }

    private Token funGetCharactersIn(Dict params) {
        Token p0 = (Token) params.elementAt(0);
        Dict newSet = new Dict();
        DimxObject o = world.getObjectExt(p0);
        if (o != null) {
            Dict cont = o.getContents();
            for (int i=0; i < cont.size(); i++) {
                AdvObject dxo = (AdvObject) cont.elementAt(i);
                if (dxo.isaCharacter()) {
                    newSet.put(dxo.id,new Token(dxo));
                }
            }
        }
        return new Token(newSet);
    }
    
    private Token funGetPlayerProperties(Dict params) throws DimxException {
        Token p0 = (Token) params.elementAt(0);
        String uname = Utils.stringFlatten(p0.strVal());
        
        String str = world.getSetting(uname + "_properties", "");

        Dict newSet = propstring2dict(str);
        return new Token(newSet);
    }
    
    private Token funGetPlayersIn(Dict params) {
        Token p0 = (Token) params.elementAt(0);
        DimxObject o = world.getObjectExt(p0);
        Dict newSet = new Dict();
        if (o != null) {
            Dict cont = o.getContents();
            for (int i=0; i < cont.size(); i++) {
                AdvObject dxo = (AdvObject) cont.elementAt(i);
                if (dxo.isPlayer()) {
                    newSet.put(dxo.id,new Token(dxo));
                }
            }
        }
        return new Token(newSet);
    }
    
    private Token funGetRooms(Dict params) throws DimxException {
        Token p0 = null;
        Dict exclusions = null;
        if (params.size() > 0 && !(p0 = (Token) params.elementAt(0)).isNull()) {
            if (p0.isDict()) {
                exclusions = p0.dictVal();
            } else {
                throw new DimxException("Set parameter expected - got: '" + p0 + "' instead.");
            }
        } else {
            exclusions = new Dict();
        }
            
        Dict cont = world.getContents();
        Dict newSet = new Dict();
        for (int i=0; i < cont.size(); i++) {
            AdvObject dxo = (AdvObject) cont.elementAt(i);
            if (dxo.isaRoom() && !Utils.isIn(dxo.id,exclusions)) {
                newSet.put(dxo.id,new Token(dxo));
            }
        }
        return new Token(newSet);
    }

    private Token funGetRoomsFrom(Dict params) throws DimxException {
        Dict rooms = new Dict();

        AdvObject room = world.getObject((Token) params.elementAt(0));
        if (room != null && room.isaRoom()) {
            Dict ways = ((Room) room).getLinks();
            for (int i=0; i < ways.size(); i++) {
                Link w = (Link) ways.elementAt(i);
                if (w.isOpen()) {
                    Room r = w.getTarget(room.id);
                    rooms.put(r.id,new Token(r));
                }
            }
        }
        return new Token(rooms);
    }

    private Token funGetSetting(Dict params) throws DimxException {
        Token p1 = (Token) params.elementAt(0);
        String def = "";
        
        if (params.size() > 1) def = ((Token) params.elementAt(1)).strVal();
       
        String setting = world.getSetting(p1.strVal(),def);
        
        if (setting.startsWith("!set!")) {
            DictSorted d = Utils.string2setTokens(setting.substring(5),",","=",true);
            return new Token(d);
        } else {
            return new Token(setting);
        }
    }

    private Token funGetTime(Dict params) throws DimxException {
        Token p1 = (Token) params.elementAt(0);
        String par = p1.strVal();
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat sdf1 = null;
        
        sdf1 = new java.text.SimpleDateFormat(par);
        
        return new Token(sdf1.format(cal.getTime()));
    }
    private Token funHttpFetch(Dict params) throws DimxException {
        Token p1 = (Token) params.elementAt(0);
        String s1 = p1.strVal();

        if (!(s1.substring(0, 7).equals("http://") || s1.substring(0, 8).equals("https://"))) {
            throw new DimxException("Must specify an http(s):// URL");
        } 
        
        return new Token(Utils.fetch(s1, "UTF-8"));
    }
    private Token funInstr(Dict params) throws DimxException {
        if (params.size() != 3 && params.size() != 2) {
            throw new DimxException("Wrong number of parameters");
        }
        
        Token p1 = (Token) params.elementAt(0);
        Token p2 = (Token) params.elementAt(1);
        Token p3 = (Token) params.get("par2");
        int start = 0;
        if (p3 != null) start = p3.intVal()-1;
        if (start < 0) throw new DimxException("Invalid startpos: must be >= 1");
        
        return new Token(1+Utils.instr(p1.strVal(),p2.strVal(),start, true));
    }
    private Token funInstrCount(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        Token p2 = (Token) params.elementAt(1);
        int count = Utils.instrCount(p1.strVal(),p2.strVal(),true);
        return new Token(count);
    }
    private Token funIsCharacter(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        AdvObject o = world.getObject(p1);
        if (o == null) return new Token(false);
        return new Token(o.isaCharacter());
    }
    private Token funIsItem(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        AdvObject o = world.getObject(p1);
        if (o == null) return new Token(false);
        return new Token(o.isanItem());
    }
    private Token funIsPlayer(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        AdvObject o = world.getObject(p1);
        if (o == null) return new Token(false);
        return new Token(o.isPlayer());
    }
    private Token funIsRoom(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        AdvObject o = world.getObject(p1);
        if (o == null) return new Token(false);
        return new Token(o.isaRoom());
    }
    private Token funLeft(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        Token p2 = (Token) params.elementAt(1);
        try {
            int len = Utils.cInt(p2.numVal());
            return new Token(p1.strVal().substring(0,len));
        } catch (java.lang.StringIndexOutOfBoundsException e) {
            return new Token(p1.strVal());
        }
    }
    private Token funLen(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        return new Token(p1.strVal().length());
    }
    private Token funLCase(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        return new Token(p1.strVal().toLowerCase());
    }
        
    private Token funLog(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        return new Token(java.lang.Math.log(p1.numVal()));
    }
    
    private Token funUCase(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        return new Token(p1.strVal().toUpperCase());
    }
    private Token funMainType(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        String res;
        if (p1.isDimxObject()) {
            Token t = p1.dimxobjVal().varGet("type");
            int x = t.strVal().indexOf(".");
            if (x > 0) {
                return new Token(t.strVal().substring(0, x));
            } else {
                return t;
            }
        }
        return new Token();
    }
    private Token funMid(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        Token p2 = (Token) params.elementAt(1);
        Token p3 = (Token) params.elementAt(2);
        try {
            int start = Utils.cInt(p2.numVal())-1;
            int len = Utils.cInt(p3.numVal());
            String s = p1.strVal();
            return new Token(s.substring(start,start+len));
        } catch (java.lang.StringIndexOutOfBoundsException e) {
            return new Token(p1.strVal());
        }
    }
    private Token funNewCharacter(Dict params) throws DimxException {
        Token pos = (Token) params.elementAt(0);
        Token name = (Token) params.elementAt(1);
        Token descr = (Token) params.elementAt(2);
        Token image = (Token) params.elementAt(3);
        String attrs = ((Token) params.elementAt(4)).strVal();
        String attrList = world.charactersDefaultAttrlist;

        AdvObject opos = world.getObject(pos);
        
        if (opos == null) throw new DimxException("Invalid location: " + pos);

        if (attrs != null && !attrs.equals("")) {
            // If default attributes present, add the custom ones
            attrList =  attrList + "," + attrs;
        }
        String id = world.getNextId();

        Character o = new Character(world, name.strVal(), id, descr.strVal(), null, world.defaultCharacter.capacity.intVal(),attrList, opos.id, "");
        o.setImage("N",image.imageVal());
        world.addCharacter(o);
        o.reset();
        return new Token(id);
    }
    
    private Token funNewItem(Dict params) throws DimxException {
        Token pos = (Token) params.elementAt(0);
        Token name = (Token) params.elementAt(1);
        Token descr = (Token) params.elementAt(2);
        Token image = (Token) params.elementAt(3);
        String attrs = ((Token) params.elementAt(4)).strVal();
        AdvObject opos = world.getObject(pos);
        
        if (opos == null && !pos.strVal().equals("")) {
            throw new DimxException("Invalid location: " + pos);
        }
        
        Item o = new Item(world, name.strVal(), "(new)", descr.strVal(), null,attrs, 5, 1, pos.strVal());
        if (!image.isNull()) {
            o.setImage("N",image.imageVal());
        }
        o.varsSet(attrs); // Sets base attributes
        world.addItem(o,Const.REDEFINE_ID);
        
        if (opos != null) {
            while (opos.getFreeSpace() < o.volume.intVal()) {
                // If target container too small then use its container
                opos = opos.container;
            }
            opos.contents.put(o.id,o);
            // Refresh players' view
            o.container = opos;
            opos.sendCmd(Const.CMDE_REFRSCENE);
        }
       
        return new Token(o.id);
    }
    
    private Token funNewLink(Dict params) throws DimxException {
        Token from = (Token) params.elementAt(0);
        Token to = (Token) params.elementAt(1);
        Token direction = (Token) params.elementAt(2);
        Token bidirect = (Token) params.elementAt(3);
        Token tname = (Token) params.elementAt(4);
        String name = "*";
        Token descr = (Token) params.elementAt(5);
        Token image = (Token) params.elementAt(6);
        String attrs = ((Token) params.elementAt(7)).strVal();

        AdvObject ofrom = world.getObject(from);
        AdvObject oto = world.getObject(to);
        
        if (ofrom == null || !ofrom.isaRoom()) {
            throw new DimxException("Invalid room: " + from);
        }
        if (oto == null || !oto.isaRoom()) {
            throw new DimxException("Invalid room: " + to);
        }
        
        if (!tname.isNull()) name = tname.strVal();
        
        Link o = new Link(world,name,"(new)",(Room) ofrom,(Room) oto,bidirect.boolVal(),direction.strVal(),descr.strVal(),null,attrs);
        if (!image.isNull()) {
            o.setImage("N",image.imageVal());
        }
        o.varsSet(attrs); // Sets base attributes
        world.addLink(o,Const.REDEFINE_ID);
        
        // Refresh players' view
        if (ofrom != null) {
            ofrom.sendCmd(Const.CMDE_REFRSCENE);
        }
        if (oto != null) {
            oto.sendCmd(Const.CMDE_REFRSCENE);
        }
       
        return new Token(o.id);
    }

    private Token funNewImage(Dict params) throws DimxException {
        Token url = (Token) params.elementAt(0);
        int width = Utils.cInt(((Token) params.elementAt(1)).numVal());
        int height = Utils.cInt(((Token) params.elementAt(2)).numVal());
        String face = null;
        
        Image ima = new Image(Utils.absolutizeUrl(url.strVal(),world.imagesFolder),
        width,
        height);
        return new Token(ima);
    }
    
    private Token funPanelHtml(Dict params) throws DimxException {
        Token p0 = (Token) params.elementAt(0);
        Panel p = world.getPanel(p0.strVal());
        if (p != null) {
            return new Token(p.toHtml(world.getSkin(null), null, world.getObject(owner), agent, target, null));
        } else {
            throw new DimxException("Unexistent panel: \"" + p0.strVal() + "\"");
        }
    }    

    private Token funReplace(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        Token p2 = (Token) params.elementAt(1);
        Token p3 = (Token) params.elementAt(2);
        String r = Utils.stringReplace(p1.strVal(),p2.strVal(),p3.strVal(),true);
        return new Token(r);
    }    
    
    private Token funRight(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        Token p2 = (Token) params.elementAt(1);
        try {
            int len = Utils.cInt(p2.numVal());
            String s = p1.strVal();
            return new Token(s.substring(s.length()-len,s.length()));
        } catch (java.lang.StringIndexOutOfBoundsException e) {
            return new Token(p1.strVal());
        }
    }
    
    private Token funRndSet(Dict params) throws DimxException {
        Token p1 = (Token) params.elementAt(0);
        if (!p1.isDict()) throw new DimxException("SET argument expected");
        
        Dict mySet = p1.dictVal();
        if (mySet.size() > 0) {
            int i = rndGen.nextInt(mySet.size());
            Token t =(Token) mySet.elementAt(i);
            return t;
        } else {
            return new Token();
        }
    }
    
    private Token funNewArray(Dict params) throws DimxException {
        if (params.size() == 0) {
            return new Token(new DictSorted());
        } 
        
        if (params.size() != 1) {
            throw new DimxException("Wrong number of parameters");
        }
        
        Token p1 = (Token) params.elementAt(0);
        Dict d = new DictSorted();
        
        Vector v = Utils.stringSplit(p1.strVal(),",");
        for (int i=0; i < v.size(); i++) {
            d.put(Utils.leadingZeroes(i+1,Const.ARRAYMAX),new Token((String) v.elementAt(i)));
        }
        return new Token(d);
    }

    private Token funNewSet(Dict params) throws DimxException {
        if (params.size() == 0) {
            return new Token(new DictSorted());
        } 
        
        if (params.size() != 1) {
            throw new DimxException("Wrong number of parameters");
        }
        
        Token p1 = (Token) params.elementAt(0);

        DictSorted d = Utils.string2setTokens(p1.strVal(),",","=",true);
        
        return new Token(d);
    }

    private Token funRound(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        Token p2 = (Token) params.elementAt(1);
        double v = p1.numVal();
        double multi = Math.pow(10.0d,p2.numVal());
        v = v * multi;
        double off = 0.5;
        if (v < 0) off = -0.5;
        v = Utils.cInt(v + off);
        return new Token(v/multi);
    }
    
    private Token funSetContainsKey(Dict params) throws DimxException {
        Token p1 = (Token) params.elementAt(0);
        Token p2 = (Token) params.elementAt(1);
        if (!p1.isDict()) throw new DimxException("SET argument expected");
        return new Token(Utils.isIn(p2.strVal(),p1.dictVal()));
    }

    private Token funSetIndexOf(Dict params) throws DimxException {
        Token p1 = (Token) params.elementAt(0);
        Token p2 = (Token) params.elementAt(1);
        if (!p1.isDict()) throw new DimxException("SET argument expected");
        return new Token(Utils.indexOf(p2,p1.dictVal(), false)+1); // false here means NOT case sensitive
    }

    private Token funSetLen(Dict params) throws DimxException {
        Token p1 = (Token) params.elementAt(0);
        if (!p1.isDict()) return new Token(-1);
        return new Token(p1.dictVal().size());
    }
    
    private Token funSetKeys(Dict params) throws DimxException {
        Token p1 = (Token) params.elementAt(0);
        if (!p1.isDict()) throw new DimxException("SET argument expected");
        Vector k = new Vector();
        p1.dictVal().copyKeysInto(k);
        DictSorted d = new DictSorted(); //new DictSorted(k.length);
        for (int i=0; i < k.size(); i++) {
            d.put(Utils.leadingZeroes(i,Const.ARRAYMAX), new Token((String) k.elementAt(i)));
        }
        return new Token(d);
    }
    
    private Token funSetKey(Dict params) throws DimxException {
        Token p1 = (Token) params.elementAt(0);
        if (!p1.isDict()) throw new DimxException("SET argument expected");
        Dict d = p1.dictVal();
        Token p2 = (Token) params.elementAt(1);
        
        int n = Utils.cInt(p2.numVal());

        if (n < 1 || n > p1.dictVal().size());

        return new Token(d.keyAt(n-1));
    }

    private Token funSplit(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        Token p2 = (Token) params.elementAt(1);
        Vector v = Utils.stringSplit(p1.strVal(), p2.strVal());
        DictSorted d = new DictSorted();
        for (int i=0; i < v.size(); i++)
            d.put(Utils.leadingZeroes(i+1,Const.ARRAYMAX),new Token((String) v.elementAt(i)));
        return new Token(d);
    }

    
    private Token funSqr(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        return new Token(java.lang.Math.sqrt(p1.numVal()));
    }

        private Token funUrlencode(Dict params) {
        Token p1 = (Token) params.elementAt(0);
        String s = p1.strVal();
        String ret = "";
        if (!s.equals("")) {
            ret = Utils.encodeURL(s);
        }
        return new Token(ret);
    }

    // returns a copy the specified array whose last element was cut.
    // If last element is not a dot, it will throw error
    private String[] allButDot(String[] originalarray) throws DimxException {
        if (!originalarray[originalarray.length-1].equals(".")) {
            //String buf
            //for (int i=0; i<originalarray.length; i++) {
            //    buf
            throw new DimxException("This array does not terminate with dot: " + originalarray);
        }
        String[] newarr = new String[originalarray.length-1];
        for (int i=0; i < originalarray.length-1; i++) newarr[i]=new String(originalarray[i]);
        return newarr;
    }
    
    private Dict propstring2dict(String propsString) {
        Dict newdict = new Dict();
        
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
                        DimxParser p = new DimxParser(world,Const.WORLDVARS,0,owner);
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
                    newdict.put((String) ke.elementAt(0),t);
                }
            }
        }
        return newdict;
    }

}
