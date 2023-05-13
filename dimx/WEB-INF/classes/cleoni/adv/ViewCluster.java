/*
 * ViewCluster.java
 *
 * Created on 10 maggio 2006, 22.23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package cleoni.adv;
import java.io.*;
import java.util.*;

/**
 *
 * @author Cris
 */
public class ViewCluster extends View {
    
    /** Creates a new instance of ViewCluster */
    public ViewCluster() {
    }
    
    public static void outPlayers(World world,PrintWriter out, Skin skin, String clusterid, Cluster cluster, String format) throws DimxException {
        out.println("<HTML><HEAD>");
        out.println(htmlCharset(world));
        out.println("<TITLE>Cluster " + clusterid + "</TITLE>");
        if (skin.stylesheet != null) out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + skin.stylesheet + "\" />\n");
        out.println(skin.toHtml() + "\n\n</HEAD>\n<BODY ");
        out.print("BGCOLOR=\"" + skin.bodyBgColor + "\" ");
        if (!skin.bodyBackground.equals("")) {
            out.print("BACKGROUND=\"" + skin.bodyBackground + "\" ");
        }
        out.println(">\n");
        
        out.println(cluster.getPlayers(format) + "\n");
    }

}
