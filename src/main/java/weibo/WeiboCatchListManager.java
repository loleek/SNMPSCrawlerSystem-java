package weibo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import loginmodule.LoginClientFactory;
import loginmodule.WBMobileUPClientFactory;

import org.apache.http.impl.client.CloseableHttpClient;

import util.CommonResources;
import util.WeiboMobileResources;
import crawler.WeiboFidGetter;
import crawler.WeiboFollowCrawler;
import crawler.WeiboFollowParser;
import crawler.WeiboTweetCrawler;
import crawler.WeiboTweetParser;

/**
 * 
 * @author loleek 用于管理抓取队列
 */
public class WeiboCatchListManager {

	private WeiboCrawlerMasterManager manager = null;
	private Integer current_type = CommonResources.WEIBO_NORMAL_CRAWLER;

	File ids = null;
	BufferedReader br = null;

	// 保存关系和微博抓取链接
	private ConcurrentLinkedQueue<String> normal_crawler_catch_queue = null;
	// 保存用于抓取个人信息的id
	private ConcurrentLinkedQueue<String> detailinfoids_queue = null;
	// 转发微博抓取队列
	private ConcurrentLinkedQueue<String> repost_queue = null;
	// 话题抓取队列
	private ConcurrentLinkedQueue<String> topic_queue = null;

	private String previousuid = null;

	public WeiboCatchListManager(WeiboCrawlerMasterManager manager,
			Integer crawler_type) {
		this.manager = manager;
		this.current_type = crawler_type;

		normal_crawler_catch_queue = new ConcurrentLinkedQueue<String>();
		detailinfoids_queue = new ConcurrentLinkedQueue<String>();
		repost_queue = new ConcurrentLinkedQueue<String>();
		topic_queue = new ConcurrentLinkedQueue<String>();

		initialize();
	}

	// 获取一条要抓取的url，可能是四种爬虫中的任何一个，爬虫会根据爬虫类型自行切换
	// 如果除去关系和数据以外队列为空就切换到默认爬虫
	public synchronized String getUrl() {
		if (current_type == CommonResources.WEIBO_NORMAL_CRAWLER) {
			// 如果用户id大于100就切换到个人信息爬虫抓取用户个人信息
			if (detailinfoids_queue.size() >= 100) {
				changeType(CommonResources.WEIBO_INFO_CRAWLER);
				return getUrl();
			} else {
				if (!normal_crawler_catch_queue.isEmpty()) {
					return normal_crawler_catch_queue.poll();
				} else {
					System.out.println("normal_queue is empty");
					if (getOffers()) {
						System.out.println("getoffers successful");
						return normal_crawler_catch_queue.poll();
					} else {
						manager.stop();
						return "";
					}
				}
			}
		} else if (current_type == CommonResources.WEIBO_INFO_CRAWLER) {
			if (!detailinfoids_queue.isEmpty())
				return detailinfoids_queue.poll();
			else {
				changeType(CommonResources.WEIBO_NORMAL_CRAWLER);
				return getUrl();
			}
		} else if (current_type == CommonResources.WEIBO_REPOST_CRAWLER) {
			checkExtraTask();
			if (!repost_queue.isEmpty())
				return repost_queue.poll();
			else {
				changeType(CommonResources.WEIBO_NORMAL_CRAWLER);
				return getUrl();
			}
		} else if (current_type == CommonResources.WEIBO_TOPIC_CRAWLER) {
			checkExtraTask();
			if (!topic_queue.isEmpty())
				return topic_queue.poll();
			else {
				changeType(CommonResources.WEIBO_NORMAL_CRAWLER);
				return getUrl();
			}
		} else {
			return "";
		}
	}

	// 对队列进行初始化，会从上次未抓取完的队列持久化文件中恢复任务
	public void initialize() {
		ids = new File(CommonResources.WEIBO_CATCH_LIST_LOCATION);
		try {
			br = new BufferedReader(new FileReader(ids));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		File taskFile = new File(
				CommonResources.WEIBO_CATCH_LIST_PERSIST_LOCATION);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(taskFile));
			String url = null;
			while ((url = reader.readLine()) != null) {
				normal_crawler_catch_queue.add(url);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File idinfo = new File(CommonResources.WEIBO_ID_INFO_LOCATION);
		if (idinfo.length() != 0) {
			try {
				BufferedReader idreader = new BufferedReader(new FileReader(
						idinfo));
				String idline = null;
				while ((idline = idreader.readLine()) != null) {
					detailinfoids_queue.add(idline);
				}
				idreader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// 关闭爬虫系统时会将未完成任务持久化到文件
	public void persist() {
		try {
			br.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			Iterator<String> iterator = normal_crawler_catch_queue.iterator();
			File file = new File(
					CommonResources.WEIBO_CATCH_LIST_PERSIST_LOCATION);
			PrintWriter out = new PrintWriter(file);
			while (iterator.hasNext()) {
				String url = iterator.next();
				out.write(url + "\n");
			}
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			if (!detailinfoids_queue.isEmpty()) {
				Iterator<String> it = detailinfoids_queue.iterator();
				File file = new File(CommonResources.WEIBO_ID_INFO_LOCATION);
				PrintWriter out = new PrintWriter(file);
				while (it.hasNext()) {
					String id = it.next();
					out.write(id + "\n");
				}
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			File file = new File(CommonResources.WEIBO_CATCH_LIST_LOCATION);
			BufferedReader idreader = new BufferedReader(new FileReader(file));

			String id = null;
			boolean flag = true;
			while (flag && (id = idreader.readLine()) != null) {
				if (id.equals(previousuid))
					flag = false;
			}
			StringBuilder sb = new StringBuilder();
			while ((id = idreader.readLine()) != null) {
				sb.append(id + "\n");
			}
			idreader.close();

			file.delete();

			file = new File(CommonResources.WEIBO_CATCH_LIST_LOCATION);

			PrintWriter out = new PrintWriter(file);
			out.print(sb.toString());
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// 切换爬虫类型
	public void changeType(Integer type) {
		this.current_type = type;
		manager.updateCrawlerType(current_type);
	}

	// 若关系和微博爬虫队列为空，则从ids.txt文件中获取5个用户id进行抓取
	public synchronized boolean getOffers() {
		// if (!normal_crawler_catch_queue.isEmpty()){
		// System.out.println(normal_crawler_catch_queue.size());
		// return true;
		// }
		WeiboAccountManager accountmanager = WeiboAccountManager
				.getAccountManager();
		boolean flag = true;
		CloseableHttpClient client = null;
		while (flag) {
			WeiboAccount account = accountmanager.getAccount();
			if (account == null) {
				persist();
				System.out
						.println("have no account...crawler will shutdown...");
				manager.stop();
			} else {
				LoginClientFactory factory = new WBMobileUPClientFactory();
				try {
					client = factory.createHttpClient(account.getName(),
							account.getPassword(),
							CommonResources.WEIBO_NORMAL_CRAWLER);
					if (client != null) {
						flag = false;
						System.out.println("connect successfully");
					} else {
						accountmanager.submitWrongAccount(account);
					}
				} catch (IOException e) {
					accountmanager.submitWrongAccount(account);
					e.printStackTrace();
				}
			}
		}

		try {
			String id = br.readLine();
			if (id != null) {
				String fid = WeiboFidGetter.catchFid(client, id);
				if (fid != null) {
					detailinfoids_queue.add(id);
					System.out.println("enqueue " + id + " " + fid);

					previousuid = id;

					String tweeturl = id + " "
							+ WeiboMobileResources.WEIBOURL_PREFIX + fid
							+ WeiboMobileResources.WEIBOURL_SUFFIX + "1";
					System.out.println(fid + " tweet");
					WeiboTweetCrawler.catchTweet(client, tweeturl);
					int tweetcount = WeiboTweetParser.getPages(id);
					System.out.println("tweetcount " + tweetcount);
					for (int j = 1; j <= tweetcount; j++) {
						String url = id + " "
								+ WeiboMobileResources.WEIBOURL_PREFIX + fid
								+ WeiboMobileResources.WEIBOURL_SUFFIX + j;
						normal_crawler_catch_queue.add(url);
					}

					String followurl = id + " "
							+ WeiboMobileResources.FOLLOWS_PREFIX + fid
							+ WeiboMobileResources.FOLLOWS_SUFFIX + "1";
					System.out.println(fid + " follow");
					WeiboFollowCrawler.catchFollow(client, followurl);
					int followcount = WeiboFollowParser.getPages(id);
					System.out.println("followcount " + followcount);
					for (int j = 1; j <= followcount; j++) {
						String url = id + " "
								+ WeiboMobileResources.FOLLOWS_PREFIX + fid
								+ WeiboMobileResources.FOLLOWS_SUFFIX + j;
						normal_crawler_catch_queue.add(url);
					}
				} else
					return false;
			} else
				return false;
		} catch (IOException e) {
			System.out.println("exception hadppens");
			e.printStackTrace();
		}
		return true;
	}

	// 抓取失败则会重新放入队列
	public void urlFailure(String url, Integer type) {
		if (type == CommonResources.WEIBO_NORMAL_CRAWLER)
			normal_crawler_catch_queue.add(url);
		else if (type == CommonResources.WEIBO_INFO_CRAWLER)
			detailinfoids_queue.add(url);
		else if (type == CommonResources.WEIBO_REPOST_CRAWLER)
			repost_queue.add(url);
		else if (type == CommonResources.WEIBO_TOPIC_CRAWLER)
			topic_queue.add(url);
	}

	// 检测是否有更高优先级的任务(除正常的关系和微博爬虫)
	private void checkExtraTask() {
		File file = new File(CommonResources.WEIBO_EXTRA_TASK_LOCATION);
		if (file.exists()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			String line = null;
			if (current_type == CommonResources.WEIBO_REPOST_CRAWLER) {
				try {
					while ((line = reader.readLine()) != null) {
						repost_queue.add(line);
					}
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (current_type == CommonResources.WEIBO_TOPIC_CRAWLER) {
				try {
					while ((line = reader.readLine()) != null) {
						topic_queue.add(line);
					}
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		file.delete();
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
