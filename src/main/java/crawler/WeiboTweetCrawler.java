package crawler;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import util.DataToLocal;

/**
 * 
 * @author loleek 用于抓取用户微博数据
 */
public class WeiboTweetCrawler {
	public static String catchTweet(CloseableHttpClient client, String url)
			throws IOException {
		String[] args = url.split(" ");
		String id = args[0];
		HttpGet get = new HttpGet(args[1]);
		CloseableHttpResponse response = null;

		response = client.execute(get);
		DataToLocal.localize(response.getEntity().getContent(), id
				+ "_weibo_tweet.json");
		response.close();

		return id;
	}
}
