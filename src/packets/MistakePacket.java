
package packets;

import java.util.Arrays;

/**
 * A class that handles everything required for an I don't care packet 
  * @author Team 17
 * An I don't care packet is a packet that only exists when packet creation (in the packet factory) failed.
 */
public class MistakePacket extends Packet {
	private final PacketType type = PacketType.MISTAKE;
	private final byte[] bytes;
	private final String message;
	
	public MistakePacket(){
		bytes = new byte[0];
		message = "";
	}
	
	private MistakePacket(byte[] data){
		bytes = data.clone();
		message = "";
	}
	
	protected MistakePacket(byte[] data, int length,String message){
		this.bytes = Arrays.copyOf(data, length);
		this.message=message;
	}
	
	public String getMessage(){
		return message;
	}
	@Override
	public byte[] getBytes() {
		return bytes.clone();
	}

	@Override
	public PacketType getType() {
		return type;
	}
	
	public MistakePacket cat(byte b){
		return this.cat(new byte[]{b});
	}
	
	public MistakePacket cat(String s){
		return this.cat(s.getBytes());
	}
	
	public MistakePacket cat(byte[] b){
		byte[] newData = new byte[bytes.length+b.length];
		System.arraycopy(bytes, 0, newData, 0, bytes.length);
		System.arraycopy(b, 0, newData, bytes.length, b.length);
		return new MistakePacket(newData);
	}
	
	
	//@Override
	public static int getBufferSize() {
		return 1024;//who cares
	}
	
}


