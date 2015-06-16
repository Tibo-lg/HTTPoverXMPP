package org.tlg.com.httpoverxmpp.api;

import java.util.HashMap;
import java.util.Map;

public class Context {
	Map<String, Object> params;
	
	public Context(){
		params = new HashMap<String, Object>();
	}
	
	public Context(Map<String, Object> params){
		this.params = params;
	}
	
	public void addParam(String name, Object obj){
		this.params.put(name, obj);
	}
	
	public Object getParam(String name, Object obj){
		return this.params.get(name);
	}
}
