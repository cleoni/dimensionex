package cleoni.adv;

/**
 * Required interface for customized world.init processing.
 * A custom processor of any classname wishing to intercept or extend
 * world.init processing must implement this interface and have a
 * default, no argument constructor.
 *
 * The custom processor should be defined as a WORLD attribute within the
 * MyWorld.DXW file.
 *
 * Example:
 * <pre>
 * WORLD
 *     ...
 *     custCmdProc   org.dimx.ACustCmdProcessor
 *     ...
 *     OTHER_TAGS
 *
 *     END_OTHER_TAGS
 *
 * END_WORLD
 * </pre>
 * @author Carl Nagle
 */
public interface IWorldInitProcessor {

    /**
     * This API is subject to change until it is fully developed.
     * @param server
     * @param loader
     * @param world
     * @param systemDir
     * @return boolean true to allow normal world.init processing to continue;
     *         false to bypass normal world.init processing.
     */
    public boolean processPreWorldInit(multiplayer server, WorldLoader loader, World world, String systemDir);

    /**
     * This API is subject to change until it is fully developed.
     * @param server
     * @param loader
     * @param world
     * @param systemDir
     * @return boolean true to allow remaining init processing to continue;
     *         false to bypass remaining init processing.
     */
    public boolean processPostWorldInit(multiplayer server, WorldLoader loader, World world, String systemDir);
}
