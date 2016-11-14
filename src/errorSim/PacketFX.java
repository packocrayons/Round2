package errorSim;

import packets.Packet;

import java.net.DatagramSocket;

import packets.PacketType;

import tftp.EffectType;
import tftp.SendReceiveInterface;

//* @author Team 17
public class PacketFX {
	//these are just offsets into the effectArgs[] array
	private static final int PACKETDELAYTIMEINDEX = 0;
	
	private static final int PACKETDUPLICATEQUANTITYINDEX = 0;
	private static final int PACKETDUPLICATETIMEBETWEENINDEX = 1;
	
	private int packetNumber; //which packet number to affect
	private PacketType packetType; //which type of packet to affect
	private EffectType effect; //REFACTOR this will probably be an ENUM later
	private Object effectArgs[];
	protected boolean hasCondition;
	private boolean enabled = true;

	public PacketFX(int packetNumber, PacketType packetType, EffectType effect, Object[] effectArgs){
		this.packetNumber = packetNumber;
		this.packetType = packetType;
		this.effect = effect;
		this.effectArgs = effectArgs;
		for (int i = 0; i < effectArgs.length; ++i) if (effectArgs[i] instanceof FXCondition) hasCondition = true;
	}
	
	public void tryMeetCondition(PacketType p, int num){
		for(int i = 0; i < effectArgs.length; ++i){
			if (effectArgs[i] instanceof FXCondition) ((FXCondition)effectArgs[i]).tryMeetCondition(p, num);
		}
	}
	
	//assumes effectThisPacket has already validated this packet.
	//In theory this could be called with a lambda, it would make it more versatile. perhaps in a later refactor but for now this works.
	public void sendEffectPacket(DatagramSocket s, Packet p, SendReceiveInterface i){ //affect the packet based on effect, then send it (if it's not dropped)
		if (!enabled) {
			i.sendFromSocket(s, p); //send the packet along as normal
			return; //this is here so that the effect can hang around to have the condition met
		}
		enabled = false; //we've passed the "enabled barrier", we're the only one that's allowed to use the effect.
		switch(effect){ //REFACTOR fix this once effect is an
		case DROP: //drop packet
			//do nothing, the packet disappears
			break;
		case DELAY: //delay packet
			new Thread(new Runnable(){ //start a new thread to delay the packet, this is required because we have to do other things while we delay the packet
				@Override
				public void run(){
					try {
						if (effectArgs[PACKETDELAYTIMEINDEX] instanceof Integer){
							Thread.sleep((Integer)effectArgs[PACKETDELAYTIMEINDEX]);
						} else {
							sleepUntilNotified(effectArgs[PACKETDELAYTIMEINDEX]);
						}
							
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("Sending delayed packet");
					i.sendFromSocket(s, p);
				}
			}).start();
			break;
		case DUPLICATE: //duplicate packet, start a new thread for this - see delay for explanation
			new Thread(new Runnable(){
				@Override
				public void run(){
					for (int x = 0; x < (Integer)effectArgs[PACKETDUPLICATEQUANTITYINDEX]; ++x){
						System.out.println("Sending duplicate packet " + x);
						i.sendFromSocket(s, p);
						try {
							if (effectArgs[PACKETDUPLICATETIMEBETWEENINDEX] instanceof Integer){
								Thread.sleep((Integer)effectArgs[PACKETDUPLICATETIMEBETWEENINDEX]);
							} else {
								sleepUntilNotified(effectArgs[PACKETDUPLICATETIMEBETWEENINDEX]);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			).start();
			break;
		case NOTHING:
			i.sendFromSocket(s, p); //don't do anything to the packet
			break;
		default :
			i.sendFromSocket(s, p); //something went wrong, forward the packet as usual
		}
	}

	//originally there was a compare method here but it can't be used since not all packets have a packetNumber
	
	public PacketType getPacketType(){
		return packetType;
	}
	
	public int getPacketNumber(){
		return packetNumber;
	}
	
	public void sleepUntilNotified(Object condition){
		FXCondition c = (FXCondition)condition; //assume that it's valid
		synchronized(c){
			System.out.println("Blocking, waiting for condition");
			while(!c.isMet()){ //wait until you're notified AND your condition is met.
				try {
					c.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //wait until the 
			}
		}
		System.out.println("Condition was met, freed from block");
		hasCondition = false; //remove this condition - this will free up processor time
		return;
	}
}