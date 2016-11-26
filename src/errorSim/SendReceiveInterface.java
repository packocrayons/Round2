/**
 * 
 */
package errorSim;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import packets.Packet;

/**
 * @author brydon team 17
 *
 */
public interface SendReceiveInterface {
	public void sendFromSocket(DatagramSocket s, Packet p);
	public void receiveFromSocket(DatagramSocket s, DatagramPacket d) throws IOException; //since there's already handling for this, the old anonymous classes handled IOExceptions on their own
}
