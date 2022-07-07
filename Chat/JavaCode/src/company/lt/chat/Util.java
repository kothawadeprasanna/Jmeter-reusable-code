package companyname.lt.chat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.SAXException;

public class Util {
	private long rid;
	private boolean enableProxy = false;
	private boolean isDebug = false;
	private String threadNumber = "";
	private long lastReqtime = 0;
	private long lastResptime = 0;
	private String proxyServer = null;
	private int proxyPort = 0;
	private String protocol = "http";
	private SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private CookieStore cookieStore = new BasicCookieStore();
	String ridPlaceHolder = "#__RID_PLACEHOLDER__#";
	private long retrySleepTime = 30000;
	private int numberOfRetries = 10;

	Util(long id, boolean enableProxy, String threadNumber, String proxyServer, int proxyPort, String protocol,
			boolean isDebug) {
		rid = id;
		this.enableProxy = enableProxy;
		this.threadNumber = threadNumber;
		lastReqtime = 0;
		lastResptime = 0;
		this.proxyServer = proxyServer;
		this.proxyPort = proxyPort;
		this.protocol = protocol;
		this.isDebug = isDebug;
	}

	Util(long id, boolean enableProxy, String threadNumber, String proxyServer, int proxyPort, String protocol) {
		rid = id;
		this.enableProxy = enableProxy;
		this.threadNumber = threadNumber;
		lastReqtime = 0;
		lastResptime = 0;
		this.proxyServer = proxyServer;
		this.proxyPort = proxyPort;
		this.protocol = protocol;
	}

	synchronized long getRid() {
		rid++;
		return rid;
	}

	synchronized long getLastReqTime() {
		return lastReqtime;
	}

	synchronized void setLastReqTime() {
		lastReqtime = System.currentTimeMillis();
	}

	String SendMsg(String url, String xml, boolean computeRid, boolean isPoll, String agentAssigned) {

		HttpPost urlPost = null;
		CloseableHttpClient httpclient = null;
		SSLContextBuilder builder = null;
		SSLConnectionSocketFactory sslcsf = null;
		CloseableHttpResponse response = null;
		String resp = null;
		boolean sendMsg = true;
		int retryCount = 0;
		try {
			builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new LTTrustStrategy());
			sslcsf = new SSLConnectionSocketFactory(builder.build());
			urlPost = new HttpPost(url);
			if (enableProxy) {
				HttpHost proxy = new HttpHost(proxyServer, proxyPort, "http");
				RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
				urlPost.setConfig(config);
			}
			httpclient = HttpClients.custom().setSSLSocketFactory(sslcsf).setDefaultCookieStore(cookieStore).build();
			while (retryCount < numberOfRetries && sendMsg) {
				long t1 = System.currentTimeMillis();
				if (computeRid)
					xml = xml.replaceAll(ridPlaceHolder, String.valueOf(getRid()));
				if (isDebug)
					System.out.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
							+ threadNumber + " Time " + sd.format(System.currentTimeMillis()) + " SendMsg XML is "
							+ xml.replaceAll("\\n", ""));
				urlPost.setEntity(new StringEntity(xml, ContentType.TEXT_XML));
				setLastReqTime();
				response = httpclient.execute(urlPost);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode > 500) {
					System.err.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
							+ threadNumber + " Time " + sd.format(System.currentTimeMillis())
							+ " SendMsg Respose Code is " + statusCode + " RetryCount " + retryCount);
					retryCount++;
					sendMsg = true;
					Thread.sleep(retrySleepTime);

				} else {
					if (statusCode > 299)
					{
						System.err.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
								+ threadNumber + " Time " + sd.format(System.currentTimeMillis())
								+ " SendMsg Respose Code is " + statusCode);
						sendMsg = false;
					} else {
						sendMsg = false;
						resp = EntityUtils.toString(response.getEntity());
						lastResptime = System.currentTimeMillis();
						if (isDebug)
							System.out.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
									+ threadNumber + " Time " + sd.format(System.currentTimeMillis())
									+ " SendMsg Respose is " + resp.replaceAll("\\n", ""));
					}
				}
			}
		} catch (IOException | InterruptedException | KeyManagementException | NoSuchAlgorithmException |KeyStoreException e) {
			System.out.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
					+ threadNumber + " Time " + sd.format(System.currentTimeMillis()) + " Exception in SendMsg: " + e);
			e.printStackTrace();
		} finally {
			try {
				if (response != null)
					response.close();
				if (httpclient != null)
					httpclient.close();
			} catch (Exception e) {
				// DO Nothing as exception occurred in finally.
			}
		}
		return resp;
	}

	Response processResponse(String response, String sid, String agentAssigned)
			throws ParserConfigurationException, SAXException, IOException {
		// GPathResult resp = null;
		SAXBuilder saxBuilder = null;
		Response resp = null;
		Document xmlDocument = null;

		try {
			if (response != null && response.length() > 0) {
				saxBuilder = new SAXBuilder();

				xmlDocument = saxBuilder.build(new ByteArrayInputStream(response.getBytes("UTF-8")));
				if (isDebug)
					System.err.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
							+ threadNumber + " Time " + sd.format(System.currentTimeMillis()) + " Parsing Response: "
							+ response.replaceAll("\\n", ""));

			}
			if (xmlDocument != null) {
				Element root = xmlDocument.getRootElement();
				if (sid == null && root.getAttribute("sid") != null) {
					sid = root.getAttribute("sid").getValue();
					if (isDebug)
						System.err.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
								+ threadNumber + " Time " + sd.format(System.currentTimeMillis()) + " Sid Found: "
								+ sid);
				}
				resp = new Response(threadNumber, sid);

				if (root.getAttribute("type") != null && "terminate".equals(root.getAttribute("type").getValue())) {
					resp.setResponseType("terminate");
					if (isDebug)
						System.err.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
								+ threadNumber + " Time " + sd.format(System.currentTimeMillis())
								+ " Response Type is terminate. Stopping Customer Poll Requests. Check App Server logs with SID: "
								+ "Response is " + response);
				} else {
					Namespace ns4 = Namespace.getNamespace("ns4", "jabber:client");
					Namespace ns2 = Namespace.getNamespace("ns2", "http://bindings.companyname.com/chat");
					List<Element> temp = root.getChildren("message", ns4);
					if (temp != null && !temp.isEmpty()) {
						if (isDebug)
							System.out.println("Username of the assigned agent" + agentAssigned
									+ " Customer: ThreadNumber:" + threadNumber + " Time "
									+ sd.format(System.currentTimeMillis()) + " resp.message is " + temp);
						for (Element msg : temp) {
							if ("headline".equals(msg.getAttribute("type").getValue())
									&& "AgentInitiateAttachment".equals(msg.getChild("body", ns4).getValue())) {
								Element companynameCmd = msg.getChild("companynameCommand", ns2);
								List<Element> companynameAddionalParams = companynameCmd.getChildren("companynameAdditionalParam", ns2);
								Attachment attach = new Attachment(threadNumber, enableProxy, proxyServer, proxyPort,
										protocol, isDebug);
								for (Element companynameAddionalParam : companynameAddionalParams) {
									if ("attachmentName"
											.equals(companynameAddionalParam.getChild("paramKey", ns2).getValue()))
										attach.setAttachmentName(
												companynameAddionalParam.getChild("paramValue", ns2).getValue());
									if ("attachmentId".equals(companynameAddionalParam.getChild("paramKey", ns2).getValue()))
										attach.setAttachmentID(
												companynameAddionalParam.getChild("paramValue", ns2).getValue());
									if ("attachmentType"
											.equals(companynameAddionalParam.getChild("paramKey", ns2).getValue()))
										attach.setAttachmentType(
												companynameAddionalParam.getChild("paramValue", ns2).getValue());
									resp.setStopSessionStats(true);
									if (isDebug)
										System.out.println("Username of the assigned agent" + agentAssigned
												+ " Customer: ThreadNumber:" + threadNumber + " Time "
												+ sd.format(System.currentTimeMillis()) + " stopSessionStats= true");
								}
								resp.addAttachmentToResponse(attach);

								if (isDebug)
									System.out.println("Username of the assigned agent" + agentAssigned
											+ " Customer: ThreadNumber:" + threadNumber + " Time "
											+ sd.format(System.currentTimeMillis()) + " Attachment found : " + attach);
							}

							else if ("headline".equals(msg.getAttribute("type").getValue())) {
								if (resp == null) {
									resp = new Response(threadNumber, sid);
								}
								Element companynameCmd = msg.getChild("companynameCommand", ns2);
								String companynameSubCmd = companynameCmd.getChild("subcmd", ns2) != null
										? companynameCmd.getChild("subcmd", ns2).getValue() : null;
								if ("agentPickup".equals(companynameSubCmd)) {
									Element body = msg.getChild("body", ns4);
									String bodyText = body.getValue();
									if (bodyText != null && !"".equals(bodyText.trim())) {
										String[] bodyArr = bodyText.split("\\s");
										int len = bodyArr.length;
										if (isDebug)
											System.out.println("Setting Agent Name as " + bodyArr[len - 1]);
										resp.setAgentName(bodyArr[len - 1]);
										if (isDebug)
											System.out.println("Agent Name in response " + resp.getAgentName());
									}
								}
								resp.setStopSessionStats(true);
								if (isDebug)
									System.out.println("Username of the assigned agent" + agentAssigned
											+ " Customer: ThreadNumber:" + threadNumber + " Time "
											+ sd.format(System.currentTimeMillis()) + " stopSessionStats= true");
							}
							/*
							 * if
							 * ("headline".equals(msg.getAttributeValue("type"))
							 * ) { if (resp == null) { resp = new
							 * Response(threadNumber, sid); }
							 * resp.setStopSessionStats(true); List<Element>
							 * msgElements = msg.getChildren(); boolean
							 * processAttach = false; if (msgElements.size() >0)
							 * { for (Element msgElem :msgElements ) { if
							 * ("AgentInitiateAttachment".equals(msgElem.
							 * getAttributeValue("body"))) {
							 *
							 * } } } }
							 */
						}
					}
				}
			}
		} catch (JDOMException e) {

			System.err.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
					+ threadNumber + " Time " + sd.format(System.currentTimeMillis())
					+ " Parsing Issue occurred for Response: " + response);
			e.printStackTrace();
		}
		return resp;
	}

	public void setCookieStoretoUse(CookieStore cookieStore) {
		this.cookieStore = cookieStore;
	}

	public CookieStore getCookieStoretoUse() {
		return cookieStore;
	}

	public String sendAttachmentNotification(String url, String sid, String attachmentName, String customerIdentity,
			int number, int attachmentSize, String agentAssigned) {
		HttpPost urlPost = null;
		CloseableHttpClient httpclient = null;
		SSLContextBuilder builder = null;
		SSLConnectionSocketFactory sslcsf = null;
		CloseableHttpResponse response = null;
		String resp = null;
		long timestamp = (new Date()).getTime();
		String uniqueId = Long.toString(timestamp) + Integer.toString(number);
		String bodyText = "sid=" + sid + "&customerIdentity=" + customerIdentity + "&attachmentName=" + attachmentName
				+ "&attachmentInternalName=" + uniqueId + "_" + attachmentName + "&attachmentId=" + uniqueId
				+ "&attachmentSize=" + attachmentSize;
		try {
			builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new LTTrustStrategy());
			sslcsf = new SSLConnectionSocketFactory(builder.build());
			urlPost = new HttpPost(url);
			if (enableProxy) {
				HttpHost proxy = new HttpHost(proxyServer, proxyPort, "http");
				RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
				urlPost.setConfig(config);
			}
			httpclient = HttpClients.custom().setSSLSocketFactory(sslcsf).setDefaultCookieStore(cookieStore).build();
			long t1 = System.currentTimeMillis();
			if (isDebug)
				System.out.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
						+ threadNumber + " Time " + sd.format(System.currentTimeMillis()) + " SendMsg Body is "
						+ bodyText.replaceAll("\\n", ""));
			urlPost.setEntity(new StringEntity(bodyText, ContentType.APPLICATION_FORM_URLENCODED));
			setLastReqTime();
			response = httpclient.execute(urlPost);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode > 299)
				System.err.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
						+ threadNumber + " Time " + sd.format(System.currentTimeMillis()) + " SendMsg Respose Code is "
						+ statusCode);
			resp = EntityUtils.toString(response.getEntity());
			lastResptime = System.currentTimeMillis();
			if (isDebug)
				System.out.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
						+ threadNumber + " Time " + sd.format(System.currentTimeMillis()) + " SendMsg Respose is "
						+ resp.replaceAll("\\n", ""));
		} catch (Exception e) {
			System.out.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
					+ threadNumber + " Time " + sd.format(System.currentTimeMillis()) + " Exception in SendMsg: " + e);
			e.printStackTrace();
		} finally {
			try {
				if (response != null)
					response.close();
				if (httpclient != null)
					httpclient.close();
			} catch (Exception e) {
				// DO Nothing as exception occurred in finally.
			}
		}
		return resp;
	}

	public String sendAttachment(String url, String sid, String agentName, String attachmentFilePath,
			String attachmentName, String fileId, String attachmentSize, String agentAssigned) {
		HttpPost urlPost = null;
		CloseableHttpClient httpclient = null;
		SSLContextBuilder builder = null;
		SSLConnectionSocketFactory sslcsf = null;
		CloseableHttpResponse response = null;
		String resp = null;
		try {
			builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new LTTrustStrategy());
			sslcsf = new SSLConnectionSocketFactory(builder.build());
			urlPost = new HttpPost(url);
			if (enableProxy) {
				HttpHost proxy = new HttpHost(proxyServer, proxyPort, "http");
				RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
				urlPost.setConfig(config);
			}
			httpclient = HttpClients.custom().setSSLSocketFactory(sslcsf).setDefaultCookieStore(cookieStore).build();
			MultipartEntityBuilder multipartBuilder = MultipartEntityBuilder.create();
			multipartBuilder.addTextBody("sid", sid, ContentType.MULTIPART_FORM_DATA);
			multipartBuilder.addTextBody("agentName", agentName, ContentType.MULTIPART_FORM_DATA);
			multipartBuilder.addTextBody("fileSize", attachmentSize, ContentType.MULTIPART_FORM_DATA);
			multipartBuilder.addTextBody("fileId", fileId, ContentType.MULTIPART_FORM_DATA);
			File f = new File(attachmentFilePath);
			multipartBuilder.addBinaryBody(fileId + "_" + attachmentName, new FileInputStream(f),
					ContentType.APPLICATION_OCTET_STREAM, attachmentName);
			HttpEntity multipart = multipartBuilder.build();
			long t1 = System.currentTimeMillis();
			if (isDebug)
				System.out.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
						+ threadNumber + " Time " + sd.format(System.currentTimeMillis()));
			urlPost.setEntity(multipart);
			setLastReqTime();
			response = httpclient.execute(urlPost);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode > 299)
				System.err.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
						+ threadNumber + " Time " + sd.format(System.currentTimeMillis()) + " SendMsg Respose Code is "
						+ statusCode);
			resp = EntityUtils.toString(response.getEntity());
			lastResptime = System.currentTimeMillis();
			if (isDebug)
				System.out.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
						+ threadNumber + " Time " + sd.format(System.currentTimeMillis()) + " SendMsg Respose is "
						+ resp.replaceAll("\\n", ""));
		} catch (Exception e) {
			System.out.println("Username of the assigned agent" + agentAssigned + " Customer: ThreadNumber:"
					+ threadNumber + " Time " + sd.format(System.currentTimeMillis()) + " Exception in SendMsg: " + e);
			e.printStackTrace();
		} finally {
			try {
				if (response != null)
					response.close();
				if (httpclient != null)
					httpclient.close();
			} catch (Exception e) {
				// DO Nothing as exception occurred in finally.
			}
		}
		return resp;
	}
}