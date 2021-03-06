
package tftp;

import java.net.DatagramSocket;
import java.net.InetAddress;

import packets.ErrorPacket;


/**
 * @author Team 17
 * Server and Client provide one of.
 * 
 * They should be their own class,
 * this means that Server and Client SHOULD NOT implement this on their own, 
 * there should be something like 'ServerErrorHandler' and 'ClientErrorHandler'
 *  
 * 
 * feel free to add arguments to these prototypes as needed.
 */
public interface ErrorHandler {
	
	/*
	 * they DO NOT close the socket, that is up to the sender and recever's .close() to do
	 */
	
	public abstract void handleLocalFileNotFound(DatagramSocket socket, InetAddress address, int port,String fname );
	public abstract void handleRemoteFileNotFound(DatagramSocket socket, InetAddress address, int port,ErrorPacket ep );
	public abstract void handleLocalAccessViolation(DatagramSocket socket, InetAddress address, int port,String fname);
	public abstract void handleRemoteAccessViolation(DatagramSocket socket, InetAddress address, int port,ErrorPacket ep );
	public abstract void handleLocalAllocationExceeded(DatagramSocket socket, InetAddress address, int port);
	public abstract void handleRemoteAllocationExceeded(DatagramSocket socket, InetAddress address, int port,ErrorPacket ep );
	public abstract void handleLocalIllegalTftpOperation(DatagramSocket socket, InetAddress address, int port,String message);
	public abstract void handleRemoteIllegalTftpOperation(DatagramSocket socket, InetAddress address, int port,ErrorPacket ep  );
	public abstract void handleLocalUnknownTransferId(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleRemoteUnknownTransferId(DatagramSocket socket, InetAddress address, int port,ErrorPacket ep );
	public abstract void handleLocalFileAlreadyExists(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleRemoteFileAlreadyExists(DatagramSocket socket, InetAddress address, int port,ErrorPacket ep  );
	
}

