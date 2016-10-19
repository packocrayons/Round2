package tftp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;


/**
 * ClientUI class has all the User Interface (UI) that the user would use on the 
 * Client side to read, write, toggle test and quiet mode, 
 * and prompt the user for input (such as the filename and instruction)
 * 
 * @author Team 15
 */
public class ClientUI implements Runnable, OutputHandler {
	private ClientController client;
	private static final PrintStream sysOut = System.out;
	private static final PrintStream doNothingOut = 
		new PrintStream(new OutputStream(){
			@Override
			public void write(int arg0) throws IOException {}
	    });
	
	private synchronized static boolean sysOutIsOpen(){
		return System.out.equals(sysOut);
	}
	
	//This feels terrible, but it will work for us
	private synchronized static boolean toggleSysOut(){
		if(sysOutIsOpen()){
			System.setOut(doNothingOut);
		    System.out.flush();
		    return false;
		}else{
			System.setOut(sysOut);
		    System.out.flush();
		    return true;
		}
	}
	
	public ClientUI(ClientController c){
		client = c;
		sysOut.println("Client Running");
		sysOut.println("Starting the UI");
		sysOut.println("Type 'toggle test' to switch to test mode and 'toggle quiet' to switch to quiet mode. Type 'help' for more information");
		sysOut.println("Type 'read (filename)' to read from server or 'write (filename)' to write to server");
	}

	
	public void run(){
		Scanner sc = new Scanner(System.in);
		while(true){
			String s = sc.nextLine();
			if(s == null || s.length() == 0){
				sysOut.println("Cannot read input... Try again");
			}else{
				s.trim();
				String[] array = (s+" , ,").split("\\s+");
				if("read".equalsIgnoreCase(array[0])){
					sysOut.println("This a read command");
					client.readFile(array[1]);
					sysOut.println("Read completed sucsessfully");
					sysOut.println("Type 'toggle test' to switch to test mode and 'toggle quiet' to switch to quiet mode. Type 'help' for more information");
					sysOut.println("Type 'read (filename)' to read from server or 'write (filename)' to write to server");
				}else if("write".equalsIgnoreCase(array[0])){
					sysOut.println("This a write command");
					client.writeFile(array[1]);
					sysOut.println("Write completed sucsessfully");
					sysOut.println("Type 'toggle test' to switch to test mode and 'toggle quiet' to switch to quiet mode. Type 'help' for more information");
					sysOut.println("Type 'read (filename)' to read from server or 'write (filename)' to write to server");
				}else if("toggle".equalsIgnoreCase(array[0])){
					if("test".equalsIgnoreCase(array[1])){
						sysOut.println((client.toggleTestMode())?("Test mode is now active"):("Test mode is now inactive"));
					}else if("quiet".equalsIgnoreCase(array[1])){
						sysOut.println((toggleSysOut())?("Quite mode is now inactive"):("Quite mode is now active"));
						
					}else{
						sysOut.println("Toggle what,"
								+ "test or quiet?");
					}
				}else if("help".equalsIgnoreCase(array[0])){
					sysOut.println(
							"Valid commands are\n"
							+ "read (file name), write (file name), toggle (quiet|test), help, quit");
				}else if("quit".equalsIgnoreCase(array[0])){
					break;
				}
			}
			
		}
		sc.close();
	}

	@Override
	public void lowPriorityPrint(Object getsToStringedAndPrinted) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void highPriorityPrint(Object getsToStringedAndPrinted) {
		// TODO Auto-generated method stub
		
	}
}
