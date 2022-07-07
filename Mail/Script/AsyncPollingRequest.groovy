import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import org.apache.http.HttpHost
import org.apache.http.NameValuePair
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils


int v_count = 3;
long timestamp = System.currentTimeMillis();

//HttpHost proxy = new HttpHost("127.0.0.1", 8888, "http");

//RequestConfig config = RequestConfig.custom()		.setProxy(proxy)		.build();

ExecutorService pool = Executors.newFixedThreadPool(1);
pool.submit(new Runnable(){

			void run() {
			log.info("Started polling thread"+vars.get("lt_start_polling"));
				int retryBody500=0;
				int respCodeRetry=0;
				while (vars.get("lt_start_polling").equals("true")) {
					log.info(vars.get("lt_userID")+" is sending mrpushlet with pushlet count= "+v_count);

					String strURL = vars.get("v_request_protocol") + "://" + vars.get("webServer") + "/system/mr_pushlet.companyname?"+
							"client_type=js&client_conn_type=non-persistent&partition_id=1&poll_wait_time=30000&poll_freq=500&__eg_request_token__="+vars.get("lt_X-companyname-csrf")+"&"+
							"topic_name=&command=poll&recovery=false&estr=CB2-1&auth_key=&cnt="+v_count+"&timestamp=" + timestamp + "&userID=" + vars.get("lt_userID");

					/* Before being able to use cookies as variables, the property 'CookieManager.save.cookies=true' should be set */
					org.apache.http.client.CookieStore cookieStore = new BasicCookieStore();
					//log.info("From vars: "+ vars.get("COOKIE_X-companyname-session")+ vars.get("webServer"));

					BasicClientCookie clientCookie = new BasicClientCookie("X-companyname-session", vars.get("COOKIE_X-companyname-session"));
					/*Prasnna Commented below set.path lines to remove Cookie rejected warning from jmeter logs. */
					clientCookie.setDomain(vars.get("webServer"));
					clientCookie.setPath("/");
					cookieStore.addCookie(clientCookie);
					BasicClientCookie jsessionCookie = new BasicClientCookie("JSESSIONID", vars.get("COOKIE_JSESSIONID"));
					jsessionCookie.setDomain(vars.get("webServer"));
					jsessionCookie.setPath("/system");
					cookieStore.addCookie(jsessionCookie);
					BasicClientCookie awselbCookie = new BasicClientCookie("AWSELB", vars.get("COOKIE_AWSELB"));
					awselbCookie.setDomain(vars.get("webServer"));
					awselbCookie.setPath("/");
					cookieStore.addCookie(awselbCookie);

					CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
					CloseableHttpResponse response = null;
					try {
						HttpPost httppost = new HttpPost(strURL);
						//httppost.setConfig(config);
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("client_type","js"));
						params.add(new BasicNameValuePair("client_conn_type","non-persistent"));
						params.add(new BasicNameValuePair("partition_id","1"));
						params.add(new BasicNameValuePair("poll_wait_time","30000"));
						params.add(new BasicNameValuePair("poll_freq","500"));
						params.add(new BasicNameValuePair("__eg_request_token__",vars.get("lt_X-companyname-csrf")));
						params.add(new BasicNameValuePair("topic_name",""));
						params.add(new BasicNameValuePair("recovery","false"));
						params.add(new BasicNameValuePair("estr","CB2-1"));
						params.add(new BasicNameValuePair("auth_key",""));
						params.add(new BasicNameValuePair("cnt",v_count.toString()));
						params.add(new BasicNameValuePair("timestamp",timestamp.toString()));
						params.add(new BasicNameValuePair("uilogs","UI logger not initialized"));
						params.add(new BasicNameValuePair("user_id","1\$"+vars.get("lt_userID")));
						httppost.setEntity(new UrlEncodedFormEntity(params));

						response = httpclient.execute(httppost);
						int respCode = -1;
						respCode = (response!=null && response.getStatusLine()!=null)?response.getStatusLine().getStatusCode():-1;
						if (respCode >500)
						{
							respCodeRetry++;
							v_count--; // decrementing the pollcount for retry
							log.info("HTTP Status Code "+respCode+" recieved, retry Count:"+respCodeRetry);
							Thread.sleep(30000);
							if (respCodeRetry >=10)
							{
								log.info("Consecutive 10 retries failed hence stopping the thread");
								break;
							}
						}
						else
						{
							respCodeRetry=0;
							String ResponseCode = EntityUtils.toString(response.getEntity());
							log.info("lt_X-companyname-csrf:- "+vars.get("lt_X-companyname-csrf"));
	
							if(ResponseCode.equals("404")){
								log.info("Stopping the mr_pushlet request due to 404 or 500 response for agent: " + vars.get("lt_username") + ". Response: " +  ResponseCode);
								break;
							}
							if (ResponseCode.equals("500"))
							{
								retryBody500++;
								v_count--; // decrementing the pollcount for retry
								log.info("mr_pushlet request returned 500 response for agent: " + vars.get("lt_username") + ". Response: " +  ResponseCode);
								log.info("Request URL is  " +strURL);
								if (retryBody500>=10)
								{
									log.info("mr_pushlet gave consecutive 500, stopping the thread "+retryBody500+ "agent: " + vars.get("lt_username"));
									break;
								}
							}
							else
							{
								// resetting the response code if 500 is not consecutive
								retryBody500=0;
							}
						}

						
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (response!=null)
								response.close();
						if (httpclient!=null)
							httpclient.close();
					}
					v_count++;
					Thread.sleep(5);
				}
				Thread.currentThread().interrupt();
			}
		});
pool.shutdown();