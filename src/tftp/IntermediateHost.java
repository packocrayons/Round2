
package tftp;

//import java.io.BufferedReader;
//import java.io.File;
import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;
import java.util.StringTokenizer;

//import packets.AcknowledgementPacket;
//import packets.DataPacket;
import packets.Packet;
import packets.PacketFactory;
import packets.PacketType;



/**
 * IntermediateHost is an error simulator for a simple TFTP server
 * based on UDP/IP. The host receives a read or write packet from a client and
 * passes it on to the server. Upon receiving a response, it passes it on to the client.
 * One well known socket (23) is used as well as one for the client, and another one for 
 * the server to send/receive.
 * @author Team 15
 */
public class IntermediateHost{
	
	private static char IHERRORFILECOMMENTCHAR = '#';
	
	private DatagramSocket wellKnownSocket, serverSocket, clientSocket;
	private InetAddress clientAdd, serverAdd; //currently unused but should be implemented at some point
	private int clientPort, serverPort; //currently unused but should be implemented at some point
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
			
			
			/*DEPRECATED
			//There are three threads so that the host can be listening on all three ports at the same time
			//for D1, the host does not need to care about what it is moving at all.
			
			//Listens on the well known port (23), updates the client's address, and creates new client ports
			 * 
			 */
			
			
			/*NEW
			 * There are three classes, WellKnownPortHandler, ClientPortHandler, and ServerPortHandler. They all extend thread and they all implement SendReceiveInterface so that PacketFX can control them
			 * When they want to send a normal packet, they do so by calling their sendFromSocket function. When they want to affect a packet, they call sendEffectPacket with the arguments to sendFromSocket. Since they all implement an interface, sendEffectPacket knows how to call their sendFromSocket function
			 */
			
			//this one listens on port23 and forwards to port 69
			class WellKnownPortHandler extends Thread implements SendReceiveInterface{
				
				@Override
				public void sendFromSocket(DatagramSocket s, Packet p){
					try{
						s.send(new DatagramPacket(p.getBytes(), p.getBytes().length, InetAddress.getLocalHost(), 69)); //destination in the WellKnownPortHandler case is always 69
					}catch(Throwable t){
						//t.printStackTrace();
						throw new RuntimeException(t.getMessage());
					}
				}
				
				@Override
				public void receiveFromSocket(DatagramSocket s, DatagramPacket d) throws IOException{
					return;
				}
				
				@Override
				public void run() {
					try{
						DatagramPacket d = new DatagramPacket(new byte[Packet.getBufferSize()],Packet.getBufferSize());
						while(true){
							wellKnownSocket.receive(d); //receive a packet on the well known socket
							newClientSocket();
							updateClientAddress(d.getAddress(), d.getPort());
							Packet p = pf.getPacket(d.getData(), d.getLength());
							System.out.println("\nReceived a packet of type ["+p.getType().getHumanReadableName()+"] and length "+p.getBytes().length+" on the well known port");
							System.out.println("Testing if this packet should be affected");
							boolean packetAffectedFlag = false;
							for (int i = 0; i < effects.length; ++i) {
								if (effects[i].getPacketType() == p.getType() && effects[i].getPacketNumber() == p.getNumber()){ //TODO effect the packet (ideally by calling affectThisPacket
									System.out.println("Affecting this packet");
			 						effects[i].sendEffectPacket(serverSocket, p, this);
			 						packetAffectedFlag = true;
								}
							}
							if(!packetAffectedFlag){ //if this particular packet was not affected, send it normally
								System.out.println("Sending it to the server's well known port");
								System.out.println("");
								sendFromSocket(serverSocket, p);
							}
						}
					}catch(Throwable t){
						t.printStackTrace();
						throw new RuntimeException(t.getMessage());
					}
				}

			}
			
			new WellKnownPortHandler().start(); //start this thread
			
			
			//listens on the client port, and sends to the server port
			class ClientPortHandler extends Thread implements SendReceiveInterface{

				@Override
				public void sendFromSocket(DatagramSocket s, Packet p){
					try{
						s.send(new DatagramPacket(p.getBytes(), p.getBytes().length, InetAddress.getLocalHost(), 69)); //destination in the WellKnownPortHandler case is always 69
					}catch(Throwable t){
						//t.printStackTrace();
						throw new RuntimeException(t.getMessage());
					}
				}
				
				@Override
				public void receiveFromSocket(DatagramSocket s, DatagramPacket d) throws IOException{
					s.receive(d);
				}
				
				@Override
				public void run() {
					try{
						DatagramPacket d = new DatagramPacket(new byte[Packet.getBufferSize()],Packet.getBufferSize());
						while(true){
							boolean good = false;
							try{
								receiveFromSocket(clientSocket, d);
								good = true;
							}catch(Throwable t){}
							if(good){
								Packet p = pf.getPacket(d.getData(), d.getLength());
								System.out.println("Received a packet of type ["+p.getType().getHumanReadableName()+"] and length "+p.getBytes().length+" from the client");
								System.out.println("Testing if this packet should be affected");
								boolean packetAffectedFlag = false;
								for (int i = 0; i < effects.length; ++i) {
									if (effects[i].getPacketType() == p.getType() && effects[i].getPacketNumber() == p.getNumber()){
										System.out.println("Affecting this packet");
				 						effects[i].sendEffectPacket(serverSocket, p, this);
				 						packetAffectedFlag = true;
									}
								}
								if(!packetAffectedFlag){ //if this particular packet was not affected, send it normally
									System.out.println("Sending it to the server port");
									System.out.println("");
									sendFromSocket(serverSocket, p); //send it  normally - but use the newly implemented interface
								}
							}
						}
					}catch(Throwable t){
						t.printStackTrace();
						throw new RuntimeException(t);
					}
				}
			
			
			}
			
			new ClientPortHandler().start();
			
			//listens on the server port, updates the server's location, and sends to the client
			class ServerPortHandler extends Thread implements SendReceiveInterface{

				@Override
				public void sendFromSocket(DatagramSocket s, Packet p){
					try{
						s.send(new DatagramPacket(p.getBytes(), p.getBytes().length, InetAddress.getLocalHost(), 69)); //destination in the WellKnownPortHandler case is always 69
					}catch(Throwable t){
						//t.printStackTrace();
						throw new RuntimeException(t.getMessage());
					}
				}
				
				@Override
				public void receiveFromSocket(DatagramSocket s, DatagramPacket d) throws IOException{
					s.receive(d);
				}
				
				@Override
				public void run() {
					try{
						DatagramPacket d = new DatagramPacket(new byte[Packet.getBufferSize()],Packet.getBufferSize());
						while(true){
							serverSocket.receive(d);
							updateServerAddress(d.getAddress(), d.getPort());
							Packet p = pf.getPacket(d.getData(), d.getLength());
							System.out.println("Received a packet of type ["+p.getType().getHumanReadableName()+"] and length "+p.getBytes().length+" from the server");
							boolean packetAffectedFlag = false;
							for (int i = 0; i < effects.length; ++i) {
								if (effects[i].getPacketType() == p.getType() && effects[i].getPacketNumber() == p.getNumber()){
									System.out.println("Affecting this packet");
			 						effects[i].sendEffectPacket(clientSocket, p, this);
			 						packetAffectedFlag = true;
								}
							}
							if(!packetAffectedFlag){ //if this particular packet was not affected, send it normally
								System.out.println("Sending it to the server port");
								System.out.println("");
								sendFromSocket(clientSocket, p); //send it  normally - but use the newly implemented interface
							}
						}
					}catch(Throwable t){
						//t.printStackTrace();
						throw new RuntimeException(t.getMessage());
					}
				}
			}
			
			new ServerPortHandler().start();
			
			
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
	
	/*private synchronized void sendToClient(Packet p){
		try{
			clientSocket.send(new DatagramPacket(p.getBytes(), p.getBytes().length, this.clientAdd, this.clientPort));
		}catch(Throwable t){
			//t.printStackTrace();
			throw new RuntimeException(t.getMessage());
		}
	}*/ //DEPRECATED
	
	/*private synchronized void sendToServerWellKnownPort(Packet p){
		try{
			serverSocket.send(new DatagramPacket(p.getBytes(), p.getBytes().length, InetAddress.getLocalHost(), 69));
		}catch(Throwable t){
			//t.printStackTrace();
			throw new RuntimeException(t.getMessage());
		}
	}*/ //DEPRECATED
	
	/*private synchronized void sendToServer(Packet p){
		try{
			serverSocket.send(new DatagramPacket(p.getBytes(), p.getBytes().length, this.serverAdd, this.serverPort));
		}catch(Throwable t){
			//t.printStackTrace();
			throw new RuntimeException(t.getMessage());
		}
	}*/ //DEPRECATED
	
	public static PacketFX[] generatePacketFX(){
		FileFactory ff;
		ff = new FileFactory(".");
		
		
		PacketFX FX[] = new PacketFX[10]; //currently support is for up to 10 effects
		
		Scanner sc;
		try {
			sc = new Scanner(ff.readFile("IHErrorFile.txt"));
			sc.useDelimiter("\\A");
		} catch (IllegalAccessException | FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			sc = null;
		}
		String IHErrorString = sc.hasNext() ? sc.next() : null;
		//System.out.println(IHErrorString != null ? IHErrorString : "Nothing in the file"); //DEBUG
		StringTokenizer IHErrorTokenizer = new StringTokenizer(IHErrorString, "\n");

		if (IHErrorString != null){ //if there was something in the file
			String line = null; //so that the first time through the loop works if the file is empty
			System.out.println("An IHErrorFile.txt was found, parsing");
			{ //leftover scoping bracket
				int FXindex = 0; //keeping track of where we insert the next effect, needed because not every line of the file is an effect
				while (IHErrorTokenizer.hasMoreTokens()){ //read each line - this is context free
					line = IHErrorTokenizer.nextToken();
					System.out.println("line read: " + line); //DEBUG
					if (!(line.charAt(0) == IHERRORFILECOMMENTCHAR)){ //skip this line if it begins with a #
						StringTokenizer token = new StringTokenizer(line, " "); //split the string by spaces
						PacketType packetType;				//setup for the constructor
						EffectType effectType;
						int[] effectArgs = new int[4];//support up to 4 arguments
						String type = token.nextToken(); //what to do to the packet
						String pType = token.nextToken(); //what type of packet to affect
						String packetNum = token.nextToken(); //which packet to drop
						String s;
						for (int i = 0; token.hasMoreTokens(); ++i){
							s = token.nextToken();
							effectArgs[i] = Integer.parseInt(s);
						}
						
						int packetNumber = Integer.parseInt(packetNum);
						
						if (type.equalsIgnoreCase("drop")){
							effectType = EffectType.DROP;
						}else if (type.equalsIgnoreCase("delay")){
							effectType = EffectType.DELAY;
						}else if (type.equalsIgnoreCase("duplicate")){
							effectType = EffectType.DUPLICATE;
						} else{
							effectType = EffectType.NOTHING;
						}
						
						if (pType.equalsIgnoreCase("ack")){
							packetType = PacketType.ACK;
						} else if (pType.equalsIgnoreCase("data")){
							packetType = PacketType.DATA;
						} else if (pType.equalsIgnoreCase("readrequest")){ //these are separate
							packetType = PacketType.RRQ;
						} else if (pType.equalsIgnoreCase("writerequest")){
							packetType = PacketType.WRQ;
						} else if (pType.equalsIgnoreCase("error")){
							packetType = PacketType.ERR;
						} else {
							packetType = PacketType.IDC;
						}
						
						System.out.println("New FX generated : packetNumber = " + packetNumber + " packetType = " + packetType.getHumanReadableName() + " effectArgs = " + Arrays.toString(effectArgs));
						FX[FXindex++] = new PacketFX(packetNumber, packetType, effectType, effectArgs);
					}else System.out.println("Comment ignored");//if not a commentChar
				} //while loop
			}//leftover scoping bracket
		}//if IHErrorString != null
		sc.close();
		return FX;
	}
	
	public static void main(String[] args){		
		System.out.print("Intermediate Host started\n");

		System.out.print("Waiting for packet");
		new IntermediateHost(generatePacketFX());
	}
}

