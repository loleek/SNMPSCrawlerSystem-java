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
 * @author loleek 解析关系数据
 */
public class WeiboFollowParser {

	private static String XML_PATH = null;
	private static Document xmlConf = null;
	private static Elements propertyList = null;

	static {
		XML_PATH = "conf"+File.separatorChar+"weiborelation.xml";
		File xmlFile = null;
		try {
			xmlFile = new File(XML_PATH);
			xmlConf = Jsoup.parse(xmlFile, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		propertyList = xmlConf.select("property");
	}

	// 获取数据（slave上调用）
	@SuppressWarnings("unchecked")
	public static String getContent(String id) throws JsonParseException,
			JsonMappingException, IOException {
		String content = "";
		File file = new File(id + "_weibo_follow.json");
		ObjectMapper relationMapper = new ObjectMapper();
		Map<String, Object> relationData = null;

		relationData = relationMapper.readValue(file, Map.class);

		content = "" + id + "\t";
		ArrayList<Object> userList = null;

		userList = (ArrayList<Object>) relationData.get("cards");

		relationData = (Map<String, Object>) userList.get(0);
		if (relationData.get("card_group") == null) {
			return null;
		} else
			userList = (ArrayList<Object>) relationData.get("card_group");

		ListIterator<Element> propertyIter = propertyList.listIterator();
		for (int i = 0; i != userList.size(); i++, propertyIter = propertyList
				.listIterator()) {
			Map<String, Object> user = (LinkedHashMap<String, Object>) userList
					.get(i);
			user = (Map<String, Object>) user.get("user");
			String follower = "";

			while (propertyIter.hasNext()) {
				Element property = propertyIter.next();
				// String name = property.getElementsByTag("name").text();
				String key = property.getElementsByTag("key").text();
				String[] keyList = key.split("\\.");
				if (keyList.length == 1) {
					Object value = user.get(keyList[0]);
					follower += value;
				}
			}
			content += follower + ",";
		}
		file.delete();
		return content;
	}

	// 获取用户关系数据页数（master上调用）
	public static int getPages(String id) {
		File file = new File(id + "_weibo_follow.json");
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
