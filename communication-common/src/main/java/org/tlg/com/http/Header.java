package org.tlg.com.http;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="header")
public class Header {
	private String name; 
	
	private String value;
	
	public Header(){}
	
	public Header(String name, String value){
		this.name = name;
		this.value = value;
	}
	
	@XmlAttribute(name="name")
	public String getName(){
		return this.name;
	}
	
	@XmlValue
	public String getValue(){
		return this.value;
	}
}
