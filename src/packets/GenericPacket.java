package packets;

public class GenericPacket extends Packet {
	private static final PacketType type = PacketType.GENERIC;
	private final byte[] bytes;
	
	public GenericPacket(){
		this(new byte[0]);
	}
	
	public GenericPacket(byte[] data){
		this.bytes = data.clone();
	}
	
	@Override
	public byte[] getBytes() {
		return bytes.clone();
	}

	@Override
	public PacketType getType() {
		return type;
	}

	public GenericPacket cat(byte b){
		return this.cat(new byte[]{b});
	}
	
	public GenericPacket cat(String s){
		return this.cat(s.getBytes());
	}
	
	public GenericPacket cat(byte[] b){
		byte[] newData = new byte[bytes.length+b.length];
		System.arraycopy(bytes, 0, newData, 0, bytes.length);
		System.arraycopy(b, 0, newData, bytes.length, b.length);
		return new GenericPacket(newData);
	}
	
}
