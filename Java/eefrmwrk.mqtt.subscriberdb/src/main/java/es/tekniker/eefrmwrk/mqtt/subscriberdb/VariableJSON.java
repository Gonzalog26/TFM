package es.tekniker.eefrmwrk.mqtt.subscriberdb;

import java.util.ArrayList;
import java.util.List;

public class VariableJSON {
	
	private String varName;
	private List<Measures> measures = new ArrayList<Measures>();
	
	public VariableJSON(){
		
	}
	
	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public List<Measures> getMeasures() {
		return measures;
	}

	public void setMeasures(List<Measures> measures) {
		this.measures = measures;
	}

	

}
