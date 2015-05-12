package crawler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
/**
 * 
 * @author loleek
 * 用于解析个人信息
 */
public class WeiboMobileInfoParser {
	public static String parseInfo(String uid, CloseableHttpClient client,
			String[] data) {
		File f = new File("weibominfo.html");
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			Document doc = Jsoup.parse(f, "UTF-8");
			Element div = doc.getElementsByClass("c").eq(2).first();
			map.put("id", uid);
			Node nick = div.childNode(0);
			// System.out.println(nick.toString().split(":")[0]);
			String nickname = nick.toString().replaceAll("[\t\r\n]", "");
			// System.out.println(nickname);
			map.put(nickname.split(":")[0], nickname.split(":")[1]);
			// System.out.println(nick.toString().split(":")[0]);
			Node nick1 = nick.nextSibling();
			while (nick1.nextSibling() != null
					&& (nick1.nextSibling().toString().startsWith("标签")) == false) {
				try {
					map.put(nick1.nextSibling().toString().split(":")[0], nick1
							.nextSibling().toString().split(":")[1]);
				} catch (Exception e) {
					nick1 = nick1.nextSibling().nextSibling();
					continue;
				}
				nick1 = nick1.nextSibling().nextSibling();
				if (nick1.equals("<div>")) {
					break;
				}
			}
			try {
				Element tag = doc.select("a[href*=/privacy/tags]").first();
				String tags = null;
				if (tag != null) {
					String tagUrl = "http://weibo.cn" + tag.attr("href");
					WeiboMobileTagCrawler.catchTags(client, tagUrl);
					tags = WeiboMobileTagParser.parseTags("weibomtags.html");
				}
				map.put("标签", tags);
			} catch (StringIndexOutOfBoundsException e) {
				throw e;
			}

			// 处理学习和工作经历
			Element divXuexi = doc.getElementsByClass("tip").eq(1).first();
			if (divXuexi.text().equals("学习经历")) {
				Element divXue = divXuexi.nextElementSibling();
				String rs = divXue.html().replaceAll("<br />", "")
						.replaceAll("&nbsp;", "").replaceAll("&middot;", "")
						.replaceAll("[\t\r\n]", "\\\\");
				String rs1 = rs.substring(0, rs.length() - 1);
				map.put("学习经历", rs1);
				Element divGongZuo = doc.getElementsByClass("tip").eq(2)
						.first();
				if (divGongZuo.text().equals("工作经历")) {
					Element divGong = divGongZuo.nextElementSibling();
					String gongzuo = divGong.html().replaceAll("<br />", "")
							.replaceAll("&nbsp;", "")
							.replaceAll("&middot;", "")
							.replaceAll("[\t\r\n]", "\\\\");
					String gongzuo1 = gongzuo
							.substring(0, gongzuo.length() - 1);
					map.put("工作经历", gongzuo1);
				}
			} else if (divXuexi.text().equals("工作经历")) {
				Element divGong = divXuexi.nextElementSibling();
				String gongzuo = divGong.html().replaceAll("<br />", "")
						.replaceAll("&nbsp;", "").replaceAll("&middot;", "")
						.replaceAll("[\t\r\n]", "\\\\");
				String gongzuo1 = gongzuo.substring(0, gongzuo.length() - 1);
				map.put("工作经历", gongzuo1);
			}
			map.put("微博数", data[0]);
			map.put("关注数", data[1]);
			map.put("粉丝数", data[2]);

			f.delete();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return createFile(map);
	}

	public static String createFile(HashMap<String, String> map) {
		StringBuffer sb = new StringBuffer();
		sb.append(map.get("id") + "\t");
		sb.append(map.get("昵称") + "\t");
		sb.append(map.get("性别") + "\t");
		sb.append(map.get("生日") + "\t");
		sb.append(map.get("地区") + "\t");
		sb.append(map.get("达人") + "\t");
		sb.append(map.get("认证") + "\t");
		sb.append(map.get("关注数") + "\t");
		sb.append(map.get("粉丝数") + "\t");
		sb.append(map.get("微博数") + "\t");
		sb.append(map.get("简介") + "\t");
		sb.append(map.get("标签") + "\t");
		sb.append(map.get("学习经历") + "\t");
		sb.append(map.get("工作经历") + "\r\n");
		return sb.toString();
	}
}
