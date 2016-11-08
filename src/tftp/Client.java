
package tftp;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import packets.Packet;

import packets.AcknowledgementPacket;
import packets.ErrorPacket;
import packets.ErrorType;
import packets.PacketFactory;
import packets.PacketType;
import packets.ReadRequestPacket;
import packets.WriteRequestPacket;
import tftp.FileType;


/**
 * Client class is the client side for TFTP. The client uses one port and sends 
 * a read or write request and gets the appropriate response from the server.
 * 
 * @author Team 17
 */
public class Client {
	
	private static final int SENDINGPORTTIMEOUT = 2000;
	private final DatagramSocket socket;
	private final FileFactory fFac;
	private ErrorHandler err;
	private OutputHandler out;
	
	
	public Client(String path){
		fFac = new FileFactory(path);
		try{
			socket = new DatagramSocket();
			
		}catch(Throwable t){
			t.printStackTrace();
			throw new RuntimeException(t.getMessage());
		}
		try {
			socket.setSoTimeout(SENDINGPORTTIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace(); //unlikely to happen, if this throws an error we have bigger problems
		}
	}
	
	public Client(){
		this(".\\client");
	}
	
	public void setOutputHandler(OutputHandler o){
		out = o;
		err = new ClientErrorHandler(out);
	}
	
	public void readFile(String filePath, InetAddress address, int port){
			try {
				OutputStream output = fFac.writeFile(filePath);
				ReadRequestPacket rq = new ReadRequestPacket(filePath, FileType.OCTET);
				//must deal with rrq lost or delayed.Maybe in receiver ? ?? 
				socket.send(new DatagramPacket(rq.getBytes(), rq.getBytes().length, address, port));
				new Receiver(err, out, output, socket, false).run();
			} catch (IllegalAccessException  e) {
				err.handleLocalAccessViolation(null, null, 0);
			}catch (Throwable t) {
				System.err.println (t.getMessage());
			}
	}

	public void writeFile(String filePath, InetAddress address, int port){
		try {
			InputStream input = fFac.readFile(filePath);
			WriteRequestPacket rq = new WriteRequestPacket(filePath, FileType.OCTET);
			DatagramPacket ack0 = new DatagramPacket(new byte[Packet.getBufferSize()], AcknowledgementPacket.getBufferSize());
			
			
			while(true){
				try{
					socket.send(new DatagramPacket(rq.getBytes(), rq.getBytes().length, address, port));
					socket.receive(ack0);
					break; //if we got it, leave the loop
				} catch (SocketTimeoutException e){
					out.highPriorityPrint("Timeout,  client retransmits the write request");
					//need to increment a counter to allow the sender to shut down after X retransmit = error packet from receiver lost
				}
			}
			
			
			PacketFactory pfac = new PacketFactory();
			Packet p = pfac.getPacket(ack0.getData(), ack0.getLength());
			
			if(p.getType().equals(PacketType.ACK)){
				if(((AcknowledgementPacket)(p)).getNumber() != 0){
					//change needed here to handle delay/loss
					throw new RuntimeException("This is the wrong Ack");
				}
				new Sender(err, out, input, socket, false, ack0.getAddress(), ack0.getPort()).run();
			}else if(p.getType().equals(PacketType.ERR)){
				ErrorPacket ep = (ErrorPacket)p;
				if(ep.getErrorType().equals(ErrorType.ACCESS_VIOLATION)){
					err.handleRemoteAccessViolation(null, null, 0);
				}
			}
		} catch (IllegalAccessException e) {
			err.handleLocalAccessViolation(null, null, 0);
		} catch (FileNotFoundException e) {
			err.handleLocalFileNotFound(null, null, 0);
		} catch (Throwable t) {
			System.err.println (t.getMessage());
		}
	}
	
	
	public static void main(String[] args){
		Client c;
		
		if(args.length < 1){
			c = new Client();
		}else{
			c = new Client(args[0]);
		}
		
		ClientController cc = new ClientController(c);
		ClientUI cu = new ClientUI(cc);
		c.setOutputHandler(cu);
		cu.run();
	}
	
}

