package org.imolem.xml.parser;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CoreParserEngine implements Constants
{
	StringBuilder hierarchy = new StringBuilder();
	
	HashMap<String, String> dataHolder = new HashMap<String, String>();
	
	ArrayList<ArrayList<String>> returnData = new ArrayList<ArrayList<String>>();
	
	boolean recentlyClosed = false;
	
	int totalRowsCreated = 0;
	
	public CoreParserEngine()
	{
		
	}
	
	public ArrayList<ArrayList<String>> parseXML(String xml) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	    Document doc = builder.parse(is);
	  
	    dataHolder.clear();
	    if(doc.hasChildNodes())
	    {
	    	hierarchy = hierarchy.append(doc.getChildNodes().item(0).getNodeName());
	    	
	    	NamedNodeMap attributes = doc.getChildNodes().item(0).getAttributes();
	    	for(int attributesIterator = 0;attributesIterator < attributes.getLength();attributesIterator++)
				dataHolder.put(hierarchy.toString() + attributeSeparator + attributes.item(attributesIterator).getNodeName(), attributes.item(attributesIterator).getNodeValue());
	    	
	    	parse(doc.getChildNodes().item(0).getChildNodes());
	    }
	    
	    System.out.println("Total Rows:" + totalRowsCreated);
		
		return null;
	}
	
	public void parse(NodeList nodes)
	{
		for(int nodesIterator = 0;nodesIterator < nodes.getLength();nodesIterator++)
		{
			Node currentNode = nodes.item(nodesIterator);
			String nodeName = nodes.item(nodesIterator).getNodeName().replaceAll("_", "");
			ArrayList<String> removeAfterwards = new ArrayList<String>();
			recentlyClosed = false;
			hierarchy.append(tagSeparator + nodeName);
			NamedNodeMap attributes = currentNode.getAttributes();
			
			for(int attributesIterator = 0;attributesIterator < attributes.getLength();attributesIterator++)
			{
				dataHolder.put(hierarchy.toString() + attributeSeparator + attributes.item(attributesIterator).getNodeName(), attributes.item(attributesIterator).getNodeValue());
				removeAfterwards.add(hierarchy.toString() + attributeSeparator + attributes.item(attributesIterator).getNodeName());
			}
					
			if(currentNode.hasChildNodes() && currentNode.getFirstChild().getNodeName().equals("#text"))
			{
				dataHolder.put(hierarchy.toString(), currentNode.getFirstChild().getTextContent());
				removeAfterwards.add(hierarchy.toString());
			}
			/*else if(!currentNode.hasChildNodes())
			{
				//System.out.println("Self closing tag !");
			}*/
			else if(currentNode.hasChildNodes() && !currentNode.getFirstChild().getNodeName().equals("#text"))
			{
				parse(currentNode.getChildNodes());
			}
			hierarchy = new StringBuilder(hierarchy.substring(0, hierarchy.lastIndexOf(tagSeparator)));
			
			if(!currentNode.getNodeName().equals("#text")  && !recentlyClosed)
			{
				totalRowsCreated++;
				recentlyClosed = true;
				System.out.println("Time to write the data!" + dataHolder);
				for(int i=0;i<removeAfterwards.size();i++)
					dataHolder.remove(removeAfterwards.get(i));
			}
			else
			{
				for(int i=0;i<removeAfterwards.size();i++)
					dataHolder.remove(removeAfterwards.get(i));
			}
		}
	}
}