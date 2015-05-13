package weibo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

import org.apache.activemq.ActiveMQConnectionFactory;

import util.CommonResources;
import core.Manager;

/**
 * 
 * @author loleek 微博爬虫master-manager
 */
public class WeiboCrawlerMasterManager implements Manager, MessageListener {

	private ConnectionFactory factory = null;
	private Connection connection = null;
	private Session session = null;
	private Destination destination = null;
	private MessageConsumer consumer = null;

	private Map<String, MessageProducer> weibo_workers = null;

	private String hostname = null;
	private Integer crawler_type = CommonResources.WEIBO_NORMAL_CRAWLER;

	private WeiboCatchListManager catchListManager = null;
	private WeiboAccountManager accountManager = null;

	private boolean waitforclose = false;

	public WeiboCrawlerMasterManager(String hostname, Integer crawler_type) {
		this.hostname = hostname;
		factory = new ActiveMQConnectionFactory(CommonResources.MQ_TCP_URL);
		try {
			connection = factory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			destination = session
					.createQueue(CommonResources.WEIBO_MASTER_LISTEN_QUEUE);
			consumer = session.createConsumer(destination);

			weibo_workers = new HashMap<String, MessageProducer>();
		} catch (JMSException e) {
			e.printStackTrace();
		}

		this.crawler_type = crawler_type;

	}

	// 启动并监听
	public void start() {
		try {
			consumer.setMessageListener(this);
			accountManager = WeiboAccountManager.getAccountManager();
			catchListManager = new WeiboCatchListManager(this, crawler_type);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	// 若运行中的爬虫全部关闭则关闭主节点
	public void stop(int level) {
		if (level == 0) {
			waitforclose = true;
			if (weibo_workers.size() == 0)
				shutdownGracefully();
		} else {
			
			try {
				TextMessage mes = session.createTextMessage();
				mes.setIntProperty("carwl-type", crawler_type);
				mes.setStringProperty("type", "close");
				mes.setStringProperty("host", hostname);
				
				Set<String> hosts = weibo_workers.keySet();
				for (String host : hosts) {
					weibo_workers.get(host).send(mes);
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
			
			shutdownGracefully();
		}
	}

	public void onMessage(Message message) {
		if (message instanceof TextMessage) {
			TextMessage tm = (TextMessage) message;
			String type = null;
			String host = null;
			String text = null;

			try {
				type = tm.getStringProperty("type");
				host = tm.getStringProperty("host");
				text = tm.getText();
			} catch (JMSException e) {
				e.printStackTrace();
			}
			// register 收到slave注册消息
			// finish slave上抓取完成请求新的url
			// failure 抓取失败
			if (type.equals("register")) {
				try {
					Destination dest = session.createQueue(host + "_"
							+ CommonResources.WEIBO_SLAVE_LISTEN_QUEUE);
					MessageProducer producer = session.createProducer(dest);
					weibo_workers.put(host, producer);
				} catch (JMSException e) {
					e.printStackTrace();
				}
				if (weibo_workers.size() == CommonResources.WEIBO_WORKER_SIZE) {
					schedule();
				}
			} else if (type.equals("finish")) {
				if (waitforclose) {
					try {
						TextMessage mes = session.createTextMessage();
						mes.setIntProperty("carwl-type", crawler_type);
						mes.setStringProperty("type", "close");
						mes.setStringProperty("host", hostname);

						MessageProducer producer = weibo_workers.get(host);
						producer.send(mes);
						weibo_workers.remove(host);

						stop(0);
					} catch (JMSException e) {
						e.printStackTrace();
					}
				} else {
					String url = catchListManager.getUrl();

					try {
						TextMessage mes = session.createTextMessage();
						mes.setIntProperty("crawl-type", crawler_type);
						mes.setStringProperty("type", "url");
						mes.setStringProperty("host", hostname);
						mes.setText(url);
						MessageProducer producer = weibo_workers.get(host);
						producer.send(mes);
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}

			} else if (type.equals("failure")) {
				try {
					String[] args = text.split(" ");
					Integer ctype = message.getIntProperty("crawl-type");

					WeiboAccount account = new WeiboAccount(args[0], args[1]);
					account.setOffertime(Long.parseLong(args[2]));
					accountManager.submitWrongAccount(account);
					if (args.length > 3) {
						String url = args[3];
						catchListManager.urlFailure(url, ctype);
					}

					WeiboAccount newaccount = accountManager.getAccount();
					TextMessage mes = session.createTextMessage();

					mes.setStringProperty("type", "account");
					mes.setIntProperty("crawl-type", crawler_type);
					mes.setStringProperty("host", hostname);
					mes.setText(newaccount.toString());
					MessageProducer producer = weibo_workers.get(host);
					producer.send(mes);
				} catch (JMSException e) {
					e.printStackTrace();
				}

			}
		}
	}

	// 启动爬虫系统
	private void schedule() {
		if (crawler_type == CommonResources.WEIBO_NORMAL_CRAWLER) {
			try {
				TextMessage message = session.createTextMessage();
				message.setStringProperty("type", "command");
				message.setStringProperty("host", hostname);
				message.setIntProperty("crawl-type",
						CommonResources.WEIBO_NORMAL_CRAWLER);

				Set<String> hosts = weibo_workers.keySet();
				for (String host : hosts) {
					WeiboAccount newaccount = accountManager.getAccount();
					message.setText(newaccount.toString());
					weibo_workers.get(host).send(message);
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}

		} else if (crawler_type == CommonResources.WEIBO_INFO_CRAWLER) {
			try {
				TextMessage message = session.createTextMessage();
				message.setStringProperty("type", "command");
				message.setStringProperty("host", hostname);
				message.setIntProperty("crawl-type",
						CommonResources.WEIBO_INFO_CRAWLER);

				Set<String> hosts = weibo_workers.keySet();
				for (String host : hosts) {
					WeiboAccount newaccount = accountManager.getAccount();
					message.setText(newaccount.toString());
					weibo_workers.get(host).send(message);
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
		} else if (crawler_type == CommonResources.WEIBO_REPOST_CRAWLER) {
			try {
				TextMessage message = session.createTextMessage();
				message.setStringProperty("type", "command");
				message.setStringProperty("host", hostname);
				message.setIntProperty("crawl-type",
						CommonResources.WEIBO_REPOST_CRAWLER);

				Set<String> hosts = weibo_workers.keySet();
				for (String host : hosts) {
					WeiboAccount newaccount = accountManager.getAccount();
					message.setText(newaccount.toString());
					weibo_workers.get(host).send(message);
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
		} else if (crawler_type == CommonResources.WEIBO_TOPIC_CRAWLER) {
			try {
				TextMessage message = session.createTextMessage();
				message.setStringProperty("type", "command");
				message.setStringProperty("host", hostname);
				message.setIntProperty("crawl-type",
						CommonResources.WEIBO_TOPIC_CRAWLER);

				Set<String> hosts = weibo_workers.keySet();
				for (String host : hosts) {
					WeiboAccount newaccount = accountManager.getAccount();
					message.setText(newaccount.toString());
					weibo_workers.get(host).send(message);
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

	// 切换爬虫类型
	public void changeCrawlerType(Integer crawler_type) {
		this.crawler_type = crawler_type;
		catchListManager.changeType(crawler_type);
	}

	// 更新爬虫类型
	public void updateCrawlerType(Integer crawler_type) {
		this.crawler_type = crawler_type;
	}

	public Integer getCrawlerType() {
		return crawler_type;
	}

	// 关闭并释放资源
	public void shutdownGracefully() {
		try {
			for (MessageProducer producer : weibo_workers.values())
				producer.close();
			accountManager.close();

			catchListManager.persist();
			consumer.close();
			session.close();
			connection.close();
			consumer = null;
			session = null;
			destination = null;
			connection = null;
			factory = null;
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

}
