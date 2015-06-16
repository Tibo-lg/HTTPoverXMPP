package org.tlg.com.httpoverxmpp.messages;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.jivesoftware.smack.packet.IQ;
import org.tlg.com.http.HTTPRequest;
import org.tlg.com.http.HTTPResponse;

public class IQHTTPRes extends IQ{
	HTTPResponse httpResponse;
	
	public IQHTTPRes(){}
	
	public IQHTTPRes(HTTPResponse httpRequest){
		this.httpResponse = httpRequest;
	}
	
	public HTTPResponse getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(HTTPResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	@Override
	public String getChildElementXML() {
		// TODO Auto-generated method stub
		String sReq = null;
		try{
			JAXBContext ctx = JAXBContext.newInstance(HTTPRequest.class);
			StringWriter writer = new StringWriter();
			Marshaller m = ctx.createMarshaller();
			m.setProperty(Marshaller.JAXB_FRAGMENT, true);
			m.marshal(httpResponse, writer);
			sReq = writer.toString();
		}catch(Exception e){
		}
		return sReq;
	}
}
