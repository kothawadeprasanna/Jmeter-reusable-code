package companyname.lt.chat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Sujit Zingade on 5/15/2017.
 */
public class RunContext {
	private String threadNumber;
	private String protocol;
	private String serverName;
	private String contextRoot;
	private Util utilHandle;
	private ReentrantLock lock;
	private Condition chatEndCondition;
	private Condition interThreadCondition;
	private Map interThreadSharedMap = new HashMap();
	private Lock sharedDataLock = new ReentrantLock();
	private String proxyServer;

	public boolean isProxyServerEnabled() {
		if (this.proxyServer != null && !"".equals(this.proxyServer.trim()))
			return true;
		return false;
	}

	public String getProxyServer() {
		return proxyServer;
	}

	public void setProxyServer(String proxyServer) {
		this.proxyServer = proxyServer;
	}

	private int proxyPort;

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public Condition getInterThreadCondition() {
		return interThreadCondition;
	}

	public void setInterThreadCondition(Condition interThreadCondition) {
		this.interThreadCondition = interThreadCondition;
	}

	public Condition getChatEndCondition() {
		return chatEndCondition;
	}

	public void setChatEndCondition(Condition condition) {
		this.chatEndCondition = condition;
	}

	public String getThreadNumber() {
		return threadNumber;
	}

	public void setThreadNumber(String threadNumber) {
		this.threadNumber = threadNumber;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getContextRoot() {
		return contextRoot;
	}

	public void setContextRoot(String contextRoot) {
		this.contextRoot = contextRoot;
	}

	public Util getUtilHandle() {
		return utilHandle;
	}

	public void setUtilHandle(Util utilHandle) {
		this.utilHandle = utilHandle;
	}

	public ReentrantLock getLock() {
		return lock;
	}

	public void setLock(ReentrantLock lock) {
		this.lock = lock;
	}

	public void setSharedData(String key, String value) {
		sharedDataLock.lock();
		interThreadSharedMap.put(key, value);
		sharedDataLock.unlock();
	}

	public void removeFormSharedData(String key) {
		sharedDataLock.lock();
		if (interThreadSharedMap.containsKey(key))
			interThreadSharedMap.remove(key);
		sharedDataLock.unlock();
	}

	public String getSharedData(String key) {
		sharedDataLock.lock();
		String retVal = (String) interThreadSharedMap.get(key);
		sharedDataLock.unlock();
		return retVal;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ThreadNumber :").append(threadNumber).append("][Protocol :").append(protocol)
				.append("][serverName :").append(serverName).append("][ContextRoot :").append(contextRoot).append("]");
		if (interThreadSharedMap != null && interThreadSharedMap.size() > 0) {
			Iterator it = interThreadSharedMap.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				String value = (String) interThreadSharedMap.get(key);
				sb.append("[").append(key).append(" :").append(value).append("]");
			}
		}

		return sb.toString();
	}
}
