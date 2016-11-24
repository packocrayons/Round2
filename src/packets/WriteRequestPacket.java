package packets;

import java.util.Arrays;

import tftp.FileType;

/**
 * A class that handles everything required for Write Request Packet 
 * @author Team 17
 */
public class WriteRequestPacket extends Packet{
	private final PacketType type = PacketType.WRQ;
	private final byte[] bytes;
	private final String filePath;
	private final FileType fileType;
	
	public WriteRequestPacket(String filePath, FileType fileType){
		this.filePath = filePath;
		this.fileType = fileType;
		this.bytes = new GenericPacket(
				(byte)0,
				(byte)type.getOpcode())
				.cat(filePath)
				.cat((byte)0)
				.cat(fileType.name())
				.cat((byte)0)
				.getBytes();
	}
	
	protected WriteRequestPacket(byte[] data, int length){
		data = Arrays.copyOf(data, length);
		bytes = data;
		
		if(data.length<5){
			throw new RuntimeException("WRQ too short, it is "+data.length+" bytes long, but it must be at least 5");
		}
		
		if(data[data.length-1] != (byte)0){
			throw new RuntimeException("WRQ doesn't end with 0");
		}
		
		if(data[0] != (byte)0){
			throw new RuntimeException("Wrong first byte "+data[0]+" != 0 for WRQ");
		}
		
		if(data[1] != this.getType().getOpcode()){
			throw new RuntimeException("Wrong opcode ("+data[1]+") for WRQ");
		}
		
		int fileStart = 2;
		int fileEnd = fileStart;
		while(((int)data[++fileEnd]) != 0){}
		//fileStart points to the first character of the filepath, fileEnd points to it's terminating null
		this.filePath = new String(Arrays.copyOfRange(data, fileStart, fileEnd));
		
		//check that the file path is at least 1 character
		if(this.filePath.length() == 0){
			throw new RuntimeException("No file path field on WRQ");
		}
		//check that we are not at the end of the byte array
		if(fileEnd == data.length-1){
			throw new RuntimeException("No mode field on WRQ");
		}
		
		int typeStart = fileEnd+1;
		int typeEnd = typeStart;
		if(typeEnd == data.length-1){
			throw new RuntimeException("No mode field on WRQ");
		}
		while(((int)data[++typeEnd]) != 0){}
		//typeStart points to the first character of the type, typeEnd points to it's terminating null, this should be the end of the array
		this.fileType = FileType.fromString(new String(Arrays.copyOfRange(data, typeStart, typeEnd)));
		
		if(typeEnd != data.length-1){
			throw new RuntimeException("Too many 0s on WRQ");
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
	public FileType getFileType() {
		return fileType;
	}

	public String getFilePath() {
		return filePath;
	}
	
	//@Override
	public static int getBufferSize() {
		return 2+260;
	}
	
}
