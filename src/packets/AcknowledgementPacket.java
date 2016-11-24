
package packets;

import java.util.Arrays;

/**
 * A class that handles everything required for an acknowledgement packet 
 * @author Team 17
 */
public class AcknowledgementPacket extends Packet {
	private final PacketType type = PacketType.ACK;
	private final byte[] bytes;
	private final int number;
	
	public AcknowledgementPacket(int number){
		this.number = number & 0xffff;
		this.bytes = new GenericPacket(
				(byte)0, 
				type.getOpcode(), 
				(byte)(number/(1<<8)), 
				(byte)(number/(1<<8))).getBytes();
	}
	
	protected AcknowledgementPacket(byte[] data, int length){
		if(length != 4){
			throw new IllegalArgumentException("Wrong length for ACK Packet");
		}
		
		this.bytes = Arrays.copyOf(data, 4);
		if(data[0] != (byte)0 || data[1] != (byte)1){
			throw new IllegalArgumentException("Wrong header for ACK packet opcode received "+data[0]+""+data[1]);
		}
		this.number = (Byte.toUnsignedInt(data[2]) << 8)+(Byte.toUnsignedInt(data[3]));
	}
		
	public int getNumber(){
		return number;
	}
	
	public boolean acknowledges(int dataBlockNumber){
		return (dataBlockNumber & 0xffff) == (number & 0xffff);
	}
	public boolean inferiorTo(int dataBlockNumber){
		return (dataBlockNumber & 0xffff) > (number & 0xffff);
	}
	
	//@Override
	public byte[] getBytes() {
		return bytes.clone();
	}

	//@Override
	public PacketType getType() {
		return type;
	}

	//@Override
	public static int getBufferSize() {
		return 4;
	}

}

