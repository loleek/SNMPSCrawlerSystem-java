package core;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
import weibo.WeiboCralwlerSlaveManager;
/**
 * 
 * @author loleek
 * 子节点守护进程
 */
public class SlaveBackend implements MessageListener {
	//建立到ActiveMQ的连接
	private ConnectionFactory factory = null;
	private Connection connection = null;
	private Session session = null;
	private Destination destination = null;
	private MessageConsumer consumer = null;

	private String hostname = null;
	//用于发送数据到master
	private SlaveSender sender = null;
	//微博爬虫manager
	private WeiboCralwlerSlaveManager wcsmanager = null;

	public SlaveBackend() {
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		factory = new ActiveMQConnectionFactory(CommonResources.MQ_TCP_URL);
		try {
			connection = factory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			destination = session.createQueue(hostname + "_"
					+ CommonResources.MASTER_TO_SLAVE_MAIN_QUEUE);
			consumer = session.createConsumer(destination);
			sender = new SlaveSender(connection, hostname,
					CommonResources.SLAVE_TO_MASTER_MAIN_QUEUE);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	//启动并注册
	public void startAndRegist() {
		try {
			consumer.setMessageListener(this);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		sender.sendRegisterMessage();
		wcsmanager = new WeiboCralwlerSlaveManager(hostname);
		wcsmanager.start();
	}
	//关闭并释放资源
	public void shutdownGracefully() {
		try {
			sender.shutdownGracegully();
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

			if (type.equals("shutwon")) {
				shutdownGracefully();
			} else {
				System.out.println(type + " " + host + " " + text);
			}
		}
	}

}
