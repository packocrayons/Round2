
package errorSim;


//import java.io.BufferedReader;
//import java.io.File;
import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.StringTokenizer;

import packets.AcknowledgementPacket;
import packets.DataPacket;
//import packets.AcknowledgementPacket;
//import packets.DataPacket;
import packets.Packet;
import packets.PacketFactory;
import packets.PacketType;
import tftp.FileFactory;



/**
 * IntermediateHost is an error simulator for a simple TFTP server
 * based on UDP/IP. The host receives a read or write packet from a client and
 * passes it on to the server. Upon receiving a response, it passes it on to the client.
 * One well known socket (23) is used as well as one for the client, and another one for 
 * the server to send/receive.
 * @author Team 17
 */
public class IntermediateHost{
	
	private static char IHERRORFILECOMMENTCHAR = '#';
	
	private DatagramSocket wellKnownSocket, serverSocket, clientSocket;
	private InetAddress clientAdd, serverAdd; //currently unused but should be implemented at some point, still there so the methods don't break
	private int clientPort, serverPort;
	private boolean clientConnected = false;
	private static final PacketFactory pf = new PacketFactory();
	public IntermediateHost(){
		this(new ArrayList<PacketFX>()); //send it an empty array
	}


//	public void setClientPort(int pnum){
//		clientPort = pnum;
//	}
//	
//	public void setServerPort(int pnum){
//		serverPort = pnum;
//	}
//	
//	public int getClientPort()
//	
	
	//this constructor now takes arguments. The original is still in place
	public IntermediateHost(ArrayList<PacketFX> fx) {
		clientPort = 0;
		serverPort = 0;
		try {
			wellKnownSocket = new DatagramSocket(23);
			serverSocket = new DatagramSocket();
			clientSocket = new DatagramSocket();
			
			
			/* OLD
			//There are three threads so that the host can be listening on all three ports at the same time
			//for D1, the host does not need to care about what it is moving at all.
			
			//Listens on the well known port (23), updates the client's address, and creates new client ports
			 * 
			 */
			
			
			/*This is the parent for all of the portHandlers, to clean up duplicate code
			 * 
			 */
			abstract class HandlerParent implements Runnable{
				protected PacketFX checkEffectPacket(int packetNum, PacketType t){
					PacketFX effect;
					for (int i = 0; i < fx.size(); ++i) {
						effect = fx.get(i); //fetch it once
						if (effect.hasCondition){
							effect.tryMeetCondition(t, packetNum);
						}
						if (effect.getPacketType().equals(t) && fx.get(i).getPacketNumber() == packetNum){ //TODO effect the packet (ideally by calling affectThisPacket
							System.out.println("Affecting this packet " + t.getHumanReadableName() + " : " + packetNum);
							if (!effect.hasCondition) {
								return fx.remove(i);
							} else {
								return effect;
							}
						}
					}
					return null;
				}
				
				protected PacketFX checkEffectPacket(PacketType t){
					PacketFX effect;
					for (int i = 0; i < fx.size(); ++i) {
						effect = fx.get(i); //fetch it once
						if (effect.hasCondition){
							effect.tryMeetCondition(t, 0);
						}
						if (effect.getPacketType().equals(t)){ //TODO effect the packet (ideally by calling affectThisPacket
							System.out.println("Affecting this packet");
							if (!effect.hasCondition) {
								return fx.remove(i);
							} else {
								return effect;
							}
						}
					}
					return null;
				}
				
			}
			
			
			/*NEW
			 * There are three classes, WellKnownPortHandler, ClientPortHandler, and ServerPortHandler. They all extend thread and they all implement SendReceiveInterface so that PacketFX can control them
			 * When they want to send a normal packet, they do so by calling their sendFromSocket function. When they want to affect a packet, they call sendEffectPacket with the arguments to sendFromSocket. Since they all implement an interface, sendEffectPacket knows how to call their sendFromSocket function
			 */
			
			//this one listens on port23 and forwards to port 69
			class WellKnownPortHandler extends HandlerParent implements SendReceiveInterface{
				
				@Override
				public void sendFromSocket(DatagramSocket s, Packet p){
					try{
						s.send(new DatagramPacket(p.getBytes(), p.getBytes().length, InetAddress.getLocalHost(), 69));//destination in the wellknownPortHandler case is always 69
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
//							receiveFromSocket(wellKnownSocket,d); //receive a packet on the well known socket
							wellKnownSocket.receive(d);
							newClientSocket(); //generate new server and client sockets - everything needs to be disconnected  to restart
							newServerSocket();
							updateClientAddress(d.getAddress(), d.getPort());
//							clientSocket.connect(clientAdd, clientPort);
							connectToClientSocket(clientAdd, clientPort);
							Packet p = pf.getPacket(d.getData(), d.getLength());
							System.out.println("\nReceived a packet of type ["+p.getType().getHumanReadableName()+"] and length "+p.getBytes().length+" on the well known port");
							PacketFX effect;
							if (p.getType().equals(PacketType.DATA)){ //if this packet is a data packet
								DataPacket pt = (DataPacket) p;
								effect = checkEffectPacket(pt.getNumber(), PacketType.DATA);
							} else if (p.getType() == PacketType.ACK){ //if this packet is an ack packet
								AcknowledgementPacket pt = (AcknowledgementPacket) p;
								effect = checkEffectPacket(pt.getNumber(), PacketType.DATA);
							} else {//this packet doesn't have a number, if it matches our packet, we effect it
								effect = checkEffectPacket(p.getType());
							}
							
			 				if(effect != null){ //if one of the above if statements picked up an effect, then mess with the packet.
			 					effect.sendEffectPacket(serverSocket, p, this);
			 				} else { //if this particular packet was not affected, send it normally
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
			
			new Thread(new WellKnownPortHandler()).start(); //start this thread
			
			
			//listens on the client port, and sends to the server port
			class ClientPortHandler extends HandlerParent implements SendReceiveInterface{

				@Override
				public void sendFromSocket(DatagramSocket s, Packet p){
					try{
						s.send(new DatagramPacket(p.getBytes(), p.getBytes().length, serverAdd, serverPort)); //destination is serverPort - set by a previous receive 
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
							while(!isClientPortConnected()); //wait until we know what port to listen to
							try{
								receiveFromSocket(clientSocket, d);
								good = true;
							}catch(Throwable t){}
							if(good){
								Packet p = pf.getPacket(d.getData(), d.getLength());
								System.out.println("Received a packet of type ["+p.getType().getHumanReadableName()+"] and length "+p.getBytes().length+" from the client : " + d.getPort());
								PacketFX effect;
								if (p.getType().equals(PacketType.DATA)){ //if this packet is a data packet
									DataPacket pt = (DataPacket) p;
									effect = checkEffectPacket(pt.getNumber(), PacketType.DATA);
								} else if (p.getType() == PacketType.ACK){ //if this packet is an ack packet
									AcknowledgementPacket pt = (AcknowledgementPacket) p;
									effect = checkEffectPacket(pt.getNumber(), PacketType.ACK);
								} else {//this packet doesn't have a number, if it matches our packet, we effect it
									effect = checkEffectPacket(p.getType());
								}
								
				 				if(effect != null){ //if one of the above if statements picked up an effect, then mess with the packet.
				 					effect.sendEffectPacket(serverSocket, p, this);
				 				} else { //if this particular packet was not affected, send it normally
									System.out.println("Sending it to the server transfer port : " + serverPort);
									System.out.println("");
									sendFromSocket(serverSocket, p);
								}
							}
						}
					}catch(Throwable t){
						t.printStackTrace();
						throw new RuntimeException(t);
					}
				}
			
			
			}
			
			new Thread (new ClientPortHandler()).start();
			
			//listens on the server port, updates the server's location, and sends to the client
			class ServerPortHandler extends HandlerParent implements SendReceiveInterface{

				@Override
				public void sendFromSocket(DatagramSocket s, Packet p){
					try{
						s.send(new DatagramPacket(p.getBytes(), p.getBytes().length, clientAdd, clientPort)); //destination is clientPort, set by a previous receive
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
								receiveFromSocket(serverSocket, d);
								good = true;
							} catch (Throwable t){}
							if (good){
								updateServerAddress(d.getAddress(), d.getPort());
								serverSocket.connect(serverAdd, serverPort);
								Packet p = pf.getPacket(d.getData(), d.getLength());
								System.out.println("Received a packet of type ["+p.getType().getHumanReadableName()+"] and length "+p.getBytes().length+" from the server : " + d.getPort());
								PacketFX effect;
								if (p.getType().equals(PacketType.DATA)){ //if this packet is a data packet
									DataPacket pt = (DataPacket) p;
									effect = checkEffectPacket(pt.getNumber(), PacketType.DATA);
								} else if (p.getType() == PacketType.ACK){ //if this packet is an ack packet
									AcknowledgementPacket pt = (AcknowledgementPacket) p;
									effect = checkEffectPacket(pt.getNumber(), PacketType.ACK);
								} else {//this packet doesn't have a number, if it matches our packet, we effect it
									effect = checkEffectPacket(p.getType());
								}
								
				 				if(effect != null){ //if one of the above if statements picked up an effect, then mess with the packet.
				 					effect.sendEffectPacket(clientSocket, p, this);
				 				} else { //if this particular packet was not affected, send it normally
									System.out.println("Sending it to the client port : " + clientPort);
									System.out.println("");
									sendFromSocket(clientSocket, p);
								}
							}
						}
					}catch(Throwable t){
						//t.printStackTrace();
						throw new RuntimeException(t.getMessage());
					}
				}
			}
			
			new Thread(new ServerPortHandler()).start();
			
			
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
	
	private synchronized void newServerSocket(){		
		try{
			serverSocket.close();
			serverSocket = new DatagramSocket();
		}catch(Throwable t){
			//t.printStackTrace();
			throw new RuntimeException(t.getMessage());
		}
	}
	
	private synchronized void connectToClientSocket(InetAddress clientAdd, int clientPort) {
		clientSocket.connect(clientAdd, clientPort);
		clientConnected = true; //required because we can't let the clientListener start listening to a port until we know what port. 
	}
	
	private synchronized boolean isClientPortConnected(){
		return clientConnected;
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
	
	public static FXCondition parseFXCondition(String packet, int num){
		
		if (packet.equalsIgnoreCase("ack")){
			return new FXCondition(PacketType.ACK, num);
		} else if (packet.equalsIgnoreCase("data")){
			return new FXCondition(PacketType.DATA, num);
		} else if (packet.equalsIgnoreCase("readrequest")){ //these are separate
			return new FXCondition(PacketType.RRQ);
		} else if (packet.equalsIgnoreCase("writerequest")){
			return new FXCondition(PacketType.WRQ);
		} else if (packet.equalsIgnoreCase("error")){
			return new FXCondition(PacketType.ERR);
		} else {
			return new FXCondition(PacketType.MISTAKE);
		}
	}
	
	public static ArrayList<PacketFX> parsePacketFX(){
		FileFactory ff;
		ff = new FileFactory(".\\intermediateHost");
		
		ArrayList<PacketFX> FX = new ArrayList<PacketFX>(); //this is an array list, support lots of effects
		
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
				while (IHErrorTokenizer.hasMoreTokens()){ //read each line - this is context free
					line = IHErrorTokenizer.nextToken().replaceAll("[\\s]+", " ").trim();
//					System.out.println("line read: " + line); //DEBUG
					if (!line.isEmpty() && !(line.charAt(0) == IHERRORFILECOMMENTCHAR)){ //skip this line if it begins with a #
						StringTokenizer token = new StringTokenizer(line, " "); //split the string by spaces
						PacketType packetType;				//setup for the constructor
						EffectType effectType;
						Object[] effectArgs = new Object[4];//support up to 4 arguments
						String type = token.nextToken(); //what to do to the packet
						String pType = token.nextToken(); //what type of packet to affect
						String packetNum = token.nextToken(); //which packet to drop
						String s = null;
						int i;
						for (i = 0; token.hasMoreTokens(); ++i){
							s = token.nextToken();
							if (s.equalsIgnoreCase("cond")) break; //there's a condition
							if (s.matches("^-?\\d+$")){
								System.out.println("matches int");
								effectArgs[i] = new Integer(s.replaceAll("", ""));
							} else {
								effectArgs[i] = s; //deal with this on the argument side
							}
							s = null;
						}
						
						if (s != null){ //if it's supposed to be a condition
							s = token.nextToken();
							if (token.hasMoreTokens()){
								String x = token.nextToken();
//								System.out.println(Integer.parseInt(x));
								effectArgs[i] = parseFXCondition(s, Integer.parseInt(x));
							} else { 
								effectArgs[i] = parseFXCondition(s, 0); //assume that they got the language right, this condition doesn't have a number, so it doesn't matter
							}
						}
						
						int packetNumber = Integer.parseInt(packetNum);
						
						if (type.equalsIgnoreCase("drop")){
							effectType = EffectType.DROP;
						}else if (type.equalsIgnoreCase("delay")){
							effectType = EffectType.DELAY;
						}else if (type.equalsIgnoreCase("duplicate")){
							effectType = EffectType.DUPLICATE;
						} else if (type.equalsIgnoreCase("opcode")){
							effectType = EffectType.OPCODE;
						} else if (type.equalsIgnoreCase("mode")){
							effectType = EffectType.MODE;
						} else if (type.equalsIgnoreCase("port")){
							effectType = EffectType.PORT;
						} else if (type.equalsIgnoreCase("size")){
							effectType = EffectType.SIZE;
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
							packetType = PacketType.MISTAKE;
						}
						
						System.out.println("New FX generated : packetNumber = " + packetNumber + " packetType = " + packetType.getHumanReadableName() + " effectArgs = " + Arrays.toString(effectArgs));
						FX.add(new PacketFX(packetNumber, packetType, effectType, effectArgs));
					}//else System.out.println("Comment ignored");//if not a commentChar
				} //while loop
			}//leftover scoping bracket
		}//if IHErrorString != null
		sc.close();
		return FX;
	}
	
	public static void main(String[] args){		
		System.out.print("Intermediate Host started\n");

		System.out.print("Waiting for packet");
		new IntermediateHost(parsePacketFX());
	}
}

