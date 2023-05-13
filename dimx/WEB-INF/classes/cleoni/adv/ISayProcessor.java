package cleoni.adv;
import java.util.Vector;

/**
 * Required interface for customized SAY processing.
 * A custom processor of any classname wishing to intercept SAY commands 
 * must implement this interface and have a default, no argument constructor.
 * 
 * The custom processor should be defined as a WORLD attribute within the 
 * MyWorld.DXW file.
 * 
 * Example:
 * <pre>
 * WORLD
 *     ...
 *     custCmdProc   org.dimx.CustCmdProcessor
 *     ...
 *     OTHER_TAGS
 * 
 *     END_OTHER_TAGS
 * 
 * END_WORLD
 * </pre>
 * @author Carl Nagle
 */
public interface ISayProcessor {
    
    /**
     * This API is subject to change until it is fully developed.
     * @param server
     * @param world
     * @param cmd -- should be "say"
     * @param arg1 -- will be "whatever the user typed in the SAY txtBox"
     * @param arg2 -- may be a target objId
     * @param player
     * @return NULL to allow normal SAY processing to continue; 
     *         Vector{new cmd,arg1,arg2} to continue normal processing.
     */
    public Vector processSay(multiplayer server, World world, String cmd, String arg1, String arg2, Player player);
}
