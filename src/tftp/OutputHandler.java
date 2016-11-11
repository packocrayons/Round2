
package tftp;

/**
 * The server and the client must both provide one of these.
 * for the client, it can be ClientUI
 * @author Team 17
 */
public interface OutputHandler {
	//for the stuff that quiet mode should keep quiet = verbose mode
	public abstract void lowPriorityPrint(Object o);
	
	//for the stuff that quiet mode should NOT keep quiet
	public abstract void highPriorityPrint(Object getsToStringedAndPrinted);
	
	public abstract boolean getQuiet();
	
	public abstract void setQuiet(boolean newQuiet);
}
