package org.tlg.com.examples.xhttp.server;

import java.io.IOException;

import org.jivesoftware.smack.XMPPException;
import org.tlg.com.httpoverxmpp.api.ResourceManager;
import org.tlg.com.httpoverxmpp.api.XmppConnectionDetails;
import org.tlg.com.httpoverxmpp.api.XmppManager;

/**
 * Hello world!
 *
 */
public class XHTTPServer 
{
    public static void main( String[] args )
    {
    	XmppConnectionDetails conDetails = new XmppConnectionDetails("delling.dpt.cs.aau.dk", "test-server", "test", "t");
    	ResourceManager rm = new ResourceManager("org.tlg.com.examples.xhttp.server");
        XmppManager xmppManager = new XmppManager(conDetails, rm);
        try {
			xmppManager.init();
			System.in.read();
		} catch (XMPPException e1) {
			System.out.println("An Xmpp error occured");
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("Stopped.");
		xmppManager.destroy();
    }
}
