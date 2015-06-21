package org.tlg.com.httpoverxmpp.api;

import java.lang.reflect.Method;
import java.util.Map;

import org.glassfish.jersey.uri.UriTemplate;
import org.jivesoftware.smackx.hoxt.packet.HttpMethod;

public class Resource {
	String url;
	UriTemplate uriTemplate;
	Method methods[];
	Object inst = null;
	
	public Resource(String url){
		this(url, null);
	}
	
	public Resource(String url, Object inst){
		this.inst = inst;
		this.url = url;
		this.uriTemplate = new UriTemplate(url);
		methods = new Method[HttpMethod.values().length];
	}
	
	public Object getInst() {
		return inst;
	}

	public void setInst(Object inst) {
		this.inst = inst;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public UriTemplate getUriTemplate() {
		return uriTemplate;
	}

	public void setUriTemplate(UriTemplate uriTemplate) {
		this.uriTemplate = uriTemplate;
	}

	public Method[] getMethods() {
		return methods;
	}

	public void setMethod(HttpMethod type, Method method){
		 methods[type.ordinal()] = method;
	}
	
	public boolean matchUri(String uri, Map<String, String> params){
		return this.uriTemplate.match(uri, params);
	}
}
