package test;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestJSON {
	public static void main(String[] args) throws JsonProcessingException, IOException {
		@SuppressWarnings("unused")
		ObjectMapper mapper=new ObjectMapper();
		File f=new File("1.html");
		String fid=null;
		Document doc = Jsoup.parse(f, "UTF-8");
		Elements es = doc.getElementsByTag("script");
		String s = es.get(1).data().trim();
		String fid1 = s.substring(s.indexOf("fid") + 6);
		fid = fid1.substring(0, fid1.indexOf(",") - 1);
		
		System.out.println(fid);
//		Long length=f.length();
//		byte[] filecontent=new byte[length.intValue()];
//		FileInputStream fis=new FileInputStream(f);
//		fis.read(filecontent);
//		fis.close();
//		String content=new String(filecontent);
//		content=content.substring(content.indexOf("window.$render_data =")+21);
//		content=content.substring(0,content.indexOf("}};")+2);
//		System.out.println(content);
//		content=content.replaceAll("\'","\"");
//		1656599602
//		JsonNode rootNode=mapper.readTree(content);
////		
//		System.out.println(rootNode.path("common").path("containerid").textValue());
//		File f=new File("tweet.json");
//		ObjectMapper mapper=new ObjectMapper();
//		JsonNode n=mapper.readTree(f);
//		System.out.println(n.path("count"));
		
		
	}
}
