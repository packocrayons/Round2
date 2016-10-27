<<<<<<< HEAD
package tftp;

import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.lang.model.type.ErrorType;

import packets.ErrorPacket;

/**
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
	
	public abstract void handleLocalFileNotFound(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleRemoteFileNotFound(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleLocalAccessViolation(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleRemoteAccessViolation(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleLocalAllocationExceeded(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleRemoteAllocationExceeded(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleLocalIllegalTftpOperation(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleRemoteIllegalTftpOperation(DatagramSocket socket, InetAddress address, int port );
	
}
=======
package tftp;

import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.lang.model.type.ErrorType;

import packets.ErrorPacket;

/**
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
	
	public abstract void handleLocalFileNotFound(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleRemoteFileNotFound(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleLocalAccessViolation(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleRemoteAccessViolation(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleLocalAllocationExceeded(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleRemoteAllocationExceeded(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleLocalIllegalTftpOperation(DatagramSocket socket, InetAddress address, int port );
	public abstract void handleRemoteIllegalTftpOperation(DatagramSocket socket, InetAddress address, int port );
	
}
>>>>>>> 9e1e11a572111a2572274ab61298e6048e5c7c7c
