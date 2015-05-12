package test;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import crawler.WeiboFollowParser;
import crawler.WeiboTweetParser;

public class TestWeibotweetParser {
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		System.out.println(WeiboTweetParser.getContent("2627787531"));
//		System.out.println(WeiboTweetParser.getPages("2627787531"));
//		System.out.println(WeiboFollowParser.getContent("2627787531"));
	}
}
