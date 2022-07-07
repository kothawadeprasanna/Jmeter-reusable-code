package companyname.lt.chat;

import java.text.SimpleDateFormat;

public class FuturePollTask implements Runnable {

	// task status 0 for incomplete 1 for complete
	private int taskStatus;
	private String sid;
	private RunContext context;
	private boolean pollReqSent;
	private SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	public FuturePollTask(int taskStatus, String sid, RunContext context) {
		super();
		this.taskStatus = taskStatus;
		this.sid = sid;
		this.context = context;
		this.pollReqSent = false;
	}

	public void cancelTask() {
		this.taskStatus = 1;
	}

	@Override
	public void run() {
		String xml = null;
		String custSendMsgURL_EP = null;
		String chatQueueId = null;
		String custName = null;
		boolean isDebug = Boolean.parseBoolean(context.getSharedData("is_debug_run"));

		if (taskStatus != 1) {

			// Send the POll
			if (isDebug)
				System.out.println("AgentName :" + context.getSharedData("agent_assigned") + "Customer: ThreadNumber:"
						+ context.getThreadNumber() + " Time" + sd.format(System.currentTimeMillis())
						+ " Sending Blank Poll To release the message poll");
			chatQueueId = context.getSharedData("v_chat_queue_id");
			custName = context.getSharedData("v_cust_name");
			custSendMsgURL_EP = context.getProtocol() + "://" + context.getServerName() + "/" + context.getContextRoot()
					+ "/companyname/chat/entrypoint?entryPointId=" + chatQueueId
					+ "&templateName=kiwi&languageCode=en&countryCode=US&ver=v11&custtimeoffset=330" + "&custName="
					+ custName.replaceAll(" ", "%20") + "&CustomerAsyncPoll" + "&at="
					+ sd.format(System.currentTimeMillis());
			xml = "<body sid='" + sid + "' rid='" + AbstractChatThread.ridPlaceHolder
					+ "' xmlns='http://jabber.org/protocol/httpbind'><message type='chat' xmlns='jabber:client'><body>This is a messge to release Message Poll."
					+ sid + "</body></message></body>";
			pollReqSent = true;
			String response = context.getUtilHandle().SendMsg(custSendMsgURL_EP, xml, true, true,
					context.getSharedData("agent_assigned"));
			// Add response processing here
			Response resp = null;
			try {
				resp = context.getUtilHandle().processResponse(response, sid, context.getSharedData("agent_assigned"));
				if (resp != null) {
					if (!resp.isChatTerminated()) {
						if (resp.isStopSessionStats())
							context.setSharedData("stop_session_stats", String.valueOf(resp.isStopSessionStats()));
						if (resp.getAttachments() != null) {							
							for (Attachment attach : resp.getAttachments()) {
								String acceptAttachmentURL = context.getProtocol() + "://" + context.getServerName()
										+ "/" + context.getContextRoot() + "/companyname/chat/entrypoint/acceptattachment/";
								attach.processAttachment(acceptAttachmentURL, sid, chatQueueId,
										context.getUtilHandle().getCookieStoretoUse());
							}
						}
					} else {
						// terminate recieved set cust exit status
						if (!Boolean.valueOf(context.getSharedData("v_cust_exit_chat"))) {
							context.setSharedData("v_cust_exit_chat", String.valueOf("true"));
						}

					}
				}
			} catch (Exception e) {
				System.out.println("AgentName :" + context.getSharedData("agent_assigned") + "Customer: ThreadNumber:"
						+ context.getThreadNumber() + " Time" + sd.format(System.currentTimeMillis())
						+ " Error processing the response of the Message Release Poll");
				e.printStackTrace();
			}
		}

	}

	public boolean isFutureTaskExecuted() {
		return pollReqSent;
	}

}
