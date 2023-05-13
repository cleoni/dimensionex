/*
 * ViewDirect.java
 *
 * Created on 9 giugno 2007, 11.00
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
public class ViewDirect extends View {
    
    /** Creates a new instance of ViewDirect */
    public ViewDirect() {
    }
    
   public static void outputHtml(World world, PrintWriter out, Player thisPlayer, Skin skin, String commands) {
        out.println(thisPlayer.getClient().getConsole());
    }
}
