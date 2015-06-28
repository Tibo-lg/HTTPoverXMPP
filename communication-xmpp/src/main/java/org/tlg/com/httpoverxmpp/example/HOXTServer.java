package org.tlg.com.httpoverxmpp.example;

import java.io.IOException;

import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.tlg.com.httpoverxmpp.api.HOXTWrapper;
import org.tlg.com.httpoverxmpp.api.ResourceManager;

/**
 * HOXT Client example
 * 
 * Example of an HTTP client. Pre-condition: The xmpp username is registered,
 * the truststore is set up. Steps: 1 - create an XMPP config. 2 - create the
 * HOXTWrapper object with no resource as only client 3 - init the wrapper with
 * true to listen for response 4 - Send requests, receive answer and handle
 * errors if any
 * 
 * @author Thibaut Le Guilly
 *
 */

public class HOXTServer {
	public static void main(String[] args) throws XmppStringprepException {
		/*
		 * Assumes the truststore is in "modulefolder/resources" You can either
		 * use VM argument -Djavax.net.ssl.trustStore=resources/clientstore.jks
		 * or the following lines
		 */
		// System.setProperty("javax.net.ssl.trustStore",
		// "resources/clientstore.jks");
		/*
		 * Create a ResourceManager using a package name. All classes annotated
		 * with @Path will be available.
		 */
		ResourceManager rm = new ResourceManager(
				"org.tlg.com.httpoverxmpp.example");
		/*
		 * Possibility to use rm.registerInstance(new MyResource([args])); for
		 * reusing the same object for each call on the resource
		 */
		XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration
				.builder()
				.setUsernameAndPassword("test-server", "test")
				 .setXmppDomain(JidCreate.domainBareFrom("service"))
				.setHost("service.com").setCompressionEnabled(false)
				.setResource("t").setSecurityMode(SecurityMode.required)
				.build();
		HOXTWrapper xmppManager = new HOXTWrapper(config, rm);
		try {
			xmppManager.init(false);
			System.in.read();
		} catch (XMPPException e1) {
			System.out.println("An Xmpp error occured");
			e1.printStackTrace();
		} catch (IOException e) {
			System.out.println("An IO error occured");
			e.printStackTrace();
		} catch (SmackException e) {
			System.out.println("An Smack error occured");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Interupted Exception");
			e.printStackTrace();
		}
		System.out.println("Stopped.");
		xmppManager.destroy();
	}
}
