package crawler;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;

import util.DataToLocal;
import util.WeiboMobileResources;
/**
 * 
 * @author loleek
 * 用于抓取个人信息
 */
public class WeiboMobileInfoCrawler {
	public static void catchInfoPage(CloseableHttpClient client, String url)
			throws ClientProtocolException, IOException {
		HttpGet get = new HttpGet(url);
		get.addHeader(new BasicHeader("User-Agent",
				WeiboMobileResources.USER_AGENT));
		CloseableHttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();
		DataToLocal.localize(entity.getContent(), "weibominfo.html");
		response.close();
	}
}
