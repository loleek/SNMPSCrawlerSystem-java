package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
/**
 * 
 * @author loleek
 * 用于验证抓取到的网页是否有效
 */
public class WeiboMobilePageValidator {
	public static int valitePage(String fileName) {
		String content = readHtml(fileName);
		if (content.contains("用户状态异常"))
			return 1;
		else if (content.contains(">微博广场") || content.contains("如果没有自动跳转")
				|| content.contains("504 Gateway Time-out"))
			return 2;
		else
			return 0;
	}

	private static String readHtml(String fileName) {
		File f = new File(fileName);
		String s = null;
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			while ((s = reader.readLine()) != null) {
				sb.append(s);
			}
			reader.close();
		} catch (IOException e) {

		}
		return sb.toString();
	}
}
