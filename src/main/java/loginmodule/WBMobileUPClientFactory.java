package loginmodule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.CommonResources;
import util.DataToLocal;
import util.WeiboMobileResources;

/**
 * 
 * @author dk 使用给定账号密码用于返回已登录新浪微博手机版的HttpCLient
 *
 */
public class WBMobileUPClientFactory implements LoginClientFactory {

	/**
	 * 返回已登录新浪微博的HttpClient
	 * 
	 * @param name
	 *            用户名
	 * @param password
	 *            密码
	 * @return 登陆后的HttpCLient
	 * @exception 可能由于账号被封或者使用次数过多导致登陆失败
	 */
	public CloseableHttpClient createHttpClient(String name, String password,
			Integer type) throws IOException {
		CloseableHttpClient client = HttpClientBuilder.create().build();
		if (type == CommonResources.WEIBO_NORMAL_CRAWLER) {
			try {
				gotoLoginPage(client);
			} catch (Exception e) {
				return null;
			}
			HttpPost post = makePost(name, password);
			String location = null;
			try {
				location = postForm(client, post);
			} catch (Exception e) {
				return null;
			}
			try {
				gotoHomePage(client, location);
			} catch (Exception e) {
				return null;
			}
			try {
				gotoTouchPage(client);
			} catch (Exception e) {
				return null;
			}
		}else if(type==CommonResources.WEIBO_INFO_CRAWLER){
			try {
				gotoLoginPage(client);
			} catch (Exception e) {
				return null;
			}
			HttpPost post = makePost(name, password);
			String location = null;
			try {
				location = postForm(client, post);
			} catch (Exception e) {
				return null;
			}
			try {
				gotoHomePage(client, location);
			} catch (Exception e) {
				return null;
			}
		}else if(type==CommonResources.WEIBO_REPOST_CRAWLER){
			
		}else if(type==CommonResources.WEIBO_TOPIC_CRAWLER){
			
		}
		return client;
	}

	/**
	 * 进入登陆界面
	 * 
	 * @param client
	 *            默认的HttpClient
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private void gotoLoginPage(CloseableHttpClient client)
			throws ClientProtocolException, IOException {
		HttpGet get = new HttpGet(WeiboMobileResources.LONGIN_URL);
		CloseableHttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();
		DataToLocal.localize(entity.getContent(), "loginpage.html");
		response.close();
	}

	/**
	 * 根据用户名和密码生成要提交的POST表单
	 * 
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @return 返回制作好的表单
	 * @throws IOException
	 */
	private HttpPost makePost(String username, String password)
			throws IOException {
		File f = new File("loginpage.html");
		Document doc = Jsoup.parse(f, "UTF-8");
		Elements elements = doc.getElementsByTag("form");
		Element element = elements.get(0);
		String formurl = element.attr("action");
		Elements es = element.getElementsByTag("input");

		ArrayList<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
		for (Element e : es) {
			String attr = e.attr("name");
			if (attr.contains("mobile"))
				list.add(new BasicNameValuePair(attr, username));
			else if (attr.contains("password"))
				list.add(new BasicNameValuePair(attr, password));
			else if (attr.contains("remember"))
				list.add(new BasicNameValuePair(attr, "on"));
			else
				list.add(new BasicNameValuePair(attr, e.attr("value")));
		}

		HttpPost post = new HttpPost("http://login.weibo.cn/login/" + formurl);
		post.setEntity(new UrlEncodedFormEntity(list));

		f.delete();

		return post;
	}

	/**
	 * 提交表单
	 * 
	 * @param client
	 *            默认的HttpCLient
	 * @param post
	 *            要提交的表单
	 * @return 将跳转链接取出
	 * @throws ClientProtocolException
	 * @throws IOException
	 *             此处可能会产生账号密码问题导致的跳转链接为空或者失效的异常
	 */
	private String postForm(CloseableHttpClient client, HttpPost post)
			throws ClientProtocolException, IOException {

		String location = null;

		CloseableHttpResponse response = client.execute(post);

		// System.out.println(response.getStatusLine());

		HeaderIterator it = response.headerIterator();

		while (it.hasNext()) {
			Header h = it.nextHeader();
			if (h.getName().equals("Location")) {
				location = h.getValue();
				break;
			}
		}

		response.close();

		return location;
	}

	/**
	 * 成功跳转后进入个人主页
	 * 
	 * @param client
	 *            取得cookie的HttpClient
	 * @param location
	 *            跳转地址
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private void gotoHomePage(CloseableHttpClient client, String location)
			throws ClientProtocolException, IOException {
		HttpGet get = new HttpGet(location);
		CloseableHttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();
		DataToLocal.localize(entity.getContent(), "homepage.html");
		response.close();
	}

	/**
	 * 进入触屏版页面（因为要抓取的数据是从触屏版API中提取的所以要提前进入触屏版页面）
	 * 
	 * @param client
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private void gotoTouchPage(CloseableHttpClient client)
			throws ClientProtocolException, IOException {
		File file = new File("homepage.html");
		Document doc = Jsoup.parse(file, "UTF-8");
		Elements elements = doc.getElementsByClass("c");
		ListIterator<Element> listit = elements.listIterator();
		String touchurl = null;
		while (listit.hasNext()) {
			Element e = listit.next();
			Elements childes = e.children();
			for (Element ce : childes) {
				if (ce.text().equals("触屏")) {
					touchurl = ce.attr("href");
					break;
				}
			}
		}
		// System.out.println(touchurl);
		HttpGet touchGet = new HttpGet(touchurl);

		Header head = new BasicHeader("User-Agent",
				WeiboMobileResources.USER_AGENT);
		touchGet.addHeader(head);
		CloseableHttpResponse response = client.execute(touchGet);

		response.close();
		file.delete();
	}

}
