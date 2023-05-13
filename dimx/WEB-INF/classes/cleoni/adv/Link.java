package cleoni.adv;

/** Implements a LINK between rooms */
public class Link extends AdvObject {
	private Dict fromto = new Dict();
	private String direction = "";	// Non � ammesso NULL
/**
 * Way constructor comment.
 */
public Link(World aWorld, String aName, String anId, Room fromRoom, Room aRoom, boolean bidirect, String aDirection, String aDescription, String anIcon, String attrList) throws DimxException {
	super(aWorld, aName, anId, aDescription, anIcon, attrList, 0, 0, true, null);
	id = anId;
	name = new Token(aName);
	direction = Utils.cStr(aDirection);	// Forza stringa valida
	fromto.put(fromRoom.id,aRoom);
	isBidirectional = bidirect;
	if (isBidirectional) 	fromto.put(aRoom.id,fromRoom);
}
public String getDirection(String from) {
	if (fromto.keyAt(0).equals(from)) {
		return direction;
	} else {
		return Utils.getOppositeDirection(direction);
	}
}

public Room getRoom() {
// Returns the first FROM room. In caso of bidirectional link this may turn into a problem
	return world.getRoom(fromto.keyAt(0));
}
/** Returns the room which is reached with the current link if starting from room with the specified ID
 * @param from ROOM id from which the link is being traversed
 * @return ROOM being reached
 */
protected Room getTarget(String from) {
	return (Room) fromto.get(from);
}
public String getTypePrefix() {
	return Const.TYPE_LINK;
}

public boolean isLink() {
    return true;
}
protected boolean isOpen() {
	// If super is open or unspecified (open is by default)
	if (super.isOpen() || !super.varExists("open")) {
		return true;
	} else {
		return false;
	}
}
protected boolean leadsTo(String roomId) {
// Returns true if this link somehow leads to the specified room
	for (int i=0; i<fromto.size(); i++) {
		if (((Room) fromto.elementAt(i)).id.equals(roomId)) {
			return true;
		}
	}
	return false;
}
protected boolean startsFrom(String roomId) {
	return (fromto.get(roomId) != null);
}
public String toString() {
	StringBuffer sb = new StringBuffer(super.toString());
	sb.append(" {");
	for (int i=0; i < fromto.size(); i++) {
		sb.append(fromto.keyAt(i) + "-- " + getDirection(fromto.keyAt(i)) + " -->" + ((Room) fromto.elementAt(i)).id);
		if (i < fromto.size()-1)
			sb.append(";");
	}
	sb.append("}");
	return sb.toString();
}

	protected boolean isBidirectional = false;

public String getIcon(String from, String facing, Skin skin) {
	String dir = getDirection(from);
	if (!dir.equals("")) {
		return (String) skin.defIconWayDir.get(Utils.getRelativeDirection(getDirection(from),facing));
	} else {
		return this.getIcon();
	}
}
}