package cleoni.adv;

/** Modified version of the Vector class, so that it works like an associative
 * array.
 * Dictionary object. SETS are Dictionary object.
 * See VBScript documentation on Dictionary objects
 */
 
 public class Dict extends ManageableObject implements Cloneable, java.io.Serializable {
	protected String keys[];

	/**
	 * The array buffer into which the components of the vector are 
	 * stored. The capacity of the vector is the length of this array buffer.
	 *
	 * @since   JDK1.0
	 */
	protected Object elementData[]; // protected per Enumerator

	/**
	 * The number of valid components in the vector. 
	 *
	 * @since   JDK1.0
	 */
	protected int elementCount; // protected per Enumerator

	/**
	 * The amount by which the capacity of the vector is automatically 
	 * incremented when its size becomes greater than its capacity. If 
	 * the capacity increment is <code>0</code>, the capacity of the 
	 * vector is doubled each time it needs to grow. 
	 *
	 * @since   JDK1.0
	 */
	private int capacityIncrement;
	private boolean allowDups = false;

	/** use serialVersionUID from JDK 1.0.2 for interoperability */
	private static final long serialVersionUID = -2767605614048989439L;
	/**
	 * Constructs an empty vector. 
	 *
	 * @since   JDK1.0
	 */
	public Dict() {
	this(10);
	}
	/**
	 * Constructs an empty vector with the specified initial capacity.
	 *
	 * @param   initialCapacity   the initial capacity of the vector.
	 * @since   JDK1.0
	 */
	public Dict(int initialCapacity) {
	this(initialCapacity, 0, false);
	}
	/** Constructs an empty vector with the specified initial capacity and
         * capacity increment.
         * @since JDK1.0
         * @param areDupsAllowed must be True is duplicates are allowed
         * @param initialCapacity the initial capacity of the vector.
         * @param capacityIncrement the amount by which the capacity is
         *                              increased when the vector overflows.
         */
	public Dict(int initialCapacity, int capacityIncrement, boolean areDupsAllowed) {
	super();
	this.elementData = new Object[initialCapacity];
	this.keys = new String[initialCapacity];
	this.capacityIncrement = capacityIncrement;
	this.allowDups = areDupsAllowed;
	}
	/**
	 * Returns the current capacity of this vector.
	 *
	 * @return  the current capacity of this vector.
	 * @since   JDK1.0
	 */
	public final int capacity() {
	return elementData.length;
	}
/**
 * Removes all components from this vector and sets its size to zero.
 *
 * @since   JDK1.0
 */
public final void clear() {
	for (int i = 0; i < elementCount; i++) {
	    elementData[i] = null;
	    keys[i] = null;
	}
	elementCount = 0;
}
/**
 * Returns a clone of this vector.
 *
 * @return  a clone of this vector.
 * @since   JDK1.0
 */
public Object clone() {
        Dict v = null;
	try { 
	    v = (Dict)super.clone();
	    v.elementData = new Object[elementCount];
	    v.keys = new String[elementCount];
	    System.arraycopy(elementData, 0, v.elementData, 0, elementCount);
	    System.arraycopy(keys, 0, v.keys, 0, elementCount);
	} catch (CloneNotSupportedException e) { 
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}
        for (int i=0; i < elementCount; i++) { // clones all inner objects
            try {
                Token t = (Token) elementData[i];
                elementData[i] = t.clone();
            } catch (Exception e) {
            }
        }
        return v;
}
/** Tests if the specified object is a component in this vector.
 *
 * @return <code>true</code> if the specified object is a component in
 *          this vector; <code>false</code> otherwise.
 * @since JDK1.0
 * @param key
 */
public final boolean containsKey(String key) {
    if (key != null) {
	return indexOf(key) >= 0;
    }
    return false;
}
/**
 * Copies the components of this vector into the specified array. 
 * The array must be big enough to hold all the objects in this  vector.
 *
 * @param   anArray   the array into which the components get copied.
 * @since   JDK1.0
 */
public final void copyInto(Object anArray[]) {
	int i = elementCount;
	while (i-- > 0) {
	    anArray[i] = elementData[i];
	}
}
/**
 * Copies the components of this vector into the specified array. 
 * The array must be big enough to hold all the objects in this  vector.
 *
 * @param   anArray   the array into which the components get copied.
 * @since   JDK1.0
 */
public final void copyKeysInto(java.util.Vector myvect) {
	for (int i=0; i < elementCount; i++) {
	    myvect.add(keys[i]);
	}
}
/** Returns the component at the specified index.
 *
 * @return the component at the specified index.
 * @since JDK1.0
 * @param index an index into this vector.
 */
public final Object elementAt(int index) {
	if (index >= elementCount) {
		return null;
	}
	/* Since try/catch is free, except when the exception is thrown,
	   put in this extra try/catch to catch negative indexes and
	   display a more informative error message.  This might not
	   be appropriate, especially if we have a decent debugging
	   environment - JP. */
	try {
	    return elementData[index];
	} catch (ArrayIndexOutOfBoundsException e) {
	    throw new ArrayIndexOutOfBoundsException(index + " < 0");
	}
}
/**
 * Returns an enumeration of the components of this vector.
 *
 * @return  an enumeration of the components of this vector.
 * @see     java.util.Enumeration
 * @since   JDK1.0
 */
public final java.util.Enumeration elements() {
	return new DictEnumerator(this);
}
/**
 * Increases the capacity of this vector, if necessary, to ensure 
 * that it can hold at least the number of components specified by 
 * the minimum capacity argument. 
 *
 * @param   minCapacity   the desired minimum capacity.
 * @since   JDK1.0
 */
public final void ensureCapacity(int minCapacity) {
	if (minCapacity > elementData.length) {
	    ensureCapacityHelper(minCapacity);
	}
}
/**
 * This implements the unsynchronized semantics of ensureCapacity.
 * Synchronized methods in this class can internally call this 
 * method for ensuring capacity without incurring the cost of an 
 * extra synchronization.
 *
 * @see java.util.Vector#ensureCapacity(int)
 */ 
protected void ensureCapacityHelper(int minCapacity) {
	int oldCapacity = elementData.length;
	Object oldData[] = elementData;
	String oldKeys[] = keys;
	int newCapacity = (capacityIncrement > 0) ?
	    (oldCapacity + capacityIncrement) : (oldCapacity * 2);
	if (newCapacity < minCapacity) {
	    newCapacity = minCapacity;
	}
	elementData = new Object[newCapacity];
	keys = new String[newCapacity];
	System.arraycopy(oldData, 0, elementData, 0, elementCount);
	System.arraycopy(oldKeys, 0, keys, 0, elementCount);
}
/**
 * Returns the first component of this vector.
 *
 * @return     the first component of this vector.
 * @exception  NoSuchElementException  if this vector has no components.
 * @since      JDK1.0
 */
public final Object firstElement() throws Exception {
	if (elementCount == 0) {
	    throw new java.util.NoSuchElementException();
	}
	return elementData[0];
}
/**
 * Returns the first component of this vector.
 *
 * @return     the first key of this vector.
 * @exception  NoSuchElementException  if this vector has no components.
 * @since      JDK1.0
 */
public final String firstKey() throws Exception {
	if (elementCount == 0) {
	    throw new java.util.NoSuchElementException();
	}
	return keys[0];
}
/** Gets an object
 * @param key key to be searched
 * @return the searched object, null otherwise
 */
public Object get(String key) {
	for (int i=0; i < elementCount; i++) {
		if (key.equals(keys[i])) {
			return elementData[i];
		}
	}
	return null;
}
public final String getS(String key) {
	return (String) get(key);
}

/**
 * Searches for the first occurence of the given argument, beginning 
 * the search at <code>index</code>, and testing for equality using 
 * the <code>equals</code> method. 
 *
 * @param   elem    an object.
 * @return  the index of the first occurrence of the object argument in
 *          this vector at position <code>index</code> or later in the
 *          vector; returns <code>-1</code> if the object is not found.
 * @see     java.lang.Object#equals(java.lang.Object)
 * @since   JDK1.0
 */
public int indexOf(String key) {
    for (int i = 0 ; i < elementCount ; i++) {
        if (key.equalsIgnoreCase(keys[i])) {
            return i;
        }
    }
    return -1;
}
/**
 * Returns the index of the last occurrence of the specified object in
 * this vector.
 *
 * @param   key  the desired component.
 * @return  the index of the last occurrence of the specified object in
 *          this vector; returns <code>-1</code> if the object is not found.
 * @since   JDK1.0
 */
/**
 * Inserts the specified object as a component in this vector at the 
 * specified <code>index</code>. Each component in this vector with 
 * an index greater or equal to the specified <code>index</code> is 
 * shifted upward to have an index one greater than the value it had 
 * previously. 
 * <p>
 * The index must be a value greater than or equal to <code>0</code> 
 * and less than or equal to the current size of the vector. 
 *
 * @param      obj     the component to insert.
 * @param      index   where to insert the new component.
 * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
 * @see        java.util.Vector#size()
 * @since      JDK1.0
 */
public final void insertElementAt(Object obj, int index) {
	int newcount = elementCount + 1;
	if (index >= newcount) {
	    throw new ArrayIndexOutOfBoundsException(index
						     + " > " + elementCount);
	}
	if (newcount > elementData.length) {
	    ensureCapacityHelper(newcount);
	}
	System.arraycopy(elementData, index, elementData, index + 1, elementCount - index);
	elementData[index] = obj;
	elementCount++;
}
	/**
	 * Tests if this vector has no components.
	 *
	 * @return  <code>true</code> if this vector has no components;
	 *          <code>false</code> otherwise.
	 * @since   JDK1.0
	 */
	public final boolean isEmpty() {
	return elementCount == 0;
	}
/**
 * Returns the component at the specified index.
 *
 * @param      index   an index into this vector.
 * @return     the component at the specified index.
 * @exception  ArrayIndexOutOfBoundsException  if an invalid index was
 *               given.
 * @since      JDK1.0
 */
public final String keyAt(int index) {
	if (index >= elementCount) {
	    throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
	}
	/* Since try/catch is free, except when the exception is thrown,
	   put in this extra try/catch to catch negative indexes and
	   display a more informative error message.  This might not
	   be appropriate, especially if we have a decent debugging
	   environment - JP. */
	try {
	    return keys[index];
	} catch (ArrayIndexOutOfBoundsException e) {
	    throw new ArrayIndexOutOfBoundsException(index + " < 0");
	}
}
/**
 * Returns the last component of the vector.
 *
 * @return  the last component of the vector, i.e., the component at index
 *          <code>size()&nbsp;-&nbsp;1</code>.
 * @exception  NoSuchElementException  if this vector is empty.
 * @since   JDK1.0
 */
public final Object lastElement() throws Exception {
	if (elementCount == 0) {
	    //throw new NoSuchElementException();
	    throw new Exception();
	}
	return elementData[elementCount - 1];
}
/**
 * Returns the last component of the vector.
 *
 * @return  the last component of the vector, i.e., the component at index
 *          <code>size()&nbsp;-&nbsp;1</code>.
 * @exception  NoSuchElementException  if this vector is empty.
 * @since   JDK1.0
 */
public final Object lastKey() throws Exception {
	if (elementCount == 0) {
	    //throw new NoSuchElementException();
	    throw new Exception();
	}
	return keys[elementCount - 1];
}
/**
 * Adds the specified component to the end of this vector, 
 * increasing its size by one. The capacity of this vector is 
 * increased if its size becomes greater than its capacity. 
 *
 * @param   obj   the component to be added.
 * @since   JDK1.0
 */
public void put(String key,Object obj) {
	if (!allowDups) {
		// controllare eventuale preesistenza
		int pos = indexOf(key);
		if (pos >= 0) {
			elementData[pos] = obj; // substitute
			return;
		}
	}
	int newcount = elementCount + 1;
	if (newcount > elementData.length) {
	    ensureCapacityHelper(newcount);
	}
	elementData[elementCount] = obj;
	keys[elementCount++] = key;
}
/**
 * Removes the first occurrence of the argument from this vector. If 
 * the object is found in this vector, each component in the vector 
 * with an index greater or equal to the object's index is shifted 
 * downward to have an index one smaller than the value it had previously.
 *
 * @param   obj   the component to be removed.
 * @return  <code>true</code> if the argument was a component of this
 *          vector; <code>false</code> otherwise.
 * @since   JDK1.0
 */
public final boolean remove(String id) {
	int i = indexOf(id);
	if (i >= 0) {
	    removeAt(i);
	    return true;
	}
	return false;
}
/**
 * Deletes the component at the specified index. Each component in 
 * this vector with an index greater or equal to the specified 
 * <code>index</code> is shifted downward to have an index one 
 * smaller than the value it had previously. 
 * <p>
 * The index must be a value greater than or equal to <code>0</code> 
 * and less than the current size of the vector. 
 *
 * @param      index   the index of the object to remove.
 * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
 * @see        java.util.Vector#size()
 * @since      JDK1.0
 */
public final void removeAt(int index) {
	if (index >= elementCount) {
	    throw new ArrayIndexOutOfBoundsException(index + " >= " + 
						     elementCount);
	}
	else if (index < 0) {
	    throw new ArrayIndexOutOfBoundsException(index);
	}
	int j = elementCount - index - 1;
	if (j > 0) {
	    System.arraycopy(elementData, index + 1, elementData, index, j);
	    System.arraycopy(keys, index + 1, keys, index, j);
	}
	elementCount--;
	elementData[elementCount] = null; /* to let gc do its work */
	keys[elementCount] = null;
}
/**
 * Sets the component at the specified <code>index</code> of this 
 * vector to be the specified object. The previous component at that 
 * position is discarded. 
 * <p>
 * The index must be a value greater than or equal to <code>0</code> 
 * and less than the current size of the vector. 
 *
 * @param      obj     what the component is to be set to.
 * @param      index   the specified index.
 * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
 * @see        java.util.Vector#size()
 * @since      JDK1.0
 */
public final void setElementAt(Object obj, int index) {
	if (index >= elementCount) {
		throw new ArrayIndexOutOfBoundsException(index + " >= " + 
						     elementCount);
	}
	elementData[index] = obj;
}
/**
 * Sets the size of this vector. If the new size is greater than the 
 * current size, new <code>null</code> items are added to the end of 
 * the vector. If the new size is less than the current size, all 
 * components at index <code>newSize</code> and greater are discarded.
 *
 * @param   newSize   the new size of this vector.
 * @since   JDK1.0
 */
public final void setSize(int newSize) {
	if ((newSize > elementCount) && (newSize > elementData.length)) {
	    ensureCapacityHelper(newSize);
	} else {
	    for (int i = newSize ; i < elementCount ; i++) {
		elementData[i] = null;
	    }
	}
	elementCount = newSize;
}
/**
 * Returns the number of components in this vector.
 *
 * @return  the number of components in this vector.
 * @since   JDK1.0
 */
public final int size() {
	return elementCount;
}
	/**
	 * Returns a string representation of this vector. 
	 *
	 * @return  a string representation of this vector.
	 * @since   JDK1.0
	 */
	public final String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("{");
	Object obj=null;
	int max = size()-1; // per detection last element
	for (int i = 0 ; i <= max; i++) {
		String k = keyAt(i);
		obj = elementAt(i);
		String s;
		if (obj != null) {
	    	s = obj.toString();
		} else {
	    	s = "null";
		}
	    buf.append(k);
	    buf.append("=");
	    buf.append(s);
	    if (i < max) {
		buf.append(", ");
	    }
	}
	buf.append("}");
	return buf.toString();

	}
 

        /**
	 * Returns a string representation of this vector. 
	 *
	 * @return  a string representation of this vector.
	 * @since   JDK1.0
	 */
	public final String toSettingsPair(String sep) {
	StringBuffer buf = new StringBuffer("!set!");
	Object obj=null;
	int max = size()-1; // per detection last element
	for (int i = 0 ; i <= max; i++) {
		String k = keyAt(i);
                k = Utils.escapeChars(k,",=");
		obj = elementAt(i);
		String s;
		if (obj != null) {
                    try {
                        Token t = (Token) obj;
                        String val;
                        if (t.isImage()) {
                            val = t.imageVal().toString();
                        } else {
                            val = t.strVal();
                        }
                        s = Utils.escapeChars(val,",=");
                    } catch (ClassCastException e) {
                        s = Utils.escapeChars(obj.toString(),",=");
                    }
		} else {
                    s = "null";
		}
	    buf.append(k);
	    buf.append("=");
	    buf.append(s);
	    if (i < max) {
		buf.append(sep);
	    }
	}
	return buf.toString();

	}
 
/**
 * Trims the capacity of this vector to be the vector's current 
 * size. An application can use this operation to minimize the 
 * storage of a vector. 
 *
 * @since   JDK1.0
 */
public final void trimToSize() {
	int oldCapacity = elementData.length;
	if (elementCount < oldCapacity) {
	    Object oldData[] = elementData;
	    String oldKeys[] = keys;
	    elementData = new Object[elementCount];
	    keys = new String[elementCount];
	    System.arraycopy(oldData, 0, elementData, 0, elementCount);
	    System.arraycopy(oldKeys, 0, keys, 0, elementCount);
	}
}

public Object getIC(String key) {
	for (int i=0; i < elementCount; i++) {
		if (keys[i].equalsIgnoreCase(key)) {
			return elementData[i];
		}
	}
	return null;
}
}