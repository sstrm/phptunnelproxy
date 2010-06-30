package cc.co.phptunnelproxy.ptplocal.experiment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WriteReadTRy {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File f = new File("tmp/a.dat");
		if(f.exists()) {
			f.delete();
		}
		WriteThread wt = new WriteThread(f);
		ReadThread rt = new ReadThread(f);
		wt.start();
		rt.start();

	}
	
}

class WriteThread extends Thread {
	
	private File file;
	
	public WriteThread(File file) {
		this.file = file;
	}
	
	public void run() {
		try {
			FileOutputStream fos = new FileOutputStream(file, true);
			for(int i = 0; i<10; i++) {
				fos.write(i);
				Thread.sleep(3000);
			}
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

class ReadThread extends Thread {
	
	private File file;
	
	public ReadThread(File file) {
		this.file = file;
	}
	
	public void run() {
		try {
			InputStream fis = new FileInputStream(file);
			for(int i = 0; i<100; i++) {
				int b = fis.read();
				System.out.println(b);
				Thread.sleep(500);
			}
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

