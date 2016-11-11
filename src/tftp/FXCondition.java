package tftp;

import packets.AcknowledgementPacket;
import packets.DataPacket;
import packets.Packet;
import packets.PacketType;


public class FXCondition {
	private boolean conditionMet;
	private PacketType conditionPacketType;
	private int conditionPacketNumber;
	
	public FXCondition(PacketType cPacket, int num){
		conditionPacketType = cPacket;
		conditionMet = false;
		conditionPacketNumber = num;
	}
	
	public FXCondition(PacketType cPacket){
		this(cPacket, 0);
	}
	
	private void meetCondition(){
		conditionMet = true;
		System.out.println("Condition met");
		synchronized(this){
			this.notifyAll();
		}
	}
	
	public void tryMeetCondition(PacketType p, int num){
		if (p == conditionPacketType && num == conditionPacketNumber){
			meetCondition();
		}
	}
	
	public boolean isMet(){
		return conditionMet;
	}
	
	public String toString(){
		return ("Condition valid on " + conditionPacketType + " " + conditionPacketNumber);
	}
}

