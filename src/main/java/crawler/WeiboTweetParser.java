package crawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * 
 * @author loleek
 * 用于解析用户微博数据
 */
public class WeiboTweetParser {
	private static String XML_PATH = null;
	private static Document xmlConf = null;
	private static Elements propertyList = null;

	static {
		XML_PATH = "conf"+File.separatorChar+"weibotweet.xml";
		File xmlFile = null;
		try {
			xmlFile = new File(XML_PATH);
			xmlConf = Jsoup.parse(xmlFile, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		propertyList = xmlConf.select("property");
	}
	//获取微博内容（slave上调用）
	@SuppressWarnings("unchecked")
	public static String getContent(String id) throws JsonParseException,
			JsonMappingException, IOException {
		String content = "";
		File file = new File(id+"_weibo_tweet.json");
		ObjectMapper tweetMapper = new ObjectMapper();
		Map<String, Object> tweetData = tweetMapper.readValue(file, Map.class);
		ArrayList<Object> tweetList = (ArrayList<Object>) tweetData
				.get("cards");

		tweetData = (Map<String, Object>) tweetList.get(0);
		if (tweetData.get("card_group") == null) {
			file.delete();
			return content;
		} else
			tweetList = (ArrayList<Object>) tweetData.get("card_group");

		ListIterator<Element> propertyIter = propertyList.listIterator();

		for (int i = 0; i != tweetList.size(); i++, propertyIter = propertyList
				.listIterator()) {
			Map<String, Object> tweet = (LinkedHashMap<String, Object>) tweetList
					.get(i);
			tweet = (Map<String, Object>) tweet.get("mblog");

			while (propertyIter.hasNext()) {
				Element property = propertyIter.next();
				// String name = property.getElementsByTag("name").text();
				String key = property.getElementsByTag("key").text();
				String[] keyList = key.split("\\.");
				if (keyList.length == 1) {
					Object value = tweet.get(keyList[0]);
					content = content + value + "\t";
				}
				if (keyList.length == 2) {
					if (tweet.get(keyList[0]) == null) {
						content = content + null + "\t";
					} else {
						if (tweet.get(keyList[0]) instanceof ArrayList) {
							ArrayList<Object> detailList = (ArrayList<Object>) tweet
									.get(keyList[0]);
							for (int j = 0; j != detailList.size(); j++) {
								Map<String, Object> detail = (LinkedHashMap<String, Object>) detailList
										.get(j);
								content = content + detail.get(keyList[1])
										+ "\t";
							}
						}
						if (tweet.get(keyList[0]) instanceof LinkedHashMap) {
							Map<String, Object> detail = (LinkedHashMap<String, Object>) tweet
									.get(keyList[0]);
							content = content + detail.get(keyList[1]) + "\t";
						}
					}
				}
			}
			content += "\n";
		}
		file.delete();
		return content;
	}
	//获取用户微博页数（master上调用）
	public static int getPages(String id) {
		File file = new File(id+"_weibo_tweet.json");

		ObjectMapper mapper = new ObjectMapper();
		int pages = 0;
		try {
			JsonNode rnode = mapper.readTree(file);
			
			String count = rnode.path("count").asText();

			pages = Integer.parseInt(count) / 10 + 1;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		file.delete();
		return pages;
	}
}
