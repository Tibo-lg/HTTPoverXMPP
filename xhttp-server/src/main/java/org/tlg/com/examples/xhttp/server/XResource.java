package org.tlg.com.examples.xhttp.server;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/test")
public class XResource {

	public XResource(){
		
	}

	@POST
	public String helloClient(String msg){
		System.out.println("Client says: " + msg);
		return new String("Hello Client, doing fine and you?");
	}
	
}
