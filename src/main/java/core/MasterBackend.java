package core;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import util.CommonResources;
import weibo.WeiboCrawlerMasterManager;
/**
 * 
 * @author loleek
 * 主节点守护进程，用于控制整个系统
 */
public class MasterBackend implements MessageListener {
	//建立到ActiveMQ的连接
	private ConnectionFactory factory = null;
	private Connection connection = null;
	private Session session = null;
	private Destination destination = null;
	private MessageConsumer consumer = null;

	private String hostname = null;
	//用于保存已经连接上的slave
	private Map<String, MasterSender> senderMap = null;
	//微博爬虫manager
	private WeiboCrawlerMasterManager wcmmanager = null;

	public MasterBackend() {
		factory = new ActiveMQConnectionFactory(CommonResources.MQ_TCP_URL);
		try {
			connection = factory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			destination = session
					.createQueue(CommonResources.SLAVE_TO_MASTER_MAIN_QUEUE);
			consumer = session.createConsumer(destination);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		senderMap = new HashMap<String, MasterSender>();
	}
	//监听并1启动微博manager
	public void startWork() {
		try {
			consumer.setMessageListener(this);
			wcmmanager = new WeiboCrawlerMasterManager(hostname,
					CommonResources.WEIBO_NORMAL_CRAWLER);
			wcmmanager.start();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	//关闭并释放资源
	public void shutdownGracefully() {
		try {
			Set<String> hosts = senderMap.keySet();
			for (String host : hosts)
				senderMap.get(host).shutdownGracegully();
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
			//分支包括子节点注册或者关闭命令
			if (type.equals("register")) {
				MasterSender sender = new MasterSender(connection, hostname,
						host + "_" + CommonResources.MASTER_TO_SLAVE_MAIN_QUEUE);
				senderMap.put(host, sender);
				System.out.println(host + "registed!!!");
				sender.sendAck();
			} else if (text.equals("shutdown")) {
				for (MasterSender sender : senderMap.values())
					sender.sendShutdown();
				shutdownGracefully();
				
			}
		}
	}

}
