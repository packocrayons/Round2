package testFileGeneration;
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
			((1<<16)-1)*512,
			(1<<16)*512,
			((1<<16)+1)*512,
			(1<<17)*512};
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
			"2^17x512.txt"};
	
	
	@Test
	public void test() throws IllegalAccessException, IOException {
		int i;
		for(i = 0; i<Math.min(fileSizes.length, fileNames.length)-1; i++){
			new Thread(new TestFileMaker(fileNames[i],Long.valueOf(fileSizes[i]).toString())).start();
		}
		new TestFileMaker(fileNames[i],Long.valueOf(fileSizes[i]).toString()).run();
	}

}
