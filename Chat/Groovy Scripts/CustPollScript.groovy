import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

import org.apache.http.HttpHost
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.util.EntityUtils

import companyname.lt.chat.MessagingThread
import companyname.lt.chat.PollThread
import companyname.lt.chat.RunContext
import companyname.lt.chat.Util
import companyname.lt.chat.LTTrustStrategy
import companyname.lt.chat.Utilities

String custFirstName = vars.get("v_cust_First_name");
String custLastName = vars.get("v_cust_Last_name");
String firstMessage = vars.get("v_cust_firstMessage");
String custName;

Utilities utility = new Utilities();
custFirstName = utility.randomizeString(custFirstName);
custLastName = utility.randomizeString(custLastName);
custName = custFirstName + " " + custLastName;
firstMessage = utility.randomizeString(firstMessage);

RunContext context = new RunContext() ;
context.setSharedData("v_cust_First_name", custFirstName);
context.setSharedData("v_cust_Last_name", custLastName);
context.setSharedData("v_cust_name", custName);

String v_cust_email = custFirstName+custLastName+"@eng.na";
String reqProtocol = vars.get("v_request_protocol");
String serverName = vars.get("v_server_name");
String contextRoot = vars.get("v_context_root");
String chatQueueId = vars.get("v_chat_queue_id");
String cust_max_msg = vars.get("v_cust_max_message");
String proxyServer = vars.get("v_server_proxy");
int proxyPort = vars.get("v_server_port")!=null?Integer.parseInt(vars.get("v_server_port")):0;
boolean isProxyEnabled = (proxyServer!=null && !"".equals(proxyServer.trim()));
boolean isLoggingEnabled = vars.get("v_logging_enabled")!=null?Boolean.parseBoolean(vars.get("v_logging_enabled")):false;
int custWaitTimeBeforeExit = vars.get("v_cust_waittime_beforeexit")!=null?Integer.parseInt(vars.get("v_cust_waittime_beforeexit")):20;
//log.info("v_server_name in customer Poll Req- " + serverName);
/*Pattern matcher to fetch agent username*/
Pattern agentUserNamePattern = ~/body>You are now chatting with (.*)</;
String customerName = custName.replaceAll(" ","%20");
ExecutorService pool = Executors.newFixedThreadPool(3);

String CloseChatEPURL = reqProtocol + "://" + serverName + "/" + contextRoot +"/companyname/chat/entrypoint?entryPointId="+ chatQueueId +"&templateName=kiwi&languageCode=en&countryCode=US&ver=v11&custtimeoffset=330" + "&custName=" + customerName + "&CustCloseChat" + "&at=" + new Date().format("yyyy-MM-dd'T'HH:mm:ss");
HttpPost closeChatEPPostRequest = new HttpPost(CloseChatEPURL);

String ridPlaceHolder = "#__RID_PLACEHOLDER__#";

if (isProxyEnabled) {
	HttpHost proxy = new HttpHost(proxyServer, proxyPort, "http");
	RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
	closeChatEPPostRequest.setConfig(config);
}


long ridInit = Long.parseLong(vars.get("v_rid"));

String sid = null;
boolean stopSessionStats = false;
vars.put("v_start_cust_polling","true");
vars.put("v_cust_exit_chat", "false");
int PollCount = 1;
//String xml = null;
int MsgCount = 1;
String threadNumber = String.valueOf(ctx.getThreadNum());
boolean blankPollOnServer =false;
boolean messagePollOnServer =false;
long msgReqTime = 0;
long msgRespTime = 0;
long pollReqTime = 0;
long pollRespTime = 0;
/**
 * Util class to have the message send in a sequence
 */
/* Before being able to use cookies as variables, the property 'CookieManager.save.cookies=true' should be set */
org.apache.http.client.CookieStore cookieStore = new BasicCookieStore();

BasicClientCookie clientCookie = new BasicClientCookie("X-companyname-session", vars.get("COOKIE_X-companyname-session"));
clientCookie.setDomain(serverName);
clientCookie.setPath("/system");
cookieStore.addCookie(clientCookie);
BasicClientCookie jsessionCookie = new BasicClientCookie("JSESSIONID", vars.get("COOKIE_JSESSIONID"));
jsessionCookie.setDomain(serverName);
jsessionCookie.setPath("/system");
cookieStore.addCookie(jsessionCookie);
BasicClientCookie awselbCookie = new BasicClientCookie("AWSELB", vars.get("COOKIE_AWSELB"));
awselbCookie.setDomain(serverName);
awselbCookie.setPath("/");
cookieStore.addCookie(awselbCookie);

Util util = new Util(ridInit, isProxyEnabled, threadNumber, proxyServer, proxyPort, reqProtocol, isLoggingEnabled) ;
util.setCookieStoretoUse(cookieStore);
Object o = new Object();
ReentrantLock lock = new ReentrantLock() ;
Condition chatEndCondition = lock.newCondition() ;

context.setContextRoot(contextRoot) ;
context.setLock(lock) ;
context.setProtocol(reqProtocol) ;
context.setServerName(serverName) ;
context.setUtilHandle(util) ;
context.setChatEndCondition(chatEndCondition) ;
context.setThreadNumber(threadNumber);
context.setInterThreadCondition(lock.newCondition()) ;
context.setSharedData("cust_max_msg", cust_max_msg);
context.setSharedData("v_request_protocol", reqProtocol);
context.setSharedData("v_server_name", serverName);
context.setSharedData("v_cust_email", v_cust_email);
context.setSharedData("v_cust_phone", vars.get("v_cust_phone"));
context.setSharedData("v_chat_queue_id", chatQueueId);
context.setSharedData("v_context_root", contextRoot);
context.setSharedData("v_start_cust_polling","true");
context.setSharedData("v_cust_exit_chat", "false");
context.setSharedData("v_first_message", firstMessage);
context.setSharedData("stop_session_stats", "false");
context.setSharedData("is_debug_run", String.valueOf(isLoggingEnabled));
context.setSharedData("custWaitTimeBeforeExit",String.valueOf(custWaitTimeBeforeExit));
context.setProxyServer(proxyServer);
context.setProxyPort(proxyPort);

if (isLoggingEnabled)
	println(context.toString());
MessagingThread msgThread = new MessagingThread(context);
PollThread pollThread = new PollThread(context);



if (vars.get("v_cust_exit_chat")=="false") {
	pool.submit(pollThread);
	pool.submit(msgThread);
}
lock.lock() ;
try{
	chatEndCondition.await(3600,TimeUnit.SECONDS) ;
	String tempRid = util.getRid().toString();
	log.info("Customer: ThreadNumber:"+threadNumber+" Time "+new Date().format("yyyy-MM-dd' 'HH:mm:ss SSS")+ "Putting the rId value as "+tempRid);
	vars.put("v_rid",tempRid);
	vars.put("v_cust_exit_chat", "true");
	vars.put("v_sid", context.getSharedData("sid"));
	String closeChatXml = "<body type='terminate' sid='" + context.getSharedData("sid") + "' rid='" + tempRid + "' xmlns='http://jabber.org/protocol/httpbind'><presence type='unavailable' xmlns='jabber:client'/></body>";
	String closeResp = util.SendMsg(CloseChatEPURL, closeChatXml, true, false, context.getSharedData("agent_assigned"));
	
	//log.info("CloseChatXml: " + closeChatXml);

	//SSLContextBuilder builder = new SSLContextBuilder();
	//builder.loadTrustMaterial(null, new LTTrustStrategy());
	//SSLConnectionSocketFactory sslcsf = new SSLConnectionSocketFactory(builder.build());

	//CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslcsf).setDefaultCookieStore(cookieStore).build();
	//if (isLoggingEnabled)
	//	System.out.println("Customer: ThreadNumber:" + threadNumber + " Time " + new Date().format("yyyy-MM-dd' 'HH:mm:ss SSS") + " Close Chat Request XML is " + closeChatXml.replaceAll("\\n", ""));
	//closeChatEPPostRequest.setEntity(new StringEntity(closeChatXml, ContentType.TEXT_XML));
	//CloseableHttpResponse closeChatResponse = null;
	//closeChatResponse = httpclient.execute(closeChatEPPostRequest);
	//int statusCode = closeChatResponse.getStatusLine().getStatusCode();
	
	//String closeResp = EntityUtils.toString(closeChatResponse.getEntity());
	if (isLoggingEnabled)
		System.out.println("Customer: ThreadNumber:" + threadNumber + " Time " + new Date().format("yyyy-MM-dd' 'HH:mm:ss SSS") + " Close Chat Response XML is " + closeResp.replaceAll("\\n", ""));
	//log.info("Posted CloseChat URL");

	log.info("Customer: ThreadNumber:"+threadNumber+" Time "+new Date().format("yyyy-MM-dd' 'HH:mm:ss SSS")+ "Putting the sid value as "+ vars.get("v_sid"));
	pool.shutdown();
}
catch (Exception e)
{
	e.printStackTrace();
}finally{
	lock.unlock() ;
}