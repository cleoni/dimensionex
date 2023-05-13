/*
 * PeopleContainer.java
 *
 * Created on 12 febbraio 2004, 10.33
 */

package cleoni.adv;

/** Base class for objects capable of containing people (ROOMS and VEHICLEs). For code optimisation only.
 * @author CrLeoni
 */
public class PeopleContainer extends AdvObject {

    private         Panel   panel = null;

    /** Creates a new instance of PeopleContainer */
    public PeopleContainer(World aWorld, String aName, String anId, String aDescription, String anIcon, String attrList, int aCapacity, int aVolume, 
    boolean setDefShowpos,String aDefContainer) throws DimxException {
	super(aWorld,aName,anId, aDescription, anIcon,attrList,aCapacity,aVolume, setDefShowpos, aDefContainer);
	panel = null;
}    

public boolean display(String msg) throws DimxException {
	// Displays the specified msg for all the players in that room

	world.logger.debug("Displaying: " + msg + " in room " + id);
	
	Character x;
	for (int i=0; i < contents.size(); i++) {
		try {
			x = (Character) contents.elementAt(i);
			x.display(msg);
		} catch (ClassCastException e) {
			// Skip this - not a character
		}
		
	}
	return true;
}
    
    
public Panel getPanel() {
	if (panel != null) 
		return panel;
	else
		return (world.getPanel());
}    
public Link getWay(String wayId) {
	return null;
}
public Dict getLinks() {
	return null;
}
public boolean isPeopleContainer() {
	return true;
}
/*
 * Tries to hear from the specified object, returns true if successful
 */
public boolean hear(DimxObject from, String msg) throws DimxException  {
        boolean res = super.hear(from,msg);
        if (!res) return false; // Block action upon FALSE
        
        int interphonelevel = Utils.cInt(world.interphone.numVal());

        if (interphonelevel == 2) { // At interphone level 2 we always speak to all world
            world.hear(from,msg);
            return res;
        } else if (interphonelevel == 1) { // At interphone level 1
            if (this.isaRoom()) { // if we are in a room we speak to world, otherwise we speak to vehicle/item contents
                world.hear(from,msg);
                return res;
            } 
        } // at interphonelevel == 0 we speak always to current container

    
	if (from == null) from = world.defaultCharacter;
        
	if (!msg.equals("")) {
		world.logger.debug("Speaking: " + msg + " in room " + id);
		
		Player x;
		for (int i=0; i < contents.size(); i++) {
			try {
				x = (Player) contents.elementAt(i);
				x.hear(from, msg);
			} catch (ClassCastException e) {
				// Skip this - not a character
			}
			
		}

	}
        return res;
}

public boolean playBackground(String soundfile, boolean loop) throws DimxException {
	// plays the specified sound
	world.logger.debug("Setting background sound: " + soundfile + " in room " + id);
	
	Player x;
	for (int i=0; i < contents.size(); i++) {
		try {
			x = (Player) contents.elementAt(i);
			x.playBackground(soundfile,loop);
		} catch (ClassCastException e) {
			// Skip this - not a player
		}
		
	}
	return true;
}

public boolean playSound(String soundfile) throws DimxException {
	// plays the specified sound
	world.logger.debug("Sounding: " + soundfile + " in room " + id);
	
	Player x;
	for (int i=0; i < contents.size(); i++) {
		try {
			x = (Player) contents.elementAt(i);
			x.playSound(soundfile);
		} catch (ClassCastException e) {
			// Skip this - not a player
		}
		
	}
	return true;
}

public boolean sendCmd(String cmd) throws DimxException {
	// Sends the specified command
        if (this.isaRoom() && world != null) world.logger.debug("Sending: " + cmd + " in " + id);
        AdvObject o;
	for (int i=0; i < contents.size(); i++) {
            o = (AdvObject) contents.elementAt(i);
            o.sendCmd(cmd);
	}
	return true;
}


public boolean setPanel(String panelId) throws DimxException {
	// Sets the specified panel for this room

	world.logger.debug("Setting panel: " + panelId + " in: " + id);
	
	Panel myPanel = (Panel) world.getPanel(panelId);

	if (myPanel == null)
		throw new DimxException("Unexistent panel: " + panelId);

	if (panelId.equalsIgnoreCase("default")) myPanel = null;
		
	panel = myPanel;
		
	return true;
}

public boolean useView(Page view) throws DimxException {
	// No view unless  World, Player or Room - needs to be overridden
        world.logger.debug("Sending view: " + view.id + " in " + id);
        AdvObject o;
	for (int i=0; i < contents.size(); i++) {
            o = (AdvObject) contents.elementAt(i);
            o.useView(view);
	}
	return true;
}
}
