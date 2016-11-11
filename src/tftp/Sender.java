
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
	private static final int MAXNUMBERTIMEOUT = 4;

	private final ErrorHandler err;
	private final OutputHandler out;
	
	private final InputStream file;
	private final String fileName;
	private final DatagramSocket socket;
	private final boolean closeItWhenDone;
	private final InetAddress address;
	private final int port;
	private final PacketFactory pfac = new PacketFactory();
	private int numberOfRetransmit=0;
	
	private boolean closed = false;
	
	public Sender(ErrorHandler err, OutputHandler out, InputStream file, DatagramSocket socket, boolean closeItWhenDone, InetAddress address, int port,String fname){
		this.err = err;
		this.out = out;
		this.file = file;
		this.fileName=fname;
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
					out.lowPriorityPrint("Packet received from :" );
					out.lowPriorityPrint(receiveWith);
					break; //if we got it, leave the loop
				} catch (SocketTimeoutException e){
					out.highPriorityPrint("Timed out, retransmitting");
					socket.send(retransmit); //keep trying to send the datagram
					out.lowPriorityPrint("Sending packet to :" );
					out.lowPriorityPrint(retransmit);
					out.lowPriorityPrint("Packet type: DATA\n Block number " + number+"\n Number of bytes: "+( retransmit.getLength()-4));
		    
					
					//need to increment a counter to allow the sender to shut down after X retransmit = error packet from receiver lost
					numberOfRetransmit+=1;
					if(numberOfRetransmit==MAXNUMBERTIMEOUT)break;
				}
			}
			if(numberOfRetransmit==MAXNUMBERTIMEOUT){
				break;//break out of everything 
			}
			else{
				numberOfRetransmit=0;
			}
			
			//if it's not from the right sender just discard the message for IT3
			if(receiveWith.getAddress()!=address || receiveWith.getPort()!=port){
				out.highPriorityPrint("this packet comes from another sender -> discarded");
				break;
			}
			
			Packet p = pfac.getPacket(receiveWith.getData(),receiveWith.getLength());
			
			if(p.getType().equals(PacketType.ACK)){
				AcknowledgementPacket ap = (AcknowledgementPacket)p;
				if (out.getQuiet())	out.highPriorityPrint("Receiving ACK"+ap.getNumber()+" from port "+receiveWith.getPort());
				out.lowPriorityPrint(ap);
				
				
				
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
						err.handleRemoteAccessViolation(socket, address, port,er);
					}else if(er.getErrorType().equals(ErrorType.ALLOCATION_EXCEEDED)){
						err.handleRemoteAllocationExceeded(socket, address, port,er);
					}else if(er.getErrorType().equals(ErrorType.FILE_NOT_FOUND)){
						err.handleRemoteFileNotFound(socket, address, port,er);//error Here need handleRemoteFileNotfound
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
					err.handleLocalAccessViolation(socket, address, port,fileName);
					break;
				}
				
				//this turns the -1 of an empty read into 0, to make it safer
				readSize = Math.max(readSize, 0);
				
				dp = new DataPacket(number, fileBuffer, readSize);
				
				DatagramPacket datagram = new DatagramPacket(dp.getBytes(), dp.getBytes().length, address, port);
				socket.send(datagram);
				
				if (out.getQuiet())out.highPriorityPrint("Sending Data"+dp.getNumber()+" to port:"+datagram.getPort()+"\nIt is "+dp.getBytes().length+" bytes long");
				out.lowPriorityPrint("Sending packet to :");
				out.lowPriorityPrint(datagram);
				out.lowPriorityPrint(dp);
				
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
			out.highPriorityPrint("Sender is shutting down");
		}
	}
	
}
