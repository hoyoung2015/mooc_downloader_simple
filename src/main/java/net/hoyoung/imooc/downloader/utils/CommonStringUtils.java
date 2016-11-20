package net.hoyoung.imooc.downloader.utils;

public class CommonStringUtils {

	public static boolean isLetter(char c) {
		int k = 0x80;
		return c / k == 0 ? true : false;
	}

	public static int length(String s) {
		if (s == null)
			return 0;
		char[] c = s.toCharArray();
		int len = 0;
		for (int i = 0; i < c.length; i++) {
			len++;
			if (!isLetter(c[i])) {
				len++;
			}
		}
		return len;
	}
}
