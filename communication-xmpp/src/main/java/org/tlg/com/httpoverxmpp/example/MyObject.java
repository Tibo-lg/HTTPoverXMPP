package org.tlg.com.httpoverxmpp.example;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * A random object, needs javax annottations to work.
 * 
 * @author Thibaut Le Guilly
 *
 */
@XmlRootElement
public class MyObject {
	
	@XmlElement
	String message;
	@XmlElement
	int code;
	
	/*Need empty constructor for JAXB*/
	public MyObject(){}
	
	public MyObject(String message, int code){
		this.message = message;
		this.code = code;
	}

}
