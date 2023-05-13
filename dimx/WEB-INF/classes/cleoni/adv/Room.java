package cleoni.adv;

/*
 * ROOM Object
 */
/** Represents a ROOM object */
public class Room  extends PeopleContainer {
	private 	Dict ways = new Dict();

protected int getFreeSpace() {
	return 10000;
}

public Room getRoom() {
	return this; // Each room is in itself
}
public String getTypePrefix() {
	return "r!";
}
public Link getWay(String wayId) {
	return (Link) ways.get(wayId);
}
public Dict getLinks() {
	return ways;
}
public boolean isaRoom() {
    return true;
}

/**
 * Room constructor comment.
 */
public Room(World aWorld, String aName, String anId, String aDescription, String anIcon, String attrList) throws DimxException {
	super(aWorld,aName,anId, aDescription, anIcon,attrList, 30000,0, false, null);
}

/*
 * adds a way to current room
 */
public void addLink(Link w) {
	ways.put(w.id,w);
}

}