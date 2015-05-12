package loginmodule;

import java.io.IOException;

import org.apache.http.impl.client.CloseableHttpClient;

/**
 * 
 * @author dk
 *
 *该接口用于生成已经输入账号密码的HttpClient对象
 */
public interface LoginClientFactory {
	/**@param name 用户名
	 * @param password 密码
	 * @return 返回一个可用的HttpClient对象
	 * @exception 由于网络或者账号问题此处可能有异常抛出
	 */
	public CloseableHttpClient createHttpClient(String name,String password,Integer type) throws IOException;
}
