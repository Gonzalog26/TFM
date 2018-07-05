package es.tekniker.eefrmwrk.mqtt.mqttsubscriber;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import es.tekniker.eefrmwrk.config.ConfigFile;
import es.tekniker.eefrmwrk.mqtt.subscriberdb.Subscriberdb;

public class MqttSubscriber  {

	private static final Log log = LogFactory.getLog(MqttSubscriber.class);
		
	ConfigFile configFile;
	String filePath="MqttSubscriber.properties";
	
	MqttClient client;
	String suscriptor;
	static String host;
	static String port;
	static String brokerSSLUrl;
	static String topic;
	static String defaultPersistenceFile;
	private int qos=1;
	
	private boolean certsenabled;
	private static String CAcert;
	private static String subscriberCert;
	private static String subscriberKey;
	
	MqttConnectOptions connOpt;		

	public MqttSubscriber() {
		try {
			
			log.debug("Starting the constructor of mqttsubscriber");
			log.debug("Getting configuration params from "+filePath);
			
			configFile = new ConfigFile(filePath);
	
			host = configFile.getStringParam("mqtt.host");
			
			if(host==null || host.isEmpty()){
				log.warn("No entry found for 'host' in configure file. Host "+host+" set by default");
				host="localhost";
			}
			
			port = configFile.getStringParam("mqtt.port");
			
			if(port==null || port.isEmpty()){
				log.warn("No entry found for 'port' in configure file. Port "+port+" set by default");
				host="8883";
			}
			
			certsenabled = Boolean.valueOf(configFile.getStringParam("mqtt.certsenabled"));
			
			if(certsenabled)
				brokerSSLUrl = "ssl://"+host+":"+port;
			else
				brokerSSLUrl = "tcp://"+host+":"+port;
			
			topic = configFile.getStringParam("mqtt.topic");
			
			if(topic==null || topic.isEmpty()){
				log.warn("No entry found for 'topic' in configure file. Topic "+topic+" set by default");
				topic="LivingLab/variables";
			}
			
			qos = configFile.getIntParam("mqtt.qos");
			
			suscriptor = configFile.getStringParam("mqtt.subscriber1");
			
			if(suscriptor==null || topic.isEmpty()){
				suscriptor="suscriptor1";
				log.warn("No entry found for 'suscriptor' in configure file. Suscriptor "+suscriptor+" set by default");
			}	
			
			defaultPersistenceFile = configFile.getStringParam("mqtt.defaultpersistencefile");
			
			if(defaultPersistenceFile==null || defaultPersistenceFile.isEmpty()){
				defaultPersistenceFile="/opt/tomcat/logs/LivingLab";
				log.warn("No entry found for 'defaultpersistencefile' in configure file. Default persistence file "+defaultPersistenceFile+" set by default");
			}	
			
			if(certsenabled) {
				
				CAcert= configFile.getStringParam("CAcert");
				
				if(CAcert==null || CAcert.isEmpty()){
					CAcert="/home/tomcatlogs/certAuthcert/cacert.pem";
					log.warn("No entry found for 'CAcert' in configure file. Default path to CAcert "+CAcert+" set by default");
				}	
				
				subscriberCert= configFile.getStringParam("mqtt.cert");
				
				if(subscriberCert==null || subscriberCert.isEmpty()){
					subscriberCert="/home/tomcatlogs/mqttSubscribercert/cert.pem";
					log.warn("No entry found for 'subscriberCert' in configure file. Default path to subscriberCert "+subscriberCert+" set by default");
				}	
				
				subscriberKey= configFile.getStringParam("mqtt.privatekey");
				
				if(subscriberKey==null || subscriberKey.isEmpty()){
					subscriberKey="home/tomcatlogs/mqttSubscribercert/key.pem";
					log.warn("No entry found for 'subscriberKey' in configure file. Default path to subscriberKey "+subscriberKey+" set by default");
				}
				
			}
		
			log.info("Config set: brokerUrl: "+brokerSSLUrl+", topic: "+topic+", qos: "+qos+", suscriptor: "+suscriptor+", default persistence file: "+defaultPersistenceFile);
			
			subscribe();			
			
		}catch (IOException e) {
			log.error("Unable to set configuration from "+filePath, e);
		}
		catch (MqttException e) {
			log.error("Connection with the broker MQTT failed. Finish.", e);
		}	
		catch (Exception e){
			log.error("Unknown error", e);
		}
		
	}

	public static void main(String[] args) {
	    new MqttSubscriber();
	}

	public void subscribe() throws Exception {
	    try {

		log.debug("Setting connection options...");
		/* Configuration options needed:
	    * Session between broker & subscriber (allows persistence for the information saved in the queues),
	    * Automatic reconnect (allows a reconnection if there is a connection lost)
	    * and SSL configuration option (Get the certificates and keys to establish an encrypted connection)*/
		connOpt = new MqttConnectOptions();	

		connOpt.setCleanSession(false);
		connOpt.setAutomaticReconnect(true);
		connOpt.setUserName("develcoSubscriber");
		connOpt.setPassword("Sii206744+".toCharArray());
		log.debug("Clean session set to FALSE & automaticReconnect set to TRUE");

		if(certsenabled) {
			
			log.debug("Setting SSL options");
			SSLSocketFactory socketFactory = getSocketFactory(CAcert,subscriberCert,subscriberKey, "Sii206744+");
			connOpt.setSocketFactory(socketFactory);
			log.debug("SSL conection options set");
			log.debug("Connection options set");
			
		}
		
		
	
		log.debug("Creating default file persistence");		
		/*It allows the subscriber to keep session information. 
		 * If there is a connection failure, the session can be reset as it was left*/
		
		log.debug("Creating MQTT client...");
		client = new MqttClient(brokerSSLUrl,suscriptor);
		log.debug("Client created");
		
		log.debug("Connecting to broker...");
		client.connect(connOpt);
		log.info("Client connected to "+brokerSSLUrl);
		
		client.setCallback(new Subscriberdb());
		
		
		client.subscribe(topic,qos);
		log.info("Subscriber "+suscriptor+" is now subscribed to topic: "+topic+" of broker "+brokerSSLUrl+" with qos "+qos);
			
	    } catch (MqttException e) {
	    	log.error("Connection( host: "+host+" port: "+port+" ) or subsciption( Topic: "+topic+" ) failed", e);
	    	throw e;
		}
	    catch(Exception e){
	    	log.error("Unknown error while connecting or subscribing", e);
	    	throw e;
	    }
	   
	}
	
	private static SSLSocketFactory getSocketFactory(final String caCrtFile,
			final String crtFile, final String keyFile, final String password)
			throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		// load CA certificate
		X509Certificate caCert = null;

		FileInputStream fis = new FileInputStream(caCrtFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		while (bis.available() > 0) {
			caCert = (X509Certificate) cf.generateCertificate(bis);
		}

		// load client certificate
		bis = new BufferedInputStream(new FileInputStream(crtFile));
		X509Certificate cert = null;
		while (bis.available() > 0) {
			cert = (X509Certificate) cf.generateCertificate(bis);
		}

		// load client private key
		PEMParser pemParser = new PEMParser(new FileReader(keyFile));
		Object object = pemParser.readObject();
		PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
				.build(password.toCharArray());
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
				.setProvider("BC");
		KeyPair key;
		if (object instanceof PEMEncryptedKeyPair) {
			log.debug("Encrypted key - we will use provided password");
			key = converter.getKeyPair(((PEMEncryptedKeyPair) object)
					.decryptKeyPair(decProv));
		} else {
			log.debug("Unencrypted key - no password needed");
			key = converter.getKeyPair((PEMKeyPair) object);
		}
		pemParser.close();

		// CA certificate is used to authenticate server
		KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
		caKs.load(null, null);
		caKs.setCertificateEntry("ca-certificate", caCert);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
		tmf.init(caKs);

		// client key and certificates are sent to server so it can authenticate
		// us
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		ks.setCertificateEntry("certificate", cert);
		ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
				new java.security.cert.Certificate[] { cert });
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
				.getDefaultAlgorithm());
		kmf.init(ks, password.toCharArray());

		// finally, create SSL socket factory
		SSLContext context = SSLContext.getInstance("TLSv1.2");
		context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

		return context.getSocketFactory();
	}
	
	

}
