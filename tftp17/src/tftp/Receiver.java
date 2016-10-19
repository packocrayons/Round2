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
	private InetAddress address;
    private boolean closed = false;
	private int port;
	private final PacketFactory pFac = new PacketFactory();
	
	public Receiver(ErrorHandler err, OutputHandler out, OutputStream file, DatagramSocket socket, boolean closeItWhenDone){
		this.err = err;
		this.out = out;
		this.file = file;
		this.socket = socket;
		this.closeItWhenDone = closeItWhenDone;
		
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
	
	
	private DataPacket narrowIncomming(Packet p){
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
			return null;
		}
		return (DataPacket)p;
		
	}
	
	@Override
	public void run() {
		try{
			int lastBlock = 0;
			byte[] buffer = new byte[Packet.getBufferSize()];
			DatagramPacket datagramIn = new DatagramPacket(buffer, buffer.length);
			
			socket.receive(datagramIn);
			address = datagramIn.getAddress();
			port = datagramIn.getPort();
			
			DataPacket dp = narrowIncomming(pFac.getPacket(datagramIn.getData(), datagramIn.getLength()));
		    if(dp == null){
		    	close(); 
		    	return;
		    }
			
		    //lastBlock = 1;
			out.lowPriorityPrint("Receiving Data"+dp.getNumber()+"\nIt is "+dp.getBytes().length+" bytes long");
			if(dp.getNumber() != 1){
				throw new RuntimeException("The initial dataBlock has the wrong number");
			}
			
			if(!writeOut(dp.getFilePart())){
				close();
				return;
			}
			
			while(true){
				if(!dp.comesAfter(lastBlock)){
					throw new RuntimeException("The blocks are not being sent sequentially");
				}
				lastBlock = dp.getNumber();
				
				AcknowledgementPacket ap = new AcknowledgementPacket(dp.getNumber()); 
				
				socket.send(new DatagramPacket(ap.getBytes(), ap.getBytes().length, address, port));
				out.lowPriorityPrint("Sending ACK"+ap.getNumber()+" to port:"+port);

				if(dp.isLast()){
					break;
				}
				
				socket.receive(datagramIn);
				
				
				dp = narrowIncomming(pFac.getPacket(datagramIn.getData(), datagramIn.getLength()));
			    if(dp == null){
			    	close(); 
			    	return;
			    }
				
				out.lowPriorityPrint("Receiving Data"+dp.getNumber()+" from port "+ datagramIn.getPort() +"\nIt is "+dp.getBytes().length+" bytes long");
				
				if(!writeOut(dp.getFilePart())){
					close();
					return;
				}
			}

			close();
		}catch(Throwable t){
			close();
			throw new RuntimeException(t.getMessage());
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
