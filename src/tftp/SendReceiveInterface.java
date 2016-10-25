/**
 * 
 */
package tftp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import packets.Packet;

/**
 * @author brydon
 *
 */
public interface SendReceiveInterface {
	public void sendFromSocket(DatagramSocket s, Packet p);
	public DatagramPacket receiveFromSocket(DatagramSocket s); 
}
