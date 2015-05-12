package crawler;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import util.DataToLocal;

/**
 * 
 * @author loleek 抓取关系数据
 */
public class WeiboFollowCrawler {
	public static String catchFollow(CloseableHttpClient client, String url)
			throws IOException {
		String[] args = url.split(" ");
		String id = args[0];
		HttpGet get = new HttpGet(args[1]);
		CloseableHttpResponse response = null;

		response = client.execute(get);
		DataToLocal.localize(response.getEntity().getContent(), id
				+ "_weibo_follow.json");
		response.close();

		return id;
	}
}
