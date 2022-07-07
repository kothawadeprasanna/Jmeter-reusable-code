package companyname.lt.chat;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

public class MessagingThread extends AbstractChatThread {

	private boolean custExitFlag = false;
	private String sid = null;
	private int cust_max_msg = 0;
	private boolean stopSessionStats = false;
	private ScheduledExecutorService pollScheduleExecutorService = Executors.newScheduledThreadPool(1);
	private int custThinkTime = 12000;

	public MessagingThread() {
		super();
	}

	public MessagingThread(RunContext context) {
		super(context);
	}

	public void setContext(RunContext context) {
		this.context = context;
	}

	public void run() {
		try {
			Thread.currentThread().setName("EG_CUST_T2");
			// System.out.println("CustomerMessage:
			// ThreadNumber:"+threadNumber+" Time
			// "+sd.format(System.currentTimeMillis())+ " Messaging Thread
			// Created");
			String xml = null;
			boolean typing = true;
			int MsgCount = 1;
			int custWaitTimeBeforeExit = context.getSharedData("custWaitTimeBeforeExit") != null
					? Integer.parseInt(context.getSharedData("custWaitTimeBeforeExit")) : 20;
			String chatQueueId = context.getSharedData("v_chat_queue_id");
			String custName = context.getSharedData("v_cust_name");
			String custSendMsgURL_EP = getProtocol() + "://" + getServerName() + "/" + getContextRoot()
					+ "/companyname/chat/entrypoint?entryPointId=" + chatQueueId
					+ "&templateName=kiwi&languageCode=en&countryCode=US&ver=v11&custtimeoffset=330" + "&custName="
					+ custName.replaceAll(" ", "%20") + "&CustomerAsyncPoll" + "&at="
					+ sd.format(System.currentTimeMillis());
			boolean isDebug = Boolean.parseBoolean(context.getSharedData("is_debug_run"));

			if (isDebug)
				System.err.println("AgentName :" + context.getSharedData("agent_assigned")
						+ " CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
						+ sd.format(System.currentTimeMillis()) + "Launching the Messaging Thread");
			String sessionStatsURL = getProtocol() + "://" + getServerName() + "/" + getContextRoot()
									+ "/companyname/chat/entrypoint/sessionStatistics";
			String SessionStatXml = null;
			while (!custExitFlag) {
				try {
					if (isDebug)
						System.err.println("AgentName :" + context.getSharedData("agent_assigned")
								+ " CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
								+ sd.format(System.currentTimeMillis()) + "custExitFlag:" + custExitFlag
								+ " stop_session_stats:"
								+ Boolean.parseBoolean(context.getSharedData("stop_session_stats")));
					while (!(Boolean.parseBoolean(context.getSharedData("stop_session_stats")))) {
						Thread.sleep(5000);
						sid = context.getSharedData("sid");
						SessionStatXml = "<sessionStatistics xmlns='http://bindings.companyname.com/chat'><sid>"
									+ sid + "</sid></sessionStatistics>";
						if (sid != null && !"".equalsIgnoreCase(sid.trim())) {
							if (isDebug)
								System.out.println("AgentName :" + context.getSharedData("agent_assigned")
									+ " CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
									+ sd.format(System.currentTimeMillis()) + " Sid found : " + sid);
							
							String sessionStatsResponse = getUtilHandle().SendMsg(sessionStatsURL, SessionStatXml, false, false, context.getSharedData("agent_assigned"));

							if (sessionStatsResponse != null
									&& sessionStatsResponse.trim().equals("") && isDebug) {
								System.err.println("AgentName :" + context.getSharedData("agent_assigned")
										+ " CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
										+ sd.format(System.currentTimeMillis()) + " sid :" + sid + " response :"
										+ sessionStatsResponse);
							}
						}

						if (Boolean.parseBoolean(context.getSharedData("stop_session_stats"))) {
							System.out.println("AgentName :" + context.getSharedData("agent_assigned")
									+ " CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
									+ sd.format(System.currentTimeMillis()) + " stopSessionStats value is "
									+ stopSessionStats);
							break;
						}
						if (context.getSharedData("chatCreationTime") != null) {
							if (!Boolean.parseBoolean(context.getSharedData("stop_session_stats"))
									&& ((System.currentTimeMillis() - Long.parseLong(
											context.getSharedData("chatCreationTime"))) > custWaitTimeBeforeExit * 60
													* 1000)) {
								System.err.println("AgentName :" + context.getSharedData("agent_assigned")
										+ " CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
										+ sd.format(System.currentTimeMillis())
										+ " Stopping the Chat as it was not assigned to any user for 20 mins " + sid);
								context.setSharedData("stop_session_stats", "true");
								setCustExitFlag(true);

							}
						}
						Thread.sleep(25000);
					}

					if (!custExitFlag && Boolean.valueOf(context.getSharedData("stop_session_stats"))) {

						if (!typing) {
							if (isDebug)
								System.out.println("AgentName :" + context.getSharedData("agent_assigned")
										+ " CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
										+ sd.format(System.currentTimeMillis())
										+ " Entered cust typing msg for MsgCount: " + MsgCount + " typing: " + typing);
							xml = "<body sid='" + sid + "' rid='" + ridPlaceHolder
									+ "' xmlns='http://jabber.org/protocol/httpbind'><message type='chat' xmlns='jabber:client'><body>This is chat message, Count: "
									+ MsgCount + " rid :" + ridPlaceHolder + "</body></message></body>";
							callSendMessage(custSendMsgURL_EP, xml, custThinkTime, sid, chatQueueId);

							xml = "<body sid='" + sid + "' rid='" + ridPlaceHolder
									+ "' xmlns='http://jabber.org/protocol/httpbind'><message type='normal' xmlns='jabber:client'><body>0</body></message></body>";
							if (isDebug)
								System.out.println("CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
										+ sd.format(System.currentTimeMillis())
										+ "Entered cust stop typing msg for MsgCount" + MsgCount + " stop typing "
										+ typing + "XML: " + xml);
							typing = true;
							callSendMessage(custSendMsgURL_EP, xml, custThinkTime, sid, chatQueueId);
						} else if ((MsgCount < 3 || MsgCount > 8) && typing) {
							xml = "<body sid='" + sid + "' rid='" + ridPlaceHolder
									+ "' xmlns='http://jabber.org/protocol/httpbind'><message type='normal' xmlns='jabber:client'><body>1</body></message></body>";
							if (isDebug)
								System.out.println("CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
										+ sd.format(System.currentTimeMillis()) + "Entered cust typing msg for MsgCount"
										+ MsgCount + " typing " + typing + "XML: " + xml);
							typing = false;
							callSendMessage(custSendMsgURL_EP, xml, custThinkTime, sid, chatQueueId);
						}

						else if (MsgCount == 3) {
							if (isDebug)
								System.out.println("AgentName :" + context.getSharedData("agent_assigned")
										+ " CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
										+ sd.format(System.currentTimeMillis()) + "VISA DETAILS EP 1 for MsgCount"
										+ MsgCount);
							xml = "<body sid='" + sid + "' rid='" + ridPlaceHolder
									+ "' xmlns='http://jabber.org/protocol/httpbind'><message type='normal' xmlns='jabber:client'><body>1</body></message></body>";
							callSendMessage(custSendMsgURL_EP, xml, custThinkTime, sid, chatQueueId);
						} else if (MsgCount == 4) {
							if (isDebug)
								System.out.println("AgentName :" + context.getSharedData("agent_assigned")
										+ " CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
										+ sd.format(System.currentTimeMillis()) + "VISA DETAILS EP 2 for MsgCount"
										+ MsgCount);
							xml = "<body sid='" + sid + "' rid='" + ridPlaceHolder
									+ "' xmlns='http://jabber.org/protocol/httpbind'><message type='chat' xmlns='jabber:client'><body>*******************</body></message><message type='headline' xmlns='jabber:client'><body>Don't worry. Your sensitive data has been masked.</body><companynameCommand xmlns='http://bindings.companyname.com/chat'><subcmd>headline</subcmd></companynameCommand></message></body>";
							callSendMessage(custSendMsgURL_EP, xml, custThinkTime, sid, chatQueueId);
						}

						else if (MsgCount == 5) {
							if (isDebug)
								System.out.println("AgentName :" + context.getSharedData("agent_assigned")
										+ " CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
										+ sd.format(System.currentTimeMillis()) + "Goes OFF record for MsgCount"
										+ MsgCount);
							xml = "<body sid='" + sid + "' rid='" + ridPlaceHolder
									+ "' xmlns='http://jabber.org/protocol/httpbind'><message type='headline' xmlns='jabber:client'><body>Your messages are now off-record and will not be saved in the system.</body><companynameCommand xmlns='http://bindings.companyname.com/chat'><subcmd>offrecord</subcmd></companynameCommand></message></body>";
							callSendMessage(custSendMsgURL_EP, xml, custThinkTime, sid, chatQueueId);
						}

						else if (MsgCount == 6) {
							if (isDebug)
								System.out.println("AgentName :" + context.getSharedData("agent_assigned")
										+ " CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
										+ sd.format(System.currentTimeMillis())
										+ "Credit card message EP 1 for MsgCount" + MsgCount);
							xml = "<body sid='" + sid + "' rid='" + ridPlaceHolder
									+ "' xmlns='http://jabber.org/protocol/httpbind'><message type='normal' xmlns='jabber:client'><body>1</body></message></body>";
							callSendMessage(custSendMsgURL_EP, xml, custThinkTime, sid, chatQueueId);
						}

						else if (MsgCount == 7) {
							if (isDebug)
								System.out.println("AgentName :" + context.getSharedData("agent_assigned")
										+ " CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
										+ sd.format(System.currentTimeMillis()) + "Credit card message 2 for MsgCount"
										+ MsgCount);
							xml = "<body sid='" + sid + "' rid='" + ridPlaceHolder
									+ "' xmlns='http://jabber.org/protocol/httpbind'><message type='chat' xmlns='jabber:client'><body>4111 1111 1111 1111</body></message></body>";
							callSendMessage(custSendMsgURL_EP, xml, custThinkTime, sid, chatQueueId);
						}

						else if (MsgCount == 8) {
							if (isDebug)
								System.out.println("AgentName :" + context.getSharedData("agent_assigned")
										+ " CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
										+ sd.format(System.currentTimeMillis()) + "Goes On record back for MsgCount"
										+ MsgCount);
							xml = "<body  sid='" + sid + "' rid='" + ridPlaceHolder
									+ "' xmlns='http://jabber.org/protocol/httpbind'><message type='headline' xmlns='jabber:client'><body>Your messages are now on record.</body><companynameCommand xmlns='http://bindings.companyname.com/chat'><subcmd>onrecord</subcmd></companynameCommand></message></body>";
							callSendMessage(custSendMsgURL_EP, xml, custThinkTime, sid, chatQueueId);
						}

					}
				} catch (Exception e) {
					System.err.println(
							"AgentName :" + context.getSharedData("agent_assigned") + " CustomerMessage: ThreadNumber:"
									+ getThreadNumber() + " Time " + sd.format(System.currentTimeMillis())
									+ "Exception occurred in the Messaging thread " + e);
					e.printStackTrace();
				}
				MsgCount++;
				cust_max_msg = Integer.parseInt(context.getSharedData("cust_max_msg"));
				if (MsgCount > cust_max_msg) {
					setCustExitFlag(true);
					System.out.println(
							"AgentName :" + context.getSharedData("agent_assigned") + " CustomerMessage: ThreadNumber:"
									+ getThreadNumber() + " Time " + sd.format(System.currentTimeMillis())
									+ "CustomerMessage: Total message count has been reached. Exiting Groovy script.");
					Thread.currentThread().interrupt();
				}
				custExitFlag = Boolean.parseBoolean(context.getSharedData("v_cust_exit_chat"));
				if (isDebug)
					System.err.println("AgentName :" + context.getSharedData("agent_assigned")
							+ " CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time "
							+ sd.format(System.currentTimeMillis()) + context.toString());
			}
			pollScheduleExecutorService.shutdown();
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}

	public void callSendMessage(String url, String xml, long timetoSleep, String sid, String chatQueueId)
			throws IOException, SAXException, ParserConfigurationException {
		Response resp = null;
		long msgReqTime = 0;
		long msgRespTime = 0;
		String response = null;
		try {
			getLock().lock();
			setMessagePollOnServer(true);
			msgReqTime = System.currentTimeMillis();
			// register a scheduled task
			FuturePollTask fPoolTask = new FuturePollTask(0, sid, this.context);
			ScheduledFuture<?> pollFutureTask = pollScheduleExecutorService.schedule(fPoolTask, (timetoSleep),
					TimeUnit.MILLISECONDS);
			response = getUtilHandle().SendMsg(url, xml, true, false, context.getSharedData("agent_assigned"));
			msgRespTime = System.currentTimeMillis();
			context.setSharedData("msg_req_time", Long.toString(msgReqTime));
			if (!(pollFutureTask.isCancelled() && pollFutureTask.isDone())) {
				fPoolTask.cancelTask();
				pollFutureTask.cancel(true);
			}
			if (!fPoolTask.isFutureTaskExecuted()) {
				setMessagePollOnServer(false);
				System.out.println("AgentName :" + context.getSharedData("agent_assigned")
						+ " CustomerMessage: ThreadNumber:" + getThreadNumber() + " Time"
						+ sd.format(System.currentTimeMillis()) + "Notifying the thread : " + getThreadNumber());
				getInterThreadCondition().signalAll();
			}
			getLock().unlock();
			resp = getUtilHandle().processResponse(response, sid, context.getSharedData("agent_assigned"));
			if (resp != null) {
				if (!resp.isChatTerminated()) {
					if (resp.isStopSessionStats())
						context.setSharedData("stop_session_stats", String.valueOf(resp.isStopSessionStats()));
					if (resp.getAgentName() != null && !"".equals(resp.getAgentName().trim()))
						context.setSharedData("agent_assigned", resp.getAgentName());

					if (resp.getAttachments() != null) {
						for (Attachment attach : resp.getAttachments()) {
							String acceptAttachmentURL = getProtocol() + "://" + getServerName() + "/"
									+ getContextRoot() + "/companyname/chat/entrypoint/acceptattachment/";
							attach.processAttachment(acceptAttachmentURL, sid, chatQueueId,
									getUtilHandle().getCookieStoretoUse());
						}
					}
				} else {
					// terminate recieved set cust exit status
					if (!custExitFlag) {
						setCustExitFlag(true);
					}

				}
			}
			if (!fPoolTask.isFutureTaskExecuted() && (timetoSleep - (System.currentTimeMillis() - msgReqTime) > 0))
				Thread.sleep(timetoSleep - (System.currentTimeMillis() - msgReqTime));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setCustExitFlag(boolean custExitFlag) {
		this.custExitFlag = custExitFlag;
		context.setSharedData("v_cust_exit_chat", String.valueOf(custExitFlag));
		if (custExitFlag) {
			getLock().lock();
			getChatEndCondition().signalAll();
			getLock().unlock();
		}
	}

	private void setMessagePollOnServer(boolean pollStatus) {
		context.setSharedData("poll_on_server", Boolean.toString(pollStatus));
	}

	public void setStopSessionStats(boolean stopSessionStats) {
		this.stopSessionStats = stopSessionStats;
	}

}