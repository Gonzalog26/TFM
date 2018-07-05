package es.tekniker.eefrmwrk.mqtt.subscriberdb;

public class Measures {
	
	private String value;
	private long timestamp;
	
	public Measures(){}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public long getTimeStamp() {
		return timestamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timestamp = timeStamp;
	}

	

}
