package weibo;

import java.io.IOException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import loginmodule.WBMobileUPClientFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.http.impl.client.CloseableHttpClient;

import util.ApiDataSender;
import util.CommonResources;
import util.WeiboMobilePageValidator;
import util.WeiboMobileResources;
import core.Manager;
import crawler.WeiboFollowCrawler;
import crawler.WeiboFollowParser;
import crawler.WeiboMobileHPCrawler;
import crawler.WeiboMobileHPParser;
import crawler.WeiboMobileInfoCrawler;
import crawler.WeiboMobileInfoParser;
import crawler.WeiboTweetCrawler;
import crawler.WeiboTweetParser;

/**
 * 
 * @author loleek 微博爬虫slave-manager
 */
public class WeiboCralwlerSlaveManager implements Manager, MessageListener {

	private ConnectionFactory factory = null;
	private Connection connection = null;
	private Session session = null;
	private Destination producer_destination = null;
	private Destination consumer_destination = null;
	private MessageConsumer consumer = null;
	private MessageProducer producer = null;

	private CloseableHttpClient client = null;
	private WeiboAccount current_account = null;
	private Integer crawler_type = CommonResources.WEIBO_NORMAL_CRAWLER;
	private String current_task = null;

	private String hostname = null;

	public WeiboCralwlerSlaveManager(String hostname) {
		this.hostname = hostname;
		factory = new ActiveMQConnectionFactory(CommonResources.MQ_TCP_URL);
		try {
			connection = factory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			consumer_destination = session.createQueue(this.hostname + "_"
					+ CommonResources.WEIBO_SLAVE_LISTEN_QUEUE);
			producer_destination = session
					.createQueue(CommonResources.WEIBO_MASTER_LISTEN_QUEUE);

			consumer = session.createConsumer(consumer_destination);
			producer = session.createProducer(producer_destination);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	// 启动爬虫并向主节点注册
	public void start() {
		register();
		try {
			consumer.setMessageListener(this);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	// 管理爬虫
	public void stop(int level) {
		shutdownGracefully();
	}

	@SuppressWarnings("unused")
	public void onMessage(Message message) {
		if (message instanceof TextMessage) {
			TextMessage tm = (TextMessage) message;
			String type = null;
			String host = null;
			String text = null;
			Integer ctype = 0;

			try {
				type = tm.getStringProperty("type");
				host = tm.getStringProperty("host");
				ctype = tm.getIntProperty("crawl-type");
				text = tm.getText();
			} catch (JMSException e) {
				e.printStackTrace();
			}
			// command 启动爬虫
			// url 抓取url
			// account 新的账户
			// close 关闭爬虫
			if (type.equals("command")) {
				String[] args = text.split(" ");
				String username = args[0];
				String password = args[1];
				Long offertime = Long.parseLong(args[2]);
				current_account = new WeiboAccount(username, password);
				current_account.setOffertime(offertime);
				System.out.println(current_account.toString());

				if (ctype == CommonResources.WEIBO_NORMAL_CRAWLER) {
					WBMobileUPClientFactory factory = new WBMobileUPClientFactory();
					try {
						client = factory.createHttpClient(
								current_account.getName(),
								current_account.getPassword(),
								CommonResources.WEIBO_NORMAL_CRAWLER);
					} catch (IOException e) {
						e.printStackTrace();
					}

					crawler_type = CommonResources.WEIBO_NORMAL_CRAWLER;
				} else if (ctype == CommonResources.WEIBO_INFO_CRAWLER) {
					WBMobileUPClientFactory factory = new WBMobileUPClientFactory();
					try {
						client = factory.createHttpClient(
								current_account.getName(),
								current_account.getPassword(),
								CommonResources.WEIBO_INFO_CRAWLER);
					} catch (IOException e) {
						e.printStackTrace();
					}

					crawler_type = CommonResources.WEIBO_INFO_CRAWLER;
				} else if (ctype == CommonResources.WEIBO_REPOST_CRAWLER) {
					crawler_type = CommonResources.WEIBO_REPOST_CRAWLER;
				} else if (ctype == CommonResources.WEIBO_TOPIC_CRAWLER) {
					crawler_type = CommonResources.WEIBO_TOPIC_CRAWLER;
				}
				sendRequest();

			} else if (type.equals("url")) {
				if (ctype != crawler_type) {
					current_task = text;

					TextMessage mes;
					try {
						mes = session.createTextMessage();
						mes.setStringProperty("type", "failure");
						mes.setStringProperty("host", hostname);
						mes.setIntProperty("crawl-type", crawler_type);
						mes.setText(current_account.toString());
						producer.send(mes);

						current_account = null;
						try {
							client.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						client = null;
					} catch (JMSException e) {
						e.printStackTrace();
					}

					crawler_type = ctype;
				} else {
					System.out.println(text);
					executeCatch(text);
				}
			} else if (type.equals("account")) {
				String[] args = text.split(" ");
				String username = args[0];
				String password = args[1];
				Long offertime = Long.parseLong(args[2]);
				current_account = new WeiboAccount(username, password);
				current_account.setOffertime(offertime);

				System.out.println("new account " + current_account);
				if (crawler_type == CommonResources.WEIBO_NORMAL_CRAWLER) {
					WBMobileUPClientFactory factory = new WBMobileUPClientFactory();
					try {
						client = factory.createHttpClient(
								current_account.getName(),
								current_account.getPassword(),
								CommonResources.WEIBO_NORMAL_CRAWLER);
					} catch (IOException e) {
						e.printStackTrace();
					}

					executeCatch(current_task);
					current_task = null;
				} else if (crawler_type == CommonResources.WEIBO_INFO_CRAWLER) {
					WBMobileUPClientFactory factory = new WBMobileUPClientFactory();
					try {
						client = factory.createHttpClient(
								current_account.getName(),
								current_account.getPassword(),
								CommonResources.WEIBO_INFO_CRAWLER);
					} catch (IOException e) {
						e.printStackTrace();
					}

					executeCatch(current_task);
					current_task = null;
				} else if (ctype == CommonResources.WEIBO_REPOST_CRAWLER) {
					crawler_type = CommonResources.WEIBO_REPOST_CRAWLER;
				} else if (ctype == CommonResources.WEIBO_TOPIC_CRAWLER) {
					crawler_type = CommonResources.WEIBO_TOPIC_CRAWLER;
				}
			} else if (type.equals("close")) {
				stop(0);
			}

		}
	}

	// 执行抓取动作
	public void executeCatch(String url) {
		System.out.println(url);
		try {
			Thread.sleep(9000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		if (crawler_type == CommonResources.WEIBO_NORMAL_CRAWLER) {
			if (url.contains("WEIBO")) {
				String id = null;
				try {
					id = WeiboTweetCrawler.catchTweet(client, url);
				} catch (IOException e1) {
					jobfailure(url);
					e1.printStackTrace();
					return;
				}
				try {
					String content = WeiboTweetParser.getContent(id);
					ApiDataSender.getSender().sendMessageToApi("weibotweet",
							content);
				} catch (IOException e) {
					System.out.println("tweetparser error");
					e.printStackTrace();
					sendRequest();
					return;
				}
			} else if (url.contains("FOLLOWERS")) {
				String id = null;
				try {
					id = WeiboFollowCrawler.catchFollow(client, url);
				} catch (IOException e1) {
					jobfailure(url);
					e1.printStackTrace();
					return;
				}
				try {
					String content = WeiboFollowParser.getContent(id);
					ApiDataSender.getSender().sendMessageToApi("weibofollow",
							content);
				} catch (IOException e) {
					System.out.println("followparser error");
					e.printStackTrace();
					sendRequest();
					return;
				}
			}
		} else if (crawler_type == CommonResources.WEIBO_INFO_CRAWLER) {
			String hpurl = WeiboMobileResources.WEIBO_MOBILE_HOMEPAGE_PREFIX
					+ url;
			String infourl = WeiboMobileResources.WEIBO_MOBILE_INFO_PREFIX
					+ url + WeiboMobileResources.WEIBO_MOBILE_INFO_SUFFIX;
			try {
				WeiboMobileHPCrawler.catchHomePage(client, hpurl);
				WeiboMobileInfoCrawler.catchInfoPage(client, infourl);
			} catch (IOException e) {
				jobfailure(url);
				e.printStackTrace();
			}
			int con = WeiboMobilePageValidator.valitePage("weibomhp.html");
			if (con == 2) {
				jobfailure(url);
				return;
			} else if (con == 0) {
				String[] data = WeiboMobileHPParser
						.parseHomepage("weibomhp.html");
				String content = WeiboMobileInfoParser.parseInfo(url, client,
						data);
				ApiDataSender.getSender()
						.sendMessageToApi("weiboinfo", content);
			}
		} else if (crawler_type == CommonResources.WEIBO_REPOST_CRAWLER) {

		} else if (crawler_type == CommonResources.WEIBO_TOPIC_CRAWLER) {

		}
		sendRequest();
	}

	// 抓取完毕请求后续任务
	private void sendRequest() {
		try {
			TextMessage mes = session.createTextMessage();
			mes.setStringProperty("type", "finish");
			mes.setStringProperty("host", hostname);
			mes.setIntProperty("crawl_type", crawler_type);
			mes.setText("");
			producer.send(mes);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	// 抓取失败后回送URL
	private void jobfailure(String url) {
		TextMessage mes;
		try {
			mes = session.createTextMessage();
			mes.setStringProperty("type", "failure");
			mes.setStringProperty("host", hostname);
			mes.setIntProperty("crawl-type", crawler_type);
			mes.setText(current_account.toString());
			producer.send(mes);

			current_account = null;
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			client = null;
		} catch (JMSException e) {
			e.printStackTrace();
		}
		current_task = url;
	}

	// 注册
	private void register() {
		try {
			TextMessage message = session.createTextMessage();
			message.setStringProperty("host", hostname);
			message.setStringProperty("type", "register");
			message.setText("");
			producer.send(message);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	// 关闭并释放资源
	private void shutdownGracefully() {
		if (client != null)
			try {
				client.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		try {
			producer.close();
			consumer.close();
			session.close();
			connection.close();
			consumer = null;
			session = null;
			producer_destination = null;
			consumer_destination = null;
			connection = null;
			factory = null;
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

}
