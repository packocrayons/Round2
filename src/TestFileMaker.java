import java.io.OutputStream;
import java.util.Scanner;

import tftp.FileFactory;

public class TestFileMaker implements Runnable {


	private String name;
	private long size;
	private String path = ".\\testFiles";
	
	public TestFileMaker(String ...args){
		if(args.length<2){
			Scanner sc = new Scanner(System.in);
			System.out.println("What do you want to call it?");
			name = sc.nextLine().replaceAll("[\\s]+", "");
			System.out.println("The filesize will be ((blocks*512)+bytes)");
			System.out.print("Blocks:");
			size = Long.valueOf(sc.nextLine())*512;
			System.out.print("Bytes:");
			size += Long.valueOf(sc.nextLine());
		}else{
			name = args[0].replaceAll("[\\s]+", "");
			size = Long.valueOf(args[1]);
			if(args.length>2){
				path = args[2];
			}
		}
	}
	
	public static void main(String ...args){
		new TestFileMaker(args).run();
	}
	
	@Override
	public void run() {
		try {
			System.out.println("Writing "+name);
			OutputStream out = new FileFactory(path).writeFile(name);
			int charValue = 0;
			long s = size;
			while(--s >= 0L){
				if(s > 0L){
					charValue = (charValue+1)%26;
					out.write(charValue+'a');
				}else{
					out.write('\n');
				}
			}
			out.close();
			System.out.println("Finished writing "+name);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
