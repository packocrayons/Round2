
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
				byte[] requestBuffer = new byte[Math.max(ReadRequestPacket.getBufferSize(), WriteRequestPacket.getBufferSize())];
				
				DatagramPacket requestDatagram = new DatagramPacket(requestBuffer, requestBuffer.length);
				
				//add timeout here as well ? 
				try{
					serverSocket.receive(requestDatagram);
				}catch(SocketException e){
					break;
				}
				
				Packet p = pFac.getPacket(requestDatagram.getData(), requestDatagram.getLength());
				
				if(p.getType().equals(PacketType.RRQ)){
					ReadRequestPacket r = (ReadRequestPacket)p;
					System.out.println("Reading requested file");
					DatagramSocket sendingSocketRRQ = new DatagramSocket();
					
					try{
						InputStream input = fFac.readFile(r.getFilePath());
						new Thread(new Sender(err, out, input, sendingSocketRRQ, true, requestDatagram.getAddress(), requestDatagram.getPort())).start();
					}catch(FileNotFoundException e){
						err.handleLocalFileNotFound(sendingSocketRRQ, requestDatagram.getAddress(), requestDatagram.getPort());
					}catch(IllegalAccessException e){
						err.handleLocalAccessViolation(sendingSocketRRQ, requestDatagram.getAddress(), requestDatagram.getPort());
					}
					
				}else if(p.getType().equals(PacketType.WRQ)){
					WriteRequestPacket r1 = (WriteRequestPacket) p;
					DatagramSocket sendingSocketWRQ = new DatagramSocket();
					
					try{
					
						OutputStream output = fFac.writeFile(r1.getFilePath());
						AcknowledgementPacket ap = new AcknowledgementPacket(0);
						byte[] ackPayload = ap.getBytes();
						sendingSocketWRQ.send(new DatagramPacket(ackPayload, ackPayload.length, requestDatagram.getAddress(), requestDatagram.getPort()));
						new Thread(new Receiver(err, out, output, sendingSocketWRQ, true)).start();//non-blocking

					}catch(IllegalAccessException e){
						err.handleLocalAccessViolation(sendingSocketWRQ, requestDatagram.getAddress(), requestDatagram.getPort());
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
		System.out.println("Type 'quit' to shutdown the server");
		Scanner scanner=new Scanner(System.in);
	    while (true) {
	        String input = scanner.nextLine();
	        if(input.toLowerCase().contains("quit")){
	        	break;
	        }
	        
	    }
    	server.close();
	    scanner.close();
	    System.out.println("Shutting Down Server");
	}

}
