package org.tlg.com.httpoverxmpp.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jivesoftware.smackx.hoxt.packet.AbstractHttpOverXmpp;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppResp;

public final class HOXTUtil {
	private HOXTUtil(){}
	
	public static AbstractHttpOverXmpp.Data getDataFromObject(Class<?> c, Object entity) throws JAXBException {
			JAXBContext ctx = JAXBContext.newInstance(c);
			Marshaller m = ctx.createMarshaller();
			m.setProperty(Marshaller.JAXB_FRAGMENT, true);
			StringWriter writer = new StringWriter();
			m.marshal(entity, writer);
			AbstractHttpOverXmpp.Xml child = new AbstractHttpOverXmpp.Xml(writer.toString());
			AbstractHttpOverXmpp.Data data = new AbstractHttpOverXmpp.Data(child);
			return data;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getEntity(String data, Class<?> c) throws JAXBException {
		JAXBContext ctx = JAXBContext.newInstance(c);
		Unmarshaller u = ctx.createUnmarshaller();
		StringReader reader = new StringReader(data);
		return (T) u.unmarshal(reader);
	}
	
	public static HttpOverXmppResp setStatus(String text, int code){
		AbstractHttpOverXmpp.Text child = new AbstractHttpOverXmpp.Text(text);
		AbstractHttpOverXmpp.Data data = new AbstractHttpOverXmpp.Data(child);

		return HttpOverXmppResp.builder()
				.setStatusCode(code)
				.setData(data)
				.build();
	}
	
	public static HttpOverXmppResp set404(){
		
		return setStatus("Not Found", 404);
	}
	
	public static HttpOverXmppResp set405(){
		return setStatus("Method Not Allowed", 405);
	}
	
	public static HttpOverXmppResp set500(){
		return setStatus("Internal Server Error", 500);
	}
}
