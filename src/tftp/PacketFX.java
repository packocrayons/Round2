package tftp;

import packets.Packet;

import packets.AcknowledgementPacket;
import packets.ErrorPacket;
import packets.ErrorType;
import packets.PacketFactory;
import packets.PacketType;
import packets.ReadRequestPacket;
import packets.WriteRequestPacket;

public class PacketFX {
	private int affectedThread; //REFACTOR MAKE AN ENUM LATER - number is thread number that it affects 1 is the request port, 2 is the thread that listens on the client port and forwards to server, 3 is server to client
	private int packetNumber; //which packet number to affect
	private PacketType packetType; //which type of packet to affect
	//private ?? effect; //this will probably be an ENUM

	public PacketFX(int affectedThread, int packetNumber, PacketType packetType){
		this.affectedThread = affectedThread;
		this.packetNumber = packetNumber;
		this.packetType = packetType;
		//TODO get the type of effect
	}

	public int getAffectedThread(){
		return affectedThread;
	}

	//assumes effectThisPacket has already validated this packet.
	public void sendEffectPacket(Packet p){ //affect the packet based on effect, then send it (if it's not dropped)

	}

	public boolean affectThisPacket(Packet p){
		return (p.getNumber() == packetNumber && p.getType().equals(packetType));
	}

}