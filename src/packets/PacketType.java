<<<<<<< HEAD
package packets;

/**
 * An enum used along with the PacketFactory class to differentiate among the various types of packets possible
 * @author Team 15
 */
public enum PacketType {
    IDC("I don't care",(byte)0),
    RRQ("Read request",(byte)1),
    WRQ("Write request",(byte)2),
    DATA("Data",(byte)3),
    ACK("Acknowledgement",(byte)4),
    ERR("Error",(byte)5);
	
	private final String humanReadableName;
	private final byte opcode;
	
	private PacketType(String humanReadableName, byte opcode){
		this.humanReadableName = humanReadableName;
		this.opcode = opcode;
	}
	
	public String getHumanReadableName(){
		return this.humanReadableName;
	}
	
	public byte getOpcode(){
		return this.opcode;
	}
}
=======
package packets;

/**
 * An enum used along with the PacketFactory class to differentiate among the various types of packets possible
 * @author Team 15
 */
public enum PacketType {
    IDC("I don't care",(byte)0),
    RRQ("Read request",(byte)1),
    WRQ("Write request",(byte)2),
    DATA("Data",(byte)3),
    ACK("Acknowledgement",(byte)4),
    ERR("Error",(byte)5);
	
	private final String humanReadableName;
	private final byte opcode;
	
	private PacketType(String humanReadableName, byte opcode){
		this.humanReadableName = humanReadableName;
		this.opcode = opcode;
	}
	
	public String getHumanReadableName(){
		return this.humanReadableName;
	}
	
	public byte getOpcode(){
		return this.opcode;
	}
}
>>>>>>> 9e1e11a572111a2572274ab61298e6048e5c7c7c
