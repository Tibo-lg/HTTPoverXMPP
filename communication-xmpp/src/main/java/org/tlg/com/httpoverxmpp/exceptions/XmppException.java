package org.tlg.com.httpoverxmpp.exceptions;

public class XmppException extends Exception {

	private static final long serialVersionUID = 1L;

	public XmppException(){
	}
	
	public XmppException(String message){
		super(message);
	}
}
