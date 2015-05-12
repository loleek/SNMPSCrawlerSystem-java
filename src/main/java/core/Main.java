package core;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import util.CommonResources;
import webapi.WebApi;
import webapi.WebApiResources;
/**
 * 
 * @author loleek
 *启动主程序
 *可选参数为三个
 *1 master start启动master shutdown 关闭master 
 *2 slave start启动slave
 *3 webapi start启动webapi shutdown 关闭webapi
 */
public class Main {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Args : class operation");
			System.exit(0);
		}

		String obj = args[0];
		String operation = args[1];

		if (obj.equals("master")) {
			if (operation.equals("start")) {
				MasterBackend master = new MasterBackend();
				master.startWork();
			} else {
				ConnectionFactory factory = null;
				Connection connection = null;
				Session session = null;
				Destination destination = null;
				MessageProducer producer = null;
				TextMessage message = null;

				factory = new ActiveMQConnectionFactory(
						CommonResources.MQ_TCP_URL);
				try {
					connection = factory.createConnection();
					connection.start();
					session = connection.createSession(false,
							Session.AUTO_ACKNOWLEDGE);
					destination = session
							.createQueue(CommonResources.SLAVE_TO_MASTER_MAIN_QUEUE);
					producer = session.createProducer(destination);
					message = session.createTextMessage();
					message.setText("shutdown");
					
					producer.send(message);
					producer.close();
					session.close();
					connection.close();
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		} else if (obj.equals("slave")) {
			if (operation.equals("start")) {
				SlaveBackend slave = new SlaveBackend();
				slave.startAndRegist();
			}
		} else if (obj.equals("webapi")) {
			if (operation.equals("start")) {
				WebApi api = new WebApi();
				api.startAndListen();
			} else {
				ConnectionFactory factory = null;
				Connection connection = null;
				Session session = null;
				Destination destination = null;
				MessageProducer producer = null;
				TextMessage message = null;

				factory = new ActiveMQConnectionFactory(
						CommonResources.MQ_TCP_URL);
				try {
					connection = factory.createConnection();
					connection.start();
					session = connection.createSession(false,
							Session.AUTO_ACKNOWLEDGE);
					destination = session
							.createQueue(WebApiResources.API_QUEUE);
					producer = session.createProducer(destination);
					message = session.createTextMessage();
					message.setStringProperty("tag",
							WebApiResources.SHUTDOWN_COMMAND);
					message.setText("shutdown");
					producer.send(message);
					
					producer.close();
					session.close();
					connection.close();
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
