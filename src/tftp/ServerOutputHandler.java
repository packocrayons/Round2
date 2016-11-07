
package tftp;

//* @author Team 17

public class ServerOutputHandler implements OutputHandler {

	@Override
	public void lowPriorityPrint(Object getsToStringedAndPrinted) {
			System.out.println(getsToStringedAndPrinted);

	}

	@Override
	public void highPriorityPrint(Object getsToStringedAndPrinted) {
		System.out.println(getsToStringedAndPrinted);
	}

}

