package tftp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


import packets.Packet;
import packets.PacketFactory;



/**
 * IntermediateHost is an error simulator for a simple TFTP server
 * based on UDP/IP. The host receives a read or write packet from a client and
 * passes it on to the server. Upon receiving a response, it passes it on to the client.
 * One well known socket (23) is used as well as one for the client, and another one for 
 * the server to send/receive.
 * @author Team 15
 */
public class IntermediateHost{
	
	private DatagramSocket wellKnownSocket, serverSocket, clientSocket;
	private InetAddress clientAdd, serverAdd;
	private int clientPort, serverPort; 
	private static final PacketFactory pf = new PacketFactory();
	private PacketFX[] effects;
	
	public IntermediateHost(){
		this(new PacketFX[0]); //send it an empty array
	}


	//this constructor now takes arguments. The original is still in place
	public IntermediateHost(PacketFX[] fx) {
		effects = fx;
		try {
			wellKnownSocket = new DatagramSocket(23);
			serverSocket = new DatagramSocket();
			clientSocket = new DatagramSocket();
			
			//There are three threads so that the host can be listening on all three ports at the same time
			//for D1, the host does not need to care about what it is moving at all.
			
			//Listens on the well known port (23), updates the client's address, and creates new client ports
			//this thread listens for packetFX of type 1
			new Thread(new Runnable(){

				@Override
				public void run() {
					PacketFX[] myFX;
					int myFXIndex = 0;
					for (int i = 0; i < effects.length; ++i) {
						if (effects[i].getAffectedThread() == 1) myFX[myFXIndex++] = effects[i];
					} //get all of my effects
					try{
						DatagramPacket d = new DatagramPacket(new byte[Packet.getBufferSize()],Packet.getBufferSize());
						while(true){
							wellKnownSocket.receive(d); //receive a packet on the well known socket
							newClientSocket();
							updateClientAddress(d.getAddress(), d.getPort());
							Packet p = pf.getPacket(d.getData(), d.getLength());
							System.out.println("\nReceived a packet of type ["+p.getType().getHumanReadableName()+"] and length "+p.getBytes().length+" on the well known port");
							System.out.println("Testing if this packet should be affected");
							for (int i = 0; i < myFX.length; ++i) {
								if (myFX[i].affectThisPacket(p)) //TODO effect the packet (ideally by calling myFX[i].sendEffectPacket)
							}
							System.out.println("Sending it to the server's well known port");
							System.out.println("");
							sendToServerWellKnownPort(p);
						}
					}catch(Throwable t){
						t.printStackTrace();
						throw new RuntimeException(t.getMessage());
					}
				}
			}).start();
			
			
			//listens on the client port, and sends to the server port
			//this thread listens for packetfx of type 2
			new Thread(new Runnable(){

				@Override
				public void run() {
					try{
						DatagramPacket d = new DatagramPacket(new byte[Packet.getBufferSize()],Packet.getBufferSize());
						while(true){
							boolean good = false;
							try{
								clientSocket.receive(d);
								good = true;
							}catch(Throwable t){}
							if(good){
								Packet p = pf.getPacket(d.getData(), d.getLength());
								System.out.println("Received a packet of type ["+p.getType().getHumanReadableName()+"] and length "+p.getBytes().length+" from the client");
								System.out.println("Sending it to the server");
								System.out.println("");
								sendToServer(p);
							}
						}
					}catch(Throwable t){
						t.printStackTrace();
						throw new RuntimeException(t);
					}
				}
			}).start();
			
			//listens on the server port, updates the server's location, and sends to the client
			//this thread listens for packetFX of type 3
			new Thread(new Runnable(){

				@Override
				public void run() {
					try{
						DatagramPacket d = new DatagramPacket(new byte[Packet.getBufferSize()],Packet.getBufferSize());
						while(true){
							serverSocket.receive(d);
							updateServerAddress(d.getAddress(), d.getPort());
							Packet p = pf.getPacket(d.getData(), d.getLength());
							System.out.println("Received a packet of type ["+p.getType().getHumanReadableName()+"] and length "+p.getBytes().length+" from the server");
							System.out.println("Sending it to the client");
							System.out.println("");
							sendToClient(p);
						}
					}catch(Throwable t){
						//t.printStackTrace();
						throw new RuntimeException(t.getMessage());
					}
				}
			}).start();
			
			
			
			
		}
		catch (Throwable t) {
			//t.printStackTrace();
			throw new RuntimeException(t.getMessage());
		}
	}

	private synchronized void updateServerAddress(InetAddress a, int port){
		serverAdd = a;
		serverPort = port;
	}
	
	private synchronized void updateClientAddress(InetAddress a, int port){
		clientAdd = a;
		clientPort = port;
	}
	
	private synchronized void newClientSocket(){
		try{
			clientSocket.close();
			clientSocket = new DatagramSocket();
		}catch(Throwable t){
			//t.printStackTrace();
			throw new RuntimeException(t.getMessage());
		}
	}
	
	private synchronized void sendToClient(Packet p){
		try{
			clientSocket.send(new DatagramPacket(p.getBytes(), p.getBytes().length, this.clientAdd, this.clientPort));
		}catch(Throwable t){
			//t.printStackTrace();
			throw new RuntimeException(t.getMessage());
		}
	}
	
	private synchronized void sendToServerWellKnownPort(Packet p){
		try{
			serverSocket.send(new DatagramPacket(p.getBytes(), p.getBytes().length, InetAddress.getLocalHost(), 69));
		}catch(Throwable t){
			//t.printStackTrace();
			throw new RuntimeException(t.getMessage());
		}
	}
	
	private synchronized void sendToServer(Packet p){
		try{
			serverSocket.send(new DatagramPacket(p.getBytes(), p.getBytes().length, this.serverAdd, this.serverPort));
		}catch(Throwable t){
			//t.printStackTrace();
			throw new RuntimeException(t.getMessage());
		}
	}
	
	public static void main(String[] args){
		
		System.out.print("Intermediate Host started\n");

		FileReader fr = new FileReader(new File("IHErrorFile.txt"));

		BufferedReader br = new BufferedReader(fr);

		String line = null; //so that the first time through the loop works if the file is empty

		while ((line = br.readLine()) != null){
			StringTokenizer token = new StringTokenizer(line, " ");

		}

		System.out.print("Waiting for packet");
		new IntermediateHost();
	}
}

