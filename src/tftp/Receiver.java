
package tftp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Objects;

import packets.AcknowledgementPacket;
import packets.DataPacket;
import packets.ErrorPacket;
import packets.ErrorType;
import packets.MistakePacket;
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
	private final OutputStream file;
	private final String fileName;
	private final DatagramSocket socket;
	private final boolean closeItWhenDone;
	private final PacketFactory pFac = new PacketFactory();
	private boolean senderFound = false;

	private boolean lastReceived=false;
	private InetAddress address;
    private boolean closed = false;
	private int port;
	
	public boolean retryRequest = true;
	
	/**
	 * @param err The error handler to use
	 * @param out Where to send the output to
	 * @param file The OutputStream to write the incomming file to
	 * @param socket The socket to use, with a preconfigured timeout
	 * @param closeItWhenDone Weather or not to close the socket when we are done
	 * @param fname The name of the file being transfered, this is to give the output some more context
	 */
	public Receiver(ErrorHandler err, OutputHandler out, OutputStream file, DatagramSocket socket, boolean closeItWhenDone,String fname){
		this.err = err;
		this.out = out;
		this.file = file;
		this.fileName=fname;
		this.socket = socket;
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
				
				//Get the data, catch the timeout
				try{
					socket.receive(datagramIn);
					out.lowPriorityPrint("Packet received from :" );
					out.lowPriorityPrint(datagramIn);
				} catch (SocketTimeoutException e){
					
					if(lastReceived==true)out.highPriorityPrint("Transmission complete, file received successfully.");
					else{out.highPriorityPrint("Receiver timed out , transfer failed");}
					break;
				}
				
				if(!senderFound){
					address = datagramIn.getAddress();
					port = datagramIn.getPort();
					out.highPriorityPrint("Recording sender address"+ address +" and sender port :"+port);
					senderFound = true;
					retryRequest = false;
				}

				
				//if it's not from the right sender it is bad
				if(!datagramIn.getAddress().equals(address) || datagramIn.getPort()!=port){
					out.highPriorityPrint("this packet comes from another sender -> discarded");
					err.handleLocalUnknownTransferId(socket, datagramIn.getAddress(), datagramIn.getPort());
					//don't proceed the packet return to wait for a packet
				}
				else{//if the sender is the expected one proceed the packet
				
					//check the input
					Packet p = pFac.getPacket(datagramIn);
					if(!p.getType().equals(PacketType.DATA)){
						if(p.getType().equals(PacketType.ERR)){
							ErrorPacket er = (ErrorPacket)p;
							
							if(out.getQuiet())out.highPriorityPrint("Error packet received of type "+er.getErrorType()+" with the following message"+er.getMessage());
							
							if(er.getErrorType().equals(ErrorType.ACCESS_VIOLATION)){
								err.handleRemoteAccessViolation(socket, address, port,er);
							}else if(er.getErrorType().equals(ErrorType.ALLOCATION_EXCEEDED)){
								err.handleRemoteAllocationExceeded(socket, address, port,er);
							}else if(er.getErrorType().equals(ErrorType.FILE_NOT_FOUND)){
								err.handleRemoteFileNotFound(socket, address, port,er);
							}else if(er.getErrorType().equals(ErrorType.ILLEGAL_TFTP_OPERATION)){//error 4
								err.handleRemoteIllegalTftpOperation(socket, address, port,er);
							}else if(er.getErrorType().equals(ErrorType.UNKNOWN_TRANSFER_ID)){//error 5
								err.handleRemoteUnknownTransferId(socket, address, port,er);
							}else if(er.getErrorType().equals(ErrorType.FILE_ALREADY_EXISTS)){//error 6
								err.handleRemoteFileAlreadyExists(socket, address, port,er);
							}else{
								throw new RuntimeException("The packet received is some unimplemented error type");
							}
						}else if (p.getType().equals(PacketType.MISTAKE)){
							//Mistake packet received create error packet 4 to send to the handler
							MistakePacket mp=(MistakePacket)p;
							err.handleLocalIllegalTftpOperation(socket, address, port, mp.getMessage());
						}else{
							//if receiver receives any thing else than mistake or data or error
							err.handleLocalIllegalTftpOperation(socket,address, port, "Packet type "+p.getType()+" not expected by the receiver");
						}
						close();
						out.highPriorityPrint("Transmission failed");
						break;
					}
					DataPacket dp = (DataPacket)p;
					if (out.getQuiet()){//quiet
						out.highPriorityPrint("Data packet #"+dp.getNumber()+" received. It has "+dp.getFilePart().length+" bytes");
					}
					out.lowPriorityPrint(dp);
					
					System.out.print("\n");
					if(dp.getNumber() <= lastBlockNumber){
						out.highPriorityPrint("It is a retransmition/duplicate packet");
						//we received a retransmition of a previous block
						ack(dp.getNumber());
					}else if(dp.comesAfter(lastBlockNumber)){
						out.lowPriorityPrint("It is the expected block (next block)");
						//we received the next block
						writeOut(dp.getFilePart());
						lastBlockNumber = (lastBlockNumber+1) & 0xffff; 
						ack(lastBlockNumber);
						if(dp.isLast()){
							lastReceived=true;
							continue;
						}
					}else{
						//we received block # over schedule
						err.handleLocalIllegalTftpOperation(socket,address, port , "DATA Packet #"+dp.getNumber()+" received by the receiver is over schedule");
						out.highPriorityPrint("Transmission failed");
						break;
					}
				}
			}
			
			//maybe leave the socket open for a short while to retransmit the last ack?
			close();
		}catch(Throwable t){
			throw new RuntimeException(t);
		}
	}

	private void ack(int n) throws IOException{
		if (out.getQuiet())	out.highPriorityPrint("Sending ack #"+n);
		DatagramPacket ackPack=new AcknowledgementPacket(n).asDatagramPacket(address, port);
		
		socket.send(ackPack);
		out.lowPriorityPrint("Sending packet to:");
		out.lowPriorityPrint(ackPack);
		out.lowPriorityPrint(new AcknowledgementPacket(n));
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
				err.handleLocalAccessViolation(socket, address, port,fileName);
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
			out.lowPriorityPrint("Closing file stream");
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
				out.lowPriorityPrint("Closing socket");
				socket.close();
			}
			out.highPriorityPrint("Receiver is shutting down");
		}
	}
}
