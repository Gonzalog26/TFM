package es.tekniker.eefrmwrk.config;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ObtainProxy {

	private static Log log = LogFactory.getLog(ObtainProxy.class);

	public static void setProxy(String host, String port, String user, String password ) {
		if ( host== null || host.isEmpty()) {
			log.info("No proxy configured");
		}
		else if (host.equals("system")){
			log.info("Setting system default proxies");
			System.setProperty("java.net.useSystemProxies", "true");
			List<Proxy> l = null;
			try {
				l = ProxySelector.getDefault().select(new URI("http://www.yahoo.com"));
			}
			catch (URISyntaxException e) {
			  e.printStackTrace();
			}
	
			if (l != null) {
			   for (Proxy proxy: l) {
			      log.info("proxy type : " + proxy.type());
			      InetSocketAddress addr = (InetSocketAddress) proxy.address();
			      if (addr == null) {
			    	  log.warn("No System Proxy configured");
			      } 
			      else {
			        log.info("proxy hostname : " + addr.getHostName());
			        log.info("proxy port : " + addr.getPort());
			      }
			   }
			}
		}
		else {
			log.info("Setting custom proxy to host: " + host + " port: " + port);
	    	System.getProperties().put("proxySet", "true");
	    	System.getProperties().put("proxyPort", port);
	    	System.getProperties().put("proxyHost", host);
	    	System.setProperty("http.proxyPort", port);
	    	System.setProperty("http.proxyHost", host);
		}
		if (user != null && !user.isEmpty()){
			log.info("Setting authentication to user: " + user);
	    	Authenticator.setDefault( new HttpAunthenticateProxy(user, password) );
		}
    }
}

