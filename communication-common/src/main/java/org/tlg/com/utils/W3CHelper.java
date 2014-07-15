package org.tlg.com.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class W3CHelper {

	public static Document parseDocument(String xml) {
		InputStream is = IOUtils.toInputStream(xml);
		Document document = parseDocument(is);
		return document;
	}
	
	public static Document parseDocument(InputStream xmlStream) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(xmlStream);
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public static NodeList selectNodes(String xPath, Node target) {
		XPath xpathForUri = XPathFactory.newInstance().newXPath();
		NodeList nodes = null;
		
		try {
			XPathExpression expr = xpathForUri.compile(xPath);
			nodes = (NodeList) expr.evaluate(target, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
		
		return nodes;
	}
	
	public static Node selectSingleNode(String xPath, Node target) {
		NodeList nodes = selectNodes(xPath, target);
		return (nodes != null && nodes.getLength() > 0 ? nodes.item(0) : null);
	}
	
	public static String nodeToString(Node node) {
		String xml = "";
		try {
			Transformer nodeTransformer = TransformerFactory.newInstance().newTransformer();
			nodeTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			nodeTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			nodeTransformer.transform(new DOMSource(node), new StreamResult(new BufferedOutputStream(baos)));
			xml = baos.toString("UTF-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return xml;
	}
	
}
