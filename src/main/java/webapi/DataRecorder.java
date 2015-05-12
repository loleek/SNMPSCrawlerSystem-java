package webapi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import util.FileNameGenerator;
/**
 * 
 * @author dk
 *将接受到的数据写入文件
 *单例模式
 */
public class DataRecorder {

	private static DataRecorder recorder = null;
	//写入文件索引集合
	private Hashtable<String, FileWriter> writerMap = null;
	//定时器
	private Timer timer = null;

	private DataRecorder() {
		writerMap = new Hashtable<String, FileWriter>();
		//定时器以daemon模式运行
		timer = new Timer(true);
	}

	private void startSchedule(){
		TimerTask task=new BackupTimerTask(this.writerMap);
		//每两小时调度一次保证
		timer.schedule(task, WebApiResources.BACKUP_TIME, WebApiResources.BACKUP_TIME);
	}
	/**
	 * 将数据写入文件
	 * @param tag 数据类型
	 * @param text 文本内容
	 */
	public void recordMessage(String tag, String text) {
		//对writerMap加锁保证不会和定时器任务产生冲突导致writer写入文件被关闭
		synchronized (writerMap) {
			if (writerMap.containsKey(tag)) {
				try {
					FileWriter writer = writerMap.get(tag);
					writer.write(text+"\n");
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				String fileName = FileNameGenerator.generateFilename(tag);
				
				File file = new File(fileName);
				try {
					//文件以追加模式写入
					FileWriter writer = new FileWriter(file,Boolean.TRUE);
					writerMap.put(tag, writer);
					writer.write(text+"\n");
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static DataRecorder getRecorder() {
		if (recorder == null) {
			synchronized (DataRecorder.class) {
				if (recorder == null) {
					recorder = new DataRecorder();
					recorder.startSchedule();
				}
			}
		}
		return recorder;
	}
	//保证正确关闭并释放所有资源
	public synchronized void shutdownGracefully() {
		Set<String> set = writerMap.keySet();
		for (String tag : set) {
			FileWriter writer = writerMap.get(tag);
			try {
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		timer.cancel();
		writerMap.clear();
		writerMap=null;
	}

}
