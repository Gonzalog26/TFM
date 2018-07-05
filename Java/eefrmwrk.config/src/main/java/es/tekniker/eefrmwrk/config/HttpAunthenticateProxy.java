package es.tekniker.eefrmwrk.config;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class HttpAunthenticateProxy extends Authenticator {

	String user = new String();
	String password = new String();
	
	public HttpAunthenticateProxy(String user, String password) {
		super();
		setCredentials(user, password);
	}
	
	void setCredentials(String userC, String passwordC){
		setUser(userC);
		setPassword(passwordC);
	}
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(getUser(),getPassword().toCharArray());
	}
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

}
