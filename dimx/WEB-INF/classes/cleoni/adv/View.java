/*
 * View.java
 *
 * Created on 27 maggio 2005, 11.11
 */

package cleoni.adv;
import java.io.*;

/** This class is used to produce custom views for the game.
 *
 * You can implement a custom view by creating of a subclass of this one, and using
 * it in the multiplayer.service method.
 *
 * In future releases, custom views could be specified also via configuration so it
 * won't be acutally necessary to change and recopile source code.
 * @author Cristiano Leoni
 */
public class View {
    public static String htmlCharset(World world) {
        if (world == null)
            return "";
        else
            return world.htmlCharset();
    }

    public static void htmlWidthHeight(Image im, StringBuffer sb, double factorx) {
        int imwidth = im.getWidth();
        if (imwidth != 0) {
            sb.append("WIDTH=\"" + Utils.cInt(factorx*imwidth) + "\" HEIGHT=\"" + Utils.cInt(factorx*im.getHeight()) + "\" ");
        }
    }
    
    public static void htmlImage(Image im, StringBuffer sb, Client client, String alt, String aclass, String aid, String astyle) {
            double factorx = client.factorx;
                String src = im.getSrc();
                int p = Utils.instrRev(src, ".", false /* check case */);
                String extension = "";
                if (p>0) {
                    extension = src.substring(p+1,src.length()); 
                }
                if (extension.equalsIgnoreCase("MOV")) { 
                    sb.append("<OBJECT CLASSID=\"clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B\" ");
                    htmlWidthHeight(im,sb,factorx);
                    sb.append("CODEBASE=\"https://www.apple.com/qtactivex/qtplugin.cab\">");
                    sb.append("<PARAM name=\"SRC\" VALUE=\""+ im.getSrc() + "\">");
                    sb.append("<PARAM name=\"AUTOPLAY\" VALUE=\"true\">\n<PARAM name=\"CONTROLLER\" VALUE=\"false\">");
                    sb.append("<EMBED SRC=\""+ im.getSrc() + "\" ");
                    htmlWidthHeight(im,sb,factorx);
                    sb.append("AUTOPLAY=\"true\" CONTROLLER=\"false\" PLUGINSPAGE=\"http://www.apple.com/quicktime/download/\"></EMBED>\n</OBJECT>");
                } else if (extension.equalsIgnoreCase("MPG") || extension.equalsIgnoreCase("MPEG")) {
                    sb.append("<EMBED SRC=\""+ im.getSrc() + "\" ");
                    htmlWidthHeight(im,sb,factorx);
                    sb.append("AUTOSTART=\"true\"");
                    sb.append("SHOWCONTROLS=\"false\"");
                    sb.append("AUTOPLAY=\"1\"");
                    sb.append("LOOP=\"1\"");
                    sb.append("pluginspage=\"https://www.apple.com/quicktime/download/\"</EMBED>\n</OBJECT>");
                } else {
                    sb.append("<img border=\"0\" ");
                    htmlWidthHeight(im,sb,factorx);
                    sb.append("src=\""+ im.getSrc() + "\" alt=\"" + alt + "\" title=\"" + alt + "\"");
                    if (aid!=null) {
                        sb.append(" id=\""+aid+"\"");
                    }
                    if (aclass!=null) {
                        sb.append(" class=\""+aclass+"\"");
                    }
                    if (astyle!=null) {
                        sb.append(" style=\""+astyle+"\"");
                    }
                    sb.append(">");
                }
    }
    public static void outputHtml(World world, PrintWriter out, Skin skin)  throws DimxException {
        out.println("undefined");
    }
    public static void outputHtml(World world, PrintWriter out, Player thisPlayer, Skin skin, String commands) throws DimxException {
        out.println("undefined");
    }
}
