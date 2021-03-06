package packets;

import java.util.Arrays;

/**
 * A class that handles everything required for a Data packet 
 * @author Team 17
 */
public class DataPacket extends Packet {
	private final PacketType type = PacketType.DATA;
	private final byte[] bytes;
	private final int number;
	private final boolean isLast;
	private final byte[] filePart;
	
	public DataPacket(int number, byte[] data, int dataLength){
		this(number, Arrays.copyOfRange(data, 0, dataLength));
	}
	
	public DataPacket(int number, byte[] data){
		if(data.length>512){
			throw new IllegalArgumentException("DATA Packet too long");
		}
		this.filePart = data.clone();
		this.isLast = data.length<512;
		this.number = number & 0xffff;
		this.bytes = new GenericPacket()
				.cat(type.getOpcode()>>8)
				.cat(type.getOpcode()) 
				.cat(number>>8)
				.cat(number)
				.cat(data).getBytes();
	}
	
	protected DataPacket(byte[] data, int length){
		data = Arrays.copyOf(data, length);
		this.bytes = data;
		
		if(length < 4){
			throw new IllegalArgumentException("DATA Packet too short");
		}
		
		if(data[0] != (byte)0 || data[1] != this.getType().getOpcode()){
			throw new IllegalArgumentException("Wrong header for DATA packet opcode received "+data[0]+data[1]);
		}
		this.number = (Byte.toUnsignedInt(data[2]) << 8)+(Byte.toUnsignedInt(data[3]));
		this.filePart = Arrays.copyOfRange(data, 4, data.length);
		this.isLast = this.filePart.length<512;//512 because it is the size of a block in the speck
	}
	
	public boolean comesAfter(int lastNumber){
		return ((this.number - lastNumber) & 0xffff) == 1;
	}
	
	public int getNumber(){
		return number;
	}
	
	@Override
	public byte[] getBytes() {
		return bytes.clone();
	}

	@Override
	public PacketType getType() {
		return type;
	}

	public boolean isLast() {
		return isLast;
	}

	public byte[] getFilePart() {
		return filePart;
	}

	//@Override
	public static int getBufferSize() {
		return 2+2+512;
	}
}
