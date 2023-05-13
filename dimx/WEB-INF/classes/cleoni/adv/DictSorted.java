/*
 * DictSorted.java
 *
 * Created on 17 febbraio 2005, 12.03
 */

package cleoni.adv;

/** Sorted Dictionary object.
 *
 * Uses dicotomic search.
 * Case-insensitive.
 * @author Cristiano Leoni
 */
public class DictSorted extends Dict {
    
    /** Creates a new instance of DictSorted */
    public DictSorted() {
        super();
    }
    
    /**
     * @param key
     * @return
     */    
    public Object get(String key) {
	int i = indexOf(key);
	if (i >=0) {
            return elementData[i];
	}
	return null;
    }

    /**
     * @param key
     * @return
     */    
    public Object getIC(String key) {
	int i = indexOf(key);
	if (i >=0) {
            return elementData[i];
	}
	return null;
    }
    
    /**
     * @param key
     * @return
     */    
    public int indexOf(String key) {
        if (elementCount == 0 || key == null) return -1;
        else return indexOf(key,0,elementCount-1);
    }

    /**
     * @param key
     * @param from
     * @param to
     * @return
     */    
    private final int indexOf(String key, int from, int to) {
        int pos = from + (to-from)/2;
        int res = key.compareToIgnoreCase(keys[pos]);
        if (res == 0) {
            return pos;
        } else if (res  < 0) {
            if (from >= to) return -1;
            else return indexOf(key, from, pos-1);
        } else {
            if (from >= to) return -1;
            else return indexOf(key, pos+1, to);
        }
    }

  
    /**
     * @param key
     * @param obj
     */    
    public void put(String key, Object obj) {
        // check existence
        int pos = indexOf(key);
        if (pos >= 0) {
                elementData[pos] = obj; // substitute
                return;
        }
        // insert element
	int newcount = elementCount + 1;
	if (newcount > elementData.length) {
	    ensureCapacityHelper(newcount);
	}
        pos = scrollDownUntil(key);
	keys[pos] = key;
	elementData[pos] = obj;
        elementCount++;
    }

    private int scrollDownUntil(String key) {
    	for (int i = elementCount-1 ; i >= 0 ; i--) {
	    if (key.compareToIgnoreCase(keys[i])<0) {
		keys[i+1]=keys[i];
		elementData[i+1]=elementData[i];
	    } else {
                return i+1;
            }
	}
	return 0;
    }
    
}
