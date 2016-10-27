<<<<<<< HEAD
package tftp;

/**
 * The server and the client must both provide one of these.
 * for the client, it can be ClientUI
 */
public interface OutputHandler {
	//for the stuff that quiet mode should keep quiet
	public abstract void lowPriorityPrint(Object getsToStringedAndPrinted);
	
	//for the stuff that quiet mode should NOT keep quiet
	public abstract void highPriorityPrint(Object getsToStringedAndPrinted);
}
=======
package tftp;

/**
 * The server and the client must both provide one of these.
 * for the client, it can be ClientUI
 */
public interface OutputHandler {
	//for the stuff that quiet mode should keep quiet
	public abstract void lowPriorityPrint(Object getsToStringedAndPrinted);
	
	//for the stuff that quiet mode should NOT keep quiet
	public abstract void highPriorityPrint(Object getsToStringedAndPrinted);
}
>>>>>>> 9e1e11a572111a2572274ab61298e6048e5c7c7c
