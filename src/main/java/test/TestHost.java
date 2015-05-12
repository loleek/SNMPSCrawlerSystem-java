package test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestHost {
	public static void main(String[] args) throws UnknownHostException {
		InetAddress ip=InetAddress.getLocalHost();
		System.out.println(ip.getHostName());
	}
}
