
package tftp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.util.Scanner;

import packets.*;


/**
 * ClientUI class has all the User Interface (UI) that the user would use on the 
 * Client side to read, write, toggle test and quiet mode, 
 * and prompt the user for input (such as the filename and instruction)
 * 
 * @author Team 17
 */
public class ClientUI implements Runnable, OutputHandler {
	private ClientController client;
	private boolean quiet = false;
	
	private boolean toggleQuiet(){
		return (quiet = !quiet);
	}
	
	public ClientUI(ClientController c){
		client = c;
		System.out.println("Client Running");
		System.out.println("Starting the UI");
		System.out.println("Type 'toggle test' to switch to test mode and 'toggle quiet' to switch to quiet mode. Type 'help' for more information");
		System.out.println("Type 'read (filename)' to read from server or 'write (filename)' to write to server");
	}

	
	public void run(){
		Scanner sc = new Scanner(System.in);
		while(true){
			String s = sc.nextLine();
			if(s == null || s.length() == 0){
				System.out.println("Cannot read input... Try again");
			}else{
				s.trim();
				String[] array = (s+" , ,").split("\\s+");
				if("read".equalsIgnoreCase(array[0])){
					System.out.println("This a read command");
					client.readFile(array[1]);
				}else if("write".equalsIgnoreCase(array[0])){
					System.out.println("This a write command");
					client.writeFile(array[1]);
				}else if("toggle".equalsIgnoreCase(array[0])){
					if("test".equalsIgnoreCase(array[1])){
						System.out.println((client.toggleTestMode())?("Test mode is now active"):("Test mode is now inactive"));
					}else if("quiet".equalsIgnoreCase(array[1])){
						System.out.println((toggleQuiet())?("Quiet mode is now active"):("Quiet mode is now inactive"));
					}else{
						System.out.println("Toggle what, test or quiet?");
					}
				}else if("help".equalsIgnoreCase(array[0])){
					System.out.println(
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
	public void lowPriorityPrint(Object o) {
		if(!quiet){
			if (o instanceof String){
				System.out.println("Client: "+o);
			}
			else if (o instanceof DatagramPacket){
				DatagramPacket p= (DatagramPacket)o;
	            System.out.println("Host: " + p.getAddress());
	            System.out.println("Host port: " + p.getPort());
	            int len = p.getLength();
	            System.out.println("Length: " + len);
			}
			else if (o instanceof ReadRequestPacket){
				ReadRequestPacket p= (ReadRequestPacket)o;
		        System.out.println("Packet type: "+ p.getType());
		        System.out.println("Filename: "+ p.getFilePath());
			}
			else if (o instanceof WriteRequestPacket){
				WriteRequestPacket p= (WriteRequestPacket)o;
		        System.out.println("Packet type: "+ p.getType());
		        System.out.println("Filename: "+ p.getFilePath());       	
			}
			else if (o instanceof DataPacket){
				DataPacket p= (DataPacket)o;
		        System.out.println("Packet type: "+ p.getType());
		        System.out.println("Block number " + p.getNumber());
            	System.out.println("Number of bytes: "+ p.getFilePart().length);
			}
			else if (o instanceof AcknowledgementPacket){
				AcknowledgementPacket p= (AcknowledgementPacket)o;
		        System.out.println("Packet type: "+ p.getType());
		        System.out.println("Block number " + p.getNumber());
			}
			else if (o instanceof ErrorPacket){
				ErrorPacket p=(ErrorPacket) o;
				System.out.println("Packet type:"+ p.getType());
            	System.out.println("Error type :"+p.getErrorType());
            	System.out.println("Error message :"+p.getMessage());
		    }
	           
	        System.out.println();
	
		}
	}

	@Override
	public void highPriorityPrint(Object getsToStringedAndPrinted) {
		System.out.println(getsToStringedAndPrinted);
	}
	
	@Override
	public boolean getQuiet(){
		return quiet;
	}
	
	@Override
	public void setQuiet(boolean newQuiet){
		quiet=newQuiet;
	}

}


