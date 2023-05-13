/*
 * Vehicle.java
 *
 * Created on 11 febbraio 2004, 14.46
 */

package cleoni.adv;

/** Implements a VEHICLE. It is a special case of ITEM, but can contain people and move.
 * @author CrLeoni
 */
public class Vehicle extends Item {
    
    /** Creates a new instance of Vehicle */
    public Vehicle(World aWorld, String aName, String anId, String aDescription, String anIcon, String attrList,
    int aCapacity,
    int aVolume,
    String aDefContainer) throws DimxException {
        super(aWorld, aName, anId, aDescription,anIcon,attrList,
        aCapacity,
        aVolume, aDefContainer);
        // showmode = Const.ONSCREEN_ICON;
    }
    public boolean isVehicle() {
	return true;
    }
    public Dict getLinks() {
	return getPeopleContainer().getLinks();
    }
}
