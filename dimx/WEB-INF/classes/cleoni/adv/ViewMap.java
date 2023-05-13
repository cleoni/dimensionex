/*
 * ViewMap.java
 *
 * Created on 27 maggio 2005, 11.24
 */

package cleoni.adv;
import java.io.*;
/** Custom view producing the MAP view
 * @author Cristiano Leoni
 */
public class ViewMap extends cleoni.adv.View {
    
    /** Creates a new instance of ViewMap */
    public ViewMap() {
    }
    
    public static void outputHtml(World world, PrintWriter out, Player thisPlayer, Skin skin, String commands) throws DimxException {
        out.println("<HTML><HEAD>");
        out.println("<STYLE TYPE=\"text/css\">");
        out.println("#dot {  position: absolute;  left: 0;  top: 0;  z-index: 1}");
        out.println("BODY {margin:0;}");
        out.println("</STYLE>");
        out.println("</HEAD>");
        if (world != null && thisPlayer != null) {
            String map = world.map; // Default map will be world's default map;
            String mapx = "top.mapx";
            String mapy = "top.mapy";
            Room r = thisPlayer.getRoom();
            if (r != null) {
                Token tmap=r.varGet("map");
                Token tmapx=r.varGet("mapx");
                Token tmapy=r.varGet("mapy");
                if (!(tmap.isNull())) { // No room or no special map defined
                    map = Utils.absolutizeUrl(r.varGet("map").strVal(), world.imagesFolder); // Specify special map
                }
                if (!(tmapx.isNull())) {
                    mapx = tmapx.strVal();
                }
                if (!(tmapy.isNull())) {
                    mapy = tmapy.strVal();
                }
            }
            out.println("<BODY onLoad=\"parent.parent.locate_on_map("+mapx+","+mapy+");\">\n");
                if (world.debugging) {
                out.println("<FORM METHOD=POST TARGET=\"scene\" ACTION=\""+world.navigatorUrl+"\">");
                out.println("<INPUT TYPE=HIDDEN NAME=view VALUE=\"scene\">");
                out.println("<INPUT TYPE=HIDDEN NAME=cmd VALUE=\"coords\">");
                out.println("<INPUT TYPE=IMAGE NAME=map ID=\"map\" SRC=\"" + map + "\" STYLE=\"cursor:crosshair\">");
                out.println("</FORM>");
            } else {        
                out.println("<IMG ID=\"map\" SRC=\"" + world.map + "\" >");
            }
        }
        out.println("<IMG ID=\"dot\" SRC=\"" + skin.icoItem + "\" WIDTH=\"8\" HEIGHT=\"8\">");
        out.println("</BODY>");
        out.println("</HTML>");
    }
    
}
