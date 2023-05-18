package cleoni.adv;

/** Represents a control in the commands panel:
 * a button, a text field, anything you use in the command panel
 * is an instance of Ctrl
 * @author: Cristiano Leoni
 */
public class Ctrl {
	public String id = "";
	public String name = null;
	public String description = null;
	public String event = null;
	public String eventModel = "";
	public Image image = null;
        public String icon = null;
	public int type = Const.CTRL_BUTTON;

public Ctrl(Panel panel,int aType, String anId, String aName, String aDescr, String anEventId, String anEventModel, Image im, String anIcon) throws DimxException {
    this(panel,aType,anId,aName,aDescr,anEventId,anEventModel,im);
    icon = anIcon;
}
/**
 * Button constructor comment.
 */
public Ctrl(Panel panel,int aType, String anId, String aName, String aDescr, String anEventId, String anEventModel, Image im) throws DimxException {
	super();
	if (anId != null && anId.equals("-")) {
		type = Const.CTRL_CR;
		if (panel != null) id = "_sep" + panel.buttons.size();
	} else {
		type = aType;
		if (anId != null) {
			id = anId; 
		} else {
			id = "_ctr" + panel.buttons.size();
		}
		name = aName;
		description = Utils.cStr(aDescr);
		event = anEventId;
		eventModel = Utils.cStr(anEventModel);
		image = im;
		if (!(eventModel.equals("") || eventModel.equals("T") || eventModel.equals("TO") ||
			eventModel.equals("O") || eventModel.equals("OO") || eventModel.equals("cookie") || eventModel.equals("session")))
			throw new DimxException("Event model not supported: " + eventModel);
	}
	
	if (panel != null) panel.buttons.put(id,this);
}
/**
 * Button constructor comment.
 */
public Ctrl(Panel panel,String anId) throws DimxException {
	this(panel,Const.CTRL_BUTTON,anId,null,null,null,null,null);
}
public int getNoArgs() {
	return eventModel.length();
}

  /**
   * Outputs the control to HTML.
   * Can be called in two ways: simplified and fully specified.
   * with simplified owner == the player
   * in all other cases we use owner,agent and target
   * @param skin 
   * @param utils 
   * @param owner this will be aPlayer if the simplified calling method is used
   * @param agent 
   * @param targetid 
   * @throws cleoni.adv.DimxException 
   * @return 
   */
  public String toHtml(Skin skin,Utils utils,AdvObject owner,AdvObject agent,String targetid) throws DimxException {
    
    StringBuffer sb = new StringBuffer("");
    
    String style = "";
    /*
    String styleNB = ""; // Style with NO brackets
    styleNB = skin.listStyle;
    if (styleNB.substring(0,1).equals("{")) {
        styleNB=styleNB.substring(1);
    }
    if (styleNB.substring(styleNB.length()-1).equals("}")) {
        styleNB=styleNB.substring(0,styleNB.length()-1);
    }   
    */
    if (skin.listStyle != null && !skin.listStyle.equals("")) {
        style = "STYLE=\"" + skin.listStyle + "\"";
        style = Utils.stringReplace(style, "{", "{background:" + skin.list2BgColor+";",false);

    }
      String buttonStyleTag = "";
        if (skin.buttonStyle != null) {
            buttonStyleTag = " style=\"" + skin.buttonStyle + "\"";
            String str = "";
            if (Utils.instrCount(buttonStyleTag,"$ICON",true)>0) {
                if (this.icon != null) {
                    str = "url(" + this.icon + ")";
                }
                buttonStyleTag = Utils.stringReplace(buttonStyleTag, "$ICON", str,Const.IGNORE_CASE);
            }
            str = "url(" + Utils.absolutizeUrl("pan" + this.id + ".gif", skin.skinFolder) + ")";
            buttonStyleTag = Utils.stringReplace(buttonStyleTag, "$SKINICON", str,Const.IGNORE_CASE);
        }
        
        if (this.id.equalsIgnoreCase("help")) {
            sb.append("<input name=\"help\" type=\"button\"");
            sb.append(buttonStyleTag);
            sb.append(" onClick=\"javascript:window.open('" + this.description +
            "', 'help', 'scrollbars=yes,resizable=yes,width=640,height=480');\" VALUE=\" "+this.name+" \"> \n");
        } else if (this.type == Const.CTRL_BUTTON) {
            
            sb.append("<input type=\"button\" name=\""+ this.id + "\" value=\"" + this.name + "\"");
            sb.append(buttonStyleTag);
            if (this.description!= null && !this.description.equals("") && this.description.length()>11 && this.description.substring(0,11).equals("javascript:")) {
                sb.append(" onClick=\""+this.description+"\"> \n");
            } else {
                sb.append(" onClick=\"parent.command(this);\"> \n");
            }
                    
            /*
            sb.append("<a id=\""+ this.id + "\" href=\"javascript:parent.command(this);\"");
            sb.append(buttonStyleTag);
            sb.append(">"+this.name);
            sb.append("</a>");
            */
        } else if (this.type == Const.CTRL_SUBMIT) {
            sb.append("<INPUT TYPE=\"SUBMIT\" NAME=\""+ this.id + "\" VALUE=\"" + this.name + "\"");
            sb.append(buttonStyleTag + "> \n");
        } else if (this.type == Const.CTRL_TEXTBOX) {
            String value = ""; String size = "";
            if (this.eventModel != null && this.eventModel.equals("cookie")) {
                name = Utils.decodeURL(utils.getCookie(this.event));
            }
            if (this.name != null) value = " VALUE=\"" + this.name + "\"";
            if (this.description != null && !this.description.equals("")) size = " SIZE=\"" + this.description + "\" ";
            if (this.id.equals("username") && (utils.getSession("nickname") != null)) { // Special case - prebuilt nickname
                String nickname = utils.gSession("nickname");
                sb.append("<input type=\"hidden\" name=\"" + this.id + "\" value=\"" + nickname + "\">"+nickname+"\n");
            } else {
                sb.append("<INPUT TYPE=\"TEXT\" NAME=\"" + this.id + "\" " + value + size + style + ">\n");
            }
        } else if (this.type == Const.CTRL_CR) {
            sb.append("<BR>\n");
        } else if (this.type == Const.CTRL_LABEL) {
            if (this.name.startsWith("@")) {
                if (agent.world != null) {
                    sb.append(agent.world.evaluateExpression(this.name.substring(1), owner, agent, targetid, null).strVal() + " ");
                }
            } else {
                sb.append(this.name + " ");
            }
        } else if (this.type == Const.CTRL_IMAGEBUTTON) {
            sb.append("<A HREF=\"#\" onMouseOver=\"window.status=window.defaultStatus;return true;\" onClick=\"parent.command('" + this.id + "');return false;\"><IMG BORDER=0 WIDTH=" + this.image.getWidth() + " HEIGHT=" + this.image.getHeight() + " SRC=\"" + this.image.getSrc() + "\" ALT=\"" + this.name + "\" TITLE=\"" + this.name + "\" ALIGN=TOP VSPACE=0></A>");
        } else if (this.type == Const.CTRL_DROPDOWN) {
            Dict list = null; // Will hold the list of possible values

            // Special case for screen resolutions selection
            boolean skip_it = false;
            if (this.id.equals("screensize")) {
                String locked = (String) utils.getSession("locked");
                String scmode = (String) utils.getSession("screenmode");
                list = Utils.string2set(this.description,",","=",true);
                if (!list.containsKey(scmode)) {
                    scmode = "640x480";
                }
                if (locked != null && !(locked.equals("0")||locked.equals("false")||locked.equals(""))) {
                    // Resolution is locked!
                    sb.append(scmode+"<input type=\"hidden\" name=\"" + this.id + "\" value=\""+scmode+"\" />\n");
                    skip_it = true;
                } 
            }

            if (!skip_it) {
                sb.append("<select name=\"" + this.id + "\" " + style + ">\n");
                if (this.eventModel != null) {
                    if (this.eventModel.equals("cookie")) {
                        name = (String) utils.getSession(event);
                    } else if (this.eventModel.equals("session")) {
                        String s = utils.getCookie(event);
                        if (s == null) { // Try searching in Session
                            s = (String) utils.getSession(event);
                        }
                        name = Utils.decodeURL(s);
                    }
                }
                String defVal = name; // Default value for dropdown list (pre-selected)

                if (this.description.equals("") && owner != null) { // Try evaluating "event" as a SET expression, and "name" as a String expression
                    DimxParser p = new DimxParser(owner.world,Const.WORLDVARS,0,1,owner.id,agent,targetid);
                    p.feed(event);
                    Token t = p.evalExpression(p.lookupToken(),0);
                    list = t.dictVal();

                    // Evaluate default value as an expression
                    p.reset();
                    p.feed(name);
                    t = p.evalExpression(p.lookupToken(),0);
                    String s = t.strVal(); 
                    if (!s.equals("")) defVal = s; // if not null, then use it
                } else {
                    list = Utils.string2set(this.description,",","=",true);
                }
                for (int i=0; i < list.size(); i++) {
                    String k = (String) list.keyAt(i);
                    sb.append("<option VALUE=\"" + k + "\"");
                    if (k.equalsIgnoreCase(defVal)) sb.append(" SELECTED");
                    Object v = list.elementAt(i);
                    if (!v.equals("")) {
                        // Value specified
                        // Display it
                        if (v.getClass().getName().indexOf("Token") > 0) {
                            Token t = (Token) v;
                            if (t.isDimxObject()) {
                                v = t.dimxobjVal().getName();
                            } else {
                                v = t.strVal();
                            }
                        }
                        sb.append(">" + v + "</option>\n");
                    } else {
                        // No value specified - use id
                        sb.append(">" + k + "</option>\n");
                    }
                }
                sb.append("</select>");
            }
        } else if (this.type == Const.CTRL_PASSWORD) {
            String value = ""; String size = "";
            if (this.name != null) value = " VALUE=\"" + this.name + "\"";
            if (this.description != null && !this.description.equals("")) size = " SIZE=\"" + this.description + "\" ";
            sb.append("<INPUT TYPE=\"PASSWORD\" NAME=\"" + this.id + "\" " + value + size + style + ">\n");
        } else if (this.type == Const.CTRL_CHECKBOX) {
            String mytag = description;
            if (this.eventModel != null && this.eventModel.equals("cookie")) {
                String cookieval = Utils.decodeURL(utils.getCookie(this.event));
                DimxParser p = new DimxParser(owner.world,Const.WORLDVARS,0,owner.id);
                p.feed(Utils.stringReplace(description,"$1",cookieval, false));
                Token t = p.evalExpression(p.lookupToken(),0);
                if (t.boolVal()) mytag = "CHECKED"; else mytag = " ";
            }
            sb.append("<INPUT TYPE=\"CHECKBOX\" NAME=\"" + this.id + "\" VALUE=\"" + this.name + "\" " + mytag + ">\n");
            //sb.append("<INPUT TYPE=CHECKBOX NAME=\"" + this.id + "\" VALUE=\"" + this.name + "\" " + this.description + " " + style + ">\n");
        } else if (this.type == Const.CTRL_GHOST) {
            // Do nothing
        } else if (this.type == Const.CTRL_HIDDEN) {
            String value = ""; 
            if (this.name != null) value = " VALUE=\"" + this.name + "\"";
            sb.append("<INPUT TYPE=\"HIDDEN\" NAME=\"" + this.id + "\" " + value + ">\n");
        } else if (this.type == Const.CTRL_MAP) {
            if (owner != null) {
                if (owner.world.map != null) {
                    sb.append("<IFRAME ID=\"mapiframe\" NAME=\"mapiframe\" WIDTH=\"200\" HEIGHT=\"200\" SCROLLING=\"no\" SRC=\"" + owner.world.navigatorUrl + "?view=map\"></IFRAME>\n");
                }
            }
        } else {
            sb.append("<P>Cannot output control: " + this.id + " name: " + this.name + " type: " + this.type + "<P>");
        }
        return sb.toString();
    }

public String toString() {
    return "[" + name + "]";
}
}
