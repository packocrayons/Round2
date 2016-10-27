<<<<<<< HEAD
package tftp;

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
	public void handleLocalFileNotFound(DatagramSocket socket, InetAddress address, int port) {
		out.highPriorityPrint("The requested file cannot be found.");
		if(socket != null && address != null && port != 0){
			//things happened after the connection was established.
			ErrorPacket e = new ErrorPacket(ErrorType.FILE_NOT_FOUND);
			try {
				socket.send(new DatagramPacket(e.getBytes(), e.getBytes().length, address, port));
			} catch (IOException e1) {
				//What the hell do I do if the error handler errors out?
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void handleRemoteFileNotFound(DatagramSocket socket, InetAddress address, int port) {
		//do nothing 
		//the server does not need this
	}

	@Override
	public void handleLocalAccessViolation(DatagramSocket socket, InetAddress address, int port) {
		out.highPriorityPrint("The requested file cannot be accessed.");
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
		out.highPriorityPrint("The file can nolonger be accessed by the client.");
	}

	@Override
	public void handleLocalAllocationExceeded(DatagramSocket socket, InetAddress address, int port) {
		out.highPriorityPrint("The server has run out of space");
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
		out.highPriorityPrint("The client has run out of space");
	}

	@Override
	public void handleLocalIllegalTftpOperation(DatagramSocket socket, InetAddress address, int port) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleRemoteIllegalTftpOperation(DatagramSocket socket, InetAddress address, int port) {
		// TODO Auto-generated method stub
		
	}

=======
package tftp;

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
	public void handleLocalFileNotFound(DatagramSocket socket, InetAddress address, int port) {
		out.highPriorityPrint("The requested file cannot be found.");
		if(socket != null && address != null && port != 0){
			//things happened after the connection was established.
			ErrorPacket e = new ErrorPacket(ErrorType.FILE_NOT_FOUND);
			try {
				socket.send(new DatagramPacket(e.getBytes(), e.getBytes().length, address, port));
			} catch (IOException e1) {
				//What the hell do I do if the error handler errors out?
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void handleRemoteFileNotFound(DatagramSocket socket, InetAddress address, int port) {
		//do nothing 
		//the server does not need this
	}

	@Override
	public void handleLocalAccessViolation(DatagramSocket socket, InetAddress address, int port) {
		out.highPriorityPrint("The requested file cannot be accessed.");
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
		out.highPriorityPrint("The file can nolonger be accessed by the client.");
	}

	@Override
	public void handleLocalAllocationExceeded(DatagramSocket socket, InetAddress address, int port) {
		out.highPriorityPrint("The server has run out of space");
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
		out.highPriorityPrint("The client has run out of space");
	}

	@Override
	public void handleLocalIllegalTftpOperation(DatagramSocket socket, InetAddress address, int port) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleRemoteIllegalTftpOperation(DatagramSocket socket, InetAddress address, int port) {
		// TODO Auto-generated method stub
		
	}

>>>>>>> 9e1e11a572111a2572274ab61298e6048e5c7c7c
}