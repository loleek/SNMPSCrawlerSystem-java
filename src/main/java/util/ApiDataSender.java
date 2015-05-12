package util;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import webapi.WebApiResources;
/**
 * 
 * @author dk
 *用于爬虫发送解析后的数据到主节点持久化
 *单例模式保证始终只有一个对象
 */
public class ApiDataSender {
	private static ApiDataSender sender = null;
	//建立到ActiveMQ broker的相关连接
	private ConnectionFactory factory=null;
	private Connection connection=null;
	private Session session=null;
	private Destination destination=null;
	private MessageProducer producer=null;
	

	private ApiDataSender() {
		factory=new ActiveMQConnectionFactory(WebApiResources.MQ_TCP_URL);
		try {
			connection=factory.createConnection();
			connection.start();
			session=connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
			destination=session.createQueue(WebApiResources.API_QUEUE);
			producer=session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		} catch (JMSException e) {
			
		}
	}
	/**
	 * 
	 * @param tag 标记数据类型
	 * @param messagecontent 需要持久化的数据内容
	 */
	public synchronized void sendMessageToApi(String tag,String messagecontent){
		try {
			TextMessage message=session.createTextMessage();
			message.setStringProperty("tag", tag);
			message.setText(messagecontent);
			producer.send(message);
			session.commit();
		} catch (JMSException e) {
			
		}
	}
	/**
	 * 用于正确关闭并释放所有资源
	 */
	public synchronized void shutdownGracefully(){
		try {
			producer.close();
			session.close();
			connection.close();
			producer=null;
			session=null;
			destination=null;
			connection=null;
			factory=null;
		} catch (JMSException e) {
			
		}
		
	}

	public static ApiDataSender getSender() {
		if (sender == null) {
			synchronized (ApiDataSender.class) {
				if(sender==null)
					sender = new ApiDataSender();
			}
		}
		return sender;
	}
}
