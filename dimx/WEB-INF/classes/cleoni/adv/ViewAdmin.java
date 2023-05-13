/*
 * ViewAdmin.java
 *
 * Created on 8 giugno 2005, 9.09
 */

package cleoni.adv;
import java.io.*;

/** Custom view producing the Admin panel
 * @author Cristiano Leoni
 */
public class ViewAdmin extends View {
    
    /** Creates a new instance of ViewAdmin */
    public ViewAdmin() {
    }
    
    public static void outputHtml(World world, PrintWriter out, Skin skin, String format, String errors) throws DimxException {
        if (format != null) { // This part may not be necessary since individual methods can be called directly as well
            if (format.equals("snapshot")) {
                outputSnapshot(world,out,skin);
            //} else if (format.equals("dump")) { // Not anymore supported
            //    outputDump(world,out,skin);
            }
            return;
        }
        out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        out.print("<html><head><TITLE>" + world.getName() + " - Admin Console</TITLE>\n" + skin.toHtml() + "\n\n");
        if (skin.stylesheet != null) out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + skin.stylesheet + "\" />\n");
        out.print("</head>\n<body ");
        out.print("BGCOLOR=\"" + skin.bodyBgColor + "\" ");
        if (!skin.bodyBackground.equals("")) {
            out.print("BACKGROUND=\"" + skin.bodyBackground + "\" ");
        }
        out.println(">\n");
        
        out.println("<TABLE BORDER=0 CELLPADDING=2 CELLSPACING=0 WIDTH=\"100%\">");
        out.println("<TR><TD COLSPAN=2 CLASS=title>Admin Console - " + Utils.now() + "</TD></TR>\n");
        out.println("<TR VALIGN=TOP><TD CLASS=text WIDTH=\"360\">");
        
        out.println("<BR>Engine version: " + multiplayer.myVersion + "<BR>\n");
        out.println("<BR>World: " + world.name.strVal() + " v." + world.version + "<BR>\n");
        String dbstat = "N/A";
        if (world.getCluster().dbConn(null) != null) {
            dbstat = "<B>present</B>";
        }
        out.println("mySQL DB: " + dbstat + "<BR>\n");
        int s = world.players.size();
        out.println("Players connected: " + s + "<BR>\n");
        out.println("<BR><HR><BR>\n");
        out.println("<A HREF=\"" + world.navigatorUrl + "\">Back to game</A><BR>\n");
        out.println("<BR><HR><BR>\n");
        out.println("<a HREF=\"" + Utils.getParentFolder(world.navigatorUrl) + "maintenance\">Maintenance panel</A><BR>\n");
        out.println("<a HREF=\"https://www.dimensionex.net\" TARGET=\"_blank\">DimensioneX web site</A><BR>\n");
        out.println("<a HREF=\"https://www.dimensionex.net/documentation/\" TARGET=\"_blank\">Documentation</A><BR>\n");
        
        out.println("</TD><TD CLASS=text>");
        
        
        out.println("<form method=POST action=\"" + world.navigatorUrl + "\">");
        out.println("<TABLE CELLPADDING=10><TR><TD ");
        out.println("BGCOLOR=\"" + skin.panelBgColor + "\" ");
        if (!skin.panelBackground.equals("")) {
            out.println("BACKGROUND=\"" + skin.panelBackground + "\" ");
        }
        out.println("CLASS=cmd>");
        
        out.println("<INPUT TYPE=radio NAME=\"cmd\" VALUE=\"restart\" CHECKED> Restart<BR>");
        out.println("<INPUT TYPE=radio NAME=\"cmd\" VALUE=\"snapshot\"> Snapshot<BR>");
        out.println("<INPUT TYPE=radio NAME=\"cmd\" VALUE=\"opti\"> Optimizer stats<BR>");
        out.println("<INPUT TYPE=radio NAME=\"cmd\" VALUE=\"showlog\"> Show Log<BR>");
        out.println("<INPUT TYPE=radio NAME=\"cmd\" VALUE=\"clearlog\"> Clear Log<BR>");
        out.println("<INPUT TYPE=radio NAME=\"cmd\" VALUE=\"_addcode\"> Enter script: (see textbox)<BR>");
        out.println("<INPUT TYPE=radio NAME=\"cmd\" VALUE=\"_execute\"> Execute command: (see textbox)<BR>");
        //htmlCtrl(new Ctrl(myPanel,Const.CTRL_BUTTON,"savexit",msgs.cmd[17],msgs.cmd[17],null,"T",null);
        out.println("<BR><TEXTAREA NAME=\"textarea\" COLS=\"70\" ROWS=\"10\">" + errors + "</TEXTAREA><br/>");
        out.println("User: <input name=\"username\" value=\"admin\"> ");
        out.println("Password: <input type=\"password\" name=\"password\">");
        out.println(" <INPUT TYPE=SUBMIT NAME=\"vai\" VALUE=\" Go! \">");
        out.println("</TD></TR></TABLE></form>\n");
        
        out.println("</TD></TR></TABLE>\n");
        out.println("</BODY>\n</HTML>\n");
        
    }
    

/*
 * Output LOG
 *
 */
    public static void outputLog(World world, PrintWriter out, Skin skin) throws DimxException, IOException {
        
        out.print("<HTML><HEAD><TITLE>" + world.getName() + " - Log</TITLE>\n" + skin.toHtml() + "\n\n");
        if (skin.stylesheet != null) out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + skin.stylesheet + "\" />\n");
        out.print("</HEAD>\n<BODY ");
        out.print("BGCOLOR=\"" + skin.bodyBgColor + "\" ");
        if (!skin.bodyBackground.equals("")) {
            out.print("BACKGROUND=\"" + skin.bodyBackground + "\" ");
        }
        out.println(">\n");
        
        header(world,out);
        out.println("<p>Logging mode is currently: " + world.logger.debugMode + " (0=file 1=console 2=none)");
        
        try {
            FileInputStream fin =  new FileInputStream(world.systemDir + "debug" + world.slot + ".log");
            BufferedReader myInput = new BufferedReader
            (new InputStreamReader(fin));
            out.println("<p><PRE>");
            out.println("<p>--------------------------------------------------\n");
            String thisLine = null;
            while ((thisLine = myInput.readLine()) != null) {
                out.println(thisLine);
            }
            myInput.close(); myInput = null;
            fin.close(); fin = null;
            out.println("<p>--------------------------------------------------\n");
            out.println("</PRE>");
        } catch (java.io.FileNotFoundException e) {
            out.println("Log file is missing.");
        }
        
        out.println("</BODY>\n</HTML>\n");
        
    }
    
    public static void outputOptimizer(World world, PrintWriter out, Skin skin) throws DimxException {
        out.println("<HTML><HEAD>");
        out.println(htmlCharset(world));
        if (skin.stylesheet != null) out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + skin.stylesheet + "\" />\n");
        out.print("<TITLE>" + world.getName() + " - Optimizer</TITLE>\n" + skin.toHtml() + "\n\n</HEAD>\n<BODY ");
        out.print("BGCOLOR=\"" + skin.bodyBgColor + "\" ");
        if (!skin.bodyBackground.equals("")) {
            out.print("BACKGROUND=\"" + skin.bodyBackground + "\" ");
        }
        out.println(">\n");
        
        header(world,out);
        
        out.println("<H1>Optimizer statistics</H1><TABLE BORDER CELLPADDING=5 CELLSPACING=0 CLASS=\"text\"><TR><TH>Event/Function/Sub</TH><TH>CPU time</TH><TH>CPU perc.</TH><TH>Calls</TH><TH>Avg.</TH></TR>");
        DictSorted properties = world.properties;
        DictSorted optiFreq = (DictSorted) world.varGet("__optiFrequency",false).dictsortedVal().clone();
        DictSorted optiCons = (DictSorted) world.varGet("__optiConsumed",false).dictsortedVal().clone();
        double totalCons = 0.0;
        double totalFreq = 0.0;
        if (optiCons != null) {
            Dict sortedCons = new Dict(); // Key: event ID , element: CPU Consumption
            for (int j=0; j < optiCons.size(); j++) {
                int max = 0;
                for (int i=0; i < optiCons.size(); i++) {
                    //Token o = (Token) optiCons.elementAt(i);
                    if ( Utils.cInt(((Token) optiCons.elementAt(i)).numVal()) > Utils.cInt(((Token) optiCons.elementAt(max)).numVal()) ) {
                        max = i;
                    }
                    if (j==0) {
                        totalCons = totalCons + ((Token) optiCons.elementAt(i)).numVal(); 
                        totalFreq = totalFreq + ((Token) optiFreq.elementAt(i)).numVal(); 
                    }
                }
                sortedCons.put(optiCons.keyAt(max), optiCons.elementAt(max));
                optiCons.setElementAt(new Token(-1),max); // Clear element MAX so that is not selected again
            }
            
            // At this point, SortedCons contains key sorted by ascending CPU consumption
            for (int i=0; i < sortedCons.size(); i++) {
                Token o = (Token) sortedCons.elementAt(i);
                double cons = o.numVal();
                out.print("<TR><TD>" + sortedCons.keyAt(i) + "</TD><TD>" + Utils.cInt(cons) + "</TD><TD>" + Utils.cInt(cons * 100.0 / totalCons) + "%</TD>");
                double freq = ((Token) optiFreq.get(sortedCons.keyAt(i))).numVal();
                out.print("<TD>" + Utils.cInt(freq)+ "</TD>");
                out.print("<TD>" + Utils.cInt(cons/freq) + "</TD>");
                out.println("</TR>");
            }
        }
        out.println("</TABLE>\n<P><B>Total consumption</B>: " + Utils.cInt(totalCons) + " msec.");
        out.println("<BR><B>Total calls</B>: " + Utils.cInt(totalFreq));
        out.println("<BR><B>Runtime errors</B>: " + Utils.cInt(world.varGet("__optiRuntimeErrors").numVal()));
        out.println("</BODY>\n</HTML>\n");
    }
    
    private static void outputSnapshot(World world, PrintWriter out, Skin skin) throws DimxException {
        out.println("<HTML><HEAD>");
        out.println(htmlCharset(world));
        out.print("<TITLE>" + world.getName() + " - Snapshot</TITLE>\n" + skin.toHtml() + "\n\n");
        if (skin.stylesheet != null) out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + skin.stylesheet + "\" />\n");
        out.print("</HEAD>\n<BODY ");
        out.print("BGCOLOR=\"" + skin.bodyBgColor + "\" ");
        if (!skin.bodyBackground.equals("")) {
            out.print("BACKGROUND=\"" + skin.bodyBackground + "\" ");
        }
        out.println(">\n");
        
        header(world,out);
        out.println(world.htmlAdminSnapshot());
        
        out.println("</BODY>\n</HTML>\n");
    }
    
    private static void header(World world, PrintWriter out) {
        out.println("Engine version: " + multiplayer.myVersion + "<BR>\n");
        out.println("World: " + world.name.strVal() + " v." + world.version + "<BR>\n");
    }
    
    public static void addCode(World world,String text) throws DimxException, Exception {
        SmallBasicLoader myLoader = new SmallBasicLoader(world, text, "console", 1, world.encoding);
        myLoader.load(world.systemDir,true);
    }
}
