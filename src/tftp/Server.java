
package tftp;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

import packets.AcknowledgementPacket;
import packets.Packet;
import packets.PacketFactory;
import packets.PacketType;
import packets.ReadRequestPacket;
import packets.WriteRequestPacket;


/**
 * The server handles requests and makes a new thread for each client connection
 * It sends back the appropriate response without any actual file transfer.
 * One socket (69) is used to receive (it stays open) and another for each response.
 * @author Team 17
 *
 */
public class Server implements Runnable{
	public static final int SERVER_PORT = 69;
	
	public static final int SENDER_TIMEOUT = 2000;//times out to retransmit
	public static final int SENDER_TIMEOUT_MAX = 5;//if the sender retransmits this many times in a row, it closes
	public static final int RECEIVER_TIMEOUT = 20000;//If the receiver times out once, it closes 
	
	private final DatagramSocket serverSocket;
	private final PacketFactory pFac= new PacketFactory();
	private final FileFactory fFac;
	private final OutputHandler out = new ServerOutputHandler();
	private final ErrorHandler err = new ServerErrorHandler(out);

	
	public Server(){
		this(".\\server");
	}
	
	public Server(String path) {
		fFac = new FileFactory(path);
		try {
			serverSocket = new DatagramSocket(SERVER_PORT);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	/**
	 * Closes the Datagramsocket
	 */
	public void close(){
		serverSocket.close();
	}
	/**
	 * Main loop that receives and handles requests
	 */
	public void run() {
		try{
			while(true){
				byte[] requestBuffer = new byte[Packet.getBufferSize()];
				
				DatagramPacket requestDatagram = new DatagramPacket(requestBuffer, requestBuffer.length);
				//add timeout here as well ? 
				//no, this should not time out.
				//when it is time to shut down the server, we close the socket
				//this causes the SocketException that breaks from the loop.
				try{
					serverSocket.receive(requestDatagram);

					out.lowPriorityPrint("Packet received from :" );
					out.lowPriorityPrint(requestDatagram);
				}catch(SocketException e){
					break;
				}
				
				Packet p = pFac.getPacket(requestDatagram.getData(), requestDatagram.getLength());
				//add printed info here.
				if(p.getType().equals(PacketType.RRQ)){
					ReadRequestPacket r = (ReadRequestPacket)p;
					
					out.lowPriorityPrint(r);
					if(out.getQuiet()){//quiet
						out.highPriorityPrint("Server receiving RRQ from client");
					}
					out.highPriorityPrint("Reading requested file");
					
					DatagramSocket sendingSocketRRQ = new DatagramSocket();
					sendingSocketRRQ.setSoTimeout(SENDER_TIMEOUT);
					InputStream input = null;
					try{
						input = fFac.readFile(r.getFilePath());
						
						
					}catch(FileNotFoundException e){
						err.handleLocalFileNotFound(sendingSocketRRQ, requestDatagram.getAddress(), requestDatagram.getPort(),r.getFilePath());
					}catch(IllegalAccessException e){
						err.handleLocalAccessViolation(sendingSocketRRQ, requestDatagram.getAddress(), requestDatagram.getPort(),r.getFilePath());
					}
					if(input != null){
						new Thread(new Sender(err, out, input, sendingSocketRRQ, true, requestDatagram.getAddress(), requestDatagram.getPort(),r.getFilePath(), SENDER_TIMEOUT_MAX)).start();
					}else{
						sendingSocketRRQ.close();
					}
					
				}else if(p.getType().equals(PacketType.WRQ)){
					WriteRequestPacket r1 = (WriteRequestPacket) p;
					out.lowPriorityPrint(r1);
					if(out.getQuiet()){//quiet
						out.highPriorityPrint("Server receiving WRQ from client");
					}
					DatagramSocket sendingSocketWRQ = new DatagramSocket();
					sendingSocketWRQ.setSoTimeout(SENDER_TIMEOUT);
					
					try{
						OutputStream output = fFac.writeFile(r1.getFilePath());
						AcknowledgementPacket ap = new AcknowledgementPacket(0);
						byte[] ackPayload = ap.getBytes();
						DatagramPacket ackPack=new DatagramPacket(ackPayload, ackPayload.length, requestDatagram.getAddress(), requestDatagram.getPort());
						//no re-sending ack0, if this does not get to the client, we will time out and they will send a new request.
						sendingSocketWRQ.send(ackPack);
						if(out.getQuiet()){
							out.highPriorityPrint("Server sending ack0 to client");
						}
						//print info of packet sent
						out.lowPriorityPrint("Sending packet to :" );
						out.lowPriorityPrint(ackPack);
						out.lowPriorityPrint(ap);
						
						new Thread(new Receiver(err, out, output, sendingSocketWRQ, true,r1.getFilePath())).start();//non-blocking

					}catch(IllegalAccessException e){
						err.handleLocalAccessViolation(sendingSocketWRQ, requestDatagram.getAddress(), requestDatagram.getPort(),r1.getFilePath());
					}
				}else{
					throw new RuntimeException("This packet is not a valid read or write request");
				}
			}	
		}catch(Throwable t){
			t.printStackTrace();
			throw new RuntimeException(t);
		}
	}
	

	/**
	 * Returns the serverSocket
	 */
	public DatagramSocket getServerSocket() {
		return serverSocket;
	}
	
	/**
	 * Functions calls are executed here
	 * @param args
	 */

	public static void main(String[] args) {
		Server server;
		if(args.length < 1){
			server = new Server();
		}else{
			server = new Server(args[0]);
		}
		
		new Thread(server).start();
		System.out.println("The server has started");
		System.out.println("Type 'toggle quiet' to switch to quiet mode.");
		System.out.println("Type 'quit' to shutdown the server");
		Scanner scanner=new Scanner(System.in);
	    while (true) {
	        String input = scanner.nextLine();
	        if(input.toLowerCase().contains("quit")){
	        	break;
	        }
	        else if (input.toLowerCase().contains("toggle quiet")){
	        	server.out.setQuiet(!server.out.getQuiet());
	        	System.out.println(server.out.getQuiet()?("Quiet mode is now active"):("Quiet mode is now inactive"));
	        }
	        
	    }
    	server.close();
	    scanner.close();
	    System.out.println("Shutting Down Server");
	}

}
