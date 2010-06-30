package cc.co.phptunnelproxy.ptplocal.net.ssl;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509KeyManager;

public class AliasKeyManager implements X509KeyManager {
	private KeyStore keyStore;
	private String alias;
	private char[] ctpassword;

	AliasKeyManager(KeyStore keyStore, char[] ctpassword, String alias)
			throws IOException, GeneralSecurityException {
		this.keyStore = keyStore;
		this.alias = alias;
		this.ctpassword = ctpassword;
	}

	private String getCommonAlias(String alias) {
		String[] tokens = alias.split("\\.");
		if (tokens.length > 2) {
			return "*." + tokens[tokens.length - 2] + "."
					+ tokens[tokens.length - 1];
		} else if (tokens.length == 2) {
			return "*." + alias;
		} else {
			return alias;
		}
	}

	public PrivateKey getPrivateKey(String alias) {
		try {
			PrivateKey key = (PrivateKey) keyStore.getKey(alias, ctpassword);

			if (key == null) {
				key = (PrivateKey) keyStore.getKey(getCommonAlias(alias),
						ctpassword);
			}

			if (key == null) {
				key = (PrivateKey) keyStore.getKey("ptproot", ctpassword);
			}
			return key;
		} catch (Exception e) {
			return null;
		}
	}

	public X509Certificate[] getCertificateChain(String alias) {
		try {
			java.security.cert.Certificate[] certs = keyStore
					.getCertificateChain(alias);
			if (certs == null || certs.length == 0)
				certs = keyStore.getCertificateChain(getCommonAlias(alias));
			if (certs == null || certs.length == 0)
				certs = keyStore.getCertificateChain("ptproot");
			X509Certificate[] x509 = new X509Certificate[certs.length];
			for (int i = 0; i < certs.length; i++)
				x509[i] = (X509Certificate) certs[i];
			return x509;
		} catch (Exception e) {
			return null;
		}
	}

	public String chooseServerAlias(String keyType, Principal[] issuers,
			Socket socket) {
		return alias;
	}

	public String[] getClientAliases(String parm1, Principal[] parm2) {
		throw new UnsupportedOperationException(
				"Method getClientAliases() not yet implemented.");
	}

	public String chooseClientAlias(String keyTypes[], Principal[] issuers,
			Socket socket) {
		throw new UnsupportedOperationException(
				"Method chooseClientAlias() not yet implemented.");
	}

	public String[] getServerAliases(String parm1, Principal[] parm2) {
		return new String[] { alias };
	}

	public String chooseServerAlias(String parm1, Principal[] parm2) {
		return alias;
	}
}
