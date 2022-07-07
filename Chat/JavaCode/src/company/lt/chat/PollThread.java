package companyname.lt.chat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jmeter.services.FileServer;

public class PollThread extends AbstractChatThread {

	boolean custExitFlag = false;
	boolean messagePollOnServer = false;
	MessagingThread msgThread = null;
	Pattern agentUserNamePattern = Pattern.compile("~/body>You are now chatting with (.*)</");
	boolean stopSessionStats = false;

	public PollThread(RunContext context) {
		super(context);
	}

	public void run() {
		// Thread.currentThread().setName("EG_CUST_T1");

		try {

			BufferedReader bufReader = null;
			String xmlBlank = null;
			boolean firstReq = true;
			String sid = null;
			String chatQueueId = context.getSharedData("v_chat_queue_id");
			String custName = context.getSharedData("v_cust_name");
			SecureRandom sr = new SecureRandom();
			long randomNo = sr.nextLong();
			if (randomNo < 0)
				randomNo = randomNo * (-1);
			String[] nameParts = custName.split(" ");
			String firstName = nameParts[0];
			String lastName = nameParts[1];
			context.setSharedData("v_cust_name",
					firstName + String.valueOf(randomNo) + " " + lastName + String.valueOf(randomNo));
			String custEmail = context.getSharedData("v_cust_email");
			context.setSharedData("v_cust_email", custEmail.replaceAll("@", String.valueOf(randomNo) + "@"));
			int PollCount = 1;
			String str = null;
			String custSendMsgURL_EP = getProtocol() + "://" + getServerName() + "/" + getContextRoot()
					+ "/companyname/chat/entrypoint?entryPointId=" + chatQueueId
					+ "&templateName=kiwi&languageCode=en&countryCode=US&ver=v11&custtimeoffset=330" + "&custName="
					+ custName.replaceAll(" ", "%20") + "&CustomerAsyncPoll" + "&at="
					+ sd.format(System.currentTimeMillis());
			boolean isDebug = Boolean.parseBoolean(context.getSharedData("is_debug_run"));

			if (isDebug)
				System.err.println("AgentName :" + context.getSharedData("agent_assigned")
						+ " CustomerPoll: ThreadNumber:" + getThreadNumber() + " Time "
						+ sd.format(System.currentTimeMillis()) + "Launching the Pool Thread");

			while (!custExitFlag) {
				try {

					if (PollCount == 1) {
						// System.out.println("CustomerPoll:
						// getThreadNumber():"+getThreadNumber()+" Time "+new
						// Date().format("yyyy-MM-dd' 'HH:mm:ss SSS")+ " Chat
						// Registration Request from URL: "+entryPointURLBlank);
						StringBuilder sb = new StringBuilder("");
						String fileLoc = FileServer.getFileServer().getBaseDir() + "\\"
								+ "CustomerLongPollRequestBody.xml";
						if (isDebug)
							System.err.println("AgentName :" + context.getSharedData("agent_assigned")
									+ " CustomerPoll: ThreadNumber:" + getThreadNumber() + " Time "
									+ sd.format(System.currentTimeMillis()) + "File Loc is " + fileLoc);
						File file = new File(fileLoc);
						Reader reader = new FileReader(file);
						bufReader = new BufferedReader(reader);
						String line = bufReader.readLine();
						while (line != null) {
							sb.append(line).append("\n");
							line = bufReader.readLine();
						}
						xmlBlank = sb.toString();
						xmlBlank = xmlBlank.replace("{placeholder_rid}", ridPlaceHolder);
						xmlBlank = xmlBlank.replace("{placeholder_queue_id}", context.getSharedData("v_chat_queue_id"));
						xmlBlank = xmlBlank.replace("{placeholder_cust_name}", context.getSharedData("v_cust_name"));
						xmlBlank = xmlBlank.replace("{placeholder_email}", context.getSharedData("v_cust_email"));
						xmlBlank = xmlBlank.replace("{placeholder_cust_phone}", context.getSharedData("v_cust_phone"));
						xmlBlank = xmlBlank.replace("{placeholder_subject_message}",
								context.getSharedData("v_first_message") + " " + context.getSharedData("v_cust_name"));
						bufReader.close();

						str = getUtilHandle().SendMsg(custSendMsgURL_EP, xmlBlank, true, false,
								context.getSharedData("agent_assigned"));

						String agentUserName = "";
						Matcher agentUserNameMatcher = agentUserNamePattern.matcher(str);
						// first request so sid is empty
						Response resp = getUtilHandle().processResponse(str, null,
								context.getSharedData("agent_assigned"));

						if (resp != null) {
							sid = resp.getSid();
							long chatCreationTime = System.currentTimeMillis();
							if (!resp.isChatTerminated() && sid.length() > 0) {
								context.setSharedData("sid", sid);
								context.setSharedData("chatCreationTime", String.valueOf(chatCreationTime));
								firstReq = false;
							} else {
								System.err.println("AgentName :" + context.getSharedData("agent_assigned")
										+ " CustomerPoll: getThreadNumber():" + getThreadNumber() + " Time "
										+ sd.format(System.currentTimeMillis())
										+ " Couldn't get Cust Registration Response: " + str);
								break;
							}
						} else {
							System.err.println("AgentName :" + context.getSharedData("agent_assigned")
									+ " CustomerPoll: getThreadNumber():" + getThreadNumber() + " Time "
									+ sd.format(System.currentTimeMillis())
									+ " Null Cust Registration Response recieved: " + str);
						}
					} else {
						// long msgReqTime =
						// Long.parseLong(context.getSharedData("msg_req_time"));
						messagePollOnServer = Boolean.parseBoolean(context.getSharedData("poll_on_server"));
						if (!(messagePollOnServer)) {
							xmlBlank = "<body sid='" + sid + "' rid='" + ridPlaceHolder
									+ "' xmlns='http://jabber.org/protocol/httpbind'/>";
							if (isDebug)
								System.out.println("AgentName :" + context.getSharedData("agent_assigned")
										+ " CustomerPoll: getThreadNumber():" + getThreadNumber() + " Time "
										+ sd.format(System.currentTimeMillis()) + " messagePollOnServer:"
										+ messagePollOnServer + " POll Request Sent PollCount: " + PollCount + " sid:"
										+ context.getSharedData("sid"));

						




	if (!custExitFlag)
			str = getUtilHandle().SendMsg(custSendMsgURL_EP, xmlBlank, true, true,
										context.getSharedData("agent_assigned"));
							else
								System.out.println("AgentName :" + context.getSharedData("agent_assigned")
										+ " CustomerPoll: getThreadNumber():" + getThreadNumber() + " Time "
										+ sd.format(System.currentTimeMillis()) + " Customer Exited the Chat");

							if (str != null) {
								Response resp = getUtilHandle().processResponse(str, context.getSharedData("sid"),
										context.getSharedData("agent_assigned"));
								if (resp != null) {
									if (!resp.isChatTerminated()) {
										if (resp.isStopSessionStats())
											context.setSharedData("stop_session_stats",
													String.valueOf(resp.isStopSessionStats()));
										if (resp.getAgentName() != null && !"".equals(resp.getAgentName().trim()))
											context.setSharedData("agent_assigned", resp.getAgentName());
										if (resp.getAttachments() != null) {										
											for (Attachment attach : resp.getAttachments()) {
												String acceptAttachmentURL = getProtocol() + "://" + getServerName()
														+ "/" + getContextRoot()
														+ "/companyname/chat/entrypoint/acceptattachment/";
												attach.processAttachment(acceptAttachmentURL, sid, chatQueueId,
														getUtilHandle().getCookieStoretoUse());
											}
										}
									} else {
										// terminate recieved set cust exit
										// status
										if (!custExitFlag) {
											setCustExitFlag(true);
										}

									}
								}
							}
						} else {
							getLock().lock();
							if (isDebug)
								System.out.println("AgentName :" + context.getSharedData("agent_assigned")
										+ " Waiting to be notifyed : " + getThreadNumber());
							try {
								if (!custExitFlag)
									getInterThreadCondition().await(240, TimeUnit.SECONDS);
							} catch (InterruptedException e) {
								e.printStackTrace();
							} finally {
								getLock().unlock();
							}
						}
					}
					PollCount++;
					custExitFlag = Boolean.parseBoolean(context.getSharedData("v_cust_exit_chat"));

					if (isDebug)
						System.err.println("AgentName :" + context.getSharedData("agent_assigned")
								+ " CustomerPoll: ThreadNumber:" + getThreadNumber() + " Time "
								+ sd.format(System.currentTimeMillis()) + context.toString());
				}

				catch (Exception e) {
					System.err.println("AgentName :" + context.getSharedData("agent_assigned")
							+ " Exception encountered in polling thread " + e);
					e.printStackTrace();
				}
			}
			System.out.println(
					"AgentName :" + context.getSharedData("agent_assigned") + "CustomerPoll: getThreadNumber():"
							+ getThreadNumber() + " Time " + sd.format(System.currentTimeMillis())
							+ " Interrupting the poll thread as customer exitied the chat ");
			Thread.currentThread().interrupt();
		} catch (Throwable th) {
			th.printStackTrace();
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
}
