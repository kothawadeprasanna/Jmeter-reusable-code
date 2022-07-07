package companyname.lt.chat;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.http.conn.ssl.TrustStrategy;

public class LTTrustStrategy implements TrustStrategy {
	public static final LTTrustStrategy INSTANCE = new LTTrustStrategy();

	public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		// trust all
		return true;
	}
}
