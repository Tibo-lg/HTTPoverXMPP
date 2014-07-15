package org.tlg.com.httpoverxmpp.messages;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.tlg.com.http.HTTPRequest;
import org.tlg.com.http.Header;
import org.xmlpull.v1.XmlPullParser;

public class IQHTTPReqProvider implements IQProvider{

	@Override
	public IQ parseIQ(XmlPullParser parser) throws Exception {
		IQHTTPReq iqreq = new IQHTTPReq();
		HTTPRequest req = new HTTPRequest();
		String entityString = "";
		boolean done = false;
		req.setMethod(parser.getAttributeValue("", "method"));
		req.setUri(parser.getAttributeValue("", "resource"));
		while(!done){
			int eventType = parser.next();
			
			if(eventType == XmlPullParser.START_TAG && "header".equals(parser.getName())){
				Header header = new Header(parser.getAttributeValue("", "name"), parser.nextText());
				req.addHeader(header);
			}
			else if(eventType == XmlPullParser.START_TAG && "data".equals(parser.getName())){
				boolean dataDone = false;
				while(!dataDone){
					eventType = parser.next();
					if(eventType == XmlPullParser.END_TAG && "data".equals(parser.getName())){
						dataDone = true;
					}else if(eventType == XmlPullParser.START_TAG && parser.isEmptyElementTag()){
						entityString += parser.getText();
						/** Skip next call with END_TAG for single tag (e.g. <foobar/>) */
						eventType = parser.next();
					}else{
						entityString += parser.getText();
					}
				}
			}
			else if(eventType == XmlPullParser.END_TAG && "req".equals(parser.getName())){
				done = true;
			}
		}
		req.setEntity(entityString);
		iqreq.setHttpRequest(req);
		return iqreq;
	}

}
