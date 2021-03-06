
package tftp;

import java.net.DatagramPacket;
import java.net.*;
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
		InetAddress ownAddress=null;
		try{
			ownAddress=InetAddress.getLocalHost();
		}catch(UnknownHostException e){
			e.printStackTrace();
		}
		System.out.println("Client Running on IP Address : "+ownAddress);
		System.out.println("Starting the UI ");
		help();
	}

	
	public void run(){
		Scanner sc = new Scanner(System.in);
		while(true){
			String s = sc.nextLine();
			if(s == null || s.length() == 0){
				System.out.println("Cannot read input... Try again");
			}else{
				s.trim();
				String[] array = (s+" , , , , ,").split("[\\s]+");
				if ("set".equalsIgnoreCase(array[0]) && "server".equalsIgnoreCase(array[1]) && "location".equalsIgnoreCase(array[2]) ){
					InetAddress newServerAddress;
					try{
						newServerAddress=InetAddress.getByName(array[3]);
						client.setAddress(newServerAddress);
						System.out.println("Server location changed to "+newServerAddress);
					}catch(UnknownHostException e){
						System.out.println("This address is invalid");
					}
				}else if("read".equalsIgnoreCase(array[0])){
					System.out.println("This a read command");
					client.readFile(array[1]);
				}else if("write".equalsIgnoreCase(array[0])){
					System.out.println("This a write command");
					client.writeFile(array[1]);
				}else if("make".equalsIgnoreCase(array[0])){
					System.out.println("Making test file");
					client.makeTestFile(array[1], Long.valueOf(array[2]));
				}else if("toggle".equalsIgnoreCase(array[0])){
					if("test".equalsIgnoreCase(array[1])){
						System.out.println((client.toggleTestMode())?("Test mode is now active"):("Test mode is now inactive"));
					}else if("quiet".equalsIgnoreCase(array[1])){
						System.out.println((toggleQuiet())?("Quiet mode is now active"):("Quiet mode is now inactive"));
					}else{
						System.out.println("Toggle what, test or quiet?");
					}
				}else if("help".equalsIgnoreCase(array[0])){
					help();
				}else if("quit".equalsIgnoreCase(array[0])){
					break;
				}
			}
			
		}
		sc.close();
	}
	
	private void help(){
		System.out.println(
				"Valid commands are\n"
				+ "set server location (address)\n"
				+ "\tSet where requests are directed\n\n"
				+ "read (file name)\n"
				+ "\tSend a read request for the names file\n\n"
				+ "write (file name)\n"
				+ "\tSend a write request for the names file\n\n"
				+ "toggle (quiet|test)\n"
				+ "\tChange client configuration\n\n"
				+ "make (file name) (number of bytes)\n"
				+ "\tCreate a new test file in the clients working directory\n\n"
				+ "help\n"
				+ "\tPrint this message\n\n"
				+ "quit\n"
				+ "\tClose thet client");
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


