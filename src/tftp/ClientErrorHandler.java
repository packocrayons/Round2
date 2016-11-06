
package tftp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import packets.ErrorPacket;
import packets.ErrorType;

// @author team 17
public class ClientErrorHandler implements ErrorHandler {
	
	private final OutputHandler out;
	
	public ClientErrorHandler(OutputHandler out){
		this.out = out;
	}

	@Override
	public void handleLocalFileNotFound(DatagramSocket socket, InetAddress address, int port) {
		out.highPriorityPrint("The file to be transfered cannot be found.");
	}

	@Override
	public void handleRemoteFileNotFound(DatagramSocket socket, InetAddress address, int port) {
		out.highPriorityPrint("The server cannot find the file that you are looking for.");
	}

	@Override
	public void handleLocalAccessViolation(DatagramSocket socket, InetAddress address, int port) {
		out.highPriorityPrint("The file to be transfered cannot be accessed.");
		if(socket != null && address != null && port != 0){
			//things happened after the connection was established.
			ErrorPacket e = new ErrorPacket(ErrorType.ACCESS_VIOLATION);
			try {
				socket.send(new DatagramPacket(e.getBytes(), e.getBytes().length, address, port));
			} catch (IOException e1) {
				//What the hell do I do if the error handler errors out?
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void handleRemoteAccessViolation(DatagramSocket socket, InetAddress address, int port) {
		out.highPriorityPrint("The file to be transfered cannot be accessed by the server.");
		
	}

	@Override
	public void handleLocalAllocationExceeded(DatagramSocket socket, InetAddress address, int port) {
		out.highPriorityPrint("There is not enough room to store the file locally");
		if(socket != null && address != null && port != 0){
			//things happened after the connection was established.
			ErrorPacket e = new ErrorPacket(ErrorType.ALLOCATION_EXCEEDED);
			try {
				socket.send(new DatagramPacket(e.getBytes(), e.getBytes().length, address, port));
			} catch (IOException e1) {
				//What the hell do I do if the error handler errors out?
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void handleRemoteAllocationExceeded(DatagramSocket socket, InetAddress address, int port) {
		out.highPriorityPrint("The server does not have enough room for the file");
	}

	@Override
	public void handleLocalIllegalTftpOperation(DatagramSocket socket, InetAddress address, int port) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleRemoteIllegalTftpOperation(DatagramSocket socket, InetAddress address, int port) {
		// TODO Auto-generated method stub
		
	}

	
	

}

