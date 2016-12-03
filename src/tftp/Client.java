
package tftp;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import packets.Packet;

import packets.AcknowledgementPacket;
import packets.ErrorPacket;
import packets.ErrorType;
import packets.FileType;
import packets.MistakePacket;
import packets.PacketFactory;
import packets.PacketType;
import packets.ReadRequestPacket;
import packets.WriteRequestPacket;


/**
 * Client class is the client side for TFTP. The client uses one port and sends 
 * a read or write request and gets the appropriate response from the server.
 * 
 * @author Team 17
 */
public class Client {

	public static final int SENDER_TIMEOUT = 2*1000;//times out to retransmit
	public static final int SENDER_TIMEOUT_MAX = 5;//if the sender retransmits this many times in a row, it closes
	public static final int RECEIVER_TIMEOUT = 10*1000;//If the receiver times out once, it closes 
	
	public static final int MAX_REQUEST_RETRIES = 5;
	
	private final DatagramSocket socket;
	private final FileFactory fFac;
	private final PacketFactory pfac = new PacketFactory();
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
				int tries = 0;
				while(true){
					FileOutputStream output = fFac.writeFile(filePath);
					ReadRequestPacket rq = new ReadRequestPacket(filePath, FileType.OCTET);
					DatagramPacket request = new DatagramPacket(rq.getBytes(), rq.getBytes().length, address, port);
					
					socket.setSoTimeout(RECEIVER_TIMEOUT);
					socket.send(request);
					out.highPriorityPrint("Client sending RRQ to Server");
					
					//print info of packet sent
					out.lowPriorityPrint("Sending packet to :" );
					out.lowPriorityPrint(request);
					out.lowPriorityPrint(rq);
					
					Receiver runner = new Receiver(err, out, output, socket, false, filePath, fFac);
					runner.run();
					if(!runner.getSenderFound()){
						break;
					}else if(tries++ < MAX_REQUEST_RETRIES){
						out.highPriorityPrint("The read request timmed out, retrying");
					}else{
						out.highPriorityPrint("None of the read requests have worked.\nGiving up");
						break;
					}
				}
			} catch (IllegalAccessException  e) {
				err.handleLocalAccessViolation(null, null, 0,filePath);
			}catch (Throwable t) {
				System.err.println (t.getMessage());
			}
	}

	public void writeFile(String filePath, InetAddress address, int port){
		try {
			InputStream input = fFac.readFile(filePath);
			WriteRequestPacket rq = new WriteRequestPacket(filePath, FileType.OCTET);
			DatagramPacket request =new DatagramPacket(rq.getBytes(), rq.getBytes().length, address, port);
			DatagramPacket ack0 = new DatagramPacket(new byte[Packet.getBufferSize()], AcknowledgementPacket.getBufferSize());
			socket.setSoTimeout(SENDER_TIMEOUT);
			
			int tries = 0;
			while(true){
				try{
					socket.send(request);
					//print info of packet sent
					if(out.getQuiet()){
						out.highPriorityPrint("Client sending WRQ to Server");
					}
					out.lowPriorityPrint("Sending packet to :" );
					out.lowPriorityPrint(request);
					out.lowPriorityPrint(rq);
					
					socket.receive(ack0);
					//print some info of packet received
					out.lowPriorityPrint("Packet received from :" );
					out.lowPriorityPrint(ack0);
					break;
				} catch (SocketTimeoutException e){
					if(tries++ < MAX_REQUEST_RETRIES){
						out.highPriorityPrint("The write request timmed out, retrying");
					}else{
						out.highPriorityPrint("None of the write requests have worked.\nGiving up");
						break;
					}
				}
			}
			
			Packet p = pfac.getPacket(ack0.getData(), ack0.getLength());
			
			if(p.getType().equals(PacketType.ACK)){
				//print rest of info 
				if(out.getQuiet()){
					out.highPriorityPrint("Client receiving ack0 from server");
				}
				out.lowPriorityPrint((AcknowledgementPacket)p);
				
				if(((AcknowledgementPacket)(p)).getNumber() != 0){
					//change needed here to handle delay/loss
					throw new RuntimeException("This is the wrong Ack");
				}
				new Sender(err, out, input, socket, false, ack0.getAddress(), ack0.getPort(),filePath, SENDER_TIMEOUT_MAX).run();
			}else if(p.getType().equals(PacketType.ERR)){
				ErrorPacket ep = (ErrorPacket)p;
				if(ep.getErrorType().equals(ErrorType.FILE_NOT_FOUND)){//error 1
					err.handleRemoteFileNotFound(null, null, 0,ep);
				}
				else if(ep.getErrorType().equals(ErrorType.ACCESS_VIOLATION)){//error 2
					err.handleRemoteAccessViolation(null, null, 0,ep);
				}
				else if(ep.getErrorType().equals(ErrorType.ILLEGAL_TFTP_OPERATION)){//error 4
					err.handleRemoteIllegalTftpOperation(null, null, 0,ep);
				}
				else if(ep.getErrorType().equals(ErrorType.UNKNOWN_TRANSFER_ID)){//error 5
					err.handleRemoteUnknownTransferId(null, null, 0,ep);
					//need to resend the request
				}
				else if(ep.getErrorType().equals(ErrorType.FILE_ALREADY_EXISTS)){//error 6
					err.handleRemoteFileAlreadyExists(null,null, 0,ep);
				}
				else{
					throw new RuntimeException("The packet received is some unimplemented error type");
				}
			}else if (p.getType().equals(PacketType.MISTAKE)){
				//Mistake packet received create error packet 4 to send to the handler
				MistakePacket mp=(MistakePacket)p;
				err.handleLocalIllegalTftpOperation(socket, ack0.getAddress(), ack0.getPort(), mp.getMessage());
			}else{
				//if client receives any thing else than mistake or ack or error
				err.handleLocalIllegalTftpOperation(socket, ack0.getAddress(), ack0.getPort(), "Packet type "+p.getType()+" not expected by the client");
			}
				
		} catch (IllegalAccessException e) {
			err.handleLocalAccessViolation(null, null, 0,filePath);//no error sent
		} catch (FileNotFoundException e) {
			err.handleLocalFileNotFound(null, null, 0,filePath);//no error sent
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

