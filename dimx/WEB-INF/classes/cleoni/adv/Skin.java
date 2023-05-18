package cleoni.adv;

/** Represents a SKIN object */
public class Skin {
	protected String id = "default";
	protected String name = "Default";
	public String skinFolder = null;
	protected Image picPlayer = null;
	protected String picSpacer = "blank.gif";
	protected String picFader = "fadewhite.gif";

	protected String icoPlayer = "icoplayer.gif";
	protected String icoCharacter = "icouser.gif";
	protected String icoItem = "icoitem.gif";
	protected String icoWay = "icoway.gif";
	protected String icoWayN = "icowayn.gif";
	protected String icoWayS = "icoways.gif";
	protected String icoWayE = "icowaye.gif";
	protected String icoWayW = "icowayw.gif";
	protected String icoWayU = "icowayu.gif";
	protected String icoWayD = "icowayd.gif";
	protected Dict defIconWayDir = new Dict();
		
	protected String bodyBgColor = "#000040"; // Sfondo finestra
	protected String bodyBackground = "";   // Sfondo grafico finestra
	protected String bodyStyle = "{font-size: 10pt;font-family: Tahoma;color:#00FFFF;margin:0;}"; // Stile testo body
	protected String sysMsgStyle = "{font-size: 10pt;font-family: Tahoma;color:#FF1111;}"; // Stile testo messaggi di sistema

	protected String linkStyle = "{font-family: Tahoma;color:#EE30EE;}"; // Stile testo body
	protected String alinkStyle = "{font-family: Tahoma;color:#EE30EE;}"; // Stile testo body
	public String buttonStyle = null;

	protected String iconLabelStyle = "{font-size: 10pt;font-family: Tahoma;color:#DDDDDD;}"; // Stile etichette icone
	protected String panelBgColor = "SILVER";  // Sfondo zona comandi
	protected String panelBackground = "";   // Sfondo grafico zona comandi
	protected String panelStyle = "{font-size: 10pt;font-family: Tahoma;color:#600000;}"; // Stile testo zona comandi
	protected String titleStyle = "{font-size: 10pt;font-family: Tahoma;color:#FFFF10;background-color:#000090;}"; // Stile testo titolo
	protected String list1BgColor = "#000000";  // Sfondo lista righe dispari
	protected String list2BgColor = "#414141";  // Sfondo lista righe pari
	protected String listStyle = "{font-size: 8pt;font-family: Tahoma;color:#A0B0C0;}"; // Stile scritte lista messaggi
	protected Image ctrlbanner = null; // Testata grafica in cima - eventualmente nulla
	protected String icoRotL = "icorotl.gif";
	protected String icoRotR = "icorotr.gif";
	protected String icoRev = "reverse.gif";
	protected String sndNewmsg = "sndnewmsg.wav";
        public String stylesheet = null;

/**
 * Skin constructor comment.
 */
public Skin(String skinsFolder) {
	super();
	skinFolder = skinsFolder + id ;
	picPlayer = new Image(Utils.absolutizeUrl("uomo.gif",skinFolder),64,100);
	absolutizeUrls();		
}

public String toString() {
	return "id=" + id + "\n" +
			"name=" + name  + "\n" +
			"picPlayer=" + picPlayer  + "\n" +
			"picSpacer=" + picSpacer  + "\n" +
			"picFader=" + picFader  + "\n"  +
			"bodyBgColor=" + bodyBgColor  + "\n" ;
}


/**
 * Skin constructor comment.
 */
public Skin(String anId, String aName, String skinsFolder, 
	String aImagesFolder, String aPicPlayer, 
	String aPicSpacer, String aPicFader,
	
	String aIcoPlayer, String aIcoCharacter, String aIcoItem,
	String aIcoWay, 
	String aIcoWayN,	String aIcoWayS,	
	String aIcoWayE,	String aIcoWayW,	
	String aIcoWayU, 	String aIcoWayD,
	String aIcoRotL, 	String aIcoRotR,  	String aIcoRev,
	
	String aSndNewmsg,
	
	String aBodyBgColor, String aBodyBackground, 
	String aBodyStyle, String aSysMsgStyle,
	String aLinkStyle, String aaLinkStyle, 
	String aIconLabelStyle,
	String aPanelBgColor, String aPanelBackground,
	String aPanelStyle, String aButtonStyle, String aTitleStyle,
	String aList1BgColor, String aList2BgColor,
	String aListStyle,
        String aStylesheet
	) {
	super();
	if (anId != null) id = anId;
	if (aName != null) name = aName;

	if (aImagesFolder != null && !aImagesFolder.equals("")) {
		skinFolder = aImagesFolder;
	} else {
		skinFolder = skinsFolder + id ;
	}
	if (aPicPlayer != null) {
		picPlayer = new Image(Utils.absolutizeUrl(aPicPlayer,skinFolder),64,100);
	} else {
		picPlayer = new Image(Utils.absolutizeUrl("uomo.gif",skinFolder),64,100);
	}
	if (aPicSpacer != null) picSpacer = aPicSpacer; 
	if (aPicFader != null) picFader = aPicFader;

	if (aIcoPlayer != null) icoPlayer = aIcoPlayer;
	if (aIcoCharacter != null) icoCharacter = aIcoCharacter;
	if (aIcoItem != null) icoItem = aIcoItem;
	if (aIcoWay != null) icoWay = aIcoWay;
	if (aIcoWayN != null) icoWayN = aIcoWayN;
	if (aIcoWayS != null) icoWayS = aIcoWayS;
	if (aIcoWayE != null) icoWayE = aIcoWayE;
	if (aIcoWayW != null) icoWayW = aIcoWayW;
	if (aIcoWayU != null) icoWayU = aIcoWayU;
	if (aIcoWayD != null) icoWayD = aIcoWayD;
	if (aIcoRotL != null) icoRotL = aIcoRotL;
	if (aIcoRotR != null) icoRotR = aIcoRotR;

	if (aSndNewmsg != null) sndNewmsg = aSndNewmsg;
		
	if (aBodyBgColor != null) bodyBgColor = aBodyBgColor;
	if (aBodyBackground != null) bodyBackground = aBodyBackground;
	if (aBodyStyle != null) bodyStyle = aBodyStyle;
        if (Utils.instr(bodyStyle,"margin",true) < 0) bodyStyle = Utils.stringReplace(bodyStyle,"{", "{margin:0;", false);

	if (aSysMsgStyle != null) sysMsgStyle = aSysMsgStyle;

	if (aLinkStyle != null) linkStyle = aLinkStyle;
	if (aaLinkStyle != null) alinkStyle = aaLinkStyle;
		
	if (aIconLabelStyle != null) iconLabelStyle = aIconLabelStyle;
	if (aPanelBgColor != null) panelBgColor = aPanelBgColor;
	if (aPanelBackground != null) panelBackground = aPanelBackground;
	if (aPanelStyle != null) panelStyle = aPanelStyle;
	if (aButtonStyle != null) buttonStyle = Utils.unbracket(aButtonStyle);
	if (aTitleStyle != null) titleStyle = aTitleStyle;
	if (aList1BgColor != null) list1BgColor = aList1BgColor;
	if (aList2BgColor != null) list2BgColor = aList2BgColor;
	if (aListStyle != null) listStyle = aListStyle;
	if (aIcoRev != null) icoRev = aIcoRev;
        stylesheet = aStylesheet;
	absolutizeUrls();
}

private void absolutizeUrls() {
	picSpacer = Utils.absolutizeUrl(picSpacer,skinFolder);
	picFader = Utils.absolutizeUrl(picFader,skinFolder);
	icoPlayer = Utils.absolutizeUrl(icoPlayer,skinFolder);
	icoCharacter = Utils.absolutizeUrl(icoCharacter,skinFolder);
	icoItem = Utils.absolutizeUrl(icoItem,skinFolder);
	icoWay = Utils.absolutizeUrl(icoWay,skinFolder);
	icoWayN = Utils.absolutizeUrl(icoWayN,skinFolder);
	icoWayS = Utils.absolutizeUrl(icoWayS,skinFolder);
	icoWayE = Utils.absolutizeUrl(icoWayE,skinFolder);
	icoWayW = Utils.absolutizeUrl(icoWayW,skinFolder);
	icoWayU = Utils.absolutizeUrl(icoWayU,skinFolder);
	icoWayD = Utils.absolutizeUrl(icoWayD,skinFolder);
	icoRotL = Utils.absolutizeUrl(icoRotL,skinFolder);
	icoRotR = Utils.absolutizeUrl(icoRotR,skinFolder);
	icoRev = Utils.absolutizeUrl(icoRev,skinFolder);
	
	sndNewmsg = Utils.absolutizeUrl(sndNewmsg,skinFolder);
	bodyBackground = Utils.absolutizeUrl(bodyBackground,skinFolder);
	panelBackground = Utils.absolutizeUrl(panelBackground,skinFolder);
        
        if (stylesheet != null) stylesheet = Utils.absolutizeUrl(stylesheet,skinFolder);

	// Carica vettore direzioni - icone passaggi
	defIconWayDir.put("N",icoWayN);
	defIconWayDir.put("S",icoWayS);
	defIconWayDir.put("E",icoWayE);
	defIconWayDir.put("W",icoWayW);
	defIconWayDir.put("U",icoWayU);
	defIconWayDir.put("D",icoWayD);
}

/** Returns the corresponding STYLE tag for the specified skin
     * @param skin the skin
     * @return the tag
     */
public String toHtml() {
    StringBuffer s = new StringBuffer("<STYLE TYPE=TEXT/CSS>\n<!--\n");
    if (!this.bodyStyle.equals("")) {
        s.append("BODY,TD " + this.bodyStyle + "\n");
        s.append(".text " + this.bodyStyle + "\n");
    }
    if (!this.panelStyle.equals("")) {
        s.append("input " + this.panelStyle + "\n");
        s.append("select " + this.panelStyle + "\n");
        s.append(".cmd " + this.panelStyle + "\n");
        s.append(".right_column { float:right; max-width:50%; width:auto !important; width:50%;}\n");
    }
    if (!this.linkStyle.equals("")) {
        s.append("A:link " + this.linkStyle + "\n");
        s.append("A:visited " + this.linkStyle + "\n");
    }
    if (!this.alinkStyle.equals("")) {
        s.append("A:hover " + this.alinkStyle + "\n");
        s.append("A:active " + this.alinkStyle + "\n");
    }
    if (!this.sysMsgStyle.equals("")) {
        s.append(".sysmsg " + this.sysMsgStyle + "\n");
    }
    if (!this.iconLabelStyle.equals("")) {
        s.append(".iconlabel " + this.iconLabelStyle + "\n");
    }
    if (!this.listStyle.equals("")) {
        s.append(".listing " + this.listStyle + "\n");
    }
    if (!this.titleStyle.equals("")) {
        s.append(".title " + this.titleStyle + "\n");
    }
    s.append("-->\n</STYLE>\n");
    return s.toString();
}
    
public String htmlBodyTag(String commands) {
        String r = "<body STYLE=\"background:" + bodyBgColor + ";";
        if (!bodyBackground.equals("")) {
            r = r + "background-image:url(" + bodyBackground + ");";
        }
        r = r + "\" ";
        if (commands != null && !commands.equals("")) 
            r = r + "onLoad=\"parent.clientExecute('" + commands + "')\" ";
        r = r + ">\n";        
        return r;
}

}