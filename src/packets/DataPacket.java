
package packets;

import java.util.Arrays;

/**
 * A class that handles everything required for a Data packet 
 * @author Team 15
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
			throw new IllegalArgumentException("This data block is too big");
		}
		this.filePart = data.clone();
		this.isLast = data.length<512;
		while(number<0)number+=(1<<16);
		while(number>=1<<16)number-=(1<<16);
		this.number = number;
		byte[] temp = new byte[4+data.length];
		temp[0] = (byte)0;
		temp[1] = type.getOpcode();
		temp[2] = (byte)(number/(1<<8));
		temp[3] = (byte)(number%(1<<8));
		System.arraycopy( data, 0, temp, 4, data.length);
		this.bytes = temp;
	}
	
	protected DataPacket(byte[] data, int length){
		data = Arrays.copyOf(data, length);
		this.bytes = data;
		
		if(length < 4){
			throw new IllegalArgumentException("This byte array is not 4 bytes long");
		}
		
		if(data[0] != (byte)0 || data[1] != this.getType().getOpcode()){
			throw new IllegalArgumentException("The header on this packet is not formed correctly, it does not represent an ACK");
		}
		this.number = (Byte.toUnsignedInt(data[2]) << 8)+(Byte.toUnsignedInt(data[3]));
		this.filePart = Arrays.copyOfRange(data, 4, data.length);
		this.isLast = this.filePart.length<512;//512 because it is the size of a block in the speck
	}
	
	public boolean comesAfter(int lastNumber){
		return (this.number-lastNumber==1)||(this.number == 0 && lastNumber == (1<<16)-1);
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
