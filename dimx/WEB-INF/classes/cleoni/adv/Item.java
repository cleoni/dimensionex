package cleoni.adv;

/** Represents an ITEM */
public class Item extends PeopleContainer {


public String getTypePrefix() {
	return Const.TYPE_ITEM;
}
public boolean isanItem() {
    return true;
}


/**
 * Item constructor comment.
 * @param aWorld leoni.adv.World
 * @param aName java.lang.String
 * @param anId java.lang.String
 */
public Item(World aWorld, String aName, String anId, String aDescription, String anIcon, String attrList,
	int aCapacity,
	int aVolume,
	String aDefContainer) throws DimxException {
	super(aWorld, aName, anId, aDescription,anIcon,attrList,aCapacity,aVolume, true, aDefContainer);
	// showmode = Const.ONSCREEN_ICON;
}


/*
 * tries to restore current item to the specified player
 * returns true upon success
 * if alerts (optional) is specified, issues messages upon failure
 */
public boolean restoreTo(Player player, StringBuffer alerts, World myworld) throws DimxException {
    boolean res = false;
    if (world == null) {
        myworld.logger.log("Tried to restore non-existent object: " + this.toString() + " type='" + this.varGet("type").strVal() + "'");
        myworld.logger.log("Trying to fix it by removing it from world objects collection.");
        myworld.logger.log("Dumping world.vars");
        myworld.logger.log("" + myworld.printContents());
        boolean resultop = myworld.removeObject(this);
        myworld.logger.log("Result of operation: " + resultop);
        if (!resultop) {
            myworld.logger.log("Dumping world.vars again");
            myworld.logger.log("" + myworld.printContents());
            myworld.logger.log("-----------------------------");
            throw new DimxException("Unexistent object");
        }
        return res;
    }
    AdvObject cont = this.container;
    if (cont != null && (cont.isaRoom() || (cont.isanItem() && cont.isOpen()))) {
        res = this.moveTo(player,player,Const.DONT_CHECK_OPEN,Const.CHECK_EVENTS);
        if (res) {
            this.varGet("hidden",Const.GETREF).assign(new Token(0),world);
            //res = true;
        }
        if (this.world == null) { // Force result = TRUE for metamorphosis objects (eg.: money packs)
            res = true; 
        }
    } else {
        if (alerts != null) {
            alerts.append(Messages.actualize(world.msgs.msg[125],this.getName()) + "\n");
        }
    }
    return res;
}
    /**
     * See AdvObject.WorldChange
     * @param See AdvObject.WorldChange
     * @param See AdvObject.WorldChange
     * @throws cleoni.adv.DimxException in case of problems
     */
    public void worldChange(World toWorld, String newid, String defContainer) throws DimxException {
        super.worldChange(toWorld,newid, defContainer);
        toWorld.addItem(this, false);
   }
}