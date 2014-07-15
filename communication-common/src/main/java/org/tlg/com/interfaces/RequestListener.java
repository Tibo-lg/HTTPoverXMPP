package org.tlg.com.interfaces;

import org.tlg.com.http.HTTPRequest;
import org.tlg.com.http.HTTPResponse;

public interface RequestListener {
	public HTTPResponse onRequestReceived(HTTPRequest httpRequest);
}
