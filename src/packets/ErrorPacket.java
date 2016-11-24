package packets;

import java.util.Arrays;

// * @author Team 17
public class ErrorPacket extends Packet {
	private static final PacketType type = PacketType.ERR;
	private final byte[] bytes;
	private final String message;
	private final ErrorType err;
	
	public ErrorPacket(ErrorType err){
		this(err, null);
	}
	
	public ErrorPacket(ErrorType err, String message){
		this.err = err;
		this.message = (message != null)?(message):("");
		//byte[] temp = new byte[2+2+this.message.getBytes().length+1];
		this.bytes = new GenericPacket(
				(byte)0,
				type.getOpcode(),
				(byte)0,
				err.getOpcode())
				.cat(message)
				.cat((byte)0)
				.getBytes();
	}
		
	protected ErrorPacket(byte[] data, int length){
		data = Arrays.copyOf(data, length);
		this.bytes = data;
		
		if(length < 4){
			throw new IllegalArgumentException("ERROR Packet too short");
		}
		
		if(data[0] != (byte)0 || data[1] != this.getType().getOpcode()){
			throw new IllegalArgumentException("Wrong header for ERROR packet opcode received "+data[0]+data[1]);
		}
		this.err = ErrorType.fromCode(data[3]);
		
		if(data[4] == (byte)0){
			this.message = "";
		}else{
			int messageStart = 4;
			int messageEnd = messageStart;
			while(data[++messageEnd] != (byte)0){}
			if(messageEnd != data.length-1){
				throw new RuntimeException("This error message contains too many 0s");
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
	
	
	public String getMessage(){
		return message;
	}

}

