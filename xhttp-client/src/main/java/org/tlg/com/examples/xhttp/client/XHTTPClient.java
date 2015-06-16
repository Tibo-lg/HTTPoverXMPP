package org.tlg.com.examples.xhttp.client;

import org.jivesoftware.smack.XMPPException;
import org.tlg.com.http.HTTPRequest;
import org.tlg.com.http.HTTPResponse;
import org.tlg.com.httpoverxmpp.api.XmppConnectionDetails;
import org.tlg.com.httpoverxmpp.api.XmppManager;
import org.tlg.com.httpoverxmpp.exceptions.XmppException;

/**
 * Hello world!
 *
 */
public class XHTTPClient 
{
    public static void main( String[] args )
    {
    	XmppConnectionDetails conDetails = new XmppConnectionDetails("your.xmpp.server.com", "test-client", "yourpassword", "yourresource");
        XmppManager xmppManager = new XmppManager(conDetails, null);
        try {
			xmppManager.init();
		} catch (XMPPException e1) {
			e1.printStackTrace();
			System.out.println("Xmpp error");
		}
        HTTPRequest req = new HTTPRequest();
        req.setMethod("POST");
        req.setEntity("Hello server! How are you?");
        req.setUri("/test");
        HTTPResponse res = null;
        try {
			res = xmppManager.sendRequest(req, "test-server@delling/t");
		} catch (XmppException e) {
			System.out.println("An error occured!");
			e.printStackTrace();
		}
        
        System.out.println("The server says: " + res.getEntity());
        xmppManager.destroy();
    }
}
