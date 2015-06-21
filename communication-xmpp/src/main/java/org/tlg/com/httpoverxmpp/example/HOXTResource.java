package org.tlg.com.httpoverxmpp.example;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/test")
public class HOXTResource {

	public HOXTResource(){
		
	}

	@POST
	public String helloClient(String msg){
		System.out.println("Client says: " + msg);
		return new String("Sure it's at /test/myobject");
	}
	
	@GET
	@Path("/myobject")
	public MyObject getMyObject(){
		return new MyObject("Here you go", 10);
	}
	
}
