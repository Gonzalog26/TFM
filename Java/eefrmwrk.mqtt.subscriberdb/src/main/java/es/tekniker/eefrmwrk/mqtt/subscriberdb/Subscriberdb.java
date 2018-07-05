package es.tekniker.eefrmwrk.mqtt.subscriberdb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.google.gson.*;

import es.tekniker.eefrmwrk.database.sql.manage.*;
import es.tekniker.eefrmwrk.database.sql.model.ValueVar;
import es.tekniker.eefrmwrk.database.sql.model.VarMetadata;
import es.tekniker.eefrmwrk.commons.BaseException;
import es.tekniker.eefrmwrk.config.ConfigFile;
import es.tekniker.eefrmwrk.mqtt.subscriberdb.VariableJSON;

public class Subscriberdb implements MqttCallbackExtended {

	private static final Log log = LogFactory.getLog(Subscriberdb.class);
	
	Session session= null;
	
	ConfigFile configFile;
	String filePath="MqttSubscriberdb.properties";
	int i=0;
	long timestamp_inicial;
	long timestamp_final;
	long timestamp_final_tramo_anterior;
	
	String suscriptor;
	static String host;
	static String port;
	static String brokerUrl; 
	static String topic;
	private int qos=1;
	int m=1;
	
	private float latenciaTotal=0;
	private float latenciaPorTramos=0;
	private int numeroMensajes=5000;
	
	MqttConnectOptions connOpt;
		
	final Gson gson = new Gson();

	public Subscriberdb() {
		
	}

	@Override
	public void connectionLost(Throwable cause) {
		log.info("Connection lost: ",cause);
		//log.debug("Closing session");
		//HibernateUtil.closeSession();
	}
	
	@Override
	public void connectComplete(boolean reconnect, java.lang.String serverURI){
		log.info("Connection with the broker succeed");
	}
	 

	@Override
	public void messageArrived(String topic, MqttMessage message)
		throws Exception{
		
		i++;
		
		int numeroMensajesxTramos=numeroMensajes/10;
		
		if(i==1) {
			timestamp_inicial=timestamp_final_tramo_anterior=System.currentTimeMillis();
		    System.out.println("Timestamp inicial: "+timestamp_inicial);
		}else if(i==(numeroMensajes)) {
			System.out.println("Timestamp final: "+timestamp_inicial);
			timestamp_final=System.currentTimeMillis();
			System.out.println("Tiempo prueba(s): "+((float)(timestamp_final-timestamp_inicial))/1000);
			System.out.println("Throughput medio(msg/s): "+numeroMensajes/(((float)(timestamp_final-timestamp_inicial))/1000));
		}			

		latenciaTotal+=(float)(System.currentTimeMillis()-Long.parseLong(message.toString()));
		latenciaPorTramos+=(float)(System.currentTimeMillis()-Long.parseLong(message.toString()));
		if(i==numeroMensajes)
			System.out.println("Latencia media(ms): "+((float)(latenciaTotal/numeroMensajes))/1000);
		
		if(i%numeroMensajesxTramos==0) {
			System.out.println("Latencia("+m+"): "+((float)(latenciaPorTramos/numeroMensajesxTramos))/1000);
			latenciaPorTramos=0;
			System.out.println("Tiempo tramo(s): "+((float)(System.currentTimeMillis()-timestamp_final_tramo_anterior))/1000);
			System.out.println("Throughput medio(msg/s)("+m+"): "+numeroMensajesxTramos/(((float)(System.currentTimeMillis()-timestamp_final_tramo_anterior))/1000));
			timestamp_final_tramo_anterior=System.currentTimeMillis();
			m++;
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		//log.debug("Message received correctly");
	}
	
	
	

}
