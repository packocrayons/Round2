package errorSim;

import packets.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

//* @author Team 17
public class PacketFX {
	//these are just offsets into the effectArgs[] array
	private static final int PACKETDELAYTIMEINDEX = 0;
	
	private static final int PACKETDUPLICATEQUANTITYINDEX = 0;
	private static final int PACKETDUPLICATETIMEBETWEENINDEX = 1;
	
	private static final int CORRUPTOPCODENEWCODE1INDEX = 0;
	private static final int CORRUPTOPCODENEWCODE2INDEX = 1;
	
	private static final char OPCODECORRUPTIONCHAR = 'l';
	
	private static final int SIZENEWSIZEINDEX = 0;
	private static final int SIZEIFNULLSINDEX = 1;
	
	private int packetNumber; //which packet number to affect
	private PacketType packetType; //which type of packet to affect
	private EffectType effect; //REFACTOR this will probably be an ENUM later
	private Object effectArgs[];
	protected boolean hasCondition;
	private boolean enabled = true;
	private PacketFactory pf;

	public PacketFX(int packetNumber, PacketType packetType, EffectType effect, Object[] effectArgs){
		this.packetNumber = packetNumber;
		this.packetType = packetType;
		this.effect = effect;
		this.effectArgs = effectArgs;
		for (int i = 0; i < effectArgs.length; ++i) if (effectArgs[i] instanceof FXCondition) hasCondition = true;
		pf = new PacketFactory();
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
			System.out.println("Effect disabled, packet sent along as normal");
			i.sendFromSocket(s, p); //send the packet along as normal
			return; //this is here so that the effect can hang around to have the condition met
		}
		enabled = false; //we've passed the "enabled barrier", we're the only one that's allowed to use the effect.
		switch(effect){
		
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
			
		case OPCODE:
			
			byte[] datacontents = p.getBytes();
			datacontents[0] = (byte)(((Integer)effectArgs[CORRUPTOPCODENEWCODE1INDEX]).intValue());
			datacontents[1] = (byte)(((Integer)effectArgs[CORRUPTOPCODENEWCODE2INDEX]).intValue());
			Packet newPacket = pf.getPacket(datacontents, datacontents.length); //this will probably return a mistake packet, that's fine because we can still send those
			i.sendFromSocket(s, newPacket);
			
			break;
			
		case MODE: //this is only applicable to readRequest and writeRequest packets, otherwise we just send it on
			
			if(p instanceof ReadRequestPacket || p instanceof WriteRequestPacket){
				byte[] bytes = p.getBytes();
				System.out.println("Old byte array: " + Arrays.toString(bytes));
				int opcodeFinder;
				for(opcodeFinder = 1; opcodeFinder < bytes.length; ++opcodeFinder){ //skip the first char - it's a null and we don't want to pick it up
					if (bytes[opcodeFinder] == 0) break;
				}
				System.out.println("changing char at index " + opcodeFinder);
				bytes[opcodeFinder + 1] = OPCODECORRUPTIONCHAR;
				Packet np = new GenericPacket(bytes);
				System.out.println("new byte array: " + Arrays.toString(bytes));
				i.sendFromSocket(s, np);
				break;
			} else {
				i.sendFromSocket(s, p);break;
			}
			
		case PORT:
			
			new Thread(new Runnable(){
			
				public void run(){
					DatagramSocket newSocket = null;
					try {
						newSocket = new DatagramSocket();
					} catch (SocketException e) {
						e.printStackTrace();
					}
					i.sendFromSocket(newSocket, p);
					System.out.println("Packet sent from port " + newSocket.getLocalPort());
					DatagramPacket dp = new DatagramPacket(new byte[512], 512);
					try {
						newSocket.receive(dp);
					} catch (IOException e) {
						e.printStackTrace();
					}
					Packet o = new PacketFactory().getPacket(dp);
					System.out.println("Packet received from server:");
					System.out.println(o.getType());
					if (o instanceof ErrorPacket){
						ErrorPacket p=(ErrorPacket) o;
						System.out.println("Packet type:"+ p.getType());
		            	System.out.println("Error type:"+p.getErrorType());
		            	System.out.println("Error message:"+p.getMessage());
				    } else { 
				    	MistakePacket mp = (MistakePacket) o;
				    	System.out.println(mp.getMessage());
				    }
					return;
				}
			}).start();
			break;
			
		case SIZE:
			
			boolean fillWithNulls = (effectArgs[SIZEIFNULLSINDEX] != null);
			if (effectArgs[SIZENEWSIZEINDEX] instanceof Integer){
				Integer newLength = (Integer)effectArgs[0];
				GenericPacket np = new GenericPacket(p.getBytes());
				byte[] npByteArray = np.getBytes(); //prefetch the array
				if (newLength < 0 ){
					System.out.println("shrinking array");
					Packet packetToSend = new GenericPacket(Arrays.copyOf(npByteArray, npByteArray.length + newLength)); //pull off newLength from the array - remember newLength is negative
					System.out.println("new array length : " + packetToSend.getBytes().length);
					i.sendFromSocket(s, packetToSend); 
				} else if (newLength > 0){
					GenericPacket pToSend;
					if (fillWithNulls){
						System.out.println("Adding nulls to array");
						byte[] nullBytes = new byte[newLength];
						for (int x = 0; x < newLength; ++x) nullBytes[x] = 0;
						pToSend = np.cat(nullBytes);
					} else{
						System.out.println("lengthening array");
						pToSend = np.cat(Arrays.copyOf(np.getBytes(), newLength)); //add garbage from the front of the array to the end of the array
					}
					i.sendFromSocket(s, pToSend);
					System.out.println("new array length : " + pToSend.getBytes().length);
				}
			} else System.out.println("Arg is not an Integer");
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
				}
			}
		}
		System.out.println("Condition was met, freed from block");
		hasCondition = false; //remove this condition - this will free up processor time
		return;
	}
}