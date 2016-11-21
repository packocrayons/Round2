
package packets;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Abstract class for Packets containing the main variables needed in a Packet
 * @author Team 17
 */
public abstract class Packet {
	public abstract byte[] getBytes();
	public abstract PacketType getType(); 
	public static int getBufferSize(){
		return 65527;
	}

	public final DatagramPacket asDatagramPacket(InetAddress address, int port){ //return a datagramPacket copy of this Packet for sending to address address on port port. 
		byte[] byteArray = getBytes();
		return new DatagramPacket(byteArray, byteArray.length, address, port);
	}
}


