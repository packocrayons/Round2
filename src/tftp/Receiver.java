
package tftp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
 * @author Team 15
 */
public class Receiver implements Runnable {

	private final ErrorHandler err;
	private final OutputHandler out;
	
	private final OutputStream file;
	private final DatagramSocket socket;
	private final boolean closeItWhenDone;
	private final PacketFactory pFac = new PacketFactory();
	private boolean senderFound = false;
	private InetAddress address;
    private boolean closed = false;
	private int port;
	
	public Receiver(ErrorHandler err, OutputHandler out, OutputStream file, DatagramSocket socket, boolean closeItWhenDone){
		this.err = err;
		this.out = out;
		this.file = file;
		this.socket = socket;
		this.closeItWhenDone = closeItWhenDone;
	}
	
	@Override
	public void run() {
		try{
			//Initialization
			int lastBlock = 0;
			byte[] buffer = new byte[Packet.getBufferSize()];
			DatagramPacket datagramIn = new DatagramPacket(buffer, buffer.length);
			
			
			while(true){
				socket.receive(datagramIn);
				if(!senderFound){
					address = datagramIn.getAddress();
					port = datagramIn.getPort();
					senderFound = true;
				}
				
				
				Packet p = pFac.getPacket(datagramIn);
				
				//check the input
				if(!p.getType().equals(PacketType.DATA)){
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
						throw new RuntimeException("The wrong type of packet was receved, it was not Data or Error");
					}
					close();
					break;
				}
				
				DataPacket dp = (DataPacket)p;
				
				
				if(dp.getNumber() == lastBlock){
					//we received a retransition of the last block
					ack(lastBlock);
				}else if(dp.comesAfter(lastBlock)){
					//we received the next block
					writeOut(dp.getBytes());
					lastBlock = (lastBlock+1) & 0xffff; 
					ack(lastBlock);
					if(dp.isLast()){
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
		socket.send(new AcknowledgementPacket(n).asDatagramPacket(address, port));
	}
	
	private boolean writeOut(byte[] data){
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
			out.highPriorityPrint("Transfer finnished.");
			out.lowPriorityPrint("Closing file stream");
			
			try {
				this.file.close();
			} catch (IOException e) {
				/*
				 * this should not be able to cause errors.
				 * If it does, it does not matter
				 * the .flush() call guarantees that the file has been written to disk already.
				 */
				e.printStackTrace();
			}
			
			if(closeItWhenDone){
				out.lowPriorityPrint("Closing socket");
				socket.close();
			}
		}
	}
}
