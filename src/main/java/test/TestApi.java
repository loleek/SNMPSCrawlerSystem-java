package test;

import webapi.WebApi;
/**
 * 
 * @author dk
 *测试API工作情况，尤其是对文件的切换操作
 */
public class TestApi {
	public static void main(String[] args) {
		WebApi api=new WebApi();
		api.startAndListen();
	}
}
