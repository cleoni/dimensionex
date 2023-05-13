package cleoni.adv;

/** Symbol table for variables
 * (i.e. space for storing an object's properties)
 * @author: Cristiano Leoni
 */
public class Varspace {
	private DictSorted elements = new DictSorted();

/**
 * Varspace constructor comment.
 */
public Varspace() {
	super();
}
public Varspace(java.util.Vector formalparams, Dict actualparams) {
    this(formalparams,actualparams,false);
}
public Varspace(java.util.Vector formalparams, Dict actualparams,boolean matchnames) {
    super();
    if (matchnames) { // MATCH required
        if (formalparams != null) {
            Token v = null;
            for (int i=0; i < formalparams.size(); i++){
                v = (Token) actualparams.get((String) formalparams.elementAt(i));
                if (v != null) // parameter was present
                    varSet((String) formalparams.elementAt(i), v.getClone() );
                else // parameter was missing - insert NULL
                    varSet((String) formalparams.elementAt(i), new Token());
            }
        }
    } else {
        if (formalparams != null) {
            for (int i=0; i < formalparams.size(); i++){
                varSet((String) formalparams.elementAt(i), ((Token) actualparams.elementAt(i)).getClone() );
            }
        }
    }
}
/**
 * @param i
 * @return
 */
public AdvObject advObjectAt(int i) {
// returns the i-th element of the varspace if it is an advObject, null otherwise
    try {
	return (AdvObject) elements.elementAt(i);
    } catch (ClassCastException e) {
        return null;
    }
}
/**
 * @param i
 * @return
 */
protected ManageableObject elementAt(int i) {
	return (ManageableObject) elements.elementAt(i);
}
/**
 * @param aKey
 * @return object or null
 */
public AdvObject getAdvObject(String aKey) {
    try {
	return (AdvObject) elements.get(aKey);
    } catch (ClassCastException e) {
        return null;
    }
}

public boolean exists(String aKey) {
    return (elements.get(aKey) != null);
}
/**
 * @param aKey
 * @return
 */
public Character getCharacter(String aKey) {
    try {
	return (Character) elements.get(aKey);
    } catch (ClassCastException e) {
        return null;
    }
}
/**
 * @param aKey
 * @return
 */
public Item getItem(String aKey) {
    try {
	return (Item) elements.get(aKey);
    } catch (ClassCastException e) {
        return null;
    }
}
/**
 * @param aKey
 * @return
 */
public Object getObject(String aKey) {
    try {
	return (Object) elements.get(aKey);
    } catch (ClassCastException e) {
        return null;
    }
}
/**
 * @param aKey
 * @return
 */
public Room getRoom(String aKey) {
    try {
	return (Room) elements.get(aKey);
    } catch (ClassCastException e) {
        return null;
    }
}
/**
 * @param aKey
 * @return
 */
public Dict getSet(String aKey) {
    try {
	return (Dict) elements.get(aKey);
    } catch (ClassCastException e) {
        return null;
    }
}
/**
 * @param aKey
 * @return
 */
public Token getToken(String aKey) {
    try {
	return (Token) elements.get(aKey);
    } catch (ClassCastException e) {
        return null;
    }
}

public DictSorted getElements() {
    return elements;
}
/**
 * @param aKey
 * @return
 */
public Link getWay(String aKey) {
    try {
	return (Link) elements.get(aKey);
    } catch (ClassCastException e) {
        return null;
    }
}
/**
 * @return
 */
public String htmlDump()
{
	StringBuffer sb = new StringBuffer("");
	String aKey = null;
	
	for (int i=0; i< elements.size() ; i++) {
		sb.append("<LI><B>");
		aKey = elements.keyAt(i);
		sb.append(aKey);
		sb.append("</b> ");
		Object val = (Object) elements.get(aKey);
                if (val == null) {
                        sb.append("null");
                } else {
                        sb.append("(" + val.getClass().getName() + ") " + val);
                }
		sb.append("<br>\n");
	}

	return sb.toString();
}
/**
 * @param aKey
 * @param anObj
 * @param detectClash
 * @return
 */
public boolean put(String aKey, ManageableObject anObj) {
	elements.put(aKey,anObj);
	return true;
}
/**
 * @param aKey
 * @return
 */
public boolean remove(String aKey) {
	return elements.remove(aKey);
}
/**
 * @return
 */
protected int size() {
	return elements.size();
}

public String toString() {
    return elements.toString();
}
/*
 * Searches a value in the varspace
 * returns null if not found
 */
public Token varGet(String key, boolean getReference) {
    Token s = (Token) elements.get(key);
    if (s != null) {
            if (getReference) return s;
            else return s.getClone();
    } 
    return null;
}
public void varSet(String key, Token val) {
    elements.put(key,val);
}

}

