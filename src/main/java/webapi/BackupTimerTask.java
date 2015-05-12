package webapi;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;
import java.util.TimerTask;

/**
 * 
 * @author dk 用于切换写入文件，默认为2小时切换一次，将所有现有文件writer索引清空
 */
public class BackupTimerTask extends TimerTask {
	// 对现有writer集合的引用
	private Hashtable<String, FileWriter> writers = null;

	public BackupTimerTask(Hashtable<String, FileWriter> writers) {
		this.writers = writers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 * 用于切换写入文件，将现有集合中所有文件索引缓存清空并关闭，再将集合清空以保证文件不为空(否则可能存在tag存在导致长时间创建文件却不写入)
	 */
	@Override
	public void run() {
		synchronized (writers) {
			Set<String> set = writers.keySet();
			for (String tag : set) {
				FileWriter writer = writers.get(tag);

				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			writers.clear();
		}

	}
}
