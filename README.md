HTTPoverXMPP
============

Prototype library for HTTP over XMPP communication

Follows XEP-0332 HTTP over XMPP transport
http://xmpp.org/extensions/inbox/http-over-xmpp.html


##Usage

###Server side

Create a resource in the jax.rs style:
```java
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
```

Start the XMPPManager:
```java
import java.io.IOException;

import org.jivesoftware.smack.XMPPException;
import org.tlg.com.httpoverxmpp.api.ResourceManager;
import org.tlg.com.httpoverxmpp.api.XmppConnectionDetails;
import org.tlg.com.httpoverxmpp.api.XmppManager;

public class XHTTPServer 
{
    public static void main( String[] args )
    {
    	XmppConnectionDetails conDetails = new XmppConnectionDetails("your.xmpp.server.com", "test-server", "yourpassword", "yourresource");
    	ResourceManager rm = new ResourceManager("org.tlg.com.examples.xhttp.server");
        XmppManager xmppManager = new XmppManager(conDetails, rm);
        try {
			xmppManager.init();
			System.in.read();
		} catch (XMPPException e1) {
			System.out.println("An Xmpp error occured");
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("Stopped.");
		xmppManager.destroy();
    }
}
```


###Client Side

```java
import org.jivesoftware.smack.XMPPException;
import org.tlg.com.http.HTTPRequest;
import org.tlg.com.http.HTTPResponse;
import org.tlg.com.httpoverxmpp.api.XmppConnectionDetails;
import org.tlg.com.httpoverxmpp.api.XmppManager;
import org.tlg.com.httpoverxmpp.exceptions.XmppException;

public class XHTTPClient 
{
    public static void main( String[] args )
    {
    	XmppConnectionDetails conDetails = new XmppConnectionDetails("your.xmpp.server.com", "test-client", "yourpassword", "yourresource");
        XmppManager xmppManager = new XmppManager(conDetails, null);
        try {
			xmppManager.init();
		} catch (XMPPException e1) {
			e1.printStackTrace();
			System.out.println("Xmpp error");
		}
        HTTPRequest req = new HTTPRequest();
        req.setMethod("POST");
        req.setEntity("Hello server! How are you?");
        req.setUri("/test");
        HTTPResponse res = null;
        try {
			res = xmppManager.sendRequest(req, "test-server@your/yourresource");
		} catch (XmppException e) {
			System.out.println("An error occured!");
			e.printStackTrace();
		}
        
        System.out.println("The server says: " + res.getEntity());
        xmppManager.destroy();
    }
}
```
