package packets;

/**
 * Abstract class for Packets containing the main variables needed in a Packet
 * @author Team 15
 */
public abstract class Packet {
	public abstract byte[] getBytes();
	public abstract PacketType getType();
	public static int getBufferSize(){
		return 65527;
	}
}
