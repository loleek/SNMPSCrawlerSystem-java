package test;

import java.util.Calendar;

/**
 * 
 * @author dk
 *测试日历类
 */
public class TestDate {
	public static void main(String[] args) {
		Calendar cal=Calendar.getInstance();
		System.out.println(cal.get(Calendar.YEAR));
		System.out.println(cal.get(Calendar.MONTH));
		System.out.println(cal.get(Calendar.DATE));
		System.out.println(cal.get(Calendar.HOUR_OF_DAY));
		System.out.println(cal.get(Calendar.MINUTE));
		
	}
}
