package org.tlg.com.httpoverxmpp.api;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This defines the Xmpp connection details 
 * 
 * @author Laurynas
 *
 */
@XmlRootElement
public class XmppConnectionDetails {
	private String xmppServer;	
	private String username;
	private String password;
	private String resource;

	public XmppConnectionDetails(){}
	
	public XmppConnectionDetails(String xmppServer, String username, String password, String resource){
		this.xmppServer = xmppServer;
		this.username = username;
		this.password = password;
		this.resource = resource;
	}
	
	public String getXmppServer() {
		return xmppServer;
	}
	public void setXmppServer(String xmppServer) {
		this.xmppServer = xmppServer;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
}
