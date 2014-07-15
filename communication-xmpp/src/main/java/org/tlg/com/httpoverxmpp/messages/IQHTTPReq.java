package org.tlg.com.httpoverxmpp.messages;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.jivesoftware.smack.packet.IQ;
import org.tlg.com.http.HTTPRequest;

public class IQHTTPReq extends IQ{

	HTTPRequest httpRequest;
	
	JAXBContext ctx;
	
	public IQHTTPReq(){}
	
	public IQHTTPReq(HTTPRequest httpRequest){
		this.httpRequest = httpRequest;
	}
	
	public HTTPRequest getHttpRequest() {
		return httpRequest;
	}

	public void setHttpRequest(HTTPRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

//	public IQHTTPReq fromIQXml(String iqXml) throws JAXBException{
//		JAXBContext ctx = JAXBContext.newInstance(HTTPRequest.class);
//		Unmarshaller m = ctx.createUnmarshaller();
//		
//	}
	
	@Override
	public String getChildElementXML() {
		String sReq = null;
		try{
			JAXBContext ctx = JAXBContext.newInstance(HTTPRequest.class);
			StringWriter writer = new StringWriter();
			Marshaller m = ctx.createMarshaller();
			m.setProperty(Marshaller.JAXB_FRAGMENT, true);
			m.marshal(httpRequest, writer);
			sReq = writer.toString();
		}catch(Exception e){
			e.printStackTrace();
		}
		return sReq;
	}

}
