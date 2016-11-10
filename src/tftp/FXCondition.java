package tftp;

import packets.Packet;

public class FXCondition {
	private boolean conditionMet;
	private Packet conditionPacket;
	
	public FXCondition(Packet cPacket){
		conditionPacket = cPacket;
		conditionMet = false;
	}
	
	public void tryMeetCondition(Packet p){
		
	}
	
	public boolean isMet(){
		return conditionMet;
	}
}
