package org.tlg.com.httpoverxmpp.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.PathParam;
import javax.xml.bind.JAXBException;

import org.tlg.com.http.HTTPRequest;
import org.tlg.com.http.HTTPResponse;

public final class RequestHandler {

	private RequestHandler() {
	}

	public static HTTPResponse handleRequest(HTTPRequest request,
			ResourceManager rm) {
		HTTPResponse response = new HTTPResponse();
		String uri = request.getUri();
		Map<String, String> params = new HashMap<String, String>();
		Resource r = rm.getResourceMatch(uri, params);
		if (r == null) {
			response.setStatusCode(404);
			return response;
		}
		Method method = r.methods[request.getMethodType().ordinal()];
		if (method == null) {
			response.setStatusCode(405);
			return response;
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
			if (ret != null && ret.getClass() != Integer.class) {
				response.setEntity(ret.getClass(), ret);
			}
		} catch (InstantiationException | IllegalAccessException
				| JAXBException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setStatusCode(500);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception!!!");
			System.out.println(e.toString());
		}
		response.setStatusCode(200);
		return response;
	}

	private static Object invokeMethod(Object inst, Method method,
			Map<String, String> params, HTTPRequest request)
			throws InstantiationException, IllegalAccessException,
			JAXBException, IllegalArgumentException, InvocationTargetException {
		if (inst == null) {
			System.out.println(method.getDeclaringClass());
			System.out.println(method.getName());
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
				args[i] = request.getEntity(types[i]);
			}
		}
		return method.invoke(inst, args);
	}
}
