
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
	
	protected MistakePacket(byte[] data, int length){
		this.bytes = Arrays.copyOf(data, length);
	}
	
	@Override
	public byte[] getBytes() {
		return bytes.clone();
	}

	@Override
	public PacketType getType() {
		return type;
	}

	//@Override
	public static int getBufferSize() {
		return 1024;//who cares
	}
	
}


