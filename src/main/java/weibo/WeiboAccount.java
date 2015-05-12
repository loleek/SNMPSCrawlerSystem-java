package weibo;
/**
 * 
 * @author loleek
 * 用于记录微博账号
 */
public class WeiboAccount {
	//微博账号用户名
	private String name = null;
	//微博账号密码
	private String password = null;
	//账号推送时间
	private long offertime = 0L;

	public WeiboAccount(String name, String password) {
		this.name = name;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getOffertime() {
		return offertime;
	}

	public void setOffertime(long offertime) {
		this.offertime = offertime;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		WeiboAccount account = (WeiboAccount) obj;
		return this.getName().equals(account.getName());
	}

	@Override
	public String toString() {
		return name+" "+password+" "+offertime;
	}

}
