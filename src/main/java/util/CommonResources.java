package util;

import java.io.File;

public class CommonResources {
	public static String MQ_TCP_URL = "tcp://49.122.47.30:61616";
	public static String SLAVE_TO_MASTER_MAIN_QUEUE = "slave_report_queue";
	public static String MASTER_TO_SLAVE_MAIN_QUEUE = "master_command_queue";

	public static String WEIBO_MASTER_LISTEN_QUEUE = "weibo_master_listen_queue";
	public static String WEIBO_SLAVE_LISTEN_QUEUE = "weibo_slave_listen_queue";
	public static int WEIBO_WORKER_SIZE = 8;
	public static String WEIBO_ACCOUNT_FILE_LOCATION = "weiboconf"
			+ File.separatorChar + "account.txt";
	public static String WEIBO_WRONG_ACCOUNT_FILE_LOCATION = "weiboconf"
			+ File.separatorChar + "wrong-account.txt";
	public static String WEIBO_CATCH_LIST_LOCATION = "weibotask"
			+ File.separatorChar + "userids.txt";
	public static String WEIBO_CATCH_LIST_PERSIST_LOCATION = "weibotask"
			+ File.separatorChar + "persist.txt";
	public static String WEIBO_EXTRA_TASK_LOCATION = "weibotask"
			+ File.separatorChar + "extratask.txt";
	public static String WEIBO_ID_INFO_LOCATION = "weibotask"
			+ File.separatorChar + "idinfo.txt";
	public static Integer WEIBO_NORMAL_CRAWLER = 0;
	public static Integer WEIBO_INFO_CRAWLER = 1;
	public static Integer WEIBO_REPOST_CRAWLER = 2;
	public static Integer WEIBO_TOPIC_CRAWLER = 3;
}
