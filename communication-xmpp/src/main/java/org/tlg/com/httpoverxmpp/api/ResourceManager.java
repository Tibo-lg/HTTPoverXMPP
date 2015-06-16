package org.tlg.com.httpoverxmpp.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.reflections.Reflections;
import org.tlg.com.api.MethodType;

public class ResourceManager {

	List<Resource> resources;

	public ResourceManager() {
		resources = new ArrayList<Resource>();
	}

	public ResourceManager(String pkgName) {
		this();
		Reflections reflections = new Reflections(pkgName);
		Set<Class<?>> beans = reflections.getTypesAnnotatedWith(Path.class);

		for (Class<?> c : beans) {
			this.addBeanResources(c, null);
		}
	}

	public void registerInstance(Object inst) {
		addBeanResources(inst.getClass(), inst);
	}

	private void addBeanResources(Class<?> c, Object inst) {
		Annotation[] annotations = c.getAnnotations();
		for (Annotation a : annotations) {
			if (a instanceof Path) {
				Resource r = new Resource(((Path) a).value(), inst);
				resources.add(r);
				addResourceMethods(c, r);
			}
		}
	}

	private void addResourceMethods(Class<?> c, Resource r) {
		Map<String, Resource> tmpResources = new HashMap<String, Resource>();
		for (Method method : c.getMethods()) {
			if (method.getAnnotations() != null) {
				List<MethodType> types = new ArrayList<MethodType>();
				Resource tmpResource = r;
				for (Annotation a : method.getAnnotations()) {
					if (a instanceof Path) {
						if (!tmpResources.containsKey(r.getUrl() + ((Path) a).value())) {
							tmpResource = new Resource(r.getUrl()
									+ ((Path) a).value(), r.getInst());
						} else {
							tmpResource = tmpResources.get(r.getUrl() + ((Path) a).value());
						}
					} else if (a instanceof GET) {
						types.add(MethodType.GET);
					} else if (a instanceof PUT) {
						types.add(MethodType.PUT);
					} else if (a instanceof POST) {
						types.add(MethodType.POST);
					} else if (a instanceof DELETE) {
						types.add(MethodType.DELETE);
					} else if (a instanceof HEAD) {
						types.add(MethodType.HEAD);
					}
				}
				if (!types.isEmpty()) {
					for (MethodType type : types) {
						tmpResource.setMethod(type, method);
					}
					tmpResources.put(tmpResource.getUrl(), tmpResource);
				}
			}
		}
		for (Resource tmpR : tmpResources.values()) {
			resources.add(tmpR);
		}
	}

	public Resource getResourceMatch(String uri, Map<String, String> params) {
		for (Resource r : resources) {
			if (r.matchUri(uri, params)) {
				return r;
			}
		}
		return null;
	}

}
