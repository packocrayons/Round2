
package tftp;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import packets.AcknowledgementPacket;
import packets.DataPacket;
import packets.ErrorPacket;
import packets.ErrorType;
import packets.Packet;
import packets.PacketFactory;
import packets.PacketType;

/**
 * Sender get the file, sends it to the port, receives 
 * the acknowledgement packet, and closes the file and socket
 * @author Team 17
 */
public class Sender implements Runnable {
	
	private static final int SENDINGPORTTIMEOUT = 2000; 

	private final ErrorHandler err;
	private final OutputHandler out;
	
	private final InputStream file;
	private final DatagramSocket socket;
	private final boolean closeItWhenDone;
	private final InetAddress address;
	private final int port;
	private final PacketFactory pfac = new PacketFactory();
	
	private boolean closed = false;
	
	public Sender(ErrorHandler err, OutputHandler out, InputStream file, DatagramSocket socket, boolean closeItWhenDone, InetAddress address, int port){
		this.err = err;
		this.out = out;
		this.file = file;
		this.socket = socket;
		try {
			this.socket.setSoTimeout(SENDINGPORTTIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace(); //unlikely to happen, if this throws an error we have bigger problems
		}
		this.closeItWhenDone = closeItWhenDone;	
		this.address = address;
		this.port = port;
		
	}

	
	private boolean getValidAckPacket(DatagramPacket receiveWith, DatagramPacket retransmit, int number) throws IOException{
		while(true){
			//wait to receive the packet, or catch a SocketTimeoutException
			//We're on the sending side, so we are responsible for retransmit
			while(true){
				try{
					socket.receive(receiveWith);
					break; //if we got it, leave the loop
				} catch (SocketTimeoutException e){
					out.lowPriorityPrint("Timed out, retransmitting");
					socket.send(retransmit); //keep trying to send the datagram
				}
			}
			
			Packet p = pfac.getPacket(receiveWith.getData(),receiveWith.getLength());
			
			if(p.getType().equals(PacketType.ACK)){
				AcknowledgementPacket ap = (AcknowledgementPacket)p;
				
				out.lowPriorityPrint("Receiving ACK"+ap.getNumber()+" from port "+receiveWith.getPort());
				
				/*DEPRECATED
				if(ap.getNumber() != number){
					throw new RuntimeException("This is the wrong acknowledgement");
				}
				*/
				
				if(ap.getNumber() < number){ //if it's less than we're working with right now, it's a duplicate ack
					//SORCERER'S APPRENTICE BUG - DO NOTHING
					//we loop around and get a packet again.
				} else if(ap.getNumber() > number){
					throw new RuntimeException("This acknowledgement is ahead of schedule");
					//TODO not sure exactly what we're supposed to do here, but freaking out seems like as good a choice as any
				} else { //it's not > and not <, it must be =. We successfully got a valid ack packet
					return true;
				}
				
			}else{
				if(p.getType().equals(PacketType.ERR)){
					ErrorPacket er = (ErrorPacket)p;
					if(er.getErrorType().equals(ErrorType.ACCESS_VIOLATION)){
						err.handleRemoteAccessViolation(socket, address, port);
					}else if(er.getErrorType().equals(ErrorType.ALLOCATION_EXCEEDED)){
						err.handleRemoteAllocationExceeded(socket, address, port);
					}else if(er.getErrorType().equals(ErrorType.FILE_NOT_FOUND)){
						err.handleRemoteAllocationExceeded(socket, address, port);
					}else{
						throw new RuntimeException("The packet receved is some unimplemented error type");
					}
				}else{
					throw new RuntimeException("The packet received is of the wrong type");
				}
				break; //break out of everything 
			}
		}
		return false; //something went wrong
	}
	
	@Override
	public void run(){
		try{
			int number = 1;
			byte[] fileBuffer = new byte[512];
			byte[] buffer = new byte[Packet.getBufferSize()];
			DataPacket dp;
			DatagramPacket ack = new DatagramPacket(buffer, buffer.length);
			int readSize = -1;
			while(!closed){
				try{
					readSize = file.read(fileBuffer);
				}catch(IOException e){
					err.handleLocalAccessViolation(socket, address, port);
					break;
				}
				
				//this turns the -1 of an empty read into 0, to make it safer
				readSize = Math.max(readSize, 0);
				
				dp = new DataPacket(number, fileBuffer, readSize);
				
				DatagramPacket datagram = new DatagramPacket(dp.getBytes(), dp.getBytes().length, address, port);
				socket.send(datagram);
				
				out.lowPriorityPrint("Sending Data"+dp.getNumber()+" to port:"+datagram.getPort()+"\nIt is "+dp.getBytes().length+" bytes long");
				
				if (!getValidAckPacket(ack, datagram, number)) break; //if something goes wrong - at the moment only an error packet causes this to return false

				number = (number+1) & 0xffff;
				
				if(readSize < 512){
					break;
				}
			
			}
			close();
		}catch(Throwable t){
			close();
			throw new RuntimeException(t);
		}
	}
	
	public void finalize(){
		close();
	}
	
	private synchronized void close(){
		if(!closed){
			closed = true;
			out.highPriorityPrint("Transfer finished.");
			out.lowPriorityPrint("Closing file stream");
			
			try {
				this.file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(closeItWhenDone){
				out.lowPriorityPrint("Closing socket");
				socket.close();
			}
		}
	}
	
}
