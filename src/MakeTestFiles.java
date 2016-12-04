import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;

import tftp.FileFactory;

public class MakeTestFiles {

	private static final long[]   fileSizes = {
			0,
			1,
			512,
			513,
			127*512,
			128*512,
			129*512,
			(((1<<16)-1)*512),
			((1<<16)*512),
			(((1<<16)+1)*512),
			1<<17*512};
	private static final String[] fileNames = {
			"0.txt",
			"1.txt",
			"512.txt",
			"513.txt",
			"127x512.txt",
			"128x512.txt",
			"129x512.txt",
			"2^16-1x512.txt",
			"2^16x512.txt",
			"2^16+1x512.txt",
			"2^17.txt"};
	
	private void write(long size, OutputStream out) throws IOException{
		int charValue = 0;
		while(--size>=0){
			if(size >0){
				charValue = (charValue+1)%26;
				out.write(charValue+'a');
			}else{
				out.write('\n');
			}
		}
		out.close();
	}
	
	@Test
	public void test() throws IllegalAccessException, IOException {
		FileFactory fFac = new FileFactory(".\\testFiles");
		for(int i = 0; i<Math.min(fileSizes.length, fileNames.length); i++){
			System.out.println(i);
			write(fileSizes[i], fFac.writeFile(fileNames[i]));
		}
	}

}
