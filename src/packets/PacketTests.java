package packets;

import org.junit.Test;

public class PacketTests {

	@Test
	public void testRRQ() {
		ReadRequestPacket ap = new ReadRequestPacket("./HelloWorld.txt", FileType.NETASCII);
		Packet p = new PacketFactory().getPacket(ap.getBytes(), ap.getBytes().length);
		assert(p.getType().equals(ap.getType()));
		assert(arrayEquality(p.getBytes(), ap.getBytes()));
	}
	
	@Test
	public void testWRQ() {
		WriteRequestPacket ap = new WriteRequestPacket("./HelloWorld.txt", FileType.NETASCII);
		Packet p = new PacketFactory().getPacket(ap.getBytes(), ap.getBytes().length);
		assert(p.getType().equals(ap.getType()));
		assert(arrayEquality(p.getBytes(), ap.getBytes()));
	}
	
	@Test
	public void testData() {
		DataPacket ap = new DataPacket(4, new byte[512]);
		Packet p = new PacketFactory().getPacket(ap.getBytes(), ap.getBytes().length);
		assert(p.getType().equals(ap.getType()));
		assert(arrayEquality(p.getBytes(), ap.getBytes()));
	}
	
	@Test
	public void testAck() {
		AcknowledgementPacket ap = new AcknowledgementPacket(4);
		Packet p = new PacketFactory().getPacket(ap.getBytes(), ap.getBytes().length);
		assert(p.getType().equals(ap.getType()));
		assert(arrayEquality(p.getBytes(), ap.getBytes()));
	}
	
	@Test
	public void testError() {
		for(ErrorType er : ErrorType.values()){
			ErrorPacket ap = new ErrorPacket(er, "Why are you doing these tests? You are just tesging constructors.");
			Packet p = new PacketFactory().getPacket(ap.getBytes(), ap.getBytes().length);
			assert(p.getType().equals(ap.getType()));
			assert(arrayEquality(p.getBytes(), ap.getBytes()));
		}
	}
	
	
	private boolean arrayEquality(byte[] a, byte[] b){
		if(a.length != b.length){
			return false;
		}
		for(int i = 0; i<a.length; i++){
			if(a[i] != b[i]){
				return false;
			}
		}
		return true;
	}
	
}
