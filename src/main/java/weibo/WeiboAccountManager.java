package weibo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import util.CommonResources;
/**
 * 
 * @author loleek
 * 用于管理微博账号
 */
public class WeiboAccountManager {
	private static WeiboAccountManager manager = null;

	private Timer timer = null;
	//保存未使用账号
	private ConcurrentLinkedQueue<WeiboAccount> unusequeue = null;
	//保存已使用账号
	private ConcurrentLinkedQueue<WeiboAccount> usedqueue = null;
	//保存异常账号
	private ConcurrentLinkedQueue<WeiboAccount> wrongaccount = null;

	private WeiboAccountManager() {
		timer = new Timer(true);
		unusequeue = new ConcurrentLinkedQueue<WeiboAccount>();
		usedqueue = new ConcurrentLinkedQueue<WeiboAccount>();
		wrongaccount = new ConcurrentLinkedQueue<WeiboAccount>();
	}

	public static WeiboAccountManager getAccountManager() {
		if (manager == null) {
			synchronized (WeiboAccountManager.class) {
				if (manager == null) {
					manager = new WeiboAccountManager();
					WeiboAccountUpdateTask task = new WeiboAccountUpdateTask(
							manager.unusequeue, manager.usedqueue,
							manager.wrongaccount);
					manager.timer.schedule(task, 0, 24 * 60 * 60 * 1000);
				}
			}
		}
		return manager;
	}
	//获得一个可用账号
	public WeiboAccount getAccount() {
		WeiboAccount account = unusequeue.poll();
		if (account == null) {
			while (usedqueue.peek() != null)
				unusequeue.add(usedqueue.poll());
			account = unusequeue.poll();
			if (account != null)
				account.setOffertime(System.currentTimeMillis());
		} else {
			account.setOffertime(System.currentTimeMillis());
		}
		return account;
	}
	/**
	 * 用于提交一个异常账号
	 * @param account
	 */
	public void submitWrongAccount(WeiboAccount account) {
		long currentTime = System.currentTimeMillis();
		long offerTime = account.getOffertime();
		if (currentTime - offerTime < 5 * 60 * 1000)
			wrongaccount.add(account);
		else
			usedqueue.add(account);
	}

	public void close() {
		timer.cancel();
	}
	//每24小时刷新一次账号，从account.txt中读取账号
	static class WeiboAccountUpdateTask extends TimerTask {

		private ConcurrentLinkedQueue<WeiboAccount> unusequeue = null;
		private ConcurrentLinkedQueue<WeiboAccount> usedqueue = null;
		private ConcurrentLinkedQueue<WeiboAccount> wrongaccount = null;

		public WeiboAccountUpdateTask(
				ConcurrentLinkedQueue<WeiboAccount> unusequeue,
				ConcurrentLinkedQueue<WeiboAccount> usedqueue,
				ConcurrentLinkedQueue<WeiboAccount> wrongaccount) {
			this.unusequeue = unusequeue;
			this.usedqueue = usedqueue;
			this.wrongaccount = wrongaccount;
		}

		public void run() {
			File file = new File(CommonResources.WEIBO_ACCOUNT_FILE_LOCATION);
			BufferedReader br=null;;
			try {
				br = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			if (file.exists()) {
				if (unusequeue.isEmpty() && usedqueue.isEmpty()
						&& wrongaccount.isEmpty()) {
					try {
						String line = null;
						while ((line = br.readLine()) != null) {
							String[] sa = line.split(" ");
							WeiboAccount account = new WeiboAccount(sa[0],
									sa[1]);
							unusequeue.add(account);
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} 
				} else {
					try {
						String line = null;
						while ((line = br.readLine()) != null) {
							String[] sa = line.split(" ");
							WeiboAccount account = new WeiboAccount(sa[0],
									sa[1]);
							if (!unusequeue.contains(account)
									&& !usedqueue.contains(account)
									&& !wrongaccount.contains(account)) {
								unusequeue.add(account);
							}
						}
						File wrongfile = new File(
								CommonResources.WEIBO_WRONG_ACCOUNT_FILE_LOCATION);
						PrintWriter out = new PrintWriter(wrongfile);
						Iterator<WeiboAccount> ite = wrongaccount.iterator();
						while (ite.hasNext()) {
							WeiboAccount wa = ite.next();
							out.println(wa.getName() + " " + wa.getPassword());
							out.flush();
						}
						out.flush();
						out.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
