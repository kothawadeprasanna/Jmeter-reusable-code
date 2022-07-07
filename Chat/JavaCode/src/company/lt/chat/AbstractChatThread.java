package companyname.lt.chat;

import java.text.SimpleDateFormat;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractChatThread implements Runnable {
	protected RunContext context;
	protected SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	public static String ridPlaceHolder = "#__RID_PLACEHOLDER__#";

	public AbstractChatThread() {
	}

	public AbstractChatThread(RunContext context) {
		this.context = context;
	}

	public String getServerName() {
		return context.getServerName();
	}

	public String getProtocol() {
		return context.getProtocol();
	}

	public String getThreadNumber() {
		return context.getThreadNumber();
	}

	public String getContextRoot() {
		return context.getContextRoot();
	}

	public Util getUtilHandle() {
		return context.getUtilHandle();
	}

	public Condition getChatEndCondition() {
		return context.getChatEndCondition();
	}

	public Condition getInterThreadCondition() {
		return context.getInterThreadCondition();
	}

	public ReentrantLock getLock() {
		return context.getLock();
	}
}