package webapi;
/**
 * 保存WebApi中使用到的字段
 * @author dk
 *
 */
public class WebApiResources {
	//ActiveMQ 的链接地址
	public static String MQ_TCP_URL="tcp://49.122.47.30:61616";
	//WebApi监听的数据队列
	public static String API_QUEUE="api_data_queue";
	//关闭WebApi
	public static String SHUTDOWN_COMMAND="shutdown";
	//文件切换时间(默认是两个小时)
	public static long BACKUP_TIME=2*60*60*1000;
}
