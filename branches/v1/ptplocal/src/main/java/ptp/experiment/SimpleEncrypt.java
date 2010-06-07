package ptp.experiment;


public class SimpleEncrypt {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		byte[] b1 = new byte[256];
		
		for(int i = 0; i<b1.length; i++) {
			b1[i] =(byte) i;
		}

		byte k = (byte)0x1;
		
		for(int i = 0; i<b1.length; i++) {
			b1[i] =(byte) (b1[i] + k);
		}
		
		for(int i = 0; i<b1.length; i++) {
			b1[i] =(byte) (b1[i] - k);
		}
		
		for(int i = 0; i<b1.length; i++) {
			System.out.println(b1[i]);
		}
		//System.out.println(ByteArrayUtil.toString(b1, 0, b1.length));
	}

}
