package crawler;

import java.io.File;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import util.DataToLocal;
import util.WeiboMobileResources;

/**
 * 
 * @author loleek 用于将微博中用户ID转换成Fid
 */
public class WeiboFidGetter {
	public static String catchFid(CloseableHttpClient client, String uid) {
		String url = WeiboMobileResources.USER_HOMEPAGE_URL + uid;
		HttpGet get = new HttpGet(url);
		String fid = null;
		CloseableHttpResponse response = null;
		try {
			response = client.execute(get);
			DataToLocal.localize(response.getEntity().getContent(), uid
					+ ".html");
			response.close();

			File file = new File(uid + ".html");
			Document doc = Jsoup.parse(file, "UTF-8");
			Elements es = doc.getElementsByTag("script");
			String s = es.get(1).data().trim();
			String fid1 = s.substring(s.indexOf("fid") + 6);
			fid = fid1.substring(0, fid1.indexOf(",") - 1);
			
			// FileInputStream in = new FileInputStream(file);
			// Long length = file.length();
			// byte[] ba = new byte[length.intValue()];
			// in.read(ba);
			// in.close();
			// String content = new String(ba);
			// content = content.substring(content
			// .indexOf("window.$render_data =") + 21);
			// content = content.substring(0, content.indexOf("}};") + 2);
			// content = content.replaceAll("\'", "\"");
			//
			// ObjectMapper mapper = new ObjectMapper();
			// JsonNode rootNode = mapper.readTree(content);
			// fid = rootNode.path("common").path("containerid").textValue();

			file.delete();

		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return fid;
		
	}
}
