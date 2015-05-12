package loginmodule;

import java.io.IOException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * 
 * @author dk
 *
 *         一个微博手机版无密码登陆的HttpCLient用于直接获取JSON形式的数据
 * @deprecated 可能无法使用
 */
public class WBMobileDefCLientFactory implements LoginClientFactory {
	/**
	 * 返回无账户密码的HttpCLient
	 * @param name null
	 * @param password null
	 */
	public CloseableHttpClient createHttpClient(String name, String password,Integer type)
			throws IOException {
		return HttpClients.createDefault();
	}

}
