package cleoni.adv;

/** Another custom error (as DimXException). This is a special type, telling you that the error has already been
 * investigated and described so the associated message is self-explaining yet.
 * @author:
 */
public class NestedException extends DimxException {

/**
 * NestedException constructor comment.
 * @param s java.lang.String
 */
public NestedException(String s) {
	super(s);
}
}
