package org.tlg.com.httpoverxmpp.api;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;
import org.tlg.com.http.HTTPRequest;
import org.tlg.com.http.HTTPResponse;
import org.tlg.com.httpoverxmpp.exceptions.XmppException;
import org.tlg.com.httpoverxmpp.messages.IQHTTPReq;
import org.tlg.com.httpoverxmpp.messages.IQHTTPReqProvider;
import org.tlg.com.httpoverxmpp.messages.IQHTTPRes;
import org.tlg.com.httpoverxmpp.messages.IQHTTPResProvider;
import org.tlg.com.interfaces.RequestListener;

/**
 * Object that enables sending and receiving HTTP requests, and answering to them
 * @author Thibaut Le Guilly
 *
 */
public class XmppManager {

	//private static final int packetReplyTimeout = 500; // millis

	private String server;
	private String resource;
	private RequestListener requestListener;
	
	/**
	 * Concurrent hashmap to stored currently pending requests.
	 */
	private ConcurrentHashMap<Integer, HTTPRequest> reqMap = new ConcurrentHashMap<Integer, HTTPRequest>();
	
	/**
	 * Used to generate id for created requests
	 */
	private AtomicInteger reqId = new AtomicInteger(0);

	//private ConnectionConfiguration config;
	/**
	 * Smack API class
	 */
	private XMPPConnection connection;

	private String username;
	private String password;

	/**
	 * Constructor
	 * @param server The XMPP server address to connect to.
	 * @param username The username corresponding to an existing user on the server
	 * @param password The password corresponding to the given user
	 * @param resource The name of the resource used for this connection (e.g. user@server/resource)
	 * @param requestListener An object implementing the RequestListener interface that will be notified of incoming requests
	 */
	public XmppManager(String server, String username, String password, String resource, RequestListener requestListener) {
		this.server = server;
		this.username = username;
		this.password = password;
		this.resource = resource;
		this.requestListener = requestListener;
	}

	/**
	 * Initialize the XMPP manager, first function to call after 
	 * constructor
	 * @throws XMPPException
	 */
	public void init() throws XMPPException {

		/* Get a debug window to see sent and received messages*/
		XMPPConnection.DEBUG_ENABLED = false;
		System.out.println(String.format("Initializing connection to server" + server));

		//SmackConfiguration.setPacketReplyTimeout(packetReplyTimeout);
		ProviderManager.getInstance().addIQProvider("req", "urn:xmpp:http", new IQHTTPReqProvider());
		ProviderManager.getInstance().addIQProvider("resp", "urn:xmpp:http", new IQHTTPResProvider());

		//config = new ConnectionConfiguration(server, port);
		//config.setSASLAuthenticationEnabled(false);
		//config.setSecurityMode(SecurityMode.disabled);

		connection = new XMPPConnection(server);
		connection.connect();
		connection.login(this.username, this.password, this.resource);

		System.out.println("Connected: " + connection.isConnected());
		
		PacketFilter reqFilter = new PacketFilter(){
			public boolean accept(Packet packet) {
				if(packet instanceof IQHTTPReq){
					return true;
				}
				return false;
			}
		};
		connection.addPacketListener(new XmppReqListener(), reqFilter);
		
		PacketFilter resFilter = new PacketFilter(){
			public boolean accept(Packet packet) {
				if(packet instanceof IQHTTPRes){
					return true;
				}
				return false;
			}
		};
		connection.addPacketListener(new XmppResListener(), resFilter);

	}

	public void destroy() {
		if (connection!=null && connection.isConnected()) {
			connection.disconnect();
		}
	}
	
	/**
	 * Create the correctly formated IQ stanza (see http://xmpp.org/extensions/inbox/http-over-xmpp.html) 
	 * and send it
	 * @param httpRequest
	 * @param dest
	 * @param id
	 */
	private void sendIQHTTPRequest(HTTPRequest httpRequest, String dest, int id){
		IQHTTPReq req = new IQHTTPReq(httpRequest);
		req.setPacketID(Integer.toString(id));
		System.out.println(req.getChildElementXML());
		req.setType(IQ.Type.SET);
		req.setTo(dest);
		connection.sendPacket(req);
	}
	
	
	/**
	 * Send an HTTP request to the node specified by dest (e.g. user@server/resource)
	 * @param request
	 * @param dest
	 * @return
	 * @throws XmppException
	 */
	public HTTPResponse sendRequest(HTTPRequest request, String dest) throws XmppException{
		/** Reset the id value in case of overflow */
		if(reqId.get() < 0){
			reqId.set(0);
		}
		int id = reqId.incrementAndGet();
		try {
			synchronized(reqMap){
				reqMap.put(id, request);
			}
			
			this.sendIQHTTPRequest(request, dest, id);
			
			synchronized(request){
				/**TODO set a time out*/
				//request.wait(5000);
				request.wait();
			}
		} catch (InterruptedException e) {
			synchronized(reqMap){
				reqMap.remove(id);
				throw new XmppException("Request was not performed");
			}
		} catch (IllegalStateException e){
			try {
				System.out.println("Reinit XMPP");
				this.init();
				this.sendIQHTTPRequest(request, dest, id);
			} catch (XMPPException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		if(request.getResponse() == null){
			throw new XmppException("Destination " + dest +" not available");
		}
		
		return request.getResponse();
	}
	
	
	/**
	 * Respond to an HTTP request
	 * @param response
	 * @param dest
	 * @param packetId
	 */
	private void sendResponse(HTTPResponse response, String dest, String packetId){
		IQHTTPRes res = new IQHTTPRes(response);
		res.setPacketID(packetId);
		res.setType(IQ.Type.RESULT);
		res.setTo(dest);
		connection.sendPacket(res);
	}
	
	private void handleRequestError(IQHTTPReq iqReq){
		HTTPRequest request;
		synchronized(reqMap){
			request = reqMap.get(Integer.parseInt(iqReq.getPacketID()));
		}
		
		synchronized(request){
			request.notify();
		}
	}
	
	/**
	 * Unmarshalls packet to IQHTTPReq and notify Request listener
	 * call sendResponse to reply to the request
	 * @param packet
	 */
	private void handleRequest(Packet packet){
		IQHTTPReq iqReq = (IQHTTPReq) packet;
		if(iqReq.getError() != null){
			handleRequestError(iqReq);
		}
		HTTPResponse res = this.requestListener.onRequestReceived(iqReq.getHttpRequest());
		this.sendResponse(res, iqReq.getFrom(), iqReq.getPacketID());
	}
	
	/**
	 * Unmarshall packet to IQHTTPRes an wakes up waiting request
	 * @param packet
	 */
	private void handleResponse(Packet packet){
		IQHTTPRes iqRes = (IQHTTPRes) packet;
		HTTPResponse response = iqRes.getHttpResponse();
		HTTPRequest request;
		synchronized(reqMap){
			request = reqMap.get(Integer.parseInt(iqRes.getPacketID()));
		}
		
		synchronized(request){
			request.setResponse(response);
			request.notify();
		}
		
	}
	
	/**
	 * Sub-class to handle incoming packet corresponding to HTTP requests or responses
	 * @author Thibaut Le Guilly
	 *
	 */
	class XmppReqListener implements PacketListener{
		
		@Override
		public void processPacket(Packet packet) {
			XmppManager.this.handleRequest(packet);
		}
	}
	
	class XmppResListener implements PacketListener{
		@Override
		public void processPacket(Packet packet) {
			XmppManager.this.handleResponse(packet);
		}
	}


}
