package crawler;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
/**
 * 
 * @author loleek
 * 用于解析用户标签
 */
public class WeiboMobileTagParser {
	public static String parseTags(String fileName) {
		StringBuilder tags = new StringBuilder();
		try {
			File f = new File(fileName);
			Document doc = Jsoup.parse(f, "UTF-8");
			Elements key = doc.select("a[href*=/search/?keyword]");

			try {
				for (int i = 0; i < key.size(); i++)
					tags.append(key.get(i).text() + "\\");
			} catch (StringIndexOutOfBoundsException e) {
				throw e;
			}

			f.delete();
		} catch (IOException e) {

		}
		return tags.toString();
	}
}
