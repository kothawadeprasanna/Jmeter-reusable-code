package companyname.lt.chat;

import java.util.ArrayList;
import java.util.List;

public class Response {

	boolean stopSessionStats;
	String threadNumber;
	String responseType;
	String sid;
	String agentName;
	List<Attachment> attachments = null;
	boolean agentMessageCompletion;

	public boolean getAgentMessageCompletion() {
		return agentMessageCompletion;
	}

	public void setAgentMessageCompletion(boolean agentMessageCompletion) {
		this.agentMessageCompletion = agentMessageCompletion;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public Response(String threadNumber, String sid) {
		super();
		this.threadNumber = threadNumber;
		this.sid = sid;
	}

	public boolean isStopSessionStats() {
		return stopSessionStats;
	}

	public void setStopSessionStats(boolean stopSessionStats) {
		this.stopSessionStats = stopSessionStats;
	}

	public String getThreadNumber() {
		return threadNumber;
	}

	public void setThreadNumber(String threadNumber) {
		this.threadNumber = threadNumber;
	}

	public String getResponseType() {
		return responseType;
	}

	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	public void addAttachmentToResponse(Attachment attach) {
		if (attachments == null)
			attachments = new ArrayList();
		attachments.add(attach);
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public boolean isChatTerminated() {
		return "terminate".equals(responseType);
	}

}
