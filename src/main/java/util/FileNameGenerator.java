package util;

import java.util.Calendar;
/**
 * 
 * @author dk
 *生成文件名工具类 文件名为 
 *数据类型-年-月-日-时.txt
 */
public class FileNameGenerator {
	public static String generateFilename(String tag){
		Calendar calendar=Calendar.getInstance();
		int year=calendar.get(Calendar.YEAR);
		int month=calendar.get(Calendar.MONTH)+1;
		int date=calendar.get(Calendar.DATE);
		int hour=calendar.get(Calendar.HOUR_OF_DAY);
//		int mintue=calendar.get(Calendar.MINUTE);
		return tag+"-"+year+"-"+month+"-"+date+"-"+hour+".txt";
	}
}
