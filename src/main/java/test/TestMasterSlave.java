package test;

import core.MasterBackend;
import core.SlaveBackend;

public class TestMasterSlave {
	public static void main(String[] args) {
		MasterBackend mb=new MasterBackend();
		mb.startWork();
		SlaveBackend sb=new SlaveBackend();
		sb.startAndRegist();
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		sb.shutdownGracefully();
		mb.shutdownGracefully();
	}
}
