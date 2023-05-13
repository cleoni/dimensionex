package cleoni.adv;

/*
 * Enumerator for looping through a collecion of Dict objects
 *
 */
final class DictEnumerator implements java.util.Enumeration {
	Dict vector;
	int count;

	DictEnumerator(Dict v) {
	vector = (Dict) v.clone();
	count = 0;
	}
	public boolean hasMoreElements() {
	return count < vector.elementCount;
	}
public Object nextElement() {
synchronized (vector)  {
	if (count < vector.elementCount) {
	return vector.elementData[count++];
	}
}
throw new java.util.NoSuchElementException("VectorEnumerator");
}
}