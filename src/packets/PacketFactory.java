package packets;

import java.net.DatagramPacket;
/**
 * Used to differentiate among the various types of packets possible
 * @author Team 15
 */
public class PacketFactory {
	public PacketFactory(){
		
	}
	
	/**
	 * Will successfully parse the data, but it will be an IDontCarePacket if it cannot be any other type
	 * Generate a packet of type based on the opcode in the data array (data[1])
	 * @param data 
	 * @param length
	 * @return Packet
	 */
	public Packet getPacket(byte[] data, int length){
		try{
			if(data[1] == PacketType.RRQ.getOpcode()){ 
				return new ReadRequestPacket(data, length);
			}else if(data[1] == PacketType.WRQ.getOpcode()){
				return new WriteRequestPacket(data, length);
			}else if(data[1] == PacketType.ACK.getOpcode()){
				return new AcknowledgementPacket(data, length);
			}else if(data[1] == PacketType.ERR.getOpcode()){
				return new ErrorPacket(data, length);
			}else if(data[1] == PacketType.DATA.getOpcode()){
				return new DataPacket(data, length);
			}else{
				return new IDontCarePacket(data, length);
			}
		}catch(IllegalArgumentException e){
			return new IDontCarePacket(data, length);
		}
	}

	public Packet getPacket(DatagramPacket p){ //turn a DatagramPacket into a Packet
		return getPacket(p.getData(), p.getLength());
	}

}
