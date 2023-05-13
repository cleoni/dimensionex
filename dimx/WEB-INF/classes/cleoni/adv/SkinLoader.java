package cleoni.adv;

/**
 * Skin loader and parser (DXS format) for the DimensioneX engine
 * Creation date: (03/01/2003)
 * @author: Cristiano Leoni
 */
import java.util.Vector;
import java.io.*;
import java.net.*;
/** Parser capable of reading a DXS file and building a SKIN */
public class SkinLoader extends DimxParser {
	private static final String[] setSkinAttrs = {
		"NAME", 
		"IMAGESFOLDER","SKINFOLDER","PICPLAYER","PICSPACER","PICFADER",
		"ICOPLAYER","ICOCHARACTER","ICOITEM","ICOWAY",
		"ICOWAYN","ICOWAYS","ICOWAYE","ICOWAYW",
		"ICOWAYU","ICOWAYD","ICOROTL","ICOROTR","ICOREV",
		"SNDNEWMSG","BODYBGCOLOR","BODYBACKGROUND","BODYSTYLE",
		"SYSMSGSTYLE","LINKSTYLE","ALINKSTYLE","ICONLABELSTYLE",
		"PANELBGCOLOR","PANELBACKGROUND","PANELSTYLE",
		"TITLESTYLE","LIST1BGCOLOR","LIST2BGCOLOR","LISTSTYLE","BUTTONSTYLE","STYLESHEET"};

	private Messages	msgs;
	private Skin		skin = null;
public SkinLoader(World w) {
	super(null,Const.WORLDVARS,0, 0);
	world = w;
	logger = w.logger;
	msgs = w.msgs;
}
public Skin load (String filePath) throws DimxException { 
// Loads a DXS skin file
    try {
        feed(Utils.fetch(filePath,"UTF-8"));
	parseSkin();
    } catch (Exception e) {
        if (e.getMessage().startsWith("SYNTAX")) {
            throw new DimxException( e.getMessage());
        } else {
            // Log and bypass
            world.logger.debug("Unable to load skin \"" + filePath + "\" (" + e.getMessage() + ")");
        }
    }
	return skin;
}
private void parseSkin() throws DimxException {
	try {
		String imagesFolder = null;
		String picplayer = "player.gif";
		String picspacer = "blank.gif";
		String picfader = "fader.gif";
		String icoplayer = "player.gif";
		String icocharacter = "character.gif";
		String icoitem = "item.gif";
		String icoway = "way.gif";
		String icowayn = "north.gif";
		String icoways = "south.gif";
		String icowaye = "east.gif";
		String icowayw = "west.gif";
		String icowayu = "up.gif";
		String icowayd = "down.gif";
		String icorotl = "left.gif";
		String icorotr = "right.gif";
		String icorev = "reverse.gif";
		String sndnewmsg = "newmsg.wav";
		String bodybgcolor = "";
		String bodybackground = ""; 	
		String bodystyle = "";
		String sysmsgstyle = "";
		String linkstyle = "";
		String alinkstyle = "";
		String iconlabelstyle = "";
		String panelbgcolor = "SILVER";
		String panelbackground = "";
		String panelstyle = "";
                String buttonstyle = null;
		String titlestyle = "";
		String list1bgcolor = "";
		String list2bgcolor = "";
		String liststyle = "";
		String stylesheet = null;
		
		
		parseToken("SKIN");

		String id = nextToken().strVal();
		if (world.skins.get(id) != null) throw new DimxException("Duplicate definition of skin: " + id);

		getLine(); // Eats CR after the 1st line

		String name = id;
		
		Token t = lookupToken();
		String s = t.strVal();
		while (Utils.isIn(s,setSkinAttrs)) {
			eat_extended();
			if (s.equalsIgnoreCase("NAME")) {
				name = readToCR();
			} else if (s.equalsIgnoreCase("IMAGESFOLDER") || s.equalsIgnoreCase("SKINFOLDER")) {
				imagesFolder = readToCR();
			} else if (s.equalsIgnoreCase("PICPLAYER")) {
				picplayer = readToCR();
			} else if (s.equalsIgnoreCase("PICSPACER")) {
				picspacer = readToCR();
			} else if (s.equalsIgnoreCase("PICFADER")) {
				picfader = readToCR();
			} else if (s.equalsIgnoreCase("ICOPLAYER")) {
				icoplayer = readToCR();
			} else if (s.equalsIgnoreCase("ICOCHARACTER")) {
				icocharacter = readToCR();
			} else if (s.equalsIgnoreCase("ICOITEM")) {
				icoitem = readToCR();
			} else if (s.equalsIgnoreCase("ICOWAY")) {
				icoway = readToCR();
			} else if (s.equalsIgnoreCase("ICOWAYN")) {
				icowayn = readToCR();
			} else if (s.equalsIgnoreCase("ICOWAYS")) {
				icoways = readToCR();
			} else if (s.equalsIgnoreCase("ICOWAYE")) {
				icowaye = readToCR();
			} else if (s.equalsIgnoreCase("ICOWAYW")) {
				icowayw = readToCR();
			} else if (s.equalsIgnoreCase("ICOWAYU")) {
				icowayu = readToCR();
			} else if (s.equalsIgnoreCase("ICOWAYD")) {
				icowayd = readToCR();
			} else if (s.equalsIgnoreCase("ICOROTL")) {
				icorotl = readToCR();
			} else if (s.equalsIgnoreCase("ICOROTR")) {
				icorotr = readToCR();
			} else if (s.equalsIgnoreCase("ICOREV")) {
				icorev = readToCR();
			} else if (s.equalsIgnoreCase("SNDNEWMSG")) {
				sndnewmsg = readToCR().trim();
			} else if (s.equalsIgnoreCase("BODYBGCOLOR")) {
				bodybgcolor = readToCR();
			} else if (s.equalsIgnoreCase("BODYBACKGROUND")) {
				bodybackground = readToCR();
			} else if (s.equalsIgnoreCase("BODYSTYLE")) {
				bodystyle = readToCR();
			} else if (s.equalsIgnoreCase("SYSMSGSTYLE")) {
				sysmsgstyle = readToCR();
			} else if (s.equalsIgnoreCase("LINKSTYLE")) {
				linkstyle = readToCR();
			} else if (s.equalsIgnoreCase("ALINKSTYLE")) {
				alinkstyle = readToCR();
			} else if (s.equalsIgnoreCase("ICONLABELSTYLE")) {
				iconlabelstyle = readToCR();
			} else if (s.equalsIgnoreCase("PANELBGCOLOR")) {
				panelbgcolor = readToCR();
			} else if (s.equalsIgnoreCase("PANELBACKGROUND")) {
				panelbackground = readToCR();
			} else if (s.equalsIgnoreCase("PANELSTYLE")) {
				panelstyle = readToCR();
			} else if (s.equalsIgnoreCase("BUTTONSTYLE")) {
				buttonstyle = readToCR();
			} else if (s.equalsIgnoreCase("TITLESTYLE")) {
				titlestyle = readToCR();
			} else if (s.equalsIgnoreCase("LIST1BGCOLOR")) {
				list1bgcolor = readToCR();
			} else if (s.equalsIgnoreCase("LIST2BGCOLOR")) {
				list2bgcolor = readToCR();
			} else if (s.equalsIgnoreCase("LISTSTYLE")) {
				liststyle = readToCR();
			} else if (s.equalsIgnoreCase("STYLESHEET")) {
				stylesheet = readToCR();
			}
			t = lookupToken();
			s = t.strVal();
		}

		skin = new Skin(id,name,Utils.getParentFolder(world.imagesFolder) + "skins/",imagesFolder,picplayer,picspacer,picfader,
			icoplayer,icocharacter,icoitem,icoway,icowayn,icoways,
			icowaye,icowayw,icowayu,icowayd,icorotl,icorotr,icorev,sndnewmsg,
			bodybgcolor,bodybackground,bodystyle,sysmsgstyle,linkstyle,alinkstyle,iconlabelstyle,
			panelbgcolor,panelbackground,panelstyle,buttonstyle,titlestyle,list1bgcolor,list2bgcolor,liststyle,stylesheet);

                skin.titleStyle = Utils.stringReplace(skin.titleStyle,"$SKINFOLDER",skin.skinFolder,Const.IGNORE_CASE);
                skin.titleStyle = Utils.stringReplace(skin.titleStyle,"$IMAGESFOLDER",skin.skinFolder,Const.IGNORE_CASE);
		parseToken("END_SKIN");

	
	} catch (DimxException e) {
		if (verbose) logger.debug(e);
		throw new DimxException("SYNTAX ERROR in skin file\n" + e.getMessage() + "\nat line: " + 
			currLine + "\n-----------------\n" + identifyLine());
	} catch (Exception e) {
		logger.debug(e);
		throw new DimxException("INTERNAL ERROR\n" + e.toString() + "\nwhile parsing line: " + 
			currLine + " of skin file\n-----------------\nPlease see log for details");
	}
}
}
