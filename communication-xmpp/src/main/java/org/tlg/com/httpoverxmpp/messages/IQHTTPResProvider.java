package org.tlg.com.httpoverxmpp.messages;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.tlg.com.http.HTTPResponse;
import org.tlg.com.http.Header;
import org.xmlpull.v1.XmlPullParser;

public class IQHTTPResProvider implements IQProvider{

	@Override
	public IQ parseIQ(XmlPullParser parser) throws Exception {
		IQHTTPRes iqres = new IQHTTPRes();
		HTTPResponse res = new HTTPResponse();
		String entityString = "";
		boolean done = false;
		res.setStatusCode(Integer.parseInt(parser.getAttributeValue("", "statusCode")));
		while(!done){
			int eventType = parser.next();
			
			if(eventType == XmlPullParser.START_TAG && "header".equals(parser.getName())){
				Header header = new Header(parser.getAttributeValue("", "name"), parser.nextText());
				res.addHeader(header);
			}
			else if(eventType == XmlPullParser.START_TAG && "data".equals(parser.getName())){
				boolean dataDone = false;
				while(!dataDone){
					eventType = parser.next();
					if(eventType == XmlPullParser.END_TAG && "data".equals(parser.getName())){
						dataDone = true;
					}else{
						entityString += parser.getText();
					}
				}
			}
			else if(eventType == XmlPullParser.END_TAG && "resp".equals(parser.getName())){
				done = true;
			}
		}
		res.setEntity(entityString);
		iqres.setHttpResponse(res);
		return iqres;
	}

}

