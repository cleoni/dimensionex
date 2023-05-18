package cleoni.adv;

/**
 * Collezione di utilities per leoni.adv
 *
 * 2008.03.02 Carl Nagle Added Include support in fetchIncludes().
 * @author Cristiano Leoni
 */
import java.util.Vector;
import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Enumeration;
import java.util.Locale;

/** Miscellaneous utilities. Most of them can be called as static methods (ie without instancing the Utils object). */
public class Utils {
    private javax.servlet.http.HttpServletRequest request;
    private javax.servlet.http.HttpServletResponse response;
    
    /** Utils constructor
     * @param aRequest
     * @param aResponse
     */
    public Utils(javax.servlet.http.HttpServletRequest aRequest, javax.servlet.http.HttpServletResponse aResponse) {
        super();
        request = aRequest;
        response = aResponse;
    }
    public static String absolutizeUrl(String aUrl, String baseHref) {
        if (aUrl == null || aUrl.equals("") || aUrl.substring(0,1).equals("/") || aUrl.indexOf(":/") > 0) {
            // Nullo o Gi� assoluto
            return aUrl;
        }
        if (baseHref.substring(baseHref.length()-1)!="/") { // If final slash missing, then add it
            baseHref=baseHref+"/";
        }
        return baseHref + aUrl;
    }
    /**
     * Converte in un double
     */
    public static double cDbl(long x) {
        return (double) x;
    }
    /**
     * Converte in un double
     */
    public static double cDbl(Object x) {
        return cDbl(cStr(x));
    }
    /**
     * Converte in un double
     */
    public static double cDbl(String x) {
        try {
            return new Double(x).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }
    /**
     * Converts to int
     */
    public static int cInt(double x) {
        return new Double(x).intValue();
    }
    /**
     * Converts to int
     */
    public static int cInt(float x) {
        return new Float(x).intValue();
    }
    /**
     * Converte in un intero
     */
    public static int cInt(long x) {
        return new Long(x).intValue();
    }
    /**
     * Converte in un intero
     */
    public static int cInt(Object x) {
        return cInt(cStr(x));
    }
    /**
     * Converte in un intero
     */
    public static int cInt(String x) {
        int ret;
        try {
            ret = new Double(Double.parseDouble(x)).intValue();
        } catch(Exception e) {
            ret = 0;
        }
        return ret;
    }
    /**
     * Converts to int
     */
    public static int cInt(boolean x) {
        if (x)
            return 1;
        else
            return 0;
    }
    /**
     * Converte in un long
     */
    public static long cLng(double x) {
        return new Double(x).longValue();
    }
    /**
     * Converte in un long
     */
    public static long cLng(float x) {
        return new Float(x).longValue();
    }
    /**
     * Converte in un long
     */
    public static long cLng(int x) {
        return (long) x;
    }
    /**
     * Converte in un long
     */
    public static long cLng(Object x) {
        return cLng(cStr(x));
    }
    /**
     * Converte in un long
     */
    public static long cLng(String x) {
        long ret;
        try {
            ret = Long.parseLong(x);
        } catch(Exception e) {
            ret = 0;
        }
        return ret;
    }
    /**
     * Converte in una stringa valida
     */
    public static String cStr(char x) {
        char matr[] = {'?'};
        matr[0] = x;
        return new String(matr);
    }
    /**
     * Converte in una stringa valida
     */
    public static String cStr(double x) {
        return Double.toString(x);
    }
    /**
     * Converte in una stringa valida
     */
    public static String cStr(float x) {
        return Float.toString(x);
    }
    /**
     * Converte in una stringa valida
     */
    public static String cStr(int x) {
        return Integer.toString(x);
    }
    /**
     * Converte in una stringa valida
     */
    public static String cStr(long x) {
        return Long.toString(x);
    }
    /**
     * Converte in una stringa valida
     */
    public static String cStr(Object x) {
        if (x==null) {
            return "";
        } else {
            if (x.getClass().isArray()) {
                // si tratta di array di oggetti "mascherato"
                // richiama ricorsivamente cStr sui componenti
                
                String catStr = "";
                StringBuffer buf= new StringBuffer("");
                if (x==null) {
                    return "";
                } else {
                    for(int i = 0; i < java.lang.reflect.Array.getLength(x); i++) {
                        buf.append(catStr + cStr(java.lang.reflect.Array.get(x,i)));
                        catStr = ",";
                    }
                }
                return "[" + buf.toString() + "]";
            } else if (x instanceof java.util.Vector) {
                return cStr((java.util.Vector) x);
            } else {
                return x.toString();
            }
        }
    }
    /**
     * Converte in una stringa valida
     */
    public static String cStr(java.util.Vector v) {
        String catStr="";
        StringBuffer buf= new StringBuffer("");
        if (v==null) {
            return "";
        } else {
            for(int i = 0; i < v.size(); i++) //prompt user for input
            {
                buf.append(catStr + cStr(v.elementAt(i)));
                catStr = ",";
            }
        }
        return "{" + buf.toString() + "}";
    }
    public static String cTimeStamp(java.util.Date aDate) {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(aDate);
    }

  
    /**
     * <p>Compares two <code>doubles</code> for order.</p>
     *
     * <p>This method is more comprehensive than the standard Java greater
     * than, less than and equals operators.</p>
     * <ul>
     *  <li>It returns <code>-1</code> if the first value is less than the second.
     *  <li>It returns <code>+1</code> if the first value is greater than the second.
     *  <li>It returns <code>0</code> if the values are equal.
     * </ul>
     *
     * <p>
     * The ordering is as follows, largest to smallest:
     * <ul>
     *  <li>NaN
     *  <li>Positive infinity
     *  <li>Maximum double
     *  <li>Normal positive numbers
     *  <li>+0.0
     *  <li>-0.0
     *  <li>Normal negative numbers
     *  <li>Minimum double (-Double.MAX_VALUE)
     *  <li>Negative infinity
     * </ul>
     * </p>
     *
     * <p>Comparing <code>NaN</code> with <code>NaN</code> will
     * return <code>0</code>.</p>
     *
     * @param lhs  the first <code>double</code>
     * @param rhs  the second <code>double</code>
     * @return <code>-1</code> if lhs is less, <code>+1</code> if greater,
     *  <code>0</code> if equal to rhs
     */
    public static int compareDouble(double lhs, double rhs) {
        if (lhs < rhs) {
            return -1;
        }
        if (lhs > rhs) {
            return +1;
        }
        // Need to compare bits to handle 0.0 == -0.0 being true
        // compare should put -0.0 < +0.0
        // Two NaNs are also == for compare purposes
        // where NaN == NaN is false
        long lhsBits = Double.doubleToLongBits(lhs);
        long rhsBits = Double.doubleToLongBits(rhs);
        if (lhsBits == rhsBits) {
            return 0;
        }
        // Something exotic! A comparison to NaN or 0.0 vs -0.0
        // Fortunately NaN's long is > than everything else
        // Also negzeros bits < poszero
        // NAN: 9221120237041090560
        // MAX: 9218868437227405311
        // NEGZERO: -9223372036854775808
        if (lhsBits < rhsBits) {
            return -1;
        } else {
            return +1;
        }
    }
    
    /**
     * This method was created in VisualAge.
     */
    public static String dbString(String aString) {
        return stringReplace(aString,"'","''",false);
    }
/*
 * Run a command query
 *
 */
    public static boolean executeSQLCommand(String sql,java.sql.Connection con,boolean tolerateErrors,boolean failOnZeroCount) throws DimxException {
        java.sql.Statement stmt = null;
        try {
            if (con == null) {
                Logger.echo("executeStandardUpdate: Impossibile eseguire la query - connessione nulla");
                return false;
            }
            stmt = con.createStatement();
            if (stmt == null) {
                Logger.echo("executeStandardUpdate: Impossibile creare oggetto statement");
                return false;
            }
            stmt.execute(sql);
            if (failOnZeroCount && stmt.getUpdateCount() == 0) {
                return false;
            } else {
                return true;
            }
        } catch (Exception ex) {
            if (tolerateErrors)
                return false;
            else
                throw new DimxException("DB Query problem: " + ex.toString());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (Exception ex) {}
        }
    }
    
    public static java.sql.ResultSet queryDatabase(String sql,java.sql.Statement st,boolean tolerateErrors) throws DimxException {
        if (st != null) {
            java.sql.ResultSet rset = null;
            try {
                rset = st.executeQuery(sql);
                //st.close();
            } catch(Exception ex) {
                if (!tolerateErrors)
                    throw new DimxException("DB Query problem: " + ex.toString());
            }
            return rset;
        } else {
            throw new DimxException("DB Query problem: Missing DB connection.");
        }
    }
    public String getCookie(String key) throws DimxException {
        // Gets a cookie from the users' client
        
        if (request != null) {
            javax.servlet.http.Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                // no cookies
                return null;
            } else {
                for (int i=0; i < cookies.length; i++) {
                    if (cookies[i].getName().equals(key)) { // Found!
                        return cookies[i].getValue();
                    }
                }
                return null; // Not found
            }
        } else {
            return null; // No request defined
        }
    }
    /**
     * Legge dal canale di input (form POST o GET)
     * il dato nel campo con chiave specificata
     *
     * NB: Proabilmente non � adatta per liste multiselezione
     * in quanto torna una String e non un vettore
     * @return java.lang.String
     */
    public String getForm(String aKey) {
        String ret;
        try {
            ret  =  (request.getParameterValues(aKey))[0];
        } catch (Exception ex) {
            ret = "";
        }
        return ret;
    }
    
/*
 * Returns a set of pairs key=value it gets from input
 */
    public DictSorted getForm() {
        DictSorted res = new DictSorted();
        
        for (Enumeration e = request.getParameterNames();e.hasMoreElements() ;) {
            String key = (String) e.nextElement();
            //This will now work for multiselect as we only consider the 1st element of the array
            res.put(key,new Token(request.getParameterValues(key)[0]));
        }
        
        return res;
    }
    
/*
 * This one was introduced by Marcus Chan for chinese support
 * charset = "GB2312"
 */
    public String getForm(String aKey,String charset) {
        String ret;
        try {
            ret = new String(request.getParameter(aKey).getBytes("ISO-8859-1"), charset);
        } catch (Exception ex) {
            ret = "";
        }
        return ret;
    }
    protected static String getOppositeDirection(String dir) {
        if (!dir.equals("")) {
            if (dir.equals("N")) {
                return "S";
            } else if (dir.equals("S")) {
                return "N";
            } else if (dir.equals("W")) {
                return "E";
            } else if (dir.equals("E")) {
                return "W";
            } else if (dir.equals("U")) {
                return "D";
            } else if (dir.equals("D")) {
                return "U";
            }
        }
        return "";
    }
    /**
     * Torna il riferimento alla sessione attuale
     * (se esiste, senn� torna null)
     * NB questa classe non crea sessioni
     * @return javax.servlet.http.HttpSession
     */
    public javax.servlet.http.HttpSession getSession() {
        if (request != null) {
            return request.getSession(false);
        } else {
            return null;
        }
    }
    /**
     * Torna il riferimento all'oggetto prelevato dall'area
     * Session (se esiste, senn� torna null)
     * NB questa classe non crea sessioni
     * @return javax.servlet.http.HttpSession
     */
    public Object getSession(String key) {
        javax.servlet.http.HttpSession session = getSession();
        if (session != null) {
            return session.getAttribute(key);
        } else { // niente sessione!
            return null;
        }
    }
    /**
     * Come getSession, ma con cast a String
     *
     * equivalente alla Session("key") di VBScript
     */
    public String gSession(String key) {
        Object ret;
        ret = getSession(key);
        if (ret == null) {
            return "";
        } else {
            return (String) ret;
        }
    }

   
    /**
     * @return java.lang.String current date/time, nicely formatted US locale
     */
    public static String now() {
        java.text.DateFormat dateFrmtCurrentDate = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.LONG,java.text.DateFormat.LONG,java.util.Locale.US) ;
        String currentDate = dateFrmtCurrentDate.format(new java.util.Date());
        
        return currentDate;
    }
    /**
     * @return java.lang.String current date/time, nicely formatted as specified
     */
    public static String now(String format) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat(format,java.util.Locale.US);
        
        return sdf1.format(cal.getTime());
    }
    /**
     * Sets the no-cache criterion for the current page.
     * STRONG version - After this one the page cannot be navigated OFF-LINE
     */
    public void pageExpires() {
        response.setDateHeader("Last-Modified", (new java.util.Date()).getTime());
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setDateHeader("Expires", 0);
    }
    /**
     * Manda un pacchetto TCP al server/porta specificati
     * torna comunque una String
     */
    public String ping(String machineName, int portInt) {
        String buf = "";
        try {
            java.net.InetAddress ia = java.net.InetAddress.getByName(machineName);
            java.net.Socket sc = new java.net.Socket( ia, portInt );
            sc.close();
            
            buf = "OK: " + ia.toString();
        } catch ( Exception e ) {
            buf = "ERROR: Could not connect to " + machineName + ":" + portInt + "\n";
            buf = buf + e;
        }
        return buf;
    }
    /** Returns the number r (which must be realmin <= r <= realmax) proportioned to a
     * new interval: targmin .. targmax
     * @param r Number to be reproportioned
     * @param realmin Minimum real value for r
     * @param realmax Maximum real value for r
     * @param targmin New minimum for result
     * @param targmax New maximum for result
     * @return result: reproportioned r
     */
    public static int proportion(int r, int realmin, int realmax, int targmin, int targmax) {
        return targmin + r * (targmax - targmin) / (realmax - realmin);
    }
    
    /**
     * Equivale ala Response.Redirect di VBScript
public void redirect(String aUrl) {
        try {
                ((com.sun.server.http.HttpServiceResponse) response).sendRedirect(aUrl);
        } catch (Exception e) {
                // errore ioException
                Utils.debug("BvUtils.Redirect: ioException per " + aUrl);
        }
}
     */
    /**
     * Equivale alla Session(key) = anObj
     * in VBScript
     */
    public void setSession(String key, Object anObj) {
        javax.servlet.http.HttpSession session = getSession();
        if (session != null) {
            if (anObj != null) {
                session.setAttribute(key, anObj);
            } else {
                session.removeAttribute(key);
            }
        } else { // niente sessione!
            if (request != null) {
                session = request.getSession(true);
                session.setAttribute(key, anObj);
            } else {
                // impossibile creare una sessione
                Logger.echo("Utils.setSession: manca oggetto request per creare la sessione");
            }
        }
    }
    /**
     * Replaces all occurrences of a substring with another specified substring
     */
    public static java.lang.String stringReplace(java.lang.String target, java.lang.String searchfor, java.lang.String substwith, boolean ignorecase) {
        
        String buf = "";
        int x;
        if (target != null) {
            while ((x = instr(target,searchfor,ignorecase)) >= 0) {
                buf = buf + target.substring(0,x) + substwith;
                target = target.substring(x+searchfor.length());
            }
            buf = buf + target;
        }
        return buf;
    }
    public static String stringReplace(String target, Dict replacements, boolean ignorecase) {
        int count;
        
        for (int i=0; i < replacements.size(); i++) {
            String k = replacements.keyAt(i);
            target = stringReplace(target,replacements.keyAt(i),(String) replacements.elementAt(i),ignorecase);
        }
        return target;
    }
    /**
     * Splits the specified string by using the specified separator
     * returns a Vector of objects
     * If problems, returns a single-valued vector containing the original string
     */
    public static Vector stringSplit(String valuestring, String separator) {
        if (valuestring.length() == 0 && separator.length() > 0) return new Vector(); // Empty vector is returned
        String[] strings; 
        strings = valuestring.split(java.util.regex.Pattern.quote(separator)); // Now this is why I think Java is a stupid language: it doesn't have even a plain and simple split function
        Vector v = new Vector(java.util.Arrays.asList(strings));
        return v;
    }
    
    /**
     * Similar to stringSplit, tolerant version
     * the separator could also not be there (this version of stringSplit was used until version 6.4.6a)
     */
    public static Vector stringSplit_tolerant(String valuestring, String separator) {
        Vector v = new Vector();
        String val;
        try {
            java.util.StringTokenizer st = new java.util.StringTokenizer(valuestring,separator);
            while (st.hasMoreTokens()) {
                val=st.nextToken();
                v.addElement(val); // add e basta
            }
        } catch (Exception e) {
            v.addElement(valuestring);
        }
        return v;
    }

    
/* similar to stringSplit
 * Assumes it is a pair so it stops after first occureence
 */
    public static Vector stringSplitPair(String valuestring, String separator) {
        Vector v = new Vector();
        String val;
        int pos = valuestring.indexOf(separator);
        if (pos > 0 ) {
            v.addElement(valuestring.substring(0,pos));
            v.addElement(valuestring.substring(pos+1));
        } else {
            v.addElement(valuestring);
        }
        return v;
    }

   
    /*
     * Transforms a string into a set (Dict)
     * If missing 2nd element uses 1st one as 2nd
     * If cheat = true then a,b,c is transformed into a=a,b=b,c=c
     */
    public static Dict string2set(String valuestring, String sep1, String sep2, boolean cheat) {
        java.util.Vector list = Utils.stringSplit(valuestring,sep1);
        Dict res = new Dict();
        for (int i=0; i < list.size(); i++) {
            String s1 = (String) list.elementAt(i);
            java.util.Vector couple = Utils.stringSplit(s1,sep2);
            if (couple.size() == 1) {
                if (cheat) {
                    couple.add(1, couple.elementAt(0));
                } else {
                    couple.add(1, "");
                }
            } 
            
            res.put((String) couple.elementAt(0),(String) couple.elementAt(1));
        }
        return res;
    }
    
    public static DictSorted string2setTokens(String valuestring, String sep1, String sep2, boolean unescape) {
        java.util.Vector list = Utils.stringSplit(valuestring,sep1);
        DictSorted res = new DictSorted();
        for (int i=0; i < list.size(); i++) {
            String s1 = (String) list.elementAt(i);
            java.util.Vector couple = Utils.stringSplit(s1,sep2);
            String k = (String) couple.elementAt(0);
            if (unescape) {
                k = unescapeChars(k, sep1+sep2);
            }
            if (couple.size() > 1) {
                String v = (String) couple.elementAt(1);
                if (unescape) {
                    v = unescapeChars(v, sep1+sep2);
                }
                Token tv = new Token(v);
                if (tv.getLikelyType() == 'N') tv.setNumber(); // If looks like a number, make it a number
                res.put(k,tv);
            } else {
                if (Utils.instr(s1,sep2,false) >= 0) { // Separator n.2 ("=") was there - empty value
                    res.put(k,new Token());
                } else {
                    res.put(k,new Token());
                }
            }
        }
        return res;
    }
    
    /**
     * Legge un parametro dalla form in ingresso
     * se non lo trova cerca in Session
     * @return javax.servlet.http.HttpSession
     */
    public String getFormSession(String key) {
        String s = getForm(key);
        if (s == null || s.equals("")) {
            s = gSession(key);
        }
        return s;
    }
    
    
    protected static String getFileExtension(String name) {
    int lastIndexOf = name.lastIndexOf(".");
    if (lastIndexOf == -1) {
        return ""; // empty extension
    }
    return name.substring(lastIndexOf+1);
}
    
    protected static String getRelativeDirection(String dir, String facing) {
        if (facing.equals("N")) {
            return dir;
        } else if (facing.equals("S")) {
            if (dir.equals("N")) {
                return "S";
            } else if (dir.equals("E")) {
                return "W";
            } else if (dir.equals("S")) {
                return "N";
            } else if (dir.equals("W")) {
                return "E";
            } else
                return dir;
        } else if (facing.equals("E")) {
            if (dir.equals("N")) {
                return "W";
            } else if (dir.equals("E")) {
                return "N";
            } else if (dir.equals("S")) {
                return "E";
            } else if (dir.equals("W")) {
                return "S";
            } else
                return dir;
        } else if (facing.equals("W")) {
            if (dir.equals("N")) {
                return "E";
            } else if (dir.equals("E")) {
                return "S";
            } else if (dir.equals("S")) {
                return "W";
            } else if (dir.equals("W")) {
                return "N";
            } else
                return dir;
        } else {
            return dir;
        }
    }
    
    public static String getSettingDB(String key,String defvalue,java.sql.Connection dbConn,String table) throws DimxException {
        key = stringReplace(key,"'", "''",false);
        java.sql.Statement stmt = null;
        try {
            stmt = dbConn.createStatement();
            String sql = "SELECT `value` FROM `" + table + "` WHERE akey='" + key + "'";
            java.sql.ResultSet rs = Utils.queryDatabase(sql, stmt,  false);
            if (rs == null) {
                return defvalue;
            }
            try {
                if ( rs.next() )  {
                    return rs.getString(1);
                }
            } catch (Exception e) {
                throw new DimxException("DB Query result error: " + e.toString());
            } finally {
                rs.close();
            }
        } catch (java.sql.SQLException ex) {
            throw new DimxException("DB Problem: " + ex.toString());
        } finally {
            try {
                stmt.close();
            } catch (Exception ex) {}
        }
        return defvalue;
    }
    
    public static String getSystemDir(ServletContext context) {
        String systemDir = "?";
        Logger.echo("Auto-detecting system folder....");
        String str = context.getRealPath("");
        Logger.echo("RealPath(1st trial)="+str);
        if (str == null) {
            str = context.getRealPath(".");
            Logger.echo("RealPath(2nd trial)="+str);
        }
        //response.getWriter().println ("RealPath="+str+" request"+request.getServletPath());
        if (str != null) {
            str = Utils.stringReplace(str,"\\","/",false);
            systemDir = /* str.substring(0,str.length()-1)*/ str + "/WEB-INF/system/";
        }
        Logger.echo("Auto-detected the following system folder: " + systemDir);
        return systemDir;
    }
    
    public static Dict getWaysSorted(Dict things, String from, String facing) {
        Dict d = new Dict(1,1,false);
        
        // UPS
        for (int i=0; i < things.size(); i++) {
            Link o = (Link) things.elementAt(i);
            if (o.getDirection(from).equals("U")) d.put(o.id,o);
        }
        
        // N
        for (int i=0; i < things.size(); i++) {
            Link o = (Link) things.elementAt(i);
            if (Utils.getRelativeDirection(o.getDirection(from),facing).equals("N")) d.put(o.id,o);
        }
        // W
        for (int i=0; i < things.size(); i++) {
            Link o = (Link) things.elementAt(i);
            if (Utils.getRelativeDirection(o.getDirection(from),facing).equals("W")) d.put(o.id,o);
        }
        // E
        for (int i=0; i < things.size(); i++) {
            Link o = (Link) things.elementAt(i);
            if (Utils.getRelativeDirection(o.getDirection(from),facing).equals("E")) d.put(o.id,o);
        }
        // S
        for (int i=0; i < things.size(); i++) {
            Link o = (Link) things.elementAt(i);
            if (Utils.getRelativeDirection(o.getDirection(from),facing).equals("S")) d.put(o.id,o);
        }
        
        // DOWNS
        for (int i=0; i < things.size(); i++) {
            Link o = (Link) things.elementAt(i);
            if (o.getDirection(from).equals("D")) d.put(o.id,o);
        }
        
        // NO DIRECTION
        for (int i=0; i < things.size(); i++) {
            Link o = (Link) things.elementAt(i);
            if (o.getDirection(from).equals("")) d.put(o.id,o);
        }
        
        return d;
    }
    
    /**
     *
     * @param target
     * @param searchfor
     * @param startpos
     * @param ignoreCase
     * @return -1 if not found. If searchfor = null or "" also returns -1
     */
    public static int instr(String target, String searchfor, int startpos, boolean ignoreCase) {
        int tl = target.length();
        if (searchfor == null || searchfor.equals("")) return -1;
        int sl = searchfor.length();
        
        if (tl >= sl) {
            for (int i=startpos; i <= tl-sl; i++) {
                if (ignoreCase) {
                    if (target.substring(i,i+sl).equalsIgnoreCase(searchfor)) {
                        return i;
                    }
                } else {
                    if (target.substring(i,i+sl).equals(searchfor)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    
    /**
     *
     * @param target
     * @param searchfor
     * @param ignoreCase
     * @return -1 if not found. If searchfor = null or "" also returns -1
 
     */
    public static int instr(String target, String searchfor, boolean ignoreCase) {
        int tl = target.length();
        if (searchfor == null || searchfor.equals("")) return -1;
        int sl = searchfor.length();
        
        if (tl >= sl) {
            for (int i=0; i <= tl-sl; i++) {
                if (ignoreCase) {
                    if (target.substring(i,i+sl).equalsIgnoreCase(searchfor)) {
                        return i;
                    }
                } else {
                    if (target.substring(i,i+sl).equals(searchfor)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    
    public static int instrCount(String target, char searchfor) {
        int count = 0;
        
        for (int i=0; i < target.length(); i++) {
            if (target.charAt(i) == searchfor) count++;
        }
        return count;
    }
    
    public static int instrCount(String target, String searchfor, boolean ignoreCase) {
        int count = 0;
        
        int tl = target.length();
        int sl = searchfor.length();
        
        //if (sl == 0) return 0; // By convention - if substring null then return 0
        
        if (tl >= sl) {
            for (int i=0; i <= tl-sl; i++) {
                if (ignoreCase) {
                    if (target.substring(i,i+sl).equalsIgnoreCase(searchfor)) {
                        count++;
                    }
                } else {
                    if (target.substring(i,i+sl).equals(searchfor)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    public static int instrRev(String target, String searchfor, boolean ignoreCase) {
        int tl = target.length();
        int sl = searchfor.length();
        
        if (tl >= sl) {
            for (int i=tl-sl; i >= 0; i--) {
                if (ignoreCase) {
                    if (target.substring(i,i+sl).equalsIgnoreCase(searchfor)) {
                        return i;
                    }
                } else {
                    if (target.substring(i,i+sl).equals(searchfor)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    
    public static boolean isIn(String s, String[] v) {
        // Determines whether a string is contained in an array of strings
        if (s == null) return false;
        int i = 0;
        while (i < v.length) {
            if (s.equalsIgnoreCase(v[i]))
                return true;
            i++;
        }
        return false;
    }
    
    public static boolean isInCsens(String s, String[] v) {
        // Determines whether a string is contained in an array of strings
        if (s == null) return false;
        int i = 0;
        while (i < v.length) {
            if (s.equals(v[i]))
                return true;
            i++;
        }
        //Logger.echo("nope: " + s + " is not in: " + arrayToString(v));
        return false;
    }
    
    public static String[] array_merge(String[] pa, String[] pb) {
        String[] arr = new String[pa.length + pb.length];
        for (int x=0; x < pa.length; x++) {
                arr[x] = pa[x];
        }
        for (int x=0; x < pb.length; x++) {
                arr[x+pa.length] = pb[x];
        }
        return arr;
    }
    public static String arrayToString(String[] v) {
        StringBuffer buf = new StringBuffer("[");
        for (int i=0; i<v.length; i++) {
            buf.append(v[i]);
            if (i<v.length-1) buf.append(",");
        }
        buf.append("]");
        return buf.toString();
    }
    public static boolean isIn(String s, Dict v) {
        // Determines whether a string is contained in a Dict
        return v.containsKey(s);
    }
    
    public static int indexOf(Token t, Dict v, boolean caseSensitive) {
        // Determines whether a string is contained in a Dict (values)
        String tstring = t.strVal();
        for (int i = 0; i < v.size(); i++) {
            if (caseSensitive) {
                if (tstring.equals(((Token) v.elementAt(i)).strVal())) return i;
            } else {
                if (tstring.equalsIgnoreCase(((Token) v.elementAt(i)).strVal())) return i;
            }
        }
        return -1;
    }
    
    public static int indexOf(String s, String[] v) {
        if (s == null) return -1;
        int i = 0;
        while (i < v.length) {
            if (s.equalsIgnoreCase(v[i]))
                return i;
            i++;
        }
        return -1;
        
    }
    
    public static boolean isIn(String searchfor, String target) {
        boolean found = false;
        
        int tl = target.length();
        int sl = searchfor.length();
        
        if (tl >= sl) {
            for (int i=0; i <= tl-sl; i++) {
                if ((target.substring(i,i+sl)).equalsIgnoreCase(searchfor)) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }
    
    public static boolean isIn(String s, Vector v) {
        // Determines whether a string is contained in a set of strings
        if (v==null) return false;
        int i = 0;
        boolean found = false;
        while (!found && i < v.size()) {
            if (s.equalsIgnoreCase((String) v.elementAt(i)))
                found = true;
            i++;
        }
        return found;
    }
    
    
    public static String leadingZeroes(int n, int howmany) {
        String s = "0000000" + n;
        return s.substring(s.length()-howmany);
    }
    protected static String nextFace(String face, String direction) {
        if (direction.equals("r")) {
            if (face.equals("N")) {
                return "E";
            } else if (face.equals("E")) {
                return "S";
            } else if (face.equals("S")) {
                return "W";
            } else { // face.equals("W")
                return "N";
            }
        } else {
            if (face.equals("N")) {
                return "W";
            } else if (face.equals("W")) {
                return "S";
            } else if (face.equals("S")) {
                return "E";
            } else { // face.equals("W")
                return "N";
            }
        }
    }
    
    public static String rotate(AdvObject r, String origFacing, String direction) {
        // rotates the specified character, originally facing as specified,
        // in the specified direction until the next room image.
        // returns the final facing direction
        String facing = origFacing;
        
        if (r.hasSeveralFaces()) {
            if (direction.equals("b")) { // Means "back", get opposite direction
                facing = Utils.getOppositeDirection(facing);
            } else {
                facing = Utils.nextFace(facing,direction); // Prossima faccia a dx
                while (r.getExactImage(facing) == null && facing != origFacing) {
                    facing = Utils.nextFace(facing,direction);
                }
            }
        }
        return facing;
    }
    
    public static boolean saveSettingDB(String key,String value,java.sql.Connection con,String table) throws DimxException {
        //Uncomment the following lines if you want to have
        //trimming of the key if it exceeds 255 chars
        //int rightpoint = 255-Utils.instrCount(key,'\'')*2;
        //if (rightpoint < key.length()) key = key.substring(0,rightpoint);
        
        key = stringReplace(key,"'", "''",false);
        value = stringReplace(value,"'", "''",false);
        String sql = "UPDATE `" + table + "` SET `value`='" + value + "' WHERE akey = '" + key + "'";
        if (!executeSQLCommand(sql, con,true, true)) {
            sql = "INSERT INTO `" + table + "` (akey,`value`) VALUES ('" + key + "','" + value + "')";
            return executeSQLCommand(sql, con, false, true);
        }
        return true;
    }
/*
 * NOTE: Cookie values MUST comply Netscape specification or, if not,
 * they must be encoded using URLENcode or Base64!
 */
    public void setCookie(String key, String value) throws DimxException {
        // Sets a cookie in the users' client
        if (response != null) {
            javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie(key,value);
            cookie.setPath("/");
            cookie.setSecure(false);
            cookie.setMaxAge(7776000); // Three months
            response.addCookie(cookie);
        } else {
            // No connection - do nothing
        }
    }
    
    public static String stringFlatten(String str) {
        // Returns the flattened string
        if (str != null) {
            return stringReplace(str.toLowerCase()," ","_",false);
        } else
            return null;
    }
    
    
    public static String getParentFolder(String ofFolder) {
        String xx = ofFolder.substring(0,ofFolder.length()-1);
        int xpos = Utils.instrRev(xx,"/",false);
        if ((xpos < 8) && (ofFolder.startsWith("https://") || ofFolder.startsWith("http://"))) return ofFolder; // If root web URL then skip cutting!
        return ofFolder.substring(0,xpos+1);
    }
    
    public static String getRidOfParentFolder(String ofFolder) {
        String xx = ofFolder.substring(0,ofFolder.length()-1);
        int xpos = Utils.instrRev(xx,"/",false);
        return ofFolder.substring(xpos+1);
    }
    /** 
     * Fetches a file from DimensioneX from disk or from the net. The file must be
     * encoded in UTF-8. Also ANSI ASCII is OK, but localised characters will not be
     * recognised correctly (use UTF-8 for this).
     * @param filePath complete path of the file to be fetched. For network fetch, must begin with
     * "http://"
     * @throws DimxException in case of problems
     * @return the fetched file, as a single String
     */
    public static String fetch(String filePath, String encoding) throws DimxException {
        BufferedReader in;        
        StringBuffer sb = new StringBuffer();
        String thisLine;
        try {
            if (filePath.startsWith("https://") || filePath.startsWith("http://")) {
                // Get from network
                URL url = new URL(filePath);
                URLConnection urlconnection = url.openConnection();                                
                if (encoding.equals("UTF-8")) {
                    in = new BufferedReader(new InputStreamReader(urlconnection.getInputStream(), "UTF-8"));
                } else {
                    in = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));
                }
            } else {
                // Get from file                
                if (encoding.equals("UTF-8")) {
                    in = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),"UTF-8"));
                } else {
                    in = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
                }
            }
            while ((thisLine = in.readLine()) != null) 
                sb.append(thisLine + "\n");
            in.close(); in = null;
            
        } catch (IOException e) {
            throw new DimxException("Problem fetching or loading: \"" + filePath + "\" (" + e.toString() + ")");
        } catch (Exception e) {
            throw new DimxException("Problem fetching or loading: \"" + filePath + "\" (" + e.getMessage() + ")");
        }
        return sb.toString();
    }
    
    /** 
     * Fetches a file from DimensioneX from disk or from the net. The file must be
     * encoded in UTF-8. Also ANSI ASCII is OK, but localised characters will not be
     * recognised correctly (use UTF-8 for this).
     * 
     * Supports 'Include "filename"' to recursively fetch "included" files.  
     * However, Include statements are ignored inside SCRIPTS and EVENTS tags 
     * to retain existing functionality. The Included FILENAME specification is 
     * assumed to be relative to the original filePath.  That is, it is assumed 
     * to be in the same directory.
     * 
     * @param filePath complete path of the file to be fetched. For network fetch, must begin with
     * "http://"
     * @throws DimxException in case of problems
     * @return the fetched file, as a single String
     */
    public static String fetchIncludes(String filePath, String encoding) throws DimxException {
        BufferedReader in;        
        StringBuffer sb = new StringBuffer();
        try {
            if (filePath.startsWith("https://") || filePath.startsWith("http://")) {
                // Get from network
                URL url = new URL(filePath);
                URLConnection urlconnection = url.openConnection();                                
                if (encoding.equals("UTF-8")) {
                    in = new BufferedReader(new InputStreamReader(urlconnection.getInputStream(), "UTF-8"));
                } else {
                    in = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));
                }
            } else {
                // Get from file                
                if (encoding.equals("UTF-8")) {
                    in = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),"UTF-8"));
                } else {
                    in = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
                }
            }
            String thisLine;
            String thisLineUC;
            boolean inEvents = false;
            boolean inScripts = false;

            while ((thisLine = in.readLine()) != null) {
                thisLineUC = thisLine.toUpperCase().trim();
                if (thisLineUC.startsWith("INCLUDE")){
                    if(inEvents || inScripts){
                        sb.append(thisLine + "\n");
                    }else{
                        String tname = thisLine.substring(thisLine.indexOf("\""));
                        String filename = tname.substring(1, Utils.instrRev(tname, "\"" , false));
                        String fdir = Utils.getParentFolder(filePath);
                        //System.out.println("Attempt INCLUDE of '"+ fdir + filename +"'");
                        String contents = Utils.fetch(fdir + filename, encoding);
                        sb.append(contents); // '\n' already appended
                    }
                }else {
                    if (thisLineUC.startsWith("EVENTS")){
                        inEvents = true;
                    }else if (thisLineUC.startsWith("SCRIPTS")){
                        inScripts = true;
                    }else if (thisLineUC.startsWith("END_EVENTS")){
                        inEvents = false;
                    }else if (thisLineUC.startsWith("END_SCRIPTS")){
                        inScripts = false;
                    }
                    sb.append(thisLine + "\n");
                }
            }                            
            in.close(); in = null;
            
        } catch (IOException e) {
            throw new DimxException("Problem fetching or loading: \"" + filePath + "\" (" + e.toString() + ")");
        } catch (Exception e) {
            throw new DimxException("Problem fetching or loading: \"" + filePath + "\" (" + e.getMessage() + ")");
        }
        return sb.toString();
    }

    public static void removeWayFacing(Dict waysV,String thisRoomId,String direction) {
        if (waysV != null) { // Find a way going forward
            for (int i = 0; i < waysV.size(); ) {
                Link wx = (Link) waysV.elementAt(i);
                if (wx.getDirection(thisRoomId).equals(direction)) {
                    waysV.removeAt(i);
                } else {
                    i++;
                }
            }
        }
        
    }
    
    public static String encodeURL(String value) {
        try {
            return java.net.URLEncoder.encode(value,"UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            //throw new DimxException("Encoding problem");
            return value;
        }
    }
    
    public static String decodeURL(String value) throws DimxException {
        try {
            return java.net.URLDecoder.decode(value,"UTF-8");
        } catch (Exception e) {
            return "?";
        }
    }
    
    public static String deflate(String aStr) {
        StringBuffer strb = new StringBuffer();
        
        aStr = aStr.toUpperCase();
        for (int i=0; i < aStr.length(); i++) {
            char c = aStr.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                strb.append(c);
            }
        }
        return strb.toString();
    }
    
    public static String escapeChars(String str, String escaped) {
        for (int i=0; i < escaped.length(); i++) {
            str = stringReplace(str, escaped.substring(i,i+1),"!"+(i+1)+"!",false);
        }
        return str;
    }
    
    public static String unescapeChars(String str, String escaped) {
        for (int i=0; i < escaped.length(); i++) {
            str = stringReplace(str, "!"+(i+1)+"!",escaped.substring(i,i+1),false);
        }
        return str;
    }
    
  public DictSorted getFilesByMask (String folder, FilenameFilter filter) {
    DictSorted res = new DictSorted();
    File dir = new File(folder);
    String[] strs = dir.list(filter);
    if (strs == null) return res;
    for (int i = 0; i < strs.length; i++) {
      res.put(strs[i], strs[i]);
    }
    return res;
  }
  
  // removes optional external brackets {} from style specs (transitional)
  public static String unbracket(String astr) {
        String styleNB = ""; // Style with NO brackets
        styleNB = astr;
        if (styleNB.substring(0,1).equals("{")) {
            styleNB=styleNB.substring(1);
        }
        if (styleNB.substring(styleNB.length()-1).equals("}")) {
            styleNB=styleNB.substring(0,styleNB.length()-1);
        }
        return styleNB;
  }


}