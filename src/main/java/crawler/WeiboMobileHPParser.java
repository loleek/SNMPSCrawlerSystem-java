package crawler;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
/**
 * 
 * @author loleek
 * 解析个人主页
 */
public class WeiboMobileHPParser {
	public static String[] parseHomepage(String fileName) {
		File file = new File(fileName);
		String[] data = new String[3];
		try {
			Document doc = Jsoup.parse(file, "UTF-8");

			String weiboshu = doc.select("span[class=tc]").first().text()
					.split("\\[")[1].split("\\]")[0];
			String guanzhushu = doc.select("a[href*=/follow]").first().text()
					.split("\\[")[1].split("\\]")[0];

			String fensishu = null;
			try {
				fensishu = doc.select("a[href*=/fans]").first().text()
						.split("\\[")[1].split("\\]")[0];
			} catch (ArrayIndexOutOfBoundsException e) {
				fensishu = doc.select("a[href*=/fans]").eq(1).text()
						.split("\\[")[1].split("\\]")[0];
			}

			data[0] = weiboshu;
			data[1] = guanzhushu;
			data[2] = fensishu;

		} catch (IOException e) {
			e.printStackTrace();
		}
		file.delete();
		return data;
	}
}
