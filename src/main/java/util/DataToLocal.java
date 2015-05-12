package util;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author dk
 * 
 *将HttpEntity中的输入流本地化存储到指定文件中
 */
public class DataToLocal {
	/**
	 * 
	 * @param in HttpEntity中的网络字节输入流
	 * @param fileName 要存储的文件名
	 * @exception 可能会存在服务器断开导致的持续等待异常
	 */
	public static void localize(InputStream in, String fileName) {
		File file=new File(fileName);
		if(file.exists()){
			file.delete();
		}
		if (in != null) {
			FileOutputStream out = null;
			byte[] b = new byte[8192];
			int len = -1;
			try {
				out = new FileOutputStream(file);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			try {
				while ((len = in.read(b)) != -1) {
					out.write(b, 0, len);
					out.flush();
				}
				out.flush();
				out.close();
				in.close();
			} catch (IOException e) {
				System.out.println("Data To Local Failed!");
				e.printStackTrace();
			}
		}
	}
}
