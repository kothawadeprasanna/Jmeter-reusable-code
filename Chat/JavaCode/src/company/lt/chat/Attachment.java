package companyname.lt.chat;

import java.text.SimpleDateFormat;

import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;

class Attachment {
	String attachmentName;
	String attachmentID;
	String attachmentType;
	String threadNumber;
	boolean enableProxy = false;
	String proxyServer;
	String protocol;
	int proxyPort = 0;
	boolean isDebug = false;
	private SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private CookieStore cookieStore = new BasicCookieStore();

	Attachment(String threadNumber, boolean enableProxy, String proxyServer, int proxyPort, String protocol,
			boolean isDebug) {
		this.threadNumber = threadNumber;
		this.enableProxy = enableProxy;
		this.proxyServer = proxyServer;
		this.proxyPort = proxyPort;
		this.protocol = protocol;
		this.isDebug = isDebug;
	}

	RunContext context = new RunContext();
	// Attachment(String threadNumber, boolean enableProxy, String proxyServer,
	// int proxyPort, String protocol) {
	// this.threadNumber = threadNumber;
	// this.enableProxy = enableProxy;
	// this.proxyServer= proxyServer;
	// this.proxyPort=proxyPort;
	// this.protocol=protocol;
	// }

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public String getAttachmentID() {
		return attachmentID;
	}

	public void setAttachmentID(String attachmentID) {
		this.attachmentID = attachmentID;
	}

	public String getAttachmentType() {
		return attachmentType;
	}

	public void setAttachmentType(String attachmentType) {
		this.attachmentType = attachmentType;
	}

	public void processAttachment(String url, String sid, String chatQueueId, CookieStore cookieStore) {
		this.cookieStore = cookieStore;
		processAttachment(url, sid, chatQueueId);
	}

	private void processAttachment(String url, String sid, String chatQueueId) {
		HttpPost acceptAttachmentURLPost = null;
		CloseableHttpClient httpclient = null;
		SSLContextBuilder builder = null;
		SSLConnectionSocketFactory sslcsf = null;
		CloseableHttpResponse response = null;
		try {
			builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new LTTrustStrategy());
			sslcsf = new SSLConnectionSocketFactory(builder.build());
			if (attachmentID != null && !"".equals(attachmentID)) {
				if (isDebug)
					System.out.println(
							"AgentName :" + context.getSharedData("agent_assigned") + " Customer: ThreadNumber:"
									+ threadNumber + " Time " + sd.format(System.currentTimeMillis())
									+ "Found Attachment in typing response" + "Attachment Name: " + attachmentName
									+ " Attachment ID: " + attachmentID + " AttachmentType: " + attachmentType);

				String acceptAttachmentURL = url + attachmentID;
				builder.loadTrustMaterial(null, new LTTrustStrategy());
				acceptAttachmentURLPost = new HttpPost(acceptAttachmentURL);
				if (enableProxy) {
					HttpHost proxy = new HttpHost(proxyServer, proxyPort, "http");
					RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
					acceptAttachmentURLPost.setConfig(config);
				}
				String acceptAttachmentBody = "sid=" + sid + "&entryPointId=" + chatQueueId + "&fileId=" + attachmentID
						+ "&fileName=" + attachmentName;
				StringEntity entity = new StringEntity(acceptAttachmentBody);
				entity.setContentType(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
				acceptAttachmentURLPost.setEntity(entity);
				CloseableHttpClient httpclient1 = HttpClients.custom().setSSLSocketFactory(sslcsf)
						.setDefaultCookieStore(cookieStore).build();
				CloseableHttpResponse responseAttach = null;
				responseAttach = httpclient1.execute(acceptAttachmentURLPost);
			}

		} catch (Exception e) {
			System.out.println("AgentName :" + context.getSharedData("agent_assigned") + " Customer: ThreadNumber:"
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
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (attachmentName != null)
			sb.append("Attachment: [AttachmentName: ").append(attachmentName).append(", AttachmentID: ")
					.append(attachmentID).append(", AttachmentType: ").append(attachmentType).append("]");
		return sb.toString();
	}
}
