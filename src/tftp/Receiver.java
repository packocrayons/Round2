
package tftp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Objects;

import packets.AcknowledgementPacket;
import packets.DataPacket;
import packets.ErrorPacket;
import packets.ErrorType;
import packets.Packet;
import packets.PacketFactory;
import packets.PacketType;

/**
 * Receiver receives the data files, writes them, and sends 
 * an acknowledgement packet when its done writing
 * @author Team 17
 */
public class Receiver implements Runnable {

	private final ErrorHandler err;
	private final OutputHandler out;
	
	private static final int RECEIVINGPORTTIMEOUT = 4000; //larger then the sending port 
	private static final int MAXNUMBERTIMEOUT = 4;
	
	private final OutputStream file;
	private final DatagramSocket socket;
	private final boolean closeItWhenDone;
	private final PacketFactory pFac = new PacketFactory();
	private boolean senderFound = false;
	private InetAddress address;
    private boolean closed = false;
	private int port;
	private int numberOfTimeout=0;
	
	public Receiver(ErrorHandler err, OutputHandler out, OutputStream file, DatagramSocket socket, boolean closeItWhenDone){
		this.err = err;
		this.out = out;
		this.file = file;
		this.socket = socket;
		try {
			this.socket.setSoTimeout(RECEIVINGPORTTIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace(); //unlikely to happen, if this throws an error we have bigger problems
		}
		this.closeItWhenDone = closeItWhenDone;
	}
	
	@Override
	public void run() {
		try{
			//Initialization
			int lastBlockNumber = 0;
			byte[] buffer = new byte[Packet.getBufferSize()];
			DatagramPacket datagramIn = new DatagramPacket(buffer, buffer.length);

			out.highPriorityPrint("Receiver waiting for Data packet");

			
			while(true){
				
				//some time out code goes around this statement
				while(true){
					try{
						socket.receive(datagramIn);
						break; //if we got it, leave the loop
					} catch (SocketTimeoutException e){
						numberOfTimeout+=1;
						out.highPriorityPrint("Timed out ("+numberOfTimeout+") no retransmit from the receiver");
						
						if(numberOfTimeout==MAXNUMBERTIMEOUT)break;
						//need to increment a counter to allow the sender to shut down after X retransmit = error packet from receiver lost
					}
				}
				if(numberOfTimeout==MAXNUMBERTIMEOUT){
					close();
					break;
				}
				else{
					numberOfTimeout=0;
				}
				
				if(!senderFound){
					
					address = datagramIn.getAddress();
					port = datagramIn.getPort();
					out.highPriorityPrint("Recording sender address"+ address +" and sender port :"+port);
					senderFound = true;
				}

				//check the input
				Packet p = pFac.getPacket(datagramIn);
				if(!p.getType().equals(PacketType.DATA)){
					if(p.getType().equals(PacketType.ERR)){
						ErrorPacket er = (ErrorPacket)p;
						out.highPriorityPrint("Error packet received of type "+er.getErrorType());
						
						if(er.getErrorType().equals(ErrorType.ACCESS_VIOLATION)){
							err.handleRemoteAccessViolation(socket, address, port);
						}else if(er.getErrorType().equals(ErrorType.ALLOCATION_EXCEEDED)){
							err.handleRemoteAllocationExceeded(socket, address, port);
						}else if(er.getErrorType().equals(ErrorType.FILE_NOT_FOUND)){
							err.handleRemoteFileNotFound(socket, address, port);
						}else{
							throw new RuntimeException("The packet receved is some unimplemented error type");
						}
					}else{
						throw new RuntimeException("The wrong type of packet was receved, it was not Data or Error");
					}
					close();
					out.highPriorityPrint("Transmission failed");
					break;
				}
				DataPacket dp = (DataPacket)p;
				out.highPriorityPrint("Data packet #"+dp.getNumber()+" receved. It has "+dp.getFilePart().length+" bytes");
				/* print the data to be writen to disk
				for(byte b : dp.getFilePart()){
					System.out.print((char) b);
				}
				*/
				System.out.print("\n");
				if(dp.getNumber() == lastBlockNumber){
					out.highPriorityPrint("It is a retransmition/duplicate packet");
					//we received a retransition of the last block
					ack(lastBlockNumber);
				}else if(dp.comesAfter(lastBlockNumber)){
					out.highPriorityPrint("It is the expected block (next block)");
					//we received the next block
					writeOut(dp.getFilePart());
					lastBlockNumber = (lastBlockNumber+1) & 0xffff; 
					ack(lastBlockNumber);
					if(dp.isLast()){

						out.highPriorityPrint("Transmission complete, file received successfully.");
						break;
					}
				}else{
					//we received some other block
					continue;
				}
				
			}
			
			//maybe leave the socket open for a short while to retransmit the last ack?
			close();
		}catch(Throwable t){
			throw new RuntimeException(t);
		}
	}

	private void ack(int n) throws IOException{
		out.highPriorityPrint("Sending ack #"+n);
		socket.send(new AcknowledgementPacket(n).asDatagramPacket(address, port));
	}
	
	private boolean writeOut(byte[] data){
		out.lowPriorityPrint("Writing to disk");
		try{
			file.write(data);
			file.flush();
			return true;
		}catch(IOException e){
			if(Objects.toString(e.getMessage(), "").toLowerCase().contains("space left on")){//"No space left on device"
				err.handleLocalAllocationExceeded(socket, address, port);
			}else{
				err.handleLocalAccessViolation(socket, address, port);
			}
			return false;
		}
	}
	
	public void finalize(){
		close();
	}

	private synchronized void close(){
		if(!closed){
			closed = true;
			out.highPriorityPrint("Closing file stream");
			try {
				this.file.close();
			} catch (IOException e) {
				/*
				 * this should not be able to cause errors.
				 * If it does, it does not matter
				 * the file.flush() call guarantees that the file has been written to disk already.
				 */
				e.printStackTrace();
			}
			
			if(closeItWhenDone){
				out.highPriorityPrint("Closing socket");
				socket.close();
			}
			out.highPriorityPrint("Receiver is shutting down");
		}
	}
}
