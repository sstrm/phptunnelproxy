package ptp.util;

public class ByteArrayUtil {

	public static String toString(byte[] array, int off, int length) {

		char[] charArray = new char[length];

		for (int i = 0; i < length; i++) {
			charArray[i] = (char) array[off + i];
		}

		return new String(charArray);
	}

	public static int toInt(byte[] array, int off, int length) {

		if (length > 0 && length <= 4) {
			int tmp = 0;
			for (int i = 0; i < length; i++) {
				tmp <<= 8;
				tmp += array[off + i] & 0xFF;
			}
			return tmp;
		}
		return 0;
	}

	public static int firstIndexof(byte b, byte[] array, int off, int length) {
		int index = -1;
		for (int i = 0; i < length; i++) {
			if (array[i + off] == b) {
				index = i + off;
				break;
			}
		}
		return index;
	}

	public static boolean startWith(byte[] b, byte[] array, int off, int length) {
		for (int i = 0; i < b.length; i++) {
			if (array[i + off] != b[i]) {
				return false;
			}
		}
		return true;
	}

	public static int firstIndexof(byte[] b, byte[] array, int off, int length) {
		int index = -1;
		for (int i = 0; i < length; i++) {
			if (startWith(b, array, i + off, length - i)) {
				index = i + off;
				break;
			}
		}
		return index;
	}

	public static byte[] trim(byte[] from, int off, int len) {
		byte[] res = new byte[len];
		for (int i = 0; i < len; i++) {
			res[i] = from[i + off];
		}
		return res;
	}

	public static void copy(byte[] from, int fromOff, byte[] to, int toOff,
			int copyLen) {
		for (int i = 0; i < copyLen; i++) {
			to[i+toOff] = from[i + fromOff];
		}
	}
}
