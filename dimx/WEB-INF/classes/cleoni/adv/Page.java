/*
 * View.java
 *
 * Created on 7 settembre 2004, 13.47
 */

package cleoni.adv;

/** Implements a PAGE (formerly VIEW) tool.
 * May disappear in future releases.
 * @author CrLeoni
 */
public class Page extends ManageableObject {
    public String id = null;
    private String src = null;
    private String template = null;
    
    /** Creates a new instance of View */
    public Page(String strId, String strTemplate) throws DimxException {
        id = strId;
        src = strTemplate;
        
        // Fetch template
        template = Utils.fetch(strTemplate,"UTF-8");
    }
    
    public String toString() {
        return "PAGE " + id + " " + src;
    }
    
    public String toHtml(Dict replacements) {
        String s = Utils.stringReplace(template,replacements,true); // true=IGNORE CASE 
        return s;
    }
}
