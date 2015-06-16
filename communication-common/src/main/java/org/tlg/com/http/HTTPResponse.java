package org.tlg.com.http;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.tlg.com.utils.W3CHelper;

@XmlRootElement(name = "resp")
public class HTTPResponse {

	int statusCode;
	String entity;

	private HashMap<String, Header> headers = new HashMap<String, Header>();

	@XmlAttribute(name = "xmlns")
	private static final String xmlns = "urn:xmpp:http";
	@XmlAttribute(name = "version")
	private static final String version = "1.1";

	public HTTPResponse() {
	}

	public HTTPResponse(int statusCode) {
		this.statusCode = statusCode;
	}

	@XmlAttribute
	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	// //@XmlElementWrapper(name="data")
	// @XmlElement(name="xml")
	public Object getEntityNode() {
		if (entity == null) {
			return null;
		}
		return W3CHelper.parseDocument("<xml>" + entity.trim() + "</xml>")
				.getDocumentElement();
	}

	@XmlElement(name = "data")
	public void setEntityNode(Object entityObject) {
		ElementNSImpl node = (ElementNSImpl) entityObject;
		entity = W3CHelper.nodeToString(node.getFirstChild()).trim();
	}

	@SuppressWarnings("unchecked")
	public <T> T getEntity(Class<?> c) throws JAXBException {
		JAXBContext ctx = JAXBContext.newInstance(c);
		Unmarshaller u = ctx.createUnmarshaller();
		StringReader reader = new StringReader(this.entity);
		return (T) u.unmarshal(reader);
	}

	/**
	 * Only handles xml
	 * 
	 * @throws JAXBException
	 */
	// @XmlTransient
	public void setEntity(Class<?> c, Object entity) throws JAXBException {
		if (c.equals(String.class)) {
			this.setEntity((String) entity);
		} else {
			JAXBContext ctx = JAXBContext.newInstance(c);
			Marshaller m = ctx.createMarshaller();
			m.setProperty(Marshaller.JAXB_FRAGMENT, true);
			StringWriter writer = new StringWriter();
			m.marshal(entity, writer);
			System.out.println("Test: " + writer.toString());
			this.entity = writer.toString();
		}
	}

	public String getEntity() {
		return this.entity;
	}

	@XmlTransient
	public void setEntity(String entity) {
		this.entity = entity;
	}

	@XmlElementWrapper(name = "headers")
	@XmlElement(name = "header")
	public List<Header> getHeaders() {
		return new ArrayList<Header>(headers.values());
	}

	public void addHeader(Header header) {
		this.headers.put(header.getName(), header);
	}

	public String getHeaderValue(String name) {
		Header header = this.headers.get(name);
		if (header != null) {
			return header.getValue();
		}
		return null;
	}
}
