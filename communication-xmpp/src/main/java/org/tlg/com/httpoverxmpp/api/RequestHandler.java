package org.tlg.com.httpoverxmpp.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.PathParam;
import javax.xml.bind.JAXBException;

import org.jivesoftware.smack.packet.NamedElement;
import org.jivesoftware.smackx.hoxt.packet.AbstractHttpOverXmpp;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppReq;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppResp;
import org.tlg.com.httpoverxmpp.util.HOXTUtil;

public final class RequestHandler {

	private RequestHandler() {
	}

	public static HttpOverXmppResp handleRequest(HttpOverXmppReq request,
			ResourceManager rm) {
		HttpOverXmppResp response = new HttpOverXmppResp();
		String uri = request.getResource();
		Map<String, String> params = new HashMap<String, String>();
		Resource r = rm.getResourceMatch(uri, params);
		if (r == null) {
			return HOXTUtil.set404(response);
		}
		Method method = r.methods[request.getMethod().ordinal()];
		if (method == null) {
			return HOXTUtil.set405(response);
		}
		method.setAccessible(true);
		Object ret = null;
		try {
			if (r.getInst() != null) {
				synchronized (r.getInst()) {
					ret = invokeMethod(r.getInst(), method, params, request);
				}
			}else{
				ret = invokeMethod(null, method, params, request);
			}
			if (ret != null && ret.getClass() != Integer.class && ret.getClass() != String.class) {
				response.setData(HOXTUtil.getDataFromObject(ret.getClass(), ret));
			}else if(ret.getClass() == String.class){
				AbstractHttpOverXmpp.Text child = new AbstractHttpOverXmpp.Text((String)ret);
				AbstractHttpOverXmpp.Data data = new AbstractHttpOverXmpp.Data(child);
				response.setData(data);
			}
		} catch (JAXBException e) {
			return HOXTUtil.set500(response);
		} catch (Exception e) {
			e.printStackTrace();
			return HOXTUtil.set500(response);
		}
		response.setStatusCode(200);
		return response;
	}

	private static Object invokeMethod(Object inst, Method method,
			Map<String, String> params, HttpOverXmppReq request)
			throws InstantiationException, IllegalAccessException,
			JAXBException, IllegalArgumentException, InvocationTargetException {
		if (inst == null) {
			inst = method.getDeclaringClass().newInstance();
		}
		Annotation annotations[][] = method.getParameterAnnotations();
		Class<?> types[] = method.getParameterTypes();
		Object args[] = new Object[types.length];
		for (int i = 0; i < annotations.length; i++) {
			for (int j = 0; j < annotations[i].length; j++) {
				if (annotations[i][j] instanceof PathParam) {
					String paramName = ((PathParam) annotations[i][j]).value();
					if (types[i] == Integer.TYPE) {
						args[i] = Integer.parseInt(params.get(paramName));
					} else {
						args[i] = paramName;
					}
				}
			}
			if (args[i] == null) {
				NamedElement child = request.getData().getChild();
				if (child instanceof AbstractHttpOverXmpp.Xml) {
	                args[i] = HOXTUtil.getEntity(((AbstractHttpOverXmpp.Xml) child).getText(), types[i]);
	            } else if(child instanceof AbstractHttpOverXmpp.Text){
	            	args[i] = ((AbstractHttpOverXmpp.Text) child).getText();
	            }else {
	                // process other AbstractHttpOverXmpp.DataChild subtypes
	            }
			}
		}
		return method.invoke(inst, args);
	}
}
