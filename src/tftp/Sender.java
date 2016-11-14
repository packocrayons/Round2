
package tftp;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
	
	private final int maxTimeouts;

	private final ErrorHandler err;
	private final OutputHandler out;
	
	private final InputStream file;
	private final String fileName;
	private final DatagramSocket socket;
	private final boolean closeItWhenDone;
	private final InetAddress address;
	private final int port;
	private final PacketFactory pfac = new PacketFactory();
		
	private boolean closed = false;
	
	public boolean retryRequest = true;
	
	/**
	 * 
	 * @param err				The error handler that this Sender should use.
	 * @param out				The Object that this should send all output
	 * @param file				The InputStream that this reads from
	 * @param socket			The socket to use, already configured with the  timeout time.
	 * @param closeItWhenDone	Should this close the socket when it is done?
	 * @param address			The address of the Receiver
	 * @param port				The port of the Receiver
	 * @param fname				The name of the file being transfered, used to give context to the output messages 
	 * @param maxRetries		The number of times to retry, 0 for INTEGER.MAXVALUE
	 */
	public Sender(ErrorHandler err, OutputHandler out, InputStream file, DatagramSocket socket, boolean closeItWhenDone, InetAddress address, int port,String fname, int maxRetries){
		this.err = err;
		this.out = out;
		this.file = file;
		this.fileName=fname;
		this.socket = socket;
		this.maxTimeouts = maxRetries;
		this.closeItWhenDone = closeItWhenDone;	
		this.address = address;
		this.port = port;
		
	}

	
	private boolean getValidAckPacket(DataPacket data) throws IOException{
		DatagramPacket dp = new DatagramPacket(new byte[Packet.getBufferSize()], Packet.getBufferSize());
		
		while(true){
			int numberOfRetransmit = 0;
			while(true){
				try{
					socket.receive(dp);
					out.lowPriorityPrint("Packet received from :"+dp.getPort());
					break; //if we got it, leave this loop
				} catch (SocketTimeoutException e){
					if(++numberOfRetransmit==maxTimeouts){
						out.highPriorityPrint("Timed out too many times");
						return false;
					}
					out.highPriorityPrint("Timed out, retransmitting");
					socket.send(data.asDatagramPacket(address, port)); //keep trying to send the datagram
					out.lowPriorityPrint("Sending packet to :"+port);
					out.lowPriorityPrint("Packet type: DATA\n Block number " + data.getNumber()+"\n Number of bytes: "+(data.getBytes().length));
				}
			}
			
			//if it's not from the right sender just discard the message for IT3
			if(!dp.getAddress().equals(address) || dp.getPort()!=port){
				out.highPriorityPrint("this packet comes from another sender -> discarded");
			}else{
				Packet p = pfac.getPacket(dp.getData(), dp.getLength());
				
				if(p.getType().equals(PacketType.ACK)){
					AcknowledgementPacket ap = (AcknowledgementPacket)p;
					if (out.getQuiet())	{
						out.highPriorityPrint("Receiving ACK"+ap.getNumber()+" from port "+dp.getPort());
					}
					out.lowPriorityPrint(ap);
					
					if(ap.acknowledges(data.getNumber())){
						out.lowPriorityPrint("ACK packet received is the expected one");
						return true;
					}
					else if(ap.inferiorTo(data.getNumber())){
						out.highPriorityPrint("Duplicate ACK Packet, discarded");
					}
					else{
						//add error 4 for # over schedule
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
		}
		return false; //something went wrong
	}
	
	@Override
	public void run(){
		try{
			int number = 0;
			byte[] fileBuffer = new byte[512];
			DataPacket dp;
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
				
				dp = new DataPacket(++number, fileBuffer, readSize);
				
				DatagramPacket datagram = new DatagramPacket(dp.getBytes(), dp.getBytes().length, address, port);
				socket.send(datagram);
				
				if (out.getQuiet())out.highPriorityPrint("Sending Data"+dp.getNumber()+" to port:"+datagram.getPort()+"\nIt is "+dp.getBytes().length+" bytes long");
				out.lowPriorityPrint("Sending packet to :");
				out.lowPriorityPrint(datagram);
				out.lowPriorityPrint(dp);
				
				if (!getValidAckPacket(dp)) {
					//an error packet arrived, or it took too long
					break;
				}
				retryRequest = false;
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
