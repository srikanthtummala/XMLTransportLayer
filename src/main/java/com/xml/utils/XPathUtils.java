/*
 * FILE:        XPathUtils.java
 *
 * CREATED:     November 11, 2005, 5:08 AM
 *
 * NOTES:
 *
 * COPYRIGHT:   
 *
 */


package com.xml.utils;

import javax.xml.xpath.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//------------------------------------------------------------------------------
// CLASS XPathUtils
//------------------------------------------------------------------------------
/**
 * This class provides some utility methods for using XPath under Java 5.
 *
 * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
 */
public abstract class XPathUtils
{
    
    //==========================================================================
    // VARIABLE(S)
    //==========================================================================
    private static XPathFactory     xpathFactory        = null;
    private static XPath            xpath               = null;
    
    //==========================================================================
    // METHOD(S)
    //==========================================================================
    
    //--------------------------------------------------------------------------
    // getAttribute
    //--------------------------------------------------------------------------
    /**
     * This method yields the Attribute at the node extracted by the expression.
     * @param parentNode The node to start at.
     * @param expression The XPath expression to evaluate.
     * @param attribute The Name of the attribute you would like to extract from the
     * XPath'd node.
     * @return The Value of the attribute, or the empty String if no matching node
     * or matching attribute could be found.
     * @throws XPathExpressionException
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public static String getAttribute( Node parentNode, String expression, String attribute ) throws XPathExpressionException
    {
        String attr = "";
        
        Node n = getNode( parentNode, expression );
        
        if( n != null )
        {
            n = n.getAttributes().getNamedItem( attribute );
        }
        
        if( n != null )
        {
            attr = n.getNodeValue();
        }
        
        return attr;
    }
    
    //--------------------------------------------------------------------------
    // getString
    //--------------------------------------------------------------------------
    /**
     * This method uses XPath to get you the String from the provided node.
     * @param parentNode The Node to start at.
     * @param expression The XPath Expression to evaluate.
     * @return The String that you want to extract using the XPath expression.
     * @throws XPathExpressionException
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public static String getString( Node parentNode, String expression ) throws XPathExpressionException
    {
        return (String)getXPath().evaluate( expression, parentNode, XPathConstants.STRING );
    }
    
    //--------------------------------------------------------------------------
    // getNode
    //--------------------------------------------------------------------------
    /**
     * This method uses XPath to get you a child node of the provided node.
     * @param parentNode The node to start at.
     * @param expression The XPath Expression to evaluate.
     * @return The Node that was retrieved by the provided node and xpath expression.
     * @throws XPathExpressionException
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public static Node getNode( Node parentNode, String expression ) throws XPathExpressionException
    {
        return (Node) getXPath().evaluate(expression, parentNode, XPathConstants.NODE);
    }
    
    //--------------------------------------------------------------------------
    // getNodeList
    //--------------------------------------------------------------------------
    /**
     * This method uses XPath to get you a NodeList of the provided node.
     * @param parentNode The node to start at.
     * @param expression The XPath Expression to evalutate.
     * @return A node list of the matching nodes.
     * @throws XPathExpressionException
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public static NodeList getNodeList(Node parentNode, String expression) throws XPathExpressionException
    {
        return (NodeList) getXPath().evaluate(expression,parentNode,XPathConstants.NODESET);
    }
    
    //--------------------------------------------------------------------------
    // getXPathFactory
    //--------------------------------------------------------------------------
    /**
     * This method yields the XPath Factory (creating it if necessary).
     * @return The XPathFactory object.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    protected static synchronized XPathFactory getXPathFactory()
    {
        if( xpathFactory == null )
        {
            xpathFactory = XPathFactory.newInstance();
        }
        
        return xpathFactory;
    }
    
    //--------------------------------------------------------------------------
    // getXPath
    //--------------------------------------------------------------------------
    /**
     * This method gives you an XPath object from the XPath Factory.
     * @return A new XPath object.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    protected static synchronized XPath getXPath()
    {
        if( xpath == null )
        {
            xpath = getXPathFactory().newXPath();
        }
        
        return xpath;
    }
    
}
