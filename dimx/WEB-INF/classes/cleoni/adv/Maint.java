/*
 * Admin.java
 *
 * Created on 30 agosto 2004, 12.27
 * This will replace the admin page of DimxServlet and will implement all administration
 * tasks
 */

package cleoni.adv;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/** Maintenance servlet - lets you perform certain maintenance operations
 * @author CrLeoni
 * @version
 */
public class Maint extends HttpServlet {
    
    private Skin skin = null;
    private String systemDir = null;
    
    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        // Get base Dir
        systemDir = getInitParameter("base");
        
        if(systemDir == null)
            systemDir = (String) this.getServletContext().getAttribute("systemDir");
        
        if (systemDir == null) 
            systemDir = Utils.getSystemDir(this.getServletContext());

        // Create default skin
        skin = new Skin(Utils.getParentFolder(Utils.getParentFolder(systemDir)) + "skins/default/");
    }
    
    /** Destroys the servlet
     */
    public void destroy() {
        
    }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    
    PrintWriter out = null;
    Utils utils = new Utils(request, response);
    
    try {    

        ServletContext context = this.getServletContext();
        String view = "maintpage.htm";
        String sinst = null; // Selected server instance (String)
        int inst = 0; // Selected server instance (int)
        
        // Get any commands
        String cmd =  utils.getForm("command");
        sinst = utils.getForm("instance");
        if (sinst != null && !sinst.equals("")) { 
            inst = Utils.cInt(sinst.substring(1));
        }

        // Detect running instances
        
        multiplayer server = (multiplayer) context.getAttribute("server");

        // Prepare output tools
        Dict replacements = new Dict();
        replacements.put("<head>","<head>" + skin.toHtml());
        replacements.put("<body>",skin.htmlBodyTag(null));
        
        StringBuffer res = new StringBuffer("");
        
        World world = null;
        String adminPass = null;
        String serverAdminPass = null;
        if (cmd != null && !cmd.equals("")) {
            world = (World) context.getAttribute("world"+inst);
            String propFile = utils.getSystemDir(context)+"worldnav"+inst+".properties";
            DictSorted settings = server.loadWorldSettings(""+inst,propFile);

           // Password check
            adminPass = Utils.cStr(settings.get("adminPasswd"));

            java.util.Properties wp = new java.util.Properties();
            try {
                wp.load(new java.io.FileInputStream(Utils.getSystemDir(context) + "dimensionex.properties"));
            } catch (Exception e) {
            }
            serverAdminPass = Utils.cStr(wp.getProperty("adminPasswd"));
            String charset = "ISO-8859-1";
            if (world != null) charset = world.msgs.charset;

            String formpass = utils.getForm("pass",charset);
            if ( Utils.instr("gogame/goadmin",cmd,false) == -1 && !adminPass.equals(formpass) && !serverAdminPass.equals(formpass) ) {
                res.append("Incorrect Admin Password.");
            } else {
                // Password checked - go on.

                if (world == null && Utils.instr("setupgame/gogame/goadmin",cmd,false) == -1) {
                    res.append("World must be loaded.");
                } else {
                    if (cmd.equals("gpselect")) {
                        //
                        // gpselect
                        //
                        view = "profilemaint.htm";
                        String seltype = utils.getForm("seltype",charset);
                        String selval = utils.getForm("selval",charset);
                       // Get World
                        String saveGame = world.getSavegameFile();
                        res.append("Save game is: "+saveGame+"<P>\n");
                        Dict profiles = new Dict();

                        java.util.Properties p = loadProperties(saveGame); 
                        if (p == null) {
                            res.append("No games saved.");
                        } else {
                            res.append("<TABLE CELLPADDING=5 CELLSPACING=0 BORDER=1 CLASS=text>\n");
                            res.append("<TR><TD ALIGN=RIGHT>id</TD><td>last login</td><TD>date saved</TD><TD>world version</TD><TD>Area</TD></TR>");
                            int i = 0;
                            java.util.Enumeration keys = p.keys();
                            while (keys.hasMoreElements()) {
                                String k = (String) keys.nextElement();
                                int x = k.indexOf("_worldVersion");
                                if (x>0) {
                                    String id = k.substring(0, x);
                                    String ver = p.getProperty(id + "_worldVersion");
                                    String login = p.getProperty(id + "_login");
                                    String when = p.getProperty(id + "_when");
                                    if (seltype.equals("") || 
                                            (seltype.equals("less") && ver.compareToIgnoreCase(selval) < 0) ||
                                            (seltype.equals("login_before") && (login == null || login.compareToIgnoreCase(selval) < 0)) ||
                                            (seltype.equals("before") && when.compareToIgnoreCase(selval) < 0) 
                                        ) {
                                        res.append("<TR><TD><INPUT TYPE=checkbox NAME=\"profile_" + i + "\" VALUE=\"" + id + "\"");
                                        if (!seltype.equals("")) res.append(" CHECKED");
                                        res.append("> " + id + "</TD><td>"+login+"</td><TD>" + when + "</TD><TD>" + ver + "</TD><TD>" + p.getProperty(id + "_worldId") + "</TD></TR>\n");
                                        i++;
                                    }
                                }
                            }
                            res.append("</TABLE>\n");
                            res.append("<INPUT TYPE=HIDDEN NAME=profilescount VALUE=\"" + i + "\">\n");
                            res.append("<P>" + i +" profiles found.");
                        }


                    } else if (cmd.equals("gpdel")) {
                        //
                        // gpdel
                        //
                        view = "maintresults.htm";

                        int profilescount = utils.cInt(utils.getForm("profilescount"));

                        String saveGame = world.getSavegameFile();
                        java.util.Properties p = loadProperties(saveGame); 
                        if (p==null)
                            res.append("No games saved.");

                        for (int i=0; i<profilescount; i++) {
                            String id = utils.getForm("profile_" + i);
                            if (!id.equals("")) {
                                res.append("<LI>deleted profile of: " + id);

                                p.remove(id + "_properties");
                                p.remove(id + "_login");
                                p.remove(id + "_cluster");
                                p.remove(id + "_events");
                                p.remove(id + "_fbid");
                                p.remove(id + "_image");
                                p.remove(id + "_items");
                                p.remove(id + "_location");
                                p.remove(id + "_name");
                                p.remove(id + "_panel");
                                p.remove(id + "_pass");
                                p.remove(id + "_when");
                                p.remove(id + "_worldId");
                                p.remove(id + "_worldInstanceId");
                                p.remove(id + "_worldVersion");
                            }
                        }

                        saveProperties(p,saveGame);
                    } else if (cmd.equals("setupgame")) {
                        if (propFile == null) {
                            String strinst = "";
                            if (inst > 1) strinst = Utils.cStr(inst);
                            propFile = Utils.getSystemDir(context) + "worldnav" + strinst + ".properties";
                        }
                        java.util.Properties nwp = new java.util.Properties();
                        try {
                            nwp.load(new java.io.FileInputStream(propFile));
                        } catch (Exception e) {
                        }
                        String url =  Utils.cStr(utils.getForm("gameurl"));
                        if (!url.equals("")) {
                           nwp.setProperty("worldFile",url);
                           saveProperties(nwp, propFile);
                           res.append("Game has been set-up. Now please restart the slot.");
                        } else {
                            res.append("Please specify a valid DXW file.");
                        }
                    } else if (cmd.equals("gogame")) {
                        String loc =  server.navigatorUrl + "?game="+inst;
                        response.sendRedirect(loc);
                        return;
                    } else if (cmd.equals("goadmin")) {
                        String loc =  server.navigatorUrl + "?view=admin&game="+inst;
                        response.sendRedirect(loc);
                        return;
                    } else if (cmd.equals("serverinit")) {
                        if (serverAdminPass.equals(utils.getForm("pass",charset))) { // if server admin passwd specified
                            String serversettings = Utils.getSystemDir(context) + "dimensionex.properties";
                            server.loadSettings(serversettings);
                            res.append("Just reloaded: "+serversettings);
                        } else {
                            res.append("Server admin's password incorrect");
                        }
                    } else if (cmd.equals("chkdb")) {
                        if (serverAdminPass.equals(utils.getForm("pass",charset))) { // if server admin passwd specified
                            res.append("<p><b>Connection string</b>: " + server.dbconnstr);
                            res.append("<br><b>DB Driver</b>: " + server.dbdriver + "</p>");
                        } else {
                            res.append("(Entering server admin's password will produce extra info)");
                        }
                        res.append("<pre>");
                        world.getCluster().checkDbConn(res);
                        res.append("</pre>");
                    } else if (cmd.equals("sav2db")) {
                        res.append(export_sav2db(world.getSavegameFile(), world.getCluster()));
                    } else if (cmd.equals("db2sav")) {
                        res.append(export_db2sav(world,world.getSavegameFile(), world.getCluster()));
                    } else {
                        res.append("Unknown command");
                    }
                }
            }
        }
        
        String values = "";
        
        // Detect slot codes
        DictSorted slotcodes = utils.getFilesByMask(utils.getSystemDir(context), new SlotSettingsFilter(null));

        for (int i=0; i<slotcodes.size(); i++) {
            if (i>0) values = values + ",";
                
            String k = Utils.stringReplace(slotcodes.keyAt(i),Utils.string2set("worldnav=,slot=,.properties=", ",","=",false),true);
            World w = (World) context.getAttribute("world" + k);
            
            values = values + "i" + k + "=" + k + ": ";
            if (w != null) {
               values = values + w.getName();
               if (sinst == null || sinst.equals("")) sinst = "i"+i;
            } else {
               DictSorted settings = server.loadWorldSettings("dummy",utils.getSystemDir(context)+slotcodes.keyAt(i));
               values = values + "(unloaded) ";
               k = settings.getS("worldFile");
               if (k.startsWith("https://")||k.startsWith("http://")) {
                   values = values + "http://.../" + Utils.getRidOfParentFolder(k);
               } else {
                   values = values + k;
               }
            }
        }

        // Output view
        utils.pageExpires();
        response.setContentType("text/html");
        out = response.getWriter();

        // Build view
        
        Ctrl ddl = new Ctrl(null,Const.CTRL_DROPDOWN,"instance",sinst,values,null,null,null);

        replacements.put("$instances",ddl.toHtml(skin,null,null,null, null));
        replacements.put("$results",res.toString());
        replacements.put("$command",cmd);
        replacements.put("$pass","");
        if (serverAdminPass != null && serverAdminPass.equals(utils.getForm("pass"))) {
            replacements.put("$pass",serverAdminPass);
        } 
        if (world != null && adminPass != null && adminPass.equals(utils.getForm("pass",world.msgs.charset))) {
            replacements.put("$pass",adminPass);
        } 
        String html = Utils.fetch(systemDir+view,"UTF-8");
        html = Utils.stringReplace(html,replacements,Const.IGNORE_CASE);
         
        // output your page here
        out.println(html);
        
         //
    } catch (Exception e) {
        if (out == null) 
            out = response.getWriter();
        out.println("Exception in " + this.getClass().getName());
        out.println("<PRE>"+ e.getMessage() + "</PRE>");
        e.printStackTrace(out);
    }
        out.close();
    }
    
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    private static String export_db2sav(World world,String saveGame, Cluster cluster) throws DimxException {
        StringBuffer res = new StringBuffer("");
        java.sql.Connection dbConn = null;
        if (cluster.dbConnected) dbConn = cluster.dbConn(null);
        if (dbConn == null) {
            res.append("No database connection.");
        } else {
            int i =0;
            java.sql.Statement stmt = null;
            try {
                stmt = dbConn.createStatement();
                java.sql.ResultSet rs = Utils.queryDatabase("SELECT * FROM `" + cluster.id + "`",stmt,false);
                if (rs != null) {
                    try {
                        java.util.Properties p = new java.util.Properties();
                        //p.load(new java.io.FileInputStream(saveGame));
                        res.append("Creating game profiles database: " + saveGame);
                        synchronized (world.getCluster().saveFileLock) {
                            // Save properties
                            java.io.FileOutputStream out = new java.io.FileOutputStream(saveGame);
                            while ( rs.next() )  {
                                //res.append("<BR>" + rs.getString("akey") + "=" + rs.getString("value"));
                                p.setProperty(rs.getString("akey"),rs.getString("value"));
                                i++;
                            }
                            p.store(out, "--- This is a generated file. Do not edit ---");
                            out.close();
                        }
                    } catch (java.io.FileNotFoundException e) {
                        Logger.echo("Creating game profiles database");
                    } catch (Exception e) {
                        throw new DimxException("DB Query result error: " + e.toString());
                    } finally {
                        rs.close();
                    }
                }
            } catch (java.sql.SQLException ex) {
                throw new DimxException("DB Problem: " + ex.toString());
            } finally {
                try {
                    stmt.close();
                    dbConn.close();
                } catch (Exception ex) {}
            }
            res.append("<P>" + i +" records exported to SAV file.");
        }
        return res.toString();
    }

    public static String export_sav2db(String saveGame, Cluster cluster) throws DimxException {
        StringBuffer res = new StringBuffer("");
        java.sql.Connection dbc = null;
        if (cluster.dbConnected) dbc = cluster.dbConn(null);
        if (dbc == null) {
            res.append("No database connection.");
        } else {
            java.util.Properties p = loadProperties(saveGame);
            if (p == null) {
                res.append("No games saved.");
            } else {
                Utils.executeSQLCommand("DELETE FROM `" + cluster.id + "`",dbc, false, false);
                int i = 0;
                int errors=0;
                java.util.Enumeration keys = p.keys();
                while (keys.hasMoreElements()) {
                    String k = (String) keys.nextElement();
                    if ((k.length() + Utils.instrCount(k,'\'')*2) <= 255) {
                        String v = p.getProperty(k);
                        Utils.saveSettingDB(k,v,dbc, cluster.id);
                        i++;
                    } else {
                        res.append("<P>Skipped key (too long): " + k);
                    }
                }
                res.append("" + i +" records stored into database.");
                try {
                    dbc.close();
                } catch (Exception e) {}
            }
        }
        return res.toString();
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Maintenance servlet";
    }

    private void saveProperties(java.util.Properties p,String propFile) throws DimxException
    {
        try {
            java.io.FileOutputStream fil = new java.io.FileOutputStream(propFile);
            p.store(fil, null);
            fil.close();
        } catch (Exception e) {
            throw new DimxException("Problem in Maint.saveProperties(" + propFile + ") - " + e.getMessage());
        }
    }
 
       private static java.util.Properties loadProperties(String propFile) throws DimxException {
       java.util.Properties p = new java.util.Properties();
       try {
            p.load(new java.io.FileInputStream(propFile));
            return p;
       } catch (FileNotFoundException e) {
            return null;
       } catch (Exception e) {
            throw new DimxException("Problem in Maint.loadProperties(" + propFile + ") - " + e.getMessage());
       }
    }

public class SlotSettingsFilter implements FilenameFilter {
  protected String pattern;
  
  public SlotSettingsFilter (String str) {
    pattern = str; // unused
    }
  public boolean accept (File dir, String name) {
    return name.toLowerCase().endsWith("properties") && (name.toLowerCase().startsWith("worldnav") || name.toLowerCase().startsWith("slot"));
    }
}

}
