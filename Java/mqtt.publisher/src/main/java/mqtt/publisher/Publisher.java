package mqtt.publisher;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import es.tekniker.eefrmwrk.config.ConfigFile;

public class Publisher {
	
	private static final Log log = LogFactory.getLog(Publisher.class);
	
	ConfigFile configFile;
	String filePath="MqttPublisher.properties";

	List<MqttClient> clients = new ArrayList<MqttClient>();
	int numberOfClients;
	int numberOfMessagesPerClient;
	int numberOfPublishMessages=0;
	
	private long timestamp_inicial;
	private long timestamp_final;
	
	String publisher;
	static String host;
	static String port;
	static String userName;
	static String passwd;
	static String brokerUrl; 
	static String topic;
	static String mensaje;
	private int qos;
	
	private boolean certsenabled;
	private static String CAcert;
	private static String subscriberCert;
	private static String subscriberKey;
	
	MqttMessage mqttMessage;
	
	MqttConnectOptions connOpt;
	
	public Publisher() {
		
		try {
			configFile = new ConfigFile(filePath);
			
			host = configFile.getStringParam("mqtt.host");
			
			if(host==null || host.isEmpty()){
				host="10.0.0.112";
				log.warn("No entry found for 'host' in configure file. Host "+host+"set by default");
			}
			
			port = configFile.getStringParam("mqtt.port");
			
			if(port==null || port.isEmpty()){
				port="1883";
				log.warn("No entry found for 'port' in configure file. Port "+port+"set by default");
			}
			
			certsenabled = Boolean.valueOf(configFile.getStringParam("mqtt.certsenabled"));
			
			if(certsenabled)
				brokerUrl = "ssl://"+host+":"+port;
			else
				brokerUrl = "tcp://"+host+":"+port;
			
			log.info("BrokerUrl: "+brokerUrl);
						
			topic = configFile.getStringParam("mqtt.topic");
			
			if(topic==null || topic.isEmpty()){
				topic="MtConnect";
				log.warn("No entry found for 'topic' in configure file. Topic "+topic+"set by default");		
			}
			
			qos = configFile.getIntParam("mqtt.qos");
			
			publisher = configFile.getStringParam("mqtt.publisher1");
			
			if(publisher==null || publisher.isEmpty()){
				publisher="publisher";
				log.warn("No entry found for 'publisher' in configure file. Publisher "+publisher+"set by default");
			}	
			
			userName = configFile.getStringParam("mqtt.userName");
			
			if(userName==null || userName.isEmpty()){
				userName="publicador";
				log.warn("No entry found for 'userName' in configure file. userName "+userName+"set by default");
			}

			passwd = configFile.getStringParam("mqtt.passwd");
			
			if(passwd==null || passwd.isEmpty()){
				passwd="publicador";
				log.warn("No entry found for 'passwd' in configure file. passwd "+passwd+"set by default");
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
			
			numberOfClients = Integer.parseInt(configFile.getStringParam("mqtt.numberClients"));
			
			numberOfMessagesPerClient = Integer.parseInt(configFile.getStringParam("mqtt.numberMessages"));
			
			System.out.println("Number of Publishers: "+numberOfClients);
			System.out.println("Number of MessagesPerClient: "+numberOfMessagesPerClient);
			
		} catch (IOException e) {
			log.error("Unable to set configuration from "+filePath, e);
		}		
		
	}

	public static void main(String[] args) {
	    	 try {
				new Publisher().publish();
			} catch (MqttException e) {
				log.error("Connection with the broker MQTT failed. Finish.", e);
			}
	}
	
	public void publish() throws MqttException{
									   
			MemoryPersistence persistence = new MemoryPersistence();
		
		    try {
		    	
		    	for(int i=0;i<numberOfClients;i++) {
		    		
		    		clients.add(new MqttClient(brokerUrl, publisher+"_"+i,persistence));        
			        MqttConnectOptions connOpts = new MqttConnectOptions();
			        connOpts.setCleanSession(false);
			        connOpts.setMaxInflight(10000);
			        connOpts.setUserName(userName);
			        connOpts.setPassword(passwd.toCharArray());
			        connOpts.setAutomaticReconnect(true);
			        
			        if(certsenabled) {
						
						log.debug("Setting SSL options");
						SSLSocketFactory socketFactory = getSocketFactory(CAcert,subscriberCert,subscriberKey, "Sii206744+");
						connOpts.setSocketFactory(socketFactory);
						log.debug("SSL conection options set");
						log.debug("Connection options set");
						
					}
			       
			        clients.get(i).connect(connOpts);
			        log.debug("Client "+i+" connected");
		    		
		    	}  	
		    	
		    	Date date = new Date();
		    	
		    	System.out.println("Starting to publish. Date: "+date.getMinutes()+":"+date.getSeconds());
		    	
		    	
				for(int i=0;i<numberOfMessagesPerClient;i++){	
					
					for(int n=0;n<numberOfClients;n++) {
						
						if(n==0 && i==0)
							timestamp_inicial=System.currentTimeMillis();
						else if(i==(numberOfMessagesPerClient-1) && n==(numberOfClients-1))
							timestamp_final=System.currentTimeMillis();
						
						mqttMessage = new MqttMessage(Long.toString(System.currentTimeMillis()).getBytes());
						mqttMessage.setQos(qos);
					 			
						clients.get(n).publish(topic, mqttMessage);
						numberOfPublishMessages++;
						//System.out.println("Numero de mensajes publicados: "+numberOfPublishMessages);
						
						/*try{
				        	log.debug("Waiting a second...");
			                Thread.sleep(2); 
				        }
						catch(InterruptedException e){
				        	log.error("An error has ocurred while waiting: ",e);
				        }*/
						
					}		
											
				}
				
				System.out.println("Finishing to publish. Date: "+date.getMinutes()+":"+date.getSeconds());
				System.out.println("Throughput medio(msg/s): "+numberOfClients*numberOfMessagesPerClient/(((float)(timestamp_final-timestamp_inicial))/1000));
						
				/*log.debug("Publishes finished");
				log.debug("Disconnecting");
				client.disconnect();
				log.debug("Disconnection finished successfully");*/
							
		    }
		    catch(MqttException e){
				log.error("Connection("+brokerUrl+"), publish or authentication failed:", e);
		    	throw e;				
			}		   
		    catch(Exception e){
		    	log.error("Unknown error:",e);
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
