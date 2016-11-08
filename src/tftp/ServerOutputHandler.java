
package tftp;

import java.net.DatagramPacket;

import packets.AcknowledgementPacket;
import packets.DataPacket;
import packets.ErrorPacket;
import packets.ReadRequestPacket;
import packets.WriteRequestPacket;

//* @author Team 17

public class ServerOutputHandler implements OutputHandler {

	@Override
	public void lowPriorityPrint(Object o) {
		
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

	@Override
	public void highPriorityPrint(Object getsToStringedAndPrinted) {
		System.out.println(getsToStringedAndPrinted);
	}

}

