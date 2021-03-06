
package tftp;

//* @author Team 17
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import packets.ErrorPacket;
import packets.ErrorType;

public class ServerErrorHandler implements ErrorHandler {

	private final OutputHandler out;
	
	public ServerErrorHandler(OutputHandler out){
		this.out = out;
	}
	
	@Override
	public void handleLocalFileNotFound(DatagramSocket socket, InetAddress address, int port,String fname) {
		out.highPriorityPrint("The requested file cannot be found on the server side.");
		if(socket != null && address != null && port != 0){
			//things happened after the connection was established.
			ErrorPacket e = new ErrorPacket(ErrorType.FILE_NOT_FOUND,"file "+fname+" requested not found");
			DatagramPacket errorPack=new DatagramPacket(e.getBytes(), e.getBytes().length, address, port);
			try {
				socket.send(errorPack);
				
				out.lowPriorityPrint("Sending Error Packet to :");
				out.lowPriorityPrint(errorPack);
				out.lowPriorityPrint(e);
			} catch (IOException e1) {
				//What the hell do I do if the error handler errors out?
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void handleRemoteFileNotFound(DatagramSocket socket, InetAddress address, int port,ErrorPacket ep) {
		//do nothing 
		//the server does not need this
	}

	@Override
	public void handleLocalAccessViolation(DatagramSocket socket, InetAddress address, int port,String fname) {
		out.highPriorityPrint("The requested file cannot be accessed in the server side.");
		if(socket != null && address != null && port != 0){
			//things happened after the connection was established.
			ErrorPacket e = new ErrorPacket(ErrorType.ACCESS_VIOLATION,"File"+fname+" cannot be accessed");
			DatagramPacket errorPack=new DatagramPacket(e.getBytes(), e.getBytes().length, address, port);
			try {
				socket.send(errorPack);
				out.lowPriorityPrint("Sending Error Packet to :");
				out.lowPriorityPrint(errorPack);
				out.lowPriorityPrint(e);
			} catch (IOException e1) {
				//What the hell do I do if the error handler errors out?
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void handleRemoteAccessViolation(DatagramSocket socket, InetAddress address, int port,ErrorPacket ep) {
		out.lowPriorityPrint(ep);
		out.highPriorityPrint("The file can nolonger be accessed by the client.");
	}

	@Override
	public void handleLocalAllocationExceeded(DatagramSocket socket, InetAddress address, int port) {
		out.highPriorityPrint("The server has run out of space");
		if(socket != null && address != null && port != 0){
			//things happened after the connection was established.
			ErrorPacket e = new ErrorPacket(ErrorType.ALLOCATION_EXCEEDED,"Server full");
			DatagramPacket errorPack=new DatagramPacket(e.getBytes(), e.getBytes().length, address, port);
			try {
				socket.send(errorPack);
				out.lowPriorityPrint("Sending Error Packet to :");
				out.lowPriorityPrint(errorPack);
				out.lowPriorityPrint(e);
			} catch (IOException e1) {
				//What the hell do I do if the error handler errors out?
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void handleRemoteAllocationExceeded(DatagramSocket socket, InetAddress address, int port,ErrorPacket ep) {
		out.lowPriorityPrint(ep);
		out.highPriorityPrint("The client has run out of space");
	}

	@Override
	public void handleLocalIllegalTftpOperation(DatagramSocket socket, InetAddress address, int port, String message) {
		// TODO Auto-generated method stub
		out.highPriorityPrint("The server received an invalid packet");
		out.lowPriorityPrint(message);
		if(socket != null && address != null && port != 0){
			//things happened after the connection was established.
			ErrorPacket e = new ErrorPacket(ErrorType.ILLEGAL_TFTP_OPERATION,message);
			DatagramPacket errorPack=new DatagramPacket(e.getBytes(), e.getBytes().length, address, port);
			try {
				socket.send(errorPack);
				out.lowPriorityPrint("Sending Error Packet to :");
				out.lowPriorityPrint(errorPack);
				out.lowPriorityPrint(e);
			} catch (IOException e1) {
				//What the hell do I do if the error handler errors out?
				e1.printStackTrace();
			}
		}
		
	}

	@Override
	public void handleRemoteIllegalTftpOperation(DatagramSocket socket, InetAddress address, int port,ErrorPacket ep ) {
		out.lowPriorityPrint(ep);
		out.highPriorityPrint("Packet sent was invalid and rejected by the client");
		
	}
	
	@Override
	public void handleLocalUnknownTransferId(DatagramSocket socket, InetAddress address, int port ){
		out.highPriorityPrint("The server doesn't know this TID");
		if(socket != null && address != null && port != 0){
			//things happened after the connection was established.
			ErrorPacket e = new ErrorPacket(ErrorType.UNKNOWN_TRANSFER_ID,"Unknown TID for the server");
			DatagramPacket errorPack=new DatagramPacket(e.getBytes(), e.getBytes().length, address, port);
			try {
				socket.send(errorPack);
				out.lowPriorityPrint("Sending Error Packet to :");
				out.lowPriorityPrint(errorPack);
				out.lowPriorityPrint(e);
			} catch (IOException e1) {
				//What the hell do I do if the error handler errors out?
				e1.printStackTrace();
			}
		}
	}
	
	@Override
	public void handleRemoteUnknownTransferId(DatagramSocket socket, InetAddress address, int port,ErrorPacket ep ){
		out.lowPriorityPrint(ep);
		out.highPriorityPrint("The client rejected packet sent (wrong TID)");
	}
	
	@Override
	public void handleLocalFileAlreadyExists(DatagramSocket socket, InetAddress address, int port ){
		//not implemented we allow override
	}
	
	@Override
	public  void handleRemoteFileAlreadyExists(DatagramSocket socket, InetAddress address, int port,ErrorPacket ep  ){
		out.lowPriorityPrint(ep);
		out.highPriorityPrint("The client doesn't allow override");
	}
	

}