package cleoni.adv;

/**
 * Produces an RSS-style journal of the 20 most recent events
 * Creation date: (03/01/2002 17.00.44)
 * @author: Cristiano Leoni
 */
import java.io.*;
/** Convenence object for log messages */
public class Journal {
    private final Object journalfilelock = new Object();

    private String journalFile = null;

    /**
     * Journal constructor comment.
     */
    public Journal(String aDebugFile) {
        super();
        journalFile = aDebugFile;
    }
    public void clear() {
        try {
            synchronized (journalfilelock) {
                BufferedWriter out = new BufferedWriter(
                new FileWriter(journalFile, false));
                out.write("This journal file was truncated at " + Utils.now() + " upon request of the administrator\n");
                out.close();
            }
        } catch (Exception e) {
            System.out.println("Cannot access file " + journalFile + " due to " + e.getClass().getName());
        }
    }
    public void newItem(String title,String link,String what,String categories) {
        try {
            if (journalFile==null) return;
            
            synchronized (journalfilelock) {

                String strLine;
                String[] myarray;
                myarray = new String[19];
                int j=0;

                try {
                FileInputStream in = new FileInputStream(journalFile);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                while (br.ready() && (j < myarray.length)) {
                    myarray[j] = br.readLine();
                    j++;
                }
                in.close();
                } catch (java.io.FileNotFoundException e) {
                    // Normal
                }

                BufferedWriter out = new BufferedWriter(new FileWriter(journalFile));
                title = Utils.stringReplace(title, "|", "/", false);
                what = Utils.stringReplace(what, "|", "/", false);
                what = what.replace("\r", "\\n");
                what = what.replace("\n", "\\n");

                link = Utils.stringReplace(link, "|", "%7C", false);
                categories = Utils.stringReplace(categories, "|", "/", false);
                out.write(Utils.now("E, dd MMM yyyy HH:mm:ss Z") + "|" + title + "|" + link + "|" +what + "|" + categories);
                out.write("\n");
                for (j = 0; ((j < myarray.length) && (myarray[j] != null)); j++){
                    out.write(myarray[j]);
                    out.write("\n");
                }
                out.close();
            }
        } catch (Exception e) {
            String x = "Cannot write to file " + journalFile + " due to " + e.getClass().getName();
            System.out.println(x);
        }
    }

}
