/*
 * FILE:        XMLUtils.java
 *
 * CREATED:     November 6, 2005, 6:19 AM
 *
 * NOTES:
 *
 * COPYRIGHT:   
 *
 */

package com.xml.utils;

import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

//--------------------------------------------------------------------------
// CLASS XMLUtils
//--------------------------------------------------------------------------
/**
 * This class provides some common XML Utility methods for your convenience.
 *
 * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
 */
public abstract class XMLUtils
{
    //==========================================================================
    // Variable(s)
    //==========================================================================
    private static DocumentBuilderFactory       dbf             = null;
    private static TransformerFactory           tf              = null;
    private static DefaultErrorHandler          errorHandler    =   null;
    
    //==========================================================================
    // Method(s)
    //==========================================================================
    
    /**
     * This method gets the child node with the provided name from the 
     * provided start node.
     * @param start The Parent node to scan children.
     * @param nodeName The name of the node that we're looking for.
     * @return The Node with name: nodeName, whose parent is the start node.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public static Node getChildNode( Node start, String nodeName )
    {
        Node child = null;
        
        NodeList children = start.getChildNodes();
        
        for( int i=0; i<children.getLength(); i++ )
        {
            Node n = children.item( i );
            
            if( nodeName.equals( n.getNodeName() ) )
            {
                child = n;
                break;
            }
        }
        
        return child;
    }    
    
    /**
     * This method creates a text node from the provided Document object, node name,
     * and node value.
     * @param doc The Document used to create the Node.
     * @param nodeName The name for the text node.
     * @param nodeValue The value for the text node.
     * @return The created node with the text under it.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public static Node createTextNode( Document doc, String nodeName, String nodeValue )
    {
       Node n = (Node)doc.createElement( nodeName );
       n.appendChild( doc.createTextNode( nodeValue ) );
       
       return n;
    }
    
    /**
     * This method reads an XML Document from the provided InputStream.
     * @param in The InputStream object.
     * @return The Document object that was read from the stream.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public static Document readDocument( InputStream in ) throws ParserConfigurationException, SAXException, IOException
    {
        getDocumentBuilderFactory().setXIncludeAware(true);
        
        DocumentBuilder builder = getDocumentBuilderFactory().newDocumentBuilder();
        
        return getDocumentBuilderFactory().newDocumentBuilder().parse( in );
    }    
    
    /**
     * This method writes the provided XML Document to the provided OutputStream.
     * @param doc The XML Document to write to the stream.
     * @param out The OutputStream to write to.
     * @throws TransformerConfigurationException
     * @throws TransformerException
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public static void writeDocument( Document doc, OutputStream out ) throws TransformerConfigurationException, TransformerException
    {
        Transformer t = getNewTransformer();
        
        DOMSource ds = new DOMSource(doc);
        StreamResult sr = new StreamResult( out );
        t.transform( ds, sr );
    }   
    
    /**
     * This method takes an XML Document, and converts it to a String for you.
     * @param doc The XML Document to be converted to a String.
     * @return The XML String that represents the Document.
     * @throws TransformerConfigurationException
     * @throws TransformerException
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public static String toString(Document doc) throws TransformerConfigurationException, TransformerException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeDocument(doc,out);
        
        return out.toString();
    }
    
    /**
     * This method gives you a new XML Document Object.
     * @return A new XML Document object.
     * @throws ParserConfigurationException
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public static Document getNewDocument() throws ParserConfigurationException
    {
        return getDocumentBuilder().newDocument();
    }
        
    /**
     * This method gives you a new Transformer Instance.
     * @return A new Transformer object instance.
     * @throws TransformerConfigurationException
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    private static Transformer getNewTransformer() throws TransformerConfigurationException
    {
        return getTransformerFactory().newTransformer();
    }
    
    /**
     * This method gives you the Transformer Factory (and creates it if necessary).
     * @return The TransformerFactory object instance.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    private static synchronized TransformerFactory getTransformerFactory()
    {
        if( tf == null )
        {
            tf = TransformerFactory.newInstance();
        }
        
        return tf;
    }    
    
    /**
     * This method yields a Document builder object.
     * @return A new Document Builder object.
     * @throws ParserConfigurationException
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException
    {
        DocumentBuilder builder = getDocumentBuilderFactory().newDocumentBuilder();
        builder.setErrorHandler(getErrorHandler());
        
        return builder;
    }
    
    /**
     * This method gives you the factory; creating it if necessary.
     * @return The DocumentBuilderFactory instance.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    private static synchronized DocumentBuilderFactory getDocumentBuilderFactory()
    {
        if( dbf == null )
        {
            dbf = DocumentBuilderFactory.newInstance();
        }
        
        return dbf;
    }
    
    /**
     * This method yields the DefaultErrorHandler for Errors inside the XML
     * API calls.
     * @return The DefaultErrorHandler object.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    private static synchronized DefaultErrorHandler getErrorHandler()
    {
        if(errorHandler==null)
        {
            errorHandler = new DefaultErrorHandler();
        }
        
        return errorHandler;
    }    
    
    //==========================================================================
    // INNER CLASS(ES)
    //==========================================================================

    /**
     * This class is used to Handle Errors.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    private static class DefaultErrorHandler implements ErrorHandler
    {
        public void warning(SAXParseException ex) throws SAXException
        {
//            Logger.getLogger(getClass()).warn("SaxParserException: " + ex.getMessage(),ex);
        	ex.printStackTrace();
        }

        public void error(SAXParseException ex) throws SAXException
        {
//            Logger.getLogger(getClass()).error("SaxParserException: " + ex.getMessage(),ex);
        	ex.printStackTrace();
        }

        public void fatalError(SAXParseException ex) throws SAXException
        {
//            Logger.getLogger(getClass()).fatal("SaxParserException: " + ex.getMessage(),ex);
        	ex.printStackTrace();
        }
    }
    
}
