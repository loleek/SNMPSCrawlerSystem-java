package test;

import webapi.DataRecorder;
/**
 * 
 * @author dk
 *测试能否正常写数据 
 */
public class TestWriter {
	public static void main(String[] args) {
		DataRecorder.getRecorder().recordMessage("test", "this is a test");
	}
}
