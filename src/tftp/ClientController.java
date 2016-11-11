
package tftp;

import java.net.InetAddress;


/**
 * ClientController controls the Read and Write operations of the client 
 * along with assigning the appropriate ports. It also manages when the 
 * system is in the quiet or verbose mode.
 * This class primarily cleans up code, it calls methods in the Client class.
* @author Team 17
 */
public class ClientController {
	private final Client client;
	
	
	private boolean testMode = false;
	
	public ClientController(Client c){
		client = c;
	
	}
	
	public boolean toggleTestMode(){
		return testMode = !testMode;
	}
	
	
	public void readFile(String path){
		try{
			client.readFile(path, InetAddress.getLocalHost(), (testMode)?23:69); //try to get the client to read the file
		}catch(Throwable t){
			//throw new RuntimeException(t.getMessage());
			System.out.println("Client : The file you are trying to read does not exist at this path.");
		}
	
		
	}
	
	public void writeFile(String path){
		try{
			client.writeFile(path, InetAddress.getLocalHost(), (testMode)?23:69);
		}catch(Throwable t){
			//throw new RuntimeException(t.getMessage());
			System.out.println("Client : The file you are trying to write does not exist at this path.");
		}
		
	}

	public boolean isTestMode() {
		return testMode;
	}

	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}
	
	
}

