import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.apache.http.HttpHost
import org.apache.http.NameValuePair
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.message.BasicNameValuePair
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.util.EntityUtils
import org.apache.commons.lang3.StringUtils
import companyname.lt.chat.LTTrustStrategy

/* starting the count from 3 because earlier requests are present in JMeter script, viz. authenticate, register, connect */
int v_count = 3;
Pattern eglvcmdPattern = ~/.*eglvcmd:(\w+).*/;
Pattern responseCodePattern = ~/<script.*response_code:(\d+).*script>/;
Pattern sCmdPattern = ~/<script.*eglvmsg.*sCmd\+\%7C\+S\%7B(.*)\%7DsId.*>/;
Pattern actIdcaseIdPattern = ~/.*activity_id:(\d+).*case_id:(\d+).*/;
Pattern chatExitedPattern = ~/.*chatstatus:[56].*/;
Pattern custIdPattern = ~/.*customer_id:(\d+).*/;
Pattern contactPointIdPattern = ~/.*contact_point_id:(\d+).*/;
Pattern contactPersonIdPattern = ~/.*eglvid:cust(\d+)|.*/;
Pattern queueIdPattern = ~/.*queue_id:(\d+).*/;

String reqProtocol = vars.get("v_request_protocol");
String serverName = vars.get("v_server_name");
String contextRoot = vars.get("v_context_root");
String agentId = vars.get("v_agent_id");
String proxyServer = vars.get("v_server_proxy");
boolean isLoggingEnabled = vars.get("v_logging_enabled")!=null?Boolean.parseBoolean(vars.get("v_logging_enabled")):false;
int proxyPort = vars.get("v_server_port")!=null?Integer.parseInt(vars.get("v_server_port")):0;
boolean isProxyEnabled = (proxyServer!=null && !"".equals(proxyServer.trim()));

/* Proxy details to capture requests on Fiddler */
HttpHost proxy = null;
RequestConfig config = null;
if (isProxyEnabled)
{
	proxy = new HttpHost(proxyServer, proxyPort, "http");
	config = RequestConfig.custom().setProxy(proxy).setConnectTimeout(4500).setSocketTimeout(4500).build();
}
else
{
	config = RequestConfig.custom().build();
	if (isLoggingEnabled)
		System.out.println("TimeOut are : "+config.getSocketTimeout()+"  "+config.getConnectTimeout());
	config = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
}

SSLContextBuilder builder = new SSLContextBuilder();
builder.loadTrustMaterial(null, new LTTrustStrategy());
SSLConnectionSocketFactory sslcsf = new SSLConnectionSocketFactory(builder.build());
String threadNumber = String.valueOf(ctx.getThreadNum());

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
log.info("Username: "+vars.get("v_username") + " entered into groovy code " + " X-companyname-session: "+ vars.get("COOKIE_X-companyname-session") +" V_EGAIN_SESSION TOKEN value is " + vars.get("v_companyname_session") + " AWSELB: "+vars.get("COOKIE_AWSELB")+ " JSESSIONID is "+vars.get("COOKIE_JSESSIONID"));
CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslcsf).setDefaultCookieStore(cookieStore).build();

ExecutorService pool = Executors.newFixedThreadPool(1);

pool.submit(new Runnable(){
			void run() {
				String respCode = "106";
				int count =0;
				/* Till agent_id is found, it means the agent session is on */
				try{
					while (vars.get("v_agent_id") != null) {
						//log.info("agent status is "+ vars.get("v_agent_id"));
						String agent_id = vars.get("v_agent_id");
						vars.put("v_count",v_count.toString());
						long timestamp = System.currentTimeMillis();

						String strURL = reqProtocol + "://" + serverName + "/" + contextRoot +"/mr_pushlet.companyname?"+
								"client_type=js&client_conn_type=non-persistent&partition_id=1&poll_wait_time=30000&poll_freq=500&"+"__eg_request_token__=" + vars.get("v_companyname_session") +
								"&topic_name=&command=poll&recovery=false&estr=&auth_key=&cnt="+v_count+"&timestamp=" + timestamp + "&activityid=" + vars.get("v_activity_id") + "&username=" + vars.get("v_username") + "&at=" + new Date().format("yyyy-MM-dd'T'HH:mm:ss");
						//println ("Username : " + vars.get("v_username") + "Agent Poll  URL is " +strURL);
						HttpPost httppost = new HttpPost(strURL);
						/* Set the config if you wanna capture Fiddler trace for this */
						httppost.setConfig(config);
						
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("client_type","js"));
						params.add(new BasicNameValuePair("client_conn_type","non-persistent"));
						params.add(new BasicNameValuePair("partition_id","1"));
						params.add(new BasicNameValuePair("poll_wait_time","30000"));
						params.add(new BasicNameValuePair("poll_freq","500"));
						params.add(new BasicNameValuePair("__eg_request_token__",vars.get("v_companyname_session")));
						params.add(new BasicNameValuePair("topic_name",""));
						params.add(new BasicNameValuePair("recovery","false"));
						params.add(new BasicNameValuePair("command","poll"));
						params.add(new BasicNameValuePair("estr",""));
						params.add(new BasicNameValuePair("auth_key",""));
						params.add(new BasicNameValuePair("cnt",v_count.toString()));
						params.add(new BasicNameValuePair("timestamp",timestamp.toString()));
						params.add(new BasicNameValuePair("user_id","1\$" + agentId));
						
						httppost.setEntity(new UrlEncodedFormEntity(params));
						
						//println ("Username : " + vars.get("v_username") + "Agent Poll  Content is " +params);
						
						if (isLoggingEnabled)
							println ("Username : " + vars.get("v_username") + "Agent Poll  Content is " +params);
						CloseableHttpResponse response = null;
						int retryCount = 0;
						boolean success = false;
						int statusCode = -1;										
						if (!success && retryCount <10)
						{
							try {
								Thread.sleep(30000);
								response = httpclient.execute(httppost);
								
								if (response != null && response.getStatusLine() != null)
								{
										statusCode=response.getStatusLine().getStatusCode();
										if (statusCode>500)
										{
											retryCount++; 
											success=false;
											println ("Username : " + vars.get("v_username") + " for Agent Poll,  statusCode: " +statusCode + ", retry: " + retryCount);

										}
										else 
											success = true;
								} 
								if (success)
								{							
										String resp = EntityUtils.toString(response.getEntity());
										//println ("Username : " + vars.get("v_username") + "Agent Poll  Response is " +resp);
										if (isLoggingEnabled)
										println ("Username : " + vars.get("v_username") + "Agent Poll  Response is " +resp.replaceAll("\\n",""));

										String eglvcmd = "";
										String sCmd = "";
										respCode = "";
										String actId = "";
										String caseId = "";
										String custId = "";
										String contactPointId = "";
										String contactPersonId = "";

										Matcher eglvcmdMatcher = eglvcmdPattern.matcher(resp);
										Matcher responseCodeMatcher = responseCodePattern.matcher(resp);
										Matcher sCmdMatcher = sCmdPattern.matcher(resp);
										Matcher actIdcaseIdMatcher = actIdcaseIdPattern.matcher(resp);
										Matcher chatExitedMatcher = chatExitedPattern.matcher(resp);
										Matcher custIdMatcher = custIdPattern.matcher(resp);
										Matcher queueIdMatcher = queueIdPattern.matcher(resp);
										Matcher contactPointIdMatcher = contactPointIdPattern.matcher(resp);
										Matcher contactPersonIdMatcher = contactPersonIdPattern.matcher(resp);

										int eglvcmdCount=StringUtils.countMatches(resp, "eglvcmd");

										if (eglvcmdMatcher.matches())
										{
											eglvcmd = eglvcmdMatcher.group(1);
										}

										if (responseCodeMatcher.matches()){
											respCode = responseCodeMatcher.group(1);
										}
										if (sCmdMatcher.matches()){
											sCmd = sCmdMatcher.group(1);
										}
										if (custIdMatcher.matches())
										{
											custId = custIdMatcher.group(1);
										}
										if (contactPointIdMatcher.matches())
										{
											contactPointId = contactPointIdMatcher.group(1);
										}
										if (queueIdMatcher.matches())
										{
											queueId = queueIdMatcher.group(1);
										}
										if (contactPersonIdMatcher.matches())
										{
											contactPersonId = contactPersonIdMatcher.group(1);
										}
										vars.put("v_eglvcmd",eglvcmd);
										vars.put("v_response_code",respCode);
										vars.put("v_command",sCmd);
								
										if (eglvcmd == "ActivityPush" && actIdcaseIdMatcher.matches())
										{
											if (count > 0)
											{
												log.info("Username : "+vars.get("v_username") + " Normal Response received after a 404" + respCode);
												count =0;
											}
											String assignedActivityId = actIdcaseIdMatcher.group(1);
											vars.put("v_activity_id_pushed",actIdcaseIdMatcher.group(1));
											vars.put("v_case_id_pushed",actIdcaseIdMatcher.group(2));
											vars.put("v_cust_id_pushed",custIdMatcher.group(1));
											vars.put("v_contactPointId_pushed",contactPointId);
											vars.put("v_contactPersonId_pushed",contactPersonId);
											vars.put("v_queue_ids",queueId);
											vars.put("v_activity_found", "true");
											log.info("Username : "+vars.get("v_username") + " v_activity_id: " + vars.get("v_activity_id_pushed") + " eglvcmd : " + eglvcmd+" v_case_id: " + vars.get("v_case_id_pushed")+" v_cust_id: " + vars.get("v_cust_id_pushed")+" v_contactPointId: " + vars.get("v_contactPointId_pushed")+" v_contactPersonId: " + vars.get("v_contactPersonId_pushed")+" v_queue_id: " + vars.get("v_queue_ids") + " X-companyname-session: "+ vars.get("COOKIE_X-companyname-session") +" V_EGAIN_SESSION TOKEN value is " + vars.get("v_companyname_session") + " AWSELB: "+vars.get("COOKIE_AWSELB")+ " JSESSIONID is "+vars.get("COOKIE_JSESSIONID"));
										}
										else if (respCode == "105" && chatExitedMatcher.matches())
										{
											if (count > 0)
											{
												log.info("Username : "+vars.get("v_username") + " Normal Response received after a 404" + respCode);
												count =0;
											}
											vars.put("v_cust_exited_chat","true");
											log.info("Username : "+vars.get("v_username") + " Customer" + vars.get("v_cust_id") + "has exited the chat" + " X-companyname-session: "+ vars.get("COOKIE_X-companyname-session") +" V_EGAIN_SESSION TOKEN value is " + vars.get("v_companyname_session") + " AWSELB: "+vars.get("COOKIE_AWSELB")+ " JSESSIONID is "+vars.get("COOKIE_JSESSIONID"));
										}
										// remove blank once the issue around blank polls are fixed.
										else if (respCode != "105" && respCode != "106" && respCode != "")
										{
											if (respCode == "404" && count <3){
												count++;
											}
											else {
												log.error("Username : "+vars.get("v_username") + " Error response obtained: " + resp + " Stopping test thread. Count :"+count);
												ctx.getThread()?.stop();
												break;
											}
										}
										else if (respCode == "")
										{
											if (count > 0)
											{
												log.info("Username : "+vars.get("v_username") + " Normal Response received after a 404" + respCode);
												count =0;
											}
											log.error("Username : "+vars.get("v_username") + " Blank RespCode found." + " Agent Id is: " +vars.get("v_agent_id") + " X-companyname-session: "+ vars.get("COOKIE_X-companyname-session") +" V_EGAIN_SESSION TOKEN value is " + vars.get("v_companyname_session") + " AWSELB: "+vars.get("COOKIE_AWSELB")+ " JSESSIONID is "+vars.get("COOKIE_JSESSIONID"));
										}
									}								
								}
							catch (Exception e)
							{
								log.info("Username : "+vars.get("v_username") + " Exception "+e);
								e.printStackTrace();
							}
							catch (Throwable th)
							{
								log.info("Username : "+vars.get("v_username") + " Throwable "+th);
								th.printStackTrace();
							}
							finally {
								response?.close();
							}
							v_count++;
							}
						}
					}
				catch (Exception e) {
						log.error("Username : "+vars.get("v_username") + "Stopping thread as Failed to make request. ",e);
						e.printStackTrace();
						ctx.getThread()?.stop();
					}
				catch (Throwable th)
					{
						log.info("Username : "+vars.get("v_username") + " Throwable "+th);
						th.printStackTrace();
					} 
				finally {
						httpclient.close();
					}
				Thread.currentThread().interrupt();
			}
		});
pool.shutdown();