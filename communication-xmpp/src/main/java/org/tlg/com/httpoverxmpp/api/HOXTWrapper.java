package org.tlg.com.httpoverxmpp.api;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.iqrequest.AbstractIqRequestHandler;
import org.jivesoftware.smack.iqrequest.IQRequestHandler.Mode;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.hoxt.packet.AbstractHttpOverXmpp;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppReq;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppResp;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.tlg.com.httpoverxmpp.exceptions.HOXTException;

/**
 * Object that enables sending and receiving HTTP requests, and answering to them
 * 
 * @author Thibaut Le Guilly
 *
 */
public class HOXTWrapper {

    @SuppressWarnings("unused")
    private static final int packetReplyTimeout = 500; // millis

    /**
     * Concurrent hashmap to stored currently pending requests.
     */
    private static ConcurrentHashMap<String, AbstractHttpOverXmpp> reqMap = new ConcurrentHashMap<String, AbstractHttpOverXmpp>();

    /**
     * Used to generate id for created requests
     */
    private AtomicInteger reqId = new AtomicInteger(0);

    /**
     * Smack API class
     */
    private AbstractXMPPConnection connection;
    private XMPPTCPConnectionConfiguration conConfig;

    /**
     * Manages jax.rs resources if used.
     */
    private ResourceManager resourceManager;

    /**
     * Constructor
     * 
     * @param server The XMPP server address to connect to.
     * @param username The username corresponding to an existing user on the server
     * @param password The password corresponding to the given user
     * @param resource The name of the resource used for this connection (e.g. user@server/resource)
     * @param requestListener An object implementing the RequestListener interface that will be notified of incoming
     *            requests
     */
    public HOXTWrapper(XMPPTCPConnectionConfiguration conConfig, ResourceManager resourceManager) {
        this.conConfig = conConfig;
        this.resourceManager = resourceManager;
    }

    private void initClient() {
        this.connection.registerIQRequestHandler(new AbstractIqRequestHandler(HttpOverXmppResp.ELEMENT, HttpOverXmppResp.NAMESPACE, Type.get, Mode.async) {
            @Override
            public IQ handleIQRequest(IQ iqRequest) {
                HOXTWrapper.this.handleResponse(iqRequest);
                return null;
            }
        });
    }

    private void initServer() {
        this.connection.registerIQRequestHandler(new AbstractIqRequestHandler(HttpOverXmppReq.ELEMENT, HttpOverXmppReq.NAMESPACE, Type.set, Mode.async) {
            @Override
            public IQ handleIQRequest(IQ iqRequest) {
                HttpOverXmppReq request = (HttpOverXmppReq) iqRequest;
                if (request.getError() != null) {
                    handleRequestError(request);
                }
                HttpOverXmppResp response = RequestHandler.handleRequest(request, resourceManager);
                response.setStanzaId(request.getStanzaId());
                response.setTo(request.getFrom());
                response.setFrom(request.getTo());
                return response;
            }
        });
    }

    /**
     * Initialize the XMPP manager, first function to call after constructor
     * 
     * @throws XMPPException
     * @throws IOException
     * @throws SmackException
     * @throws InterruptedException
     */
    public void init(boolean initClient) throws XMPPException, SmackException, IOException, InterruptedException {

        System.out.println(String.format("Initializing connection to server "
        + conConfig.getXMPPServiceDomain()));

        connection = new XMPPTCPConnection(conConfig);
        connection.connect();
        /* To avoid exception when looking for Roster */
        Roster.getInstanceFor(connection).setRosterLoadedAtLogin(false);

        connection.login();
        System.out.println("Connected: " + connection.isConnected());

        if (SmackConfiguration.DEBUG == true) {
            ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(connection);
            DiscoverInfo discoInfo = discoManager.discoverInfo(this.connection.getUser());
            System.out.println("Test hoxt supported: " + discoInfo.containsFeature("urn:xmpp:http"));
        }

        if (resourceManager != null) {
            initServer();
        }
        if (initClient) {
            initClient();
        }
    }

    public void destroy() {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
    }

    public boolean isConnected() {
        return (connection != null) && (connection.isConnected());
    }

    /**
     * Send an HTTP request to the node specified by dest (e.g. user@server/resource)
     * 
     * @param request
     * @param dest
     * @return
     * @throws HOXTException
     * @throws NotConnectedException
     * @throws XmppStringprepException
     */
    public HttpOverXmppResp sendRequest(HttpOverXmppReq request, String dest) throws HOXTException, NotConnectedException, XmppStringprepException {
        /** Reset the id value in case of overflow */
        if (reqId.get() < 0) {
            reqId.set(0);
        }
        String id = Integer.toString(reqId.incrementAndGet());
        try {
            /** TODO use the timed out version poll **/
            synchronized (reqMap) {
                reqMap.put(id, request);
            }

            request.setStanzaId(id);
            request.setTo(JidCreate.from(dest));
            request.setFrom(JidCreate.from(id));
            connection.sendStanza(request);
            synchronized (request) {
                /** TODO set a time out */
                // request.wait(5000);
                request.wait();
            }
        } catch (InterruptedException e) {
            synchronized (reqMap) {
                reqMap.remove(id);
                throw new HOXTException("Request was not performed");
            }
        }

        AbstractHttpOverXmpp tmp = reqMap.get(id);
        if (!(tmp instanceof HttpOverXmppResp)) {
            throw new HOXTException("No response received or error");
        }
        HttpOverXmppResp response = (HttpOverXmppResp) reqMap.get(id);
        if (response == null) {
            throw new HOXTException("Destination " + dest + " not available");
        }

        if (response.getStatusCode() == 404) {
            throw new HOXTException("404 - Resource not foud on server.");
        }

        return response;
    }

    private void handleRequestError(HttpOverXmppReq request) {
        synchronized (reqMap) {
            request = (HttpOverXmppReq) reqMap.get(request.getStanzaId());
        }

        synchronized (request) {
            request.notify();
        }
    }

    /**
     * Handles a HttpOverXmppResp
     * 
     * @param packet
     */
    private void handleResponse(Stanza packet) {
        HttpOverXmppResp response = (HttpOverXmppResp) packet;
        HttpOverXmppReq request;
        synchronized (reqMap) {
            request = (HttpOverXmppReq) reqMap.get(response.getStanzaId());
            reqMap.put(request.getStanzaId(), response);
        }

        synchronized (request) {
            request.notify();
        }

    }


}