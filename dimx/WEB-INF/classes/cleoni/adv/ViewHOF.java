/*
 * ViewHOF.java
 *
 * Created on 31 maggio 2005, 11.05
 */

package cleoni.adv;
import java.io.*;
import java.util.*;

/** Custom view producing the Hall of Fame
 * @author Cristiano Leoni
 */
public class ViewHOF extends View {
    
    /** Creates a new instance of ViewHOF */
    public ViewHOF() {
    }
    
    public static void outputHtml(World world, PrintWriter out, Skin skin) throws DimxException {
        out.println("<HTML><HEAD>");
        out.println(htmlCharset(world));
        out.println("<TITLE>" + world.getName() + " - " + world.msgs.msg[154] + "</TITLE>");
        if (skin.stylesheet != null) out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + skin.stylesheet + "\" />\n");
        out.print(skin.toHtml() + "\n\n</HEAD><BODY ");
        out.print("BGCOLOR=\"" + skin.bodyBgColor + "\" ");
        if (!skin.bodyBackground.equals("")) {
            out.print("BACKGROUND=\"" + skin.bodyBackground + "\" ");
        }
        out.println(">\n");
        
        out.println("<H2>" + world.getName() + "</H2>");
        if (world.version != null && !world.version.equals(""))
            out.println("version " + world.version);

        out.println("<H3>" + world.msgs.msg[154] + "</H3>\n");

        String hiscoretext = world.getSetting("hiscoretext",null);
        if (hiscoretext != null) {
            out.println(hiscoretext + "<P>");
        } else { // Try old system
            String hiscore = world.getSetting("hiscore",null);
            if (hiscore != null) {
                out.println("Best score ever: <B>" + hiscore + "</B><P>");
            }
        }

        java.util.Properties p = new java.util.Properties();
        int winnersCount =0;
        try {
            p.load(new java.io.FileInputStream(world.getHofFile()));
            
            String tmp = Utils.cStr(p.getProperty("winnersCount"));
            if (!tmp.equals("")) {
                winnersCount = Utils.cInt(tmp);
            }
        } catch (FileNotFoundException e) {
        } catch (java.io.IOException e) {
            throw new DimxException(e.getMessage());
        }
        
        if (winnersCount <= 0) {
            out.println("<b>Nobody was able to finish this game yet</b>.");
        } else {
            out.println("<UL>");
            String winVersion = "";
            for (int i=winnersCount-1; i >= 0; i--) {
                winVersion = p.getProperty("winner"+i+"_worldVersion");
                if (!winVersion.equals(world.version) && i==(winnersCount-1)) {
                    out.println("<LI>Nobody was able to finish <b>this version</b> yet.");
                }
                String msg = p.getProperty("winner"+i+"_message");
                if (msg != null && !msg.equals(""))
                    out.println("<LI>" + msg + " (at " + p.getProperty("winner"+i+"_when") + ")");
                else
                    out.println("<LI>User <B>" + p.getProperty("winner"+i+"_name") + "</B> finished version " + winVersion + " at " + p.getProperty("winner"+i+"_when"));
                
            }
            out.println("</UL>");
        }
        out.println("<H3>" + world.msgs.msg[180] + "</H3><UL>\n");
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar stopNow = Calendar.getInstance();
        
        long mins_start = world.startDate.get(Calendar.MINUTE) +
        world.startDate.get(Calendar.HOUR_OF_DAY)*60  +
        world.startDate.get(Calendar.DAY_OF_WEEK)*24*60  +
        world.startDate.get(Calendar.WEEK_OF_YEAR)*7*24*60  +
        (world.startDate.get(Calendar.YEAR)-2002)*52*7*24*60 ;
        
        long mins_now = stopNow.get(Calendar.MINUTE)  +
        stopNow.get(Calendar.HOUR_OF_DAY)*60  +
        stopNow.get(Calendar.DAY_OF_WEEK)*24*60  +
        stopNow.get(Calendar.WEEK_OF_YEAR)*7*24*60  +
        (stopNow.get(Calendar.YEAR)-2002)*52*7*24*60 ;
        
        long mins_diff = mins_now - mins_start;
        long diff_days = mins_diff / (60*24);
        long diff_hours = (mins_diff - diff_days * (60*24)) / 60;
        long diff_mins = mins_diff - diff_days * 24 * 60 - diff_hours*60;
        
        out.println("<LI>This game is running since: " + sdf.format(world.startDate.getTime()) + "\n");
        out.println("<LI>Time is now: " + sdf.format(stopNow.getTime()) + "\n");
        out.println("<LI>Uptime: " + diff_days + " days, " + diff_hours + " hours, " + diff_mins + " minutes.\n");
        out.println("<LI>Players since last restart: " + world.playersCounter + "\n");
        out.println("<LI>Killed players since last restart: " + world.killedCounter + "\n");
        out.println("<LI>Saved games on this server: " + getSavedGamesCount(world) + "\n");
        
        out.println("</UL></BODY></HTML>\n");
        
    }

    private static long getSavedGamesCount(World world) throws DimxException {
        long count=0;
        
        Cluster mycluster = world.getCluster();
        java.sql.Connection dbConn = null;
        if (mycluster.dbConnected) dbConn = mycluster.dbConn(null);
        if (dbConn == null) { // file

            java.util.Properties p = new java.util.Properties();
            try {
                p.load(new java.io.FileInputStream(world.getSavegameFile()));
            } catch (Exception e) {
                return 0;
            }

            // logger.debug("Player's data may be " + p.getProperty(Utils.stringFlatten(username) + "_name"));
            Enumeration k = p.keys();

            String key = null;
            while (k.hasMoreElements()) {
                key = (String) k.nextElement();
                if (key.endsWith("_name")) count++;
            }
        } else { // DB supported
            java.sql.Statement stmt = null;
            try {
                stmt = dbConn.createStatement();
                java.sql.ResultSet rs = null;
                try {
                    rs = Utils.queryDatabase("SELECT count(*) FROM `" + world.cluster + "` WHERE akey LIKE '%_name'", stmt, false);
                    if (rs != null)  {
                            if ( rs.next() )  {
                                return rs.getInt(1);
                            }
                    }
                } catch(Exception ex)  { 
                   world.logger.log("DB Problem: " + ex.toString() + " at " + Utils.now()); 
                   throw new DimxException("DB Problem: " + ex.toString()); 
                  } finally {
                      rs.close();
                  }
            } catch (java.sql.SQLException ex) {
                    throw new DimxException("DB Problem: " + ex.toString());
            } finally {
                try {  
                    if (stmt != null) stmt.close();
                    dbConn.close();  
                } catch (Exception ex) {}
            }
        }

        return count;
    }
    
}
