package org.tlg.com.http;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.xerces.dom.ElementNSImpl;
import org.tlg.com.api.Header;
import org.tlg.com.api.MethodType;
import org.tlg.com.utils.W3CHelper;

@XmlRootElement(name="req")
public class HTTPRequest {

	private String uri;
	private String method;
	private String entity;

	private List<Header> headers = new ArrayList<Header>();

	private HTTPResponse response;

	@XmlAttribute(name="xmlns")
	private static final String xmlns = "urn:xmpp:http";
	@XmlAttribute(name="version")
	private static final String version = "1.1";

	public HTTPRequest(){}

	public HTTPRequest(String uri){
		this.uri = uri;
		this.method = "GET";
	}

	public HTTPRequest(String uri, String method){
		this.uri = uri;
		this.method = method;
	}

	public HTTPRequest(String uri, String method, String body){
		this.uri = uri;
		this.method = method;
		this.entity = body;
	}

	@XmlAttribute(name="resource")
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	@XmlAttribute(name="method")
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
	public MethodType getMethodType(){
		for(int i=0; i<MethodType.values().length; i++){
			if(this.method.equals(MethodType.values()[i].name())){
				return MethodType.values()[i];
			}
		}
		return null;
	}

	//	//@XmlElementWrapper(name="data")
	//	@XmlElement(name="xml")
	private Object getEntityNode() {
		if (entity == null) {
			return null;
		}
		return W3CHelper.parseDocument("<xml>" + entity.trim() + "</xml>").getDocumentElement();
	}

	@XmlElement(name="data")
	public void setEntityNode(Object entityObject){
		ElementNSImpl node = (ElementNSImpl) entityObject;
		entity = W3CHelper.nodeToString(node.getFirstChild()).trim();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getEntity(Class<?> c) throws JAXBException {
		if(c.equals(String.class)){
			return (T) this.getEntity();
		}
		JAXBContext ctx = JAXBContext.newInstance(c);
		Unmarshaller u = ctx.createUnmarshaller();
		StringReader reader = new StringReader(this.entity);
		return (T) u.unmarshal(reader);
	}

	/**Only handles xml
	 * @throws JAXBException */
	//@XmlTransient
	public void setEntity(Class<?> c, Object entity) throws JAXBException {
		JAXBContext ctx = JAXBContext.newInstance(c);
		Marshaller m = ctx.createMarshaller();
		m.setProperty(Marshaller.JAXB_FRAGMENT, true);
		StringWriter writer = new StringWriter();
		m.marshal(entity, writer);
		this.entity = writer.toString();
	}
	
	public String getEntity(){
		return this.entity;
	}
	
	@XmlTransient
	public void setEntity(String entity){
		this.entity = entity;
	}

	public HTTPResponse getResponse() {
		return response;
	}

	public void setResponse(HTTPResponse response) {
		this.response = response;
	}

	@XmlElementWrapper(name="headers")
	@XmlElement(name="header")
	public List<Header> getHeaders() {
		return this.headers;
	}

	public void addHeader(Header header){
		this.headers.add(header);
	}

	public String getHeaderValue(String name){
		for(Header h: this.headers){
			if(h.getName().equalsIgnoreCase(name)){
				return h.getValue();
			}
		}
		return null;
	}
}
