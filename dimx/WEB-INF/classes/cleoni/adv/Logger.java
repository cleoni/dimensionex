package cleoni.adv;

/**
 * Insert the type's description here.
 * Creation date: (03/01/2002 17.00.44)
 * @author:
 */
import java.io.*;
/** Convenence object for log messages */
public class Logger {
    private Object debugfilelock = new Object();
    
    private String debugFile = "/members/12gh8AAvhJ3geSl6ENKCNl9dH2u7g21A/debug.log"; // Poi modificata da service
    public int debugMode = 0; // 0=file 1=console 2=off
    private int indentLevel = 0;
    public boolean on = true;
    
    public void log(String what) {
        try {
            synchronized (debugfilelock) {
                BufferedWriter out = new BufferedWriter(
                new FileWriter(debugFile, true));
                out.write(indents());
                out.write(what);
                out.write("\n");
                out.close();
            }
        } catch (Exception e) {
            System.out.println("Cannot debug to file " + debugFile + " due to " + e.getClass().getName());
        }
    }
        public void log(Throwable x) {
        try {
            synchronized (debugfilelock) {
                PrintWriter out = new PrintWriter(
                new FileOutputStream(debugFile,true), true);
                x.printStackTrace(out);
            }
        } catch (Exception e) {
            System.out.println("Cannot debug to file " + debugFile + " due to " + e.getClass().getName());
        }
    }
    /**
     * Logger constructor comment.
     */
    public Logger(String aDebugFile, int aDebugMode) {
        super();
        if (aDebugFile != null) debugFile = aDebugFile;
        debugMode = aDebugMode;
        if (debugMode == 2) on = false;
    }
    public void clear() {
        try {
            synchronized (debugfilelock) {
                BufferedWriter out = new BufferedWriter(
                new FileWriter(debugFile, false));
                out.write("This logfile was truncated at " + Utils.now() + " upon request of the administrator\n");
                out.close();
            }
        } catch (Exception e) {
            System.out.println("Cannot access file " + debugFile + " due to " + e.getClass().getName());
        }
    }
    public void debug(String what) {
        if (debugMode != 2) {
            if (debugMode==0) {
                try {
                    synchronized (debugfilelock) {
                        BufferedWriter out = new BufferedWriter(
                        new FileWriter(debugFile, true));
                        out.write(indents());
                        out.write(what);
                        out.write("\n");
                        out.close();
                    }
                } catch (Exception e) {
                    System.out.println("Cannot debug to file " + debugFile + " due to " + e.getClass().getName());
                }
            } else {
                System.out.print(indents());
                System.out.println(what);
            }
        }
    }
    public void debug(Throwable x) {
        if (debugMode!=2) {
            if (debugMode==0) {
                try {
                    synchronized (debugfilelock) {
                        PrintWriter out = new PrintWriter(
                        new FileOutputStream(debugFile,true), true);
                        x.printStackTrace(out);
                    }
                } catch (Exception e) {
                    System.out.println("Cannot debug to file " + debugFile + " due to " + e.getClass().getName());
                }
            } else {
                x.printStackTrace(System.out);
            }
        }
    }
    
    private String indents() {
        if (indentLevel == 0) return "";
        else {
            StringBuffer sb = new StringBuffer("");
            for (int i=0; i<indentLevel; i++) {
                sb.append("\t");
            }
            return sb.toString();
        }
    }
    
    public void incIndent() {
        indentLevel++;
    }
    
    public void decIndent() {
        indentLevel--;
    }
    
    public static void echo(String what) {
        System.out.println(what);
    }
}
