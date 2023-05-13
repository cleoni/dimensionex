package cleoni.adv;

/** The commands panel to be displayed on the user's browser
 * @author: Cristiano Leoni
 */
public class Panel {
	public String id = null;
	public Dict buttons = new Dict();
        public String hasFocus = null;
/**
 * Panel constructor comment.
 */
public Panel(World world, String anId) throws DimxException {
	super();
	id = anId;
	Ctrl x;
	x = new Ctrl(this,Const.CTRL_GHOST,"go",world.msgs.cmd[20],world.msgs.cmd[20] + " ...",null,"O",null);
	x = new Ctrl(this,Const.CTRL_GHOST,"look",world.msgs.cmd[5],world.msgs.cmd[5] + " ...",null,"O",null);
}
public String toString() {
	StringBuffer sb = new StringBuffer(id + ": ");
	for (int i=0; i < buttons.size(); i++) {
		sb.append(((Ctrl) buttons.elementAt(i)).id);
		if (i+1 < buttons.size()) sb.append(", ");
	}
	return sb.toString();
}

/**
 * 
 * @param skin 
 * @param anAgent typically a player, serves to auto-fill drop-down lists based on sets
 * @param utils serves for cookie-based and session-based controls. If you use them and this is null you will get a nullPointer
 * @throws cleoni.adv.DimxException 
 * @return 
 */
public String htmlControls(Skin skin, Utils utils,AdvObject owner, AdvObject agent, String targetid) throws DimxException {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i < buttons.size(); i++) {
        sb.append(((Ctrl) buttons.elementAt(i)).toHtml(skin,utils, owner, agent, targetid));
    }
    return sb.toString();
}

/**
 * 
 * @param skin 
 * @param anAgent typically a player, serves to auto-fill drop-down lists based on sets
 * @param utils serves for cookie-based and session-based controls. If you use them and this is null you will get a nullPointer
 * @throws cleoni.adv.DimxException 
 * @return 
 */
public String toHtml(Skin skin,Utils utils, AdvObject owner, AdvObject agent, String targetid, Dict options) throws DimxException {
    StringBuffer sb = new StringBuffer();
    String targ = "";
    String view = "scene";
    if (options != null) {
        targ = Utils.cStr(options.get("target"));
        if (options.getS("view") != null) view = options.getS("view");
    }
    String targtag = "";
    if (!targ.equals("")) {
        targtag = " TARGET=\""+targ+"\"";
    }
    sb.append("<FORM METHOD=POST ACTION=\"" + owner.world.navigatorUrl + "\"" + targtag + ">\n");
    if (!view.equals("")) {
        sb.append("<INPUT TYPE=HIDDEN NAME=view VALUE=\"" + view + "\">\n");
    }
    sb.append("<INPUT TYPE=HIDDEN NAME=arg0 VALUE=\"\">\n");
    sb.append("<INPUT TYPE=HIDDEN NAME=arg1 VALUE=\"\">\n");
    sb.append("<INPUT TYPE=HIDDEN NAME=cmd VALUE=\"\">\n");
    sb.append(htmlControls(skin, utils, owner, agent, targetid));
    sb.append("</FORM>");
    return Utils.stringReplace(sb.toString(),"\n", "", false);
}
}
