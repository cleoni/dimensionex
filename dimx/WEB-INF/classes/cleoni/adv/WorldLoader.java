package cleoni.adv;

/**
 * World Loader and parser (DXW format) for the DimensioneX engine
 * Creation date: (03/01/2003)
 * 03/02/2008 use Utils.fetchIncludes for World loading.
 * @author: Cristiano Leoni
 */
import java.util.Vector;
import java.io.*;
import java.net.*;
/** Parser capable of reading a DXW file and building a WORLD */
public class WorldLoader extends DimxParser {
    String fileName = null;
    protected static final String[] setWorldAttrs = {"NAME", "VERSION","AUTHOR",
    "AUTHOREMAIL","HELP","IMAGESFOLDER","IMAGESFOLDER_PUBLIC","IMAGESFOLDER_LOCAL","LOGOSRC","COUNTERHTML",
    "INTERPHONE","MSGLISTSIZE","COMPASS","EASYNAV","ATTRLIST",
    "SCENEWIDTH","SCENEHEIGHT","SITE","SKINS","SAVEGAME_PERSISTENCE","CLUSTER","ENCODING","MUTING","CUSTCMDPROC"};
    private static final String[] setFaces = {"N","S","E","W","U","D"};
    private static final String[] setRoomAttrs = {"NAME","DESCRIPTION","DEFAULT","IMAGE","PANEL","ATTRLIST"};
    private static final String[] setLinkAttrs = {"NAME","DESCRIPTION","IMAGE","ICON","ATTRLIST","SHOW"};
    private static final String[] setCharacterParams = {"NAME","DESCRIPTION","IMAGE","ICON","ATTRLIST","POSITION","ACCEPTS","SHOW"};
    private static final String[] setItemParams = {"NAME","DESCRIPTION","IMAGE","INNER","ZOOMIMAGE","ICON","ATTRLIST","POSITION","CAPACITY","VOLUME","SHOW","PANEL","TYPE"};
    private static final String[] setPanelParams = {"BUTTON", "TEXTBOX", "CR", "CMD","LABEL","DROPDOWN","MAP","DELETE"};
    private static final String[] setGuiTags = {"PANEL","SCREEN","SCENE","LOGOSRC","SKINS","MSGLISTSIZE","COMPASS","VIEW","PAGE","EASYNAV","SHOW","MAP","HOOKS"};
    
    private Messages msgs;
    private String skinslist = "";
    private String systemDir = null;
    private String encoding = "UTF-8";
    
    public WorldLoader(Logger aLogger, Messages aMsgs) {
        super(null,null,0,0);
        logger = aLogger;
        msgs = aMsgs;
    }
    public WorldLoader(World aWorld, String aBuf) {
        super(null,null,0,0);
        world = aWorld;
        buf = aBuf;
        startLine = 1;
        logger = world.logger;
        msgs = world.msgs;
    }
    public World load(multiplayer server, String aSystemDir, String aFileName, String serverType)
    throws DimxException, Exception {
        // Loads a world DXW file
        fileName = aFileName;
        systemDir = aSystemDir;
        if ((!fileName.startsWith("http:")) && (!fileName.startsWith("https:"))) fileName = systemDir + fileName;
        try {
            buf = Utils.fetchIncludes(fileName,encoding);
            startLine = 1;
            parseWorld(server, serverType);
        } catch (DimxException e) {
            if (e.getMessage().equals("Wrong Encoding")) {
                Logger.echo("Wrong encoding: " + encoding + " - trying to reload as: ANSI ASCII");
                encoding = "ANSI"; // Try alternate
                this.reset();
                buf = Utils.fetchIncludes(fileName,encoding);
                startLine = 1;
                parseWorld(server, serverType);
            } else {
                throw new DimxException(e.getMessage());
            }
        }
        world.worldFile = fileName;
        
        Skin skin;
        SkinLoader skl = new SkinLoader(world);
        
        Vector skv = Utils.stringSplit(skinslist,",");
        for (int i=0; i<skv.size(); i++) {
            String skpath = (String) skv.elementAt(i);
            
            if (skpath.equalsIgnoreCase("default")) {
                logger.debug("Creating default skin");
                skin = new Skin(Utils.getParentFolder(world.imagesFolder) + "skins/");
            } else {
                if (!(skpath.startsWith("https://") || skpath.startsWith("http://"))) { 
                    String skname = skpath;
                    int x = skpath.indexOf('.');
                    if (x > 0) {
                        skname = skpath.substring(0, x);
                    }
                    skpath = Utils.getParentFolder(world.imagesFolder) + "skins/"+skname+"/"+skpath;
                }
                logger.debug("Loading skin: " + skpath);
                skin = skl.load(skpath);
                // last attempt after failure
                if ((skin==null)&&(!(skpath.startsWith("https://") || skpath.startsWith("http://")))) {
                    String part1 = server.getServletContext().getRealPath("/");
                    String part2 = skpath;
                    // Horrible hack to avoid double "/dimx" in local server load
                    if (((part1.indexOf("/dimx")>0) || (part1.indexOf("\\dimx")>0)) && part2.indexOf("dimx/")>0) {
                        part2 = Utils.stringReplace(part2, "dimx/", "", false);
                    }
                    skpath =  part1 + part2.substring(1);
                    logger.debug("Retrying Skin: " + skpath);
                    skin = skl.load(skpath);
                }
            }
            if (skin != null) // Load succeeded
                world.skins.put(skin.id,skin);
        }
        
        if (world.skins.size() == 0) {
            // Create default skin anyway
            logger.debug("Creating default skin");
            skin = new Skin(Utils.getParentFolder(world.imagesFolder) + "skins/");
            world.skins.put(skin.id,skin);
        }

        world.fixSkins(world.getPanel("connect")); // Skins are now available

        return world;
    }
    private Ctrl parseButton(Panel panel) throws DimxException {
        // Parses a Button line body - the IMAGE keyword is supposed to have been consumed already
        Token t = null;
        Ctrl b = null;
        String name = null;
        String description = null;
        String event = null;
        String eventModel = null;
        Image im = null;
        String icon = null;
        
        parseToken("BUTTON");
        String id = nextToken().strVal(); // Eat id
        
        t = lookupToken();
        if (t.strVal().equals(",")) { // Complete definition
            eat();
            name = nextToken().strVal();
            parseToken(",");
            description = nextToken().strVal();
            parseToken(",");
            event = nextToken().strVal();
            
            if (Utils.isIn(event,setPanelParams) || event.equalsIgnoreCase("PANEL") || event.equalsIgnoreCase("END_GUI")) {
                throw new DimxException("Event identifier expected");
            }
            
            t = lookupToken();
            if (t.strVal().equals(",")) { // Event model specified
                eat();
                eventModel = nextToken().strVal();
                t = lookupToken();
            }
            
            if (t.strVal().equalsIgnoreCase("IMAGE")) {
                parseToken("IMAGE");
                Object[] res = parseImageBody(Const.NO_SHOWAREA);
                im = (Image) res[0];
            } else if (t.strVal().equalsIgnoreCase("ICON")) {
                parseToken("ICON");
                icon = readToCR();
                icon = Utils.absolutizeUrl(icon, world.imagesFolder);
            } else
                world.logger.debug("-" + t.strVal() + "-");
            
            int type = Const.CTRL_BUTTON;
            if (im != null) { // Image button
                type = Const.CTRL_IMAGEBUTTON;
            }
            
            b = new Ctrl(panel,type,id,name,description,event,eventModel,im,icon);
        } else { // id Only - copy from Default panel
            Panel defp = (Panel) world.getPanel("default");
            
            b = (Ctrl) ((Panel) world.getPanel("default")).buttons.getIC(id);
            if (b == null) {
                throw new DimxException("Default panel misses command: >" + id + "<");
            }
            readToCR();
            t = lookupToken();
            if (t.strVal().equalsIgnoreCase("ICON")) {
                parseToken("ICON");
                icon = readToCR();
                icon = Utils.absolutizeUrl(icon, world.imagesFolder);
                b.icon = icon;
            }
            panel.buttons.put(b.id,b);
        }
        
        return b;
    }
    
    private Ctrl parseCMD(Panel panel) throws DimxException {
        // Parses a CMD line body
        Token t = null;
        Ctrl b = null;
        String name = null;
        String description = null;
        String event = null;
        String eventModel = null;
        Image im = null;
        String icon = null;
        
        parseToken("CMD");
        String id = nextToken().strVal(); // Eat id
        
        t = lookupToken();
        if (t.strVal().equals(",")) { // Complete definition
            eat();
            name = nextToken().strVal();
            parseToken(",");
            description = nextToken().strVal();
            parseToken(",");
            event = nextToken().strVal();
            
            if (Utils.isIn(event,setPanelParams) || event.equalsIgnoreCase("PANEL") || event.equalsIgnoreCase("END_GUI")) {
                throw new DimxException("Event identifier expected");
            }
            
            t = lookupToken();
            if (t.strVal().equals(",")) { // Event model specified
                eat();
                eventModel = nextToken().strVal();
                t = lookupToken();
            }
            
            world.logger.debug("-" + t.strVal() + "-");
            
            int type = Const.CTRL_GHOST;
            
            b = new Ctrl(panel,type,id,name,description,event,eventModel,im,icon);
        } else { // id Only - copy from Default panel
            Panel defp = (Panel) world.getPanel("default");
            
            b = (Ctrl) ((Panel) world.getPanel("default")).buttons.getIC(id);
            if (b == null) {
                throw new DimxException("Default panel misses command: >" + id + "<");
            }
            readToCR();
            panel.buttons.put(b.id,b);
        }
        
        return b;
    }    

    /** Parses a DROPDOWN line body
     * @param panel
     * @throws DimxException
     * @return
     */    
    private Ctrl parseDropdown(Panel panel) throws DimxException {
        Token t = null;
        Ctrl b = null;
        String setex = null;
        String defaultex = null;
        String workingModel = null;
        Image im = null;
        String icon = null;
        
        parseToken("DROPDOWN");
        String id = nextToken().strVal(); // Eat id
        
        parseToken(",");

        setex = nextToken().strVal();

        parseToken(",");

        Token tt = nextToken();
        defaultex = tt.strVal();
        //if (tt.isLiteral()) defaultex = "\""+defaultex+"\"";

        t = lookupToken();
        if (t.strVal().equals(",")) { // Event model specified
            eat();
            workingModel = nextToken().strVal();
            if (workingModel.equalsIgnoreCase("AUTOSUBMIT")) {
                // OK
            } else {
                throw new DimxException("Un-supported working model: " + workingModel);
            }
            //t = lookupToken();
        }


        int type = Const.CTRL_DROPDOWN;

        b = new Ctrl(panel,type,id,defaultex,null,setex,workingModel,null);
        return b;
    }
    
    private void parseArray() throws DimxException {
        String name = "*";
        String list = "";
        
        parseToken("ARRAY");
        
        // Parse ID
        String id = nextToken().strVal();
        if (world.getObject(id) != null) throw new DimxException("Duplicate definition of: " + id);
        
        list = readToCR();
        
        //logger.debug("Building set: " + id);
        DictSorted mySet = new DictSorted();
        Vector ids = Utils.stringSplit(list,",");
        for (int i=0; i<ids.size(); i++) {
            String fullId = (String) ids.elementAt(i);
            mySet.put(Utils.leadingZeroes(i+1,Const.ARRAYMAX),new Token(fullId));
        }
        world.varGet(id,Const.GETREF).assign(new Token(mySet),world);
    }
    
    private void parseCharacter() throws DimxException {
        String name = "*";
        String desc = "";
        String icon = null;
        String attrList = world.charactersDefaultAttrlist;
        String acceptList = "";
        Dict images = new Dict();
        boolean isdefault = false;
        String position = null;
        int capacity = 5;
        Dict showp = new Dict();	// Display mode
        showp.put("x","-1");
        showp.put("y","-1");
        showp.put("mode",Utils.cStr(Const.ONSCREEN_IMAGE));
        
        parseToken("CHARACTER");
        
        // Parse ID
        String id = nextToken().strVal();
        if (world.getObject(id) != null) throw new DimxException("Duplicate definition of: " + id);
        
        if (lookupToken().strVal().equalsIgnoreCase("DEFAULT")) {
            eat_extended();
            isdefault = true;
        }
        getLine(); // Eats CR after the id
        
        
        Token t = lookupToken();
        String s = t.strVal();
        while (Utils.isIn(s,setCharacterParams)) {
            eat_extended();
            if (s.equalsIgnoreCase("NAME")) {
                name = readToCR();
            } else if (s.equalsIgnoreCase("ATTRLIST")) {
                if (attrList != null && !attrList.equals("")) {
                    // If default attributes present, add the custom ones
                    attrList = attrList + "," + readToCR();
                } else { // Set them
                    attrList = readToCR();
                }
            } else if (s.equalsIgnoreCase("ACCEPTS")) {
                acceptList = readToCR();
            } else if (s.equalsIgnoreCase("ICON")) {
                icon = readToCR();
            } else if (s.equalsIgnoreCase("DESCRIPTION")) {
                desc = readToCR();
            } else if (s.equalsIgnoreCase("POSITION")) {
                position = readToCR();
            } else if (s.equalsIgnoreCase("IMAGE")) {
                Object[] res = parseImageBody(Const.NO_SHOWAREA);
                images.put((String) res[1],(Image) res[0]);
            } else if (s.equalsIgnoreCase("SHOW")) {
                parseShowMode(showp);
            }
            t = lookupToken();
            s = t.strVal();
        }
        
        //logger.debug("Building character: " + id);
        attrList = "showx=" + Utils.cInt(showp.get("x")) + ",showy=" + Utils.cInt(showp.get("y")) + "," + attrList;
        Character c = new Character(world,name,id,desc,icon,capacity,attrList,position,acceptList);
        
        // add defined images
        for (int j=0; j < images.size(); j++) {
            c.setImage(images.keyAt(j), (Image) images.elementAt(j));
        }
        
        c.showmode = new Token(showp.getS("mode"));
        c.showfor = new Token(showp.getS("for"));
        if (images.size()==0) c.showmode = new Token(Const.OFFSCREEN);
        
        world.addCharacter(c);
        if (isdefault) {
            world.defaultCharacter = c;
        }
    }
    private void parseCharacters() throws DimxException {
        
        parseToken("CHARACTERS");
        Token t = lookupToken();
        String s = t.strVal();
        if (s.equalsIgnoreCase("ATTRLIST")) {
            parseToken("ATTRLIST");
            world.charactersDefaultAttrlist = readToCR();
            t = lookupToken();
            s = t.strVal();
        }
        
        while (s.equalsIgnoreCase("CHARACTER")) {
            parseCharacter();
            t = lookupToken();
            s = t.strVal();
        }
        parseToken("END_CHARACTERS");
        
    }
    
    private void parseCoords(Dict d) throws DimxException {
        // Parses a Coordinates pair
        
        d.put("x",evalExpression(lookupElement(setTabSpace),0).strVal());
        parseToken(",");
        d.put("y",evalExpression(lookupElement(setTabSpace),0).strVal());
        if (lookupToken().strVal().equalsIgnoreCase("FOR")) {
            eat();
            String url = readToCR().trim();
            d.put("for",url);
            d.put("forRoom",world.getRoomFromImageUrl(url));
        }
        
    }
    private void parseEvents(String startToken) throws NestedException {
        try {
            parseToken(startToken);
            parseCR();
            int startingLine = currLine;
            
            StringBuffer sb = new StringBuffer();
            
            String s = lookupToken().strVal();
            while (!(s.equalsIgnoreCase("END_EVENTS") || s.equalsIgnoreCase("END_SCRIPTS"))) {
                String aline = readToCR();
                sb.append(aline);
                sb.append("\n");
                s = lookupToken().strVal();
            }
            
            SmallBasicLoader myLoader = new SmallBasicLoader(world,sb.toString(),fileName, startingLine, encoding);
            myLoader.load(systemDir,false);
            
            if (startToken.equalsIgnoreCase("SCRIPTS"))
                parseToken("END_SCRIPTS");
            else
                parseToken("END_EVENTS");
            
        } catch (NestedException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new NestedException("Error parsing EVENTS section: " + e.getMessage() );
        }
    }
    private void parseGUI() throws DimxException {
        
        parseToken("GUI");
        Token t = lookupToken();
        String s = t.strVal();
        
        while (Utils.isIn(s,setGuiTags)) {
            if (s.equalsIgnoreCase("PANEL")) {
                parsePanel();
            } else if (s.equalsIgnoreCase("SCREEN")) {
                parseScreen();
            } else if (s.equalsIgnoreCase("SCENE")) {
                parseScene();
            } else if (s.equalsIgnoreCase("VIEW")) {
                world.logger.debug("WARNING: VIEW keyword is no more supported - Please replace with PAGE");
                parsePage("VIEW");
            } else if (s.equalsIgnoreCase("PAGE")) {
                parsePage("PAGE");
            } else {
                // GUI attribute
                eat_extended(); // Eat keyword
                
                if (s.equalsIgnoreCase("LOGOSRC")) {
                    String logosrc = readToCR();
                    world.logoSrc = Utils.absolutizeUrl(logosrc,world.imagesFolder);
                } else if (s.equalsIgnoreCase("SKINS")) {
                    skinslist = readToCR();
                } else if (s.equalsIgnoreCase("MSGLISTSIZE")) {
                    int	msglistsize = 3;
                    msglistsize = Utils.cInt(nextToken().numVal());
                    getLine();
                    world.msgListSize = msglistsize;
                } else if (s.equalsIgnoreCase("COMPASS")) {
                    String compass = "0";
                    compass = readToCR();
                    world.compass = (compass.equalsIgnoreCase("true") || compass.equals("1"));
                } else if (s.equalsIgnoreCase("MAP")) {
                    world.map = Utils.absolutizeUrl(readToCR(),world.imagesFolder);
                } else if (s.equalsIgnoreCase("EASYNAV")) {
                    String easynav = "1";
                    easynav = readToCR();
                    logger.debug("Warning: EASYNAV tag is no more supported. Ignoring it.");
                } else if (s.equalsIgnoreCase("SHOW")) {
                    parseToken("PROPERTIES");
                    world.showproperties = Utils.stringSplit(readToCR(),",");
                } else if (s.equalsIgnoreCase("HOOKS")) {
                    world.hooks = Utils.string2set(readToCR(),",","=",false);
                } else {
                    throw new DimxException("Unhandled tag: " + s);
                }
            }
            t = lookupToken();
            s = t.strVal();
        }
        parseToken("END_GUI");
        
    }
    
    public Image parseImage() throws DimxException {
        parseToken("IMAGE");
        Object[] res = parseImageBody(Const.NO_SHOWAREA);
        //((String) res[1],(Image) res[0]);
        return (Image) res[0];
    }

    
/*
 *  Parses an Image line body - the IMAGE keyword is supposed to have been consumed already
 * showAreaAccepted must be true if the SHOWAREA clause should be accepted
 * element 0 is the image element 1 is the facing
 */
    public Object[] parseImageBody(boolean showAreaAccepted) throws DimxException {
        
        Object[] res = new Object[2];
        String face = "N"; // North by default
        String url = "";
        int width = 0;
        int height = 0;
        Dict showp = new Dict();	// Display mode
        showp.put("x1",new Token(-1));
        showp.put("x2",new Token(-1));
        showp.put("y",new Token(-1));
        
        Token t = lookupToken();
        String s = t.strVal();
        
        // Optional - FACE
        if (Utils.isIn(s,setFaces)) {
            // it's a face
            face = nextToken().strVal();
            t = lookupToken();
            s = t.strVal();
        }
        
        // Optional - DIMENSIONS
        if (Utils.instrCount(s,'.')==0) {
            // may be
            Vector v = Utils.stringSplit(s,"x");
            if (v.size() == 2) {
                width = Utils.cInt(v.elementAt(0));
                height = Utils.cInt(v.elementAt(1));
                eat();
                t = lookupToken();
                s = t.strVal();
            }
        }
        
        if (s.equalsIgnoreCase("SHOWAREA")) {
            if (showAreaAccepted) {
                parseShowArea(showp);
            } else {
                throw new DimxException("SHOWAREA is allowed only for ROOM objects");
            }
        }
        
        url = readToCR();
        url.trim(); // Cut final spaces
        
        Image i = new Image(Utils.absolutizeUrl(url,world.imagesFolder),width,height);
        // Define show area
        i.baseline = Utils.cInt(((Token) showp.get("y")).numVal());
        i.showareax1 = Utils.cInt(((Token) showp.get("x1")).numVal());
        i.showareax2 = Utils.cInt(((Token) showp.get("x2")).numVal());
        if (i.showareax2 <= i.showareax1) i.showareax2 = -1;
        
        res[0] = i;
        res[1] = face;
        return res;
    }
    
    /**
     * @param isVehicle
     * @throws DimxException
     */
    private void parseItem(boolean isVehicle) throws DimxException {
        PeopleContainer item = null;
        String desc = "";
        String icon = null;
        String type = null;
        String attrList = "";
        Dict images = new Dict();
        Image zoomImage = null;
        Image innerImage = null;
        String position = null;
        String panelId = null;
        int capacity = 5;
        int volume = 1;
        Dict showp = new Dict();	// Display mode
        showp.put("x","-1");
        showp.put("y","-1");
        showp.put("mode",Utils.cStr(Const.OFFSCREEN));
        
        if (isVehicle) {
            parseToken("VEHICLE");
            capacity = 10;
            volume = 10;
        } else
            parseToken("ITEM");
        
        // Parse ID
        String id = nextToken().strVal();
        if (world.getObject(id) != null) throw new DimxException("Duplicate definition of: " + id);
        
        getLine(); // Eats CR after the 1st line
        
        String name = "a " + id;
        
        
        Token t = lookupToken();
        String s = t.strVal();
        while (Utils.isIn(s,setItemParams)) {
            eat_extended();
            if (s.equalsIgnoreCase("NAME")) {
                name = readToCR();
            } else if (s.equalsIgnoreCase("ATTRLIST")) {
                attrList = readToCR();
            } else if (s.equalsIgnoreCase("ICON")) {
                icon = readToCR();
            } else if (s.equalsIgnoreCase("TYPE")) {
                type = readToCR();
            } else if (s.equalsIgnoreCase("DESCRIPTION")) {
                desc = readToCR();
            } else if (s.equalsIgnoreCase("POSITION")) {
                position = readToCR();
            } else if (s.equalsIgnoreCase("CAPACITY")) {
                capacity = Utils.cInt(nextToken().numVal()); readToCR();
            } else if (s.equalsIgnoreCase("VOLUME")) {
                volume = Utils.cInt(nextToken().numVal()); readToCR();
            } else if (s.equalsIgnoreCase("IMAGE")) {
                Object[] res = parseImageBody(Const.NO_SHOWAREA);
                images.put((String) res[1],(Image) res[0]);
            } else if (s.equalsIgnoreCase("ZOOMIMAGE")) {
                Object[] res = parseImageBody(Const.NO_SHOWAREA);
                zoomImage = (Image) res[0];
            } else if (s.equalsIgnoreCase("INNER")) {
                parseToken("IMAGE");
                Object[] res = parseImageBody(Const.NO_SHOWAREA);
                innerImage = (Image) res[0];
            } else if (s.equalsIgnoreCase("PANEL")) {
                panelId = readToCR();
            } else if (s.equalsIgnoreCase("SHOW")) {
                parseShowMode(showp);
            }
            t = lookupToken();
            s = t.strVal();
        }
        
        //logger.debug("Building item: " + id);
        attrList = "showx=" + Utils.cInt(showp.get("x")) + ",showy=" + Utils.cInt(showp.get("y")) + "," + attrList;
        if (type != null) attrList = attrList + ",type=" + type;
        if (isVehicle) {
            item = new Vehicle(world,name,id,desc,icon,null,capacity,volume,position);
            item.varGet("hideable",Const.GETREF).assign(new Token(0),world);
            item.varsSet(attrList);
        } else {
            item = new Item(world,name,id,desc,icon,attrList,capacity,volume,position);
        }
        // add defined images
        for (int j=0; j < images.size(); j++) {
            item.setImage(images.keyAt(j), (Image) images.elementAt(j));
        }
        
        item.showmode = new Token(showp.getS("mode"));
        item.showfor = new Token(showp.getS("for"));
        if (innerImage != null) item.varGet("innerImage",Const.GETREF).assign(new Token(innerImage),world);
        if (zoomImage != null) item.varGet("zoomImage",Const.GETREF).assign(new Token(zoomImage),world);
        
        if (images.size()==0) item.showmode = new Token(Const.OFFSCREEN); // Correct if needed
        
        if (panelId != null) item.setPanel(panelId);
        
        world.addItem(item,Const.KEEP_ID);
    }
    private void parseItems() throws DimxException {
        
        parseToken("ITEMS");
        Token t = lookupToken();
        String s = t.strVal();
        
        while (s.equalsIgnoreCase("ITEM") || s.equalsIgnoreCase("VEHICLE")) {
            parseItem(s.equalsIgnoreCase("VEHICLE"));
            t = lookupToken();
            s = t.strVal();
        }
        parseToken("END_ITEMS");
        
    }
    private void parseLink(String s) throws DimxException {
        String name = "*";
        String desc = "";
        String direction = "";
        String icon = null;
        String attrList = "";
        Dict images = new Dict();
        boolean isdefault = false;
        Room fromRoom, toRoom;
        boolean bidirect = s.equalsIgnoreCase("LINK");
        Dict showp = new Dict();	// Display mode
        showp.put("x","-1");
        showp.put("y","-1");
        showp.put("mode",Utils.cStr(Const.OFFSCREEN));
        
        parseToken(s);
        
        // Parse ID
        String id = nextToken().strVal();
        if (world.getObject(id) != null) throw new DimxException("Duplicate definition of: " + id);
        
        String p1 = nextToken().strVal();
        fromRoom = world.getRoom(p1);
        if (fromRoom == null) throw new DimxException("Undefined room: " + p1);
        parseToken("-");
        String p2 = nextToken().strVal();
        toRoom = world.getRoom(p2);
        if (toRoom == null) throw new DimxException("Undefined room: " + p2);
        
        if (Utils.isIn(lookupToken().strVal(),setFaces)) {
            direction = nextToken().strVal();
        }
        
        //getLine(); // Eats CR after the 1st line
        
        
        Token t = lookupToken();
        s = t.strVal();
        while (Utils.isIn(s,setLinkAttrs)) {
            eat_extended();
            if (s.equalsIgnoreCase("NAME")) {
                name = readToCR();
            } else if (s.equalsIgnoreCase("ATTRLIST")) {
                attrList = readToCR();
            } else if (s.equalsIgnoreCase("ICON")) {
                icon = readToCR();
            } else if (s.equalsIgnoreCase("DESCRIPTION")) {
                desc = readToCR();
            } else if (s.equalsIgnoreCase("IMAGE")) {
                Object[] res = parseImageBody(Const.NO_SHOWAREA);
                images.put((String) res[1],(Image) res[0]);
            } else if (s.equalsIgnoreCase("SHOW")) {
                parseShowMode(showp);
            }
            t = lookupToken();
            s = t.strVal();
        }
        
        //logger.debug("Building link " + id);
        attrList = "showx=" + Utils.cInt(showp.get("x")) + ",showy=" + Utils.cInt(showp.get("y")) + "," + attrList;
        Link w = new Link(world,name,id,fromRoom,toRoom,bidirect,direction,desc,icon,attrList);
        
        // add defined images
        for (int j=0; j < images.size(); j++) {
            w.setImage(images.keyAt(j), (Image) images.elementAt(j));
        }
        w.showmode = new Token(showp.getS("mode"));
        w.showfor = new Token(showp.getS("for"));
        
        world.addLink(w, false); // false=don't redefine ID
    }
    private void parseLinks() throws DimxException {
        parseToken("LINKS");
        Token t = lookupToken();
        String s = t.strVal();
        while (s.equalsIgnoreCase("LINK") || s.equalsIgnoreCase("MLINK")) {
            parseLink(s);
            t = lookupToken();
            s = t.strVal();
        }
        parseToken("END_LINKS");
        
    }
    private void parsePanel() throws DimxException {
        Panel parentPanel	= null;
        
        parseToken("PANEL");
        
        // Parse ID
        String id = nextToken().strVal();
        if (world.getPanel(id) != null) world.logger.debug("Warning: Duplicate definition of panel: " + id);
        
        if (lookupToken().strVal().equalsIgnoreCase("VERSION")) {
            eat();
            parseToken("OF");
            String parentid = nextToken().strVal();
            parentPanel = (Panel) world.getPanel(parentid);
            if (parentPanel == null) {
                throw new DimxException("Unexistent panel: " + parentid);
            }
        }
        getLine(); // Eats CR after the id
        Panel panel = new Panel(world,id);
        
        if (parentPanel != null) { // Import from parent
            for (int i=0; i < parentPanel.buttons.size(); i++) {
                panel.buttons.put(parentPanel.buttons.keyAt(i),(Ctrl) parentPanel.buttons.elementAt(i));
            }
        }
        
        Token t = lookupToken();
        String s = t.strVal();
        while (Utils.isIn(s,setPanelParams)) {
            
            if (s.equalsIgnoreCase("BUTTON")) {
                parseButton(panel);
            } else if (s.equalsIgnoreCase("CMD")) {
                parseCMD(panel);
            } else if (s.equalsIgnoreCase("TEXTBOX")){
                eat_extended();
                String txtid = "txtBox";
                t = lookupTokenCRsens();
                if (!t.strVal().equals(CR)) txtid = nextToken().strVal();
                parseCR();
                new Ctrl(panel,Const.CTRL_TEXTBOX,txtid,null,null,null,null,null);
            } else if (s.equalsIgnoreCase("CR")){
                readToCR();
                new Ctrl(panel,"-");
            } else if (s.equalsIgnoreCase("LABEL")){
                eat_extended();
                t = nextToken();
                readToCR();
                new Ctrl(panel,Const.CTRL_LABEL,null,t.strVal(),null,null,null,null);
            } else if (s.equalsIgnoreCase("DROPDOWN")) {
                parseDropdown(panel);
            } else if (s.equalsIgnoreCase("MAP")) {
                readToCR();
                new Ctrl(panel,Const.CTRL_MAP,null,"MAP",null,null,null,null);
            } else if (s.equalsIgnoreCase("DELETE")) {
                parseToken("DELETE");
                String ctrid = nextToken().strVal();
                parseCR();
                panel.buttons.remove(ctrid);
            } else {
                throw new DimxException("Internal error: missing panel-parsing code");
            }
            
            t = lookupToken();
            s = t.strVal();
        }
        
        world.definePanel(panel);
    }
    
    private void parseRoom() throws DimxException {
        String name = "Unnamed room";
        String desc = "";
        String icon = "";
        String panelId = null;
        String attrList = null;
        Dict images = new Dict();
        boolean isdefault = false;
        
        parseToken("ROOM");
        
        // Parse ID
        String id = nextToken().strVal();
        if (world.getObject(id) != null) throw new DimxException("Duplicate definition of: " + id);
        
        if (lookupToken().strVal().equalsIgnoreCase("DEFAULT")) {
            eat_extended();
            isdefault = true;
        }
        
        Token t = lookupToken();
        String s = t.strVal();
        while (Utils.isIn(s,setRoomAttrs)) {
            eat_extended();
            if (s.equalsIgnoreCase("NAME")) {
                name = readToCR();
            } else if (s.equalsIgnoreCase("DESCRIPTION")) {
                desc = readToCR();
            } else if (s.equalsIgnoreCase("IMAGE")) {
                Object[] res = parseImageBody(Const.ALLOW_SHOWAREA);
                Image i = (Image) res[0];
                // Patch dimensions if needed
                if (i.getHeight() == 0) i.height = new Token(world.getSceneHeight());
                if (i.getWidth() == 0) i.width = new Token(world.getSceneWidth());
                images.put((String) res[1],i);
            } else if (s.equalsIgnoreCase("ATTRLIST")) {
                attrList = readToCR();
            } else if (s.equalsIgnoreCase("PANEL")) {
                panelId = readToCR();
            } else {
                throw new DimxException("Unexpected attribute: " + s);
            }
            t = lookupToken();
            s = t.strVal();
        }
        
        Room r = new Room(world,name, id, desc,icon,attrList);
        if (panelId != null) {
            r.setPanel(panelId);
        }
        // add defined images
        for (int j=0; j < images.size(); j++) {
            r.setImage(images.keyAt(j), (Image) images.elementAt(j));
        }
        world.addRoom(r, isdefault);
    }
    private void parseRooms() throws DimxException {
        parseToken("ROOMS");
        while (lookupToken().strVal().equalsIgnoreCase("ROOM")) {
            parseRoom();
        }
        parseToken("END_ROOMS");
        
    }
    private void parseSet() throws DimxException {
        String name = "*";
        String list = "";
        
        parseToken("SET");
        
        // Parse ID
        String id = nextToken().strVal();
        if (world.getObject(id) != null) throw new DimxException("Duplicate definition of: " + id);
        
        list = readToCR();
        
        //logger.debug("Building set: " + id);
        DictSorted mySet = new DictSorted();
        Vector ids = Utils.stringSplit(list,",");
        for (int i=0; i<ids.size(); i++) {
            String fullId = (String) ids.elementAt(i);
            
            AdvObject obj = (AdvObject) world.getObject(fullId);
            if (obj != null) {
                mySet.put(fullId,new Token(obj));
            } else {
                throw new DimxException("Undefined object: " + fullId + " in definition of SET " + id + ". Please check or define an ARRAY instead of a SET");
            }
        }
        world.varGet(id,Const.GETREF).assign(new Token(mySet),world);
    }
    
    private void parseSets() throws DimxException {
        
        parseToken("SETS");
        Token t = lookupToken();
        String s = t.strVal();
        
        while (s.equalsIgnoreCase("SET") || s.equalsIgnoreCase("ARRAY")) {
            if (s.equalsIgnoreCase("SET")) {
                parseSet();
            } else {
                parseArray();
            }
            t = lookupToken();
            s = t.strVal();
        }
        parseToken("END_SETS");
        
    }
    private void parseShowArea(Dict d) throws DimxException {
        // Parses a SHOWAREA specification
        
        parseToken("SHOWAREA");
        
        Token x1 = evalExpression(lookupElement(setTabSpace),0);
        if (x1 != null) d.put("x1",x1);
        
        parseToken(",");
        
        Token x2 = evalExpression(lookupElement(setTabSpace),0);
        if (x2 != null) d.put("x2",x2);
        
        parseToken(",");
        
        Token y = evalExpression(lookupElement(setTabSpace),0);
        if (y != null) d.put("y",y);
        
        //logger.debug("SHOWAREA is " + d.get("x1") + " to " + d.get("x2") + ", baseline " + d.get("y"));
        
    }
    private void parseShowMode(Dict d) throws DimxException {
        // Parses a SHOW mode line
        
        Token t = lookupToken();
        String s = t.strVal();
        
        if (s.equalsIgnoreCase("ONSCREEN")) {
            d.put("mode",Utils.cStr(Const.ONSCREEN_IMAGE));
            eat();
            Token x = lookupElement(setTabSpace);
            if (x.strVal().equals("\n")) {
                readToCR();
            } else {
                parseCoords(d);
            }
        } else if (s.equalsIgnoreCase("ICON")) {
            d.put("mode",Utils.cStr(Const.ONSCREEN_ICON));
            eat();
            Token x = lookupElement(setTabSpace);
            if (x.strVal().equals("\n")) {
                readToCR();
            } else {
                parseCoords(d);
            }
        } else if (s.equalsIgnoreCase("OFFSCREEN")) {
            d.put("mode",Utils.cStr(Const.OFFSCREEN));
            eat_extended();
        } else {
            throw new DimxException("Unrecognized SHOW mode: " + s);
        }
        //logger.debug("Display mode is " + d.get("mode") + " at " + d.get("x") + "," + d.get("y"));
        
    }
    private void parseWorld(multiplayer server, String serverType) throws DimxException {
        currLine = startLine;
        try {
            String name = "Unnamed world";
            String version = "";
            int sceneWidth = 350;
            int sceneHeight = 235;
            String imagesFolder = "/unspecified/images/folder";
            String author = "";
            String authoremail = "";
            String helpurl = "http://www.dimensionex.net/en/quick_start.htm";
            String logosrc = "";
            String counterhtml = "";
            String cluster = "";
            int	msglistsize = 3;
            int	savegamePersistence = 2;
            String interphone = "1";
            String easynav = "1";
            String compass = "0";
            String site = "";
            String muting = "1";
            String custCmdProc = "";
            String attrlist = "";
            
            parseToken("WORLD");
            
            
            Token t = lookupToken();
            String s = t.strVal();
            while (Utils.isIn(s,setWorldAttrs)) {
                eat_extended();
                if (s.equalsIgnoreCase("NAME")) {
                    name = readToCR().trim();
                } else if (s.equalsIgnoreCase("IMAGESFOLDER")) {
                    imagesFolder = readToCR();
                } else if (s.equalsIgnoreCase("IMAGESFOLDER_LOCAL")) {
                    String tmp = readToCR();
                    if (serverType.equalsIgnoreCase("local")) imagesFolder = tmp;
                } else if (s.equalsIgnoreCase("IMAGESFOLDER_PUBLIC")) {
                    String tmp = readToCR();
                    if (serverType.equalsIgnoreCase("public")) imagesFolder = tmp;
                } else if (s.equalsIgnoreCase("VERSION")) {
                    version = readToCR();
                } else if (s.equalsIgnoreCase("AUTHOR")) {
                    author = readToCR();
                } else if (s.equalsIgnoreCase("AUTHOREMAIL")) {
                    authoremail = readToCR();
                } else if (s.equalsIgnoreCase("HELP")) {
                    helpurl = readToCR();
                } else if (s.equalsIgnoreCase("LOGOSRC")) {
                    logosrc = readToCR();
                    logger.debug("Warning: use of LOGOSRC inside WORLD is deprecated and will not be supported in future releases. Please move it in the GUI section. See the updated Developer's Reference for details.");
                } else if (s.equalsIgnoreCase("COUNTERHTML")) {
                    String tmp = readToCR();
                    if (serverType.equalsIgnoreCase("public")) {
                        counterhtml = tmp;
                    } else {
                        logger.debug("Ignoring COUNTERHTML tag because this is a local server.");
                    }
                } else if (s.equalsIgnoreCase("INTERPHONE")) {
                    interphone = readToCR();
                } else if (s.equalsIgnoreCase("MUTING")) {
                    muting = readToCR();
                } else if (s.equalsIgnoreCase("MSGLISTSIZE")) {
                    msglistsize = Utils.cInt(nextToken().numVal());
                    getLine();
                    logger.debug("Warning: use of MSGLISTSIZE inside WORLD is deprecated and will not be supported in future releases. Please move it in the GUI section. See the updated Developer's Reference for details.");
                } else if (s.equalsIgnoreCase("COMPASS")) {
                    compass = readToCR();
                    logger.debug("Warning: use of COMPASS inside WORLD is deprecated and will not be supported in future releases. Please move it in the GUI section. See the updated Developer's Reference for details.");
                } else if (s.equalsIgnoreCase("EASYNAV")) {
                    easynav = readToCR();
                    logger.debug("Warning: use of EASYNAV inside WORLD is deprecated and will not be supported in future releases. Please move it in the GUI section. See the updated Developer's Reference for details.");
                } else if (s.equalsIgnoreCase("SCENEHEIGHT")) {
                    sceneHeight = Utils.cInt(nextToken().numVal());
                    getLine();
                    logger.debug("Warning: use of SCENEHEIGHT tag is deprecated and will not be supported in future releases. Please use SCENE SIZE in the GUI section. See the updated Developer's Reference for details.");
                } else if (s.equalsIgnoreCase("SCENEWIDTH")) {
                    sceneWidth = Utils.cInt(nextToken().numVal());
                    getLine();
                    logger.debug("Warning: use of SCENEWIDTH tag is deprecated and will not be supported in future releases. Please use SCENE SIZE in the GUI section. See the updated Developer's Reference for details.");
                } else if (s.equalsIgnoreCase("SITE")) {
                    site = readToCR();
                } else if (s.equalsIgnoreCase("SAVEGAME_PERSISTENCE")) {
                    savegamePersistence = Utils.cInt(nextToken().numVal());
                    getLine();
                } else if (s.equalsIgnoreCase("CLUSTER")) {
                    cluster = nextToken().strVal();
                    getLine();
                } else if (s.equalsIgnoreCase("ENCODING")) {
                    String realenc = readToCR();
                    if (!this.encoding.equalsIgnoreCase(realenc)) {
                        if (!Utils.isIn(realenc,"ANSI,UTF-8"))
                            throw new DimxException("Allowed encoding formats are: UTF-8 and ANSI");
                        throw new DimxException("Wrong Encoding");
                    }
                } else if (s.equalsIgnoreCase("CUSTCMDPROC")) {
                    custCmdProc = readToCR().trim();
                } else if (s.equalsIgnoreCase("ATTRLIST")) {
                    attrlist = readToCR();
                }
                t = lookupToken();
                s = t.strVal();
            }
            
            world = new World(server,msgs,name,cluster,imagesFolder,logger,helpurl);
            world.version = version;
            world.setSceneDimensions(sceneWidth,sceneHeight);
            world.author = author;
            world.authoremail = authoremail;
            world.logoSrc = Utils.absolutizeUrl(logosrc,world.imagesFolder);
            world.counterHtml = counterhtml;
            world.msgListSize = msglistsize;
            world.varGet("interphone",Const.GETREF).assign(new Token(interphone.equalsIgnoreCase("true") || interphone.equals("1")),world);
            world.compass = (compass.equalsIgnoreCase("true") || compass.equals("1"));
            world.savegamePersistence = savegamePersistence;
            world.muting = Utils.cInt(muting);
            world.site = site;
            world.encoding = this.encoding;
            if(custCmdProc.length()>0){
                try{
                    world.custCmdProc = Class.forName(custCmdProc).newInstance();
                }catch(Exception e){
                    throw new DimxException("Specified custCmdProc '"+ custCmdProc +"' could not be instantiated.");
                }
            }            
            varsSet(attrlist);
            
            parseWorldSections();
            
            parseToken("END_WORLD");
            
        } catch (NestedException e) {
            if (verbose) logger.debug(e);
            throw new DimxException(e.getMessage());
        } catch (DimxException e) {
            if (e.getMessage().equals("Wrong Encoding")) {
                throw new DimxException(e.getMessage()); // propagate
            }
            if (verbose) logger.debug(e);
            throw new DimxException("SYNTAX ERROR\n" + e.getMessage() + "\nat line: " +
            currLine + "\n-----------------\n" + identifyLine());
        } catch (Exception e) {
            logger.debug(e);
            throw new DimxException("INTERNAL ERROR\n" + e.toString() + "\nwhile parsing line: " +
            currLine + "\n-----------------\nPlease see log for details");
        }
    }

    /** Setting World attrlist properties **/
    protected boolean varsSet(String attrlist) throws DimxException {
        Token t = null;
        if(world==null) return false;
        if (attrlist != null) {
            // Tutto sugli attributi di default
            Vector list = Utils.stringSplit(attrlist,",");
            for (int i=0; i < list.size(); i++) {
                String s1 = (String) list.elementAt(i);
                Vector couple = Utils.stringSplit(s1,"=");
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
                world.varGet(k, Const.GETREF).assign(t,world);
            }
        }
        return true; // Normalmente ritorna OK
    }
    
    private void parseWorldSections() throws NestedException,DimxException {
        String t = lookupToken().strVal();
        
        while (!t.equals(EOF) && !t.equalsIgnoreCase("END_WORLD")) {
            if (t.equals("EVENTS")) {
                world.logger.debug("Warning: Syntax has changed. Please turn EVENTS section into SCRIPTS section.");
                parseEvents("EVENTS");
            } else if (t.equals("SCRIPTS")) {
                parseEvents("SCRIPTS");
            } else if (t.equals("ROOMS")) {
                parseRooms();
            } else if (t.equals("LINKS")) {
                parseLinks();
            } else if (t.equals("CHARACTERS")) {
                parseCharacters();
            } else if (t.equals("ITEMS")) {
                parseItems();
            } else if (t.equals("SETS")) {
                parseSets();
            } else if (t.equals("GUI")) {
                parseGUI();
            } else {
                throw new DimxException("Unexpected section: " + t);
            }
            
            t = lookupToken().strVal();
        }
    }
    
    public void parseScene() throws DimxException {
        int sceneWidth = 350;
        int sceneHeight = 235;
        
        parseToken("SCENE");

        String s = lookupToken().strVal();
        if (s.equalsIgnoreCase("SIZE")) {
            parseToken("SIZE");

            String dimensions = readToCR();

            Vector dim = Utils.stringSplit(dimensions,"x");

            if (dim.size() != 2) throw new DimxException("Syntax error");

            sceneWidth = Utils.cInt(dim.elementAt(0));
            sceneHeight = Utils.cInt(dim.elementAt(1));
            world.setSceneDimensions(sceneWidth,sceneHeight);
        } else if (s.equalsIgnoreCase("LOOK")) {
            
            parseToken("LOOK");
            s = nextToken().strVal();
            
            if (s.equalsIgnoreCase("3rdperson")) {
                parseCR();
                world.sceneLook = Const.LOOK_3RDPERSON;
            } else if (s.equalsIgnoreCase("1stperson")) {
                parseCR();
                world.sceneLook = Const.LOOK_1STPERSON;
            } else {
                throw new DimxException("Unsupported SCENE LOOK. Valid values are: 1STPERSON or 3RDPERSON");
            }
        }
    }
    
    public void parseScreen() throws DimxException {
        parseToken("SCREEN");
        parseToken("SIZE");
        
        String dimensions = readToCR();
        
        Vector dim = Utils.stringSplit(dimensions,"x");
        
        if (dim.size() != 2) throw new DimxException("Syntax error");
        
        world.screenwidth = Utils.cInt(dim.elementAt(0));
        world.screenheight = Utils.cInt(dim.elementAt(1));
        world.defaultClient = new Client(world, 3, false, false, world.screenwidth, world.screenheight, "No Browser");
    }
    
    
    public void parsePage(String token) throws DimxException {
        parseToken(token);
        
        String id = nextToken().strVal();
        if (world.getPanel(id) != null) world.logger.debug("Warning: Duplicate definition of: " + id);
        String template = readToCR();
        if (!(template.startsWith("https:") || template.startsWith("http:")))
            template = Utils.getParentFolder(this.fileName) + template;
        Page newView = new Page(id,template);
        
        world.varGet(id,Const.GETREF).assign(new Token(newView),world);
    }
    
}
