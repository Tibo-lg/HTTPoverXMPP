package org.tlg.com.httpoverxmpp.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.hoxt.packet.AbstractHttpOverXmpp;
import org.jivesoftware.smackx.hoxt.packet.AbstractHttpOverXmpp.Text;
import org.jivesoftware.smackx.hoxt.packet.AbstractHttpOverXmpp.Xml;
import org.jivesoftware.smackx.hoxt.packet.HttpMethod;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppReq;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppResp;
import org.jivesoftware.smackx.shim.packet.Header;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.tlg.com.httpoverxmpp.api.HOXTWrapper;
import org.tlg.com.httpoverxmpp.exceptions.HOXTException;
import org.tlg.com.httpoverxmpp.util.HOXTUtil;

/**
 * HOXT Client example
 * 
 * Example of an HTTP client. Pre-condition: The xmpp user is registered, the truststore is set up. Steps: 1 - create an
 * XMPP config. 2 - create the HOXTWrapper object with no resource as only client 3 - init the wrapper with true to
 * listen for response 4 - Send requests, receive answer and handle errors if any
 * 
 * @author Thibaut Le Guilly
 *
 */

public class HOXTClient {

    /* The server Jid */
    public final static String serverJid = "test-server@service/t";

    public static void main(String[] args) throws XmppStringprepException, InterruptedException {

        /*
         * Assumes the truststore is in "modulefolder/resources" You can either use VM argument
         * -Djavax.net.ssl.trustStore=resources/clientstore.jks or the following lines
         */
        // System.setProperty("javax.net.ssl.trustStore",
        // "resources/clientstore.jks");

        /* Fill in the connection details */
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder().setUsernameAndPassword("test-client", "test")
        .setXmppDomain(JidCreate.domainBareFrom("service")).setHost("service.com").setSecurityMode(SecurityMode.required).setResource("t").setCompressionEnabled(false).build();
        HOXTWrapper hoxtWrapper = new HOXTWrapper(config, null);
        try {
            hoxtWrapper.init(true);
        } catch (XMPPException e1) {
            e1.printStackTrace();
            System.out.println("Xmpp error");
        } catch (SmackException e) {
            System.out.println("Smak error");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO error");
            e.printStackTrace();
        }
        String message = new String("Hello server! Can I get my object?");
        /* Need to set data otherwise it crashes */
        AbstractHttpOverXmpp.Text child = new AbstractHttpOverXmpp.Text(message);
        AbstractHttpOverXmpp.Data data = new AbstractHttpOverXmpp.Data(child);
        /* Need to set headers otherwise exception raised */
        /* Cannot use Set as in documentation, HeadersExtension won't accept it */
        List<Header> set = new ArrayList<Header>();
        set.add(new Header("Content-Type", "application/xml"));
        HttpOverXmppReq req = HttpOverXmppReq.builder().setMethod(HttpMethod.POST).setResource("/test")
        // .setHeaders(new HeadersExtension(set))
        .setData(data).build();
        HttpOverXmppResp res = null;
        try {
            res = hoxtWrapper.sendRequest(req, serverJid);
            child = (Text) res.getData().getChild();
            System.out.println("The server says: " + child.getText());
            req = HttpOverXmppReq.builder().setMethod(HttpMethod.GET).setResource("/test/myobject").build();

            /* Need to set headers */
            // req.setHeaders(new HeadersExtension(set));
            /* Need to set data otherwise it crashes */
            // req.setData(data);
            res = hoxtWrapper.sendRequest(req, serverJid);
            Xml xml = (Xml) res.getData().getChild();
            MyObject obj = HOXTUtil.getEntity(xml.getText(), MyObject.class);
            System.out.println("My message is: " + obj.message + " Code: " + obj.code);
        } catch (HOXTException e) {
            System.out.println("Xmpp error");
            e.printStackTrace();
        } catch (NotConnectedException e) {
            System.out.println("Not connected.");
            e.printStackTrace();
        } catch (JAXBException e) {
            System.out.println("Error processing object.");
            e.printStackTrace();
        }
        hoxtWrapper.destroy();
    }
}
