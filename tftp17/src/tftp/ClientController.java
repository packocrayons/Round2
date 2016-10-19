package tftp;

import java.net.InetAddress;


/**
 * ClientController controls the Read and Write operations of the client 
 * along with assigning the appropriate ports. It also manages when the 
 * system is in the quiet or verbose mode.
 * 
 * @author Team 15
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
			client.readFile(path, InetAddress.getLocalHost(), (testMode)?23:69);
		}catch(Throwable t){
			//throw new RuntimeException(t.getMessage());
			System.out.println("The file you are trying to read does not exist.");
		}
	
		
	}
	
	public void writeFile(String path){
		try{
			client.writeFile(path, InetAddress.getLocalHost(), (testMode)?23:69);
		}catch(Throwable t){
			//throw new RuntimeException(t.getMessage());
			System.out.println("The file you are trying to write does not exist.");
		}
		
	}

	public boolean isTestMode() {
		return testMode;
	}

	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}
	
	
}
