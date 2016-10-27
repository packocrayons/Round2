package packets;

import java.util.Arrays;

public class ErrorPacket extends Packet {
	private static final PacketType type = PacketType.ERR;
	private final byte[] bytes;
	private final String message;
	private final ErrorType err;
	
	public ErrorPacket(ErrorType err){
		this(err, "");
	}
	
	public ErrorPacket(ErrorType err, String message){
		this.err = err;
		this.message = (message != null)?(message):("");
		byte[] temp = new byte[2+2+this.message.getBytes().length+1];
		
		temp[0] = (byte)0;
		temp[1] = type.getOpcode();
		temp[3] = (byte)0;
		temp[4] = err.getOpcode();
		
		System.arraycopy(this.message.getBytes(), 0, temp, 5, this.message.getBytes().length);
		
		temp[temp.length-1] = (byte)0;
		
		this.bytes=temp;
	}
	
	protected ErrorPacket(byte[] data, int length){
		data = Arrays.copyOf(data, length);
		this.bytes = data;
		
		if(length < 5){
			throw new IllegalArgumentException("This byte array is not 5 bytes long");
		}
		
		if(data[0] != (byte)0 || data[1] != this.getType().getOpcode()){
			throw new IllegalArgumentException("The header on this packet is not formed correctly, it does not represent an ACK");
		}
		this.err = ErrorType.fromCode(data[3]);
		
		if(data[4] == (byte)0){
			this.message = "";
		}else{
			int messageStart = 4;
			int messageEnd = messageStart;
			while(data[++messageEnd] != (byte)0){}
			if(messageEnd != data.length){
				throw new RuntimeException("This data contains too many 0s");
			}
			this.message = new String(Arrays.copyOfRange(data, messageStart, messageEnd));
		}
	}
	
	@Override
	public byte[] getBytes() {
		return bytes.clone();
	}

	@Override
	public PacketType getType() {
		return type;
	}

	public ErrorType getErrorType() {
		return err;
	}
	
	@Override
	public int getNumber(){
		return -1;
	}

}
