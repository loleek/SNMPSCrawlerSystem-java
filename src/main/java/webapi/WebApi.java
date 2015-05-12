package webapi;

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
/**
 * 
 * @author dk
 * 用于接收爬虫传送的数据，接收到数据后调用DataRecorder写入文件
 */
public class WebApi implements MessageListener {
	//建立到ActiveMQ broker的链接
	private ConnectionFactory factory = null;
	private Connection connection = null;
	private Session session = null;
	private Destination destination = null;
	private MessageConsumer consumer = null;

	public WebApi() {

	}
	/*
	 * 初始化并监听消息
	 */
	public void startAndListen() {
		factory = new ActiveMQConnectionFactory(WebApiResources.MQ_TCP_URL);
		try {
			connection = factory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			destination = session.createQueue(WebApiResources.API_QUEUE);
			consumer = session.createConsumer(destination);
			
			consumer.setMessageListener(this);
		} catch (JMSException e) {

		}
	}
	/*
	 * 确保资源正确关闭并释放
	 */
	public synchronized void shutdownGracefully() {
		try {
			consumer.close();
			session.close();
			connection.close();
			consumer = null;
			session = null;
			destination = null;
			connection = null;
			factory = null;
		} catch (JMSException e) {

		}

	}
	/**
	 * 处理接收到的消息，通过tag辨别数据类型
	 */
	public void onMessage(Message message) {
		if (message instanceof TextMessage) {
			TextMessage tm = (TextMessage) message;
			String tag = null;
			String text = null;
			try {
				tag = tm.getStringProperty("tag");
				text = tm.getText();
				
				if (tag != null && text != null) {
					if (tag.equals(WebApiResources.SHUTDOWN_COMMAND)) {
						DataRecorder.getRecorder().shutdownGracefully();
						shutdownGracefully();
					} else {
						DataRecorder.getRecorder().recordMessage(tag, text);
					}
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}

		}

	}

}
