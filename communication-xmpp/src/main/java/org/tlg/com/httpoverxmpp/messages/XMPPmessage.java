package org.tlg.com.httpoverxmpp.messages;

import org.jivesoftware.smack.packet.Message;

public class XMPPmessage{	
	private Message msg;
	
	public Message getMsg() {
		return msg;
	}
	
	
	public XMPPmessage(Message msg)
	{
		this.msg = msg;
	}
}
