/*
 * Cluster.java
 *
 * Created on 19 aprile 2006, 14.19
 */

package cleoni.adv;

import java.sql.*;

/**
 *
 * @author  leoni
 */
public class Cluster {
    
    public String id = null;
    final Object saveFileLock  = new Object(); // Synchronisation tool;
    public boolean dbConnected = false; // Is dbConn != null? DB connected?
    public DictSorted worlds = new DictSorted();
    public Journal journal = null; // Journal's filename
    public multiplayer server = null;
    
    /** Creates a new instance of Cluster */
    public Cluster(String clusterid,multiplayer aServer) {
        id = clusterid;
        server = aServer;
    }
    
    public java.sql.Connection dbConn(StringBuffer errorMessages) throws DimxException {
        if (server.isDBConfigured()) { // Connect !
            java.sql.Connection dbc=null;
            boolean dbConnectedBefore = dbConnected;
            try {
                Class.forName(server.dbdriver).newInstance();
                dbc = DriverManager.getConnection(server.dbconnstr);
            } catch (Exception ex)  {
                if (errorMessages != null) {
                    errorMessages.append(ex.toString());
                }
                    /*
                   if (Utils.isIn(ex.getClass().getName(),"com.mysql.jdbc.CommunicationsException")) {
                       return false;
                    }
                   server.getWorld(worlds.keyAt(0)).logger.log("DB Problem opening connection: " + ex.getClass().getName() + "\nmsg:" + ex.getMessage() + "\n--\n" + ex.toString() + " at " + Utils.now());
                     **/
            }
            dbConnected = (dbc != null);
            if (dbConnectedBefore != dbConnected) {
                String eventId = "onDbUp";
                if (!dbConnected) {
                    eventId = "onDbDown";
                }
                notifyWorlds(eventId);
            }
            return dbc;
        }
        return null;
    }
    
    public String htmlPullUp(String excluded) {
        return server.htmlAreasPullUp(this.id,excluded);
    }
    public boolean checkDbConn(StringBuffer errMessage) throws DimxException {
        java.sql.Connection dbConn = dbConn(errMessage);
        try {
            Class.forName(server.dbdriver).newInstance();
            dbConn = DriverManager.getConnection(server.dbconnstr);
            if (dbConn != null) {
                java.sql.Statement stmt = null;
                try {
                    java.sql.ResultSet rs = null;
                    try {
                        stmt = dbConn.createStatement();
                        rs = Utils.queryDatabase("SELECT 'OK'", stmt, false);
                        if (rs != null)  {
                            if ( rs.next() )  {
                                errMessage.append("Database connected - OK\n");
                            }
                            rs.close();
                        }
                        try {
                            rs = Utils.queryDatabase("SELECT VERSION()", stmt, false);
                            if (rs != null)  {
                                if ( rs.next() )  {
                                    errMessage.append("Database version: ").append(rs.getString(1)).append("\n");
                                }
                                rs.close();
                            }
                        } catch (java.lang.Exception ex) {
                                // No support for SELECT VERSION - CONTINUE
                        }                        
                        rs = Utils.queryDatabase("DESCRIBE `" + this.id + "", stmt, true);
                        if (rs == null)  {
                            errMessage.append("Table: " + this.id + " does not exist - Creating it...\n" );
                            boolean res = false; 
                            String sql = null;
                            try {
                                sql = "CREATE TABLE `" + this.id + "` (akey varchar(255) NOT NULL default '',`value` text,UNIQUE KEY akey (akey)) TYPE=MyISAM;" ;
                                res = Utils.executeSQLCommand(sql,dbConn, false, false);
                            } catch (java.lang.Exception ex2) {
                                // Most likely, this isn't a mySQL database - try standard syntax
                                sql = "CREATE TABLE `" + this.id + "` (akey varchar(255) NOT NULL,`value` TEXT)";
                                res = Utils.executeSQLCommand(sql,dbConn, false, false);
                            }
                            if (res) {
                                errMessage.append("Storing settings taken from SAV file... " + Maint.export_sav2db(server.getWorld(worlds.keyAt(0)).getSavegameFile(), this) + "\n");
                            }
                        } else {
                            if (rs.next() && rs.getString(1).equalsIgnoreCase("akey")) {
                                errMessage.append("Table: " + this.id + " already exists - OK\n" );
                                return true;
                            } else {
                                throw new DimxException("DB Problem opening connection: table " + this.id + " does not have the correct structure.");
                            }
                        }
                    } finally {
                        if (rs != null) rs.close();
                        dbConn.close();
                    }
                } catch (java.sql.SQLException ex) {
                        throw new DimxException("DB Problem: " + ex.toString());
                } finally {
                    try {  
                        if (stmt != null) stmt.close();
                    } catch (Exception ex) {}
                }
                return true;
            } else {
                errMessage.append("NULL connection");
                return false;
            }
        } catch (Exception ex)  {
            errMessage.append("DB Problem opening connection: " + ex.getClass().getName() + "\nmsg:" + ex.getMessage() + "\n--\n" + ex.toString() + " at " + Utils.now());
            return false;
            //throw new DimxException("DB Problem opening connection: " + ex.toString());
        }
    }
    
    /**
     * @param aName
     * @return
     */
    public boolean playerExists(String aName) {
        for (int i=0; i<worlds.size(); i++) {
            World world = server.getWorld(worlds.keyAt(i));
            if ( world != null && world.playerExists(aName)) {
                return true;
            }
        }
        return false;
    }
    
    public void registerWorld(String areaid) throws DimxException {
        worlds.put(areaid,areaid);
    }
    
    public String toString() {
        return id + ":" + worlds.toString();
    }
    
    public String getPlayers(String format) throws DimxException {
        int count = 0;
        StringBuffer sb = new StringBuffer("<br/><br/>\n");
        for (int i=0; i<worlds.size(); i++) {
            World world = server.getWorld(worlds.keyAt(i));
                if (world == null) {
                    throw new DimxException("ERROR: World -"+worlds.keyAt(i)+"- is null");
                }
                if (world.players == null) {
                    throw new DimxException("ERROR: World.players for world -"+worlds.keyAt(i)+"- is null");
                }
            count += world.players.size();
            if (format.equalsIgnoreCase("extended")) {
                for (int j=0; j < world.players.size(); j++) {
                    Player x = (Player) world.players.elementAt(j);
                    AdvObject c = x.container;
                    if (c == null) {
                        sb.append(world.msgs.actualize(world.msgs.msg[183],"<b>"+x.getName()+"</b>",world.name.strVal() + ": NOWHERE")+"<br />\n");
                        //sb.append("<b>" + x.getName() + "</b> is in " + world.name.strVal() + ": NOWHERE<br>\n");
                    } else {
                        sb.append(world.msgs.actualize(world.msgs.msg[183],"<b>"+x.getName()+"</b>",world.name.strVal() + ": "+c.getName())+"<br />\n");
                        //sb.append("<b>" + x.getName() + "</b> is in " + world.name.strVal() + ": " + c.getName() + "<br>\n");
                    }
                }
            }
        }
        if (count > 0) {
            return count + " " + server.getWorld(worlds.keyAt(0)).msgs.msg[2] + sb;
        } else {
            return "Server up!";
        }
    }
    
    /**
     * Gets the slot number of the specified world
     * @param worldId world ID to be searched
     * @return slot number of the world, null otherwise
     */
    public String getSlotOf(String worldId) {
        int i = worlds.indexOf(worldId);
        if (i>=0) {
            return worlds.keyAt(i);
        } else {
            return null;
        }
    }
    
    private void notifyWorlds(String eventId) throws DimxException {
        for (int i=0; i<worlds.size(); i++) { // Notify all worlds
            World world = server.getWorld(worlds.keyAt(i));
            world.fireEvent(eventId,null,null,null,true);
        }
    }
}
