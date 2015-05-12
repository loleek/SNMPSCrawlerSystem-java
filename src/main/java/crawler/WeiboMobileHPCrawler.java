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
 * 个人信息爬虫中获取个人主页
 */
public class WeiboMobileHPCrawler {
	public static void catchHomePage(CloseableHttpClient client,String url) throws ClientProtocolException, IOException{
		HttpGet get = new HttpGet(url);
		get.addHeader(new BasicHeader("User-Agent",WeiboMobileResources.USER_AGENT));
		CloseableHttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();
		DataToLocal.localize(entity.getContent(), "weibomhp.html");
		response.close();
	}
}
