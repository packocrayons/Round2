
package packets;

import java.util.Arrays;

import tftp.FileType;

/**
 * A class that handles everything required for Read Request Packet 
 * @author Team 17
 */
public class ReadRequestPacket extends Packet{
	private final PacketType type = PacketType.RRQ;
	private final byte[] bytes;
	private final String filePath;
	private final FileType fileType;
	
	public ReadRequestPacket(String filePath, FileType fileType){
		this.filePath = filePath;
		this.fileType = fileType;
		
		byte[] path = filePath.getBytes();
		byte[] type = fileType.name().getBytes();
		byte[] data = new byte[2+filePath.length()+1+fileType.name().length()+1];
		
		data[0] = (byte)0;
		data[1] = this.getType().getOpcode();
		System.arraycopy( path, 0, data, 2, path.length);
		data[2+path.length] = (byte)0;
		System.arraycopy( type, 0, data, 2+path.length+1, type.length );
		data[2+path.length+1+type.length] = 0;
		
		this.bytes = data;
		
	}
	
	protected ReadRequestPacket(byte[] data, int length){
		data = Arrays.copyOf(data, length);
		bytes = data;
		
		if(data.length<5){
			throw new RuntimeException("RRQ too short, it is "+data.length+" bytes long, but it must be at least 5");
		}
		
		if(data[data.length-1] != (byte)0){
			throw new RuntimeException("RRQ doesn't end with 0");
		}
		
		if(data[0] != (byte)0){
			throw new RuntimeException("Wrong first byte "+data[0]+" != 0 for RRQ");
		}
		
		if(data[1] != this.getType().getOpcode()){
			throw new RuntimeException("Wrong opcode ("+data[1]+") for RRQ");
		}
		
		int fileStart = 2;
		int fileEnd = fileStart;
		while(((int)data[++fileEnd]) != 0){}
		//fileStart points to the first character of the filepath, fileEnd points to it's terminating null
		this.filePath = new String(Arrays.copyOfRange(data, fileStart, fileEnd));
		
		//check that the file path is at least 1 character
		if(this.filePath.length() == 0){
			throw new RuntimeException("No file path field on RRQ");
		}
		//check that we are not at the end of the byte array
		if(fileEnd == data.length-1){
			throw new RuntimeException("No mode field on RRQ");
		}
		
		int typeStart = fileEnd+1;
		int typeEnd = typeStart;
		if(typeEnd == data.length-1){
			throw new RuntimeException("No mode field on RRQ");
		}
		while(((int)data[++typeEnd]) != 0){}
		//typeStart points to the first character of the type, typeEnd points to it's terminating null, this should be the end of the array
		this.fileType = FileType.fromString(new String(Arrays.copyOfRange(data, typeStart, typeEnd)));
		
		if(typeEnd != data.length-1){
			throw new RuntimeException("Too many 0s on RRQ");
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

