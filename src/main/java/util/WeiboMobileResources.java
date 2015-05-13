package util;

/**
 * 
 * @author dk
 *
 *         文件中包含所有微博手机版登陆抓取获取过程中需要的相关连接
 */
public class WeiboMobileResources {

	// 登陆手机版可能需要指定一个手机版Agent
	public static String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";

	// 登陆界面链接
	public static String LONGIN_URL = "http://login.weibo.cn/login/?ns=1&revalid=2&backURL=http%3A%2F%2Fweibo.cn%2F%3Frl%3D1&backTitle=%CE%A2%B2%A9&vt=";

	// 个人主页地址
	public static String USER_HOMEPAGE_URL = "http://m.weibo.cn/u/";

	public static String CONTAINER_PREFIX = "100505";

	// 用于拼接关系数据抓取链接
	public static String FOLLOWS_PREFIX = "http://m.weibo.cn/page/json?containerid=";
	public static String FOLLOWS_SUFFIX = "_-_FOLLOWERS&page=";

	// 用于拼接微博数据抓取链接
	public static String WEIBOURL_PREFIX = "http://m.weibo.cn/page/json?containerid=";
	public static String WEIBOURL_SUFFIX = "_-_WEIBO_SECOND_PROFILE_WEIBO&page=";

	// 用于拼接微博个人信息抓取链接
	public static String WEIBO_MOBILE_HOMEPAGE_PREFIX = "http://weibo.cn/u/";
	public static String WEIBO_MOBILE_INFO_PREFIX = "http://weibo.cn/";
	public static String WEIBO_MOBILE_INFO_SUFFIX = "/info";

}
