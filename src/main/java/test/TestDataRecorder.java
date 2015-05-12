package test;

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
 * 测试文件记录和切换是否正常，按分钟或者秒切换 先运行TestApi
 * 
 */
public class TestDataRecorder {
	public static void main(String[] args) throws JMSException, InterruptedException {
		ConnectionFactory fac = new ActiveMQConnectionFactory(
				WebApiResources.MQ_TCP_URL);
		Connection conn = fac.createConnection();
		conn.start();
		Session ses = conn
				.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
		Destination dest = ses.createQueue(WebApiResources.API_QUEUE);
		MessageProducer producer = ses.createProducer(dest);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		int i = 0;
		while (i < 500) {
			TextMessage mess = ses.createTextMessage();
			mess.setStringProperty("tag","test");
			mess.setText("this is the"+i+" test ");
			producer.send(mess);
			
			i++;
			System.out.println(i);
			Thread.sleep((long)(Math.random()*1000));
		}
		TextMessage mess = ses.createTextMessage();
		mess.setStringProperty("tag", WebApiResources.SHUTDOWN_COMMAND);
		mess.setText("shutdown gracefully!!!");
		producer.send(mess);
		
		producer.close();
		ses.close();
		conn.close();
	}
}
