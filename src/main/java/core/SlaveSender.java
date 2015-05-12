package core;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
/**
 * 
 * @author loleek
 * 用于将子节点上的数据发送到master上
 */
public class SlaveSender{

	private String hostname = null;
	private String queue_name = null;
	private Connection connection = null;
	private Session session = null;
	private Destination destination = null;
	private MessageProducer producer = null;

	public SlaveSender(Connection connection, String hostname, String queue_name) {
		this.connection = connection;
		this.hostname = hostname;
		this.queue_name = queue_name;

		try {
			session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			destination = session.createQueue(this.queue_name);
			producer = session.createProducer(destination);
			
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

	public synchronized void shutdownGracegully() {
		try {
			producer.close();
			session.close();
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void sendRegisterMessage(){
		try {
			TextMessage message=session.createTextMessage();
			message.setStringProperty("host", hostname);
			message.setStringProperty("type", "register");
			message.setText("");
			producer.send(message);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
