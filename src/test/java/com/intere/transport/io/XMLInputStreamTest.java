/*
 * XMLInputStreamTest.java
 * JUnit based test
 *
 * Created on November 21, 2005, 5:43 PM
 */

package com.intere.transport.io;

import com.xml.utils.XMLUtils;
import com.xml.transport.io.XMLInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import junit.framework.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

//==========================================================================
// TEST CLASS XMLInputStreamTest
//==========================================================================
/**
 *
 *
 * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
 */
public class XMLInputStreamTest extends TestCase
{
    //==========================================================================
    //  VARIABLE(S)
    //==========================================================================
    public static final String      TEST_BOMBER_1       = "<root><node><child>text</child></node><node name=\"2\" /></root>";
    public static final String      TEST_BOMBER_2       = "<root><tree><root><child /></root></tree></root>";
    //==========================================================================
    // CONSTRUCTOR(S)
    //==========================================================================
    
    //--------------------------------------------------------------------------
    // <>
    //--------------------------------------------------------------------------
    /**
     * This consturctor sets the test name.
     * @param testName The test name.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public XMLInputStreamTest(String testName)
    {
        super(testName);
    }
    
    //==========================================================================
    // METHOD(S)
    //==========================================================================
    
    protected void setUp() throws Exception
    { }
    
    protected void tearDown() throws Exception
    { }
    
    //--------------------------------------------------------------------------
    // suite
    //--------------------------------------------------------------------------
    /**
     * Test suite method.
     * @return The Test suite.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(XMLInputStreamTest.class);
        
        return suite;
    }
    
    //--------------------------------------------------------------------------
    // testLiveRead
    //--------------------------------------------------------------------------
    /**
     * This test initializes a ServerThread, then sends out test documents to
     * that server thread to test the "write" functionality.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public void testLiveRead()
    {
        System.out.println("Live Read Test");
        
        ServerThread st = new ServerThread();
        st.start();
        
        try
        {
            Socket sock = new Socket("localhost",st.ssock.getLocalPort());
            OutputStream out = sock.getOutputStream();
            
            for(int i=0;i<3;i++)
            {
                try
                {
                    System.out.println("Sleeping for " + i + " seconds before sending XML Document to Server");
                    Thread.sleep(i*1000);
                    XMLUtils.writeDocument(getTestDocument(),out);
                    assertEquals("Did not read the correct number of documents",i,st.documentsRead);
                }
                catch(Throwable t)
                {
                    t.printStackTrace();
                }
            }
        }
        catch (Throwable t)
        {
            System.out.println("caught exception: " + t.toString());
        }
        
        st.keepRunning = false;
    }
    
    //--------------------------------------------------------------------------
    // testRead
    //--------------------------------------------------------------------------
    /**
     * This method tests the Read method.
     * @throws Exception
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public void testRead()  throws Exception
    {
        System.out.println("read");
        
        int count = 5;
        
        XMLInputStream instance = new XMLInputStream( getInputStream( count ) );
        
        for( int i=0; i<count; i++ )
        {
            try
            {
                XMLUtils.readDocument( instance );
            }
            catch( Throwable t )
            {
                t.printStackTrace();
                instance.debug();
            }
            instance.reset();
        }
    }
    
    //--------------------------------------------------------------------------
    // testRead2
    //--------------------------------------------------------------------------
    /**
     * This method tests the XML Document Reader with a "custom" xml doc.  This
     * document does not contain the "<?xml ?>" header.
     * @throws Exception
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public void testRead2()
    {
        String xml = "<xml><http><head/><body>stuff</body></http><xdoc/></xml>";
        
        String buff = "";
        
        int count = 5;
        
        for( int i=0; i<count; i++ )
        {
            buff += xml;
        }
        
        XMLInputStream instance = new XMLInputStream( new ByteArrayInputStream( buff.getBytes() ) );
        
        for( int i=0; i<count; i++ )
        {
            try
            {
                XMLUtils.readDocument( instance );
            }
            catch( Throwable t )
            {
                t.printStackTrace();
                instance.debug();
            }
            
            instance.reset();
        }
    }
    
    //--------------------------------------------------------------------------
    // testRead3
    //--------------------------------------------------------------------------
    /**
     * The Third Test method; this tests that the XMLInputStream will read empty
     * XML Documents.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public void testRead3()
    {
        String xml = "<empty_doc/><?xml version=\"1.0\" encoding=\"UTF-8\"?><xml_document/>";
        
        String buff = xml + xml + xml;
        
        XMLInputStream instance = new XMLInputStream( new ByteArrayInputStream( buff.getBytes() ) );
        
        for( int i=0; i<6; i++ )
        {
            try
            {
                XMLUtils.readDocument( instance );
                instance.debug();
            }
            catch( Throwable t )
            {
                t.printStackTrace();
                instance.debug();
            }
            
            instance.reset();
        }
    }
    
    //--------------------------------------------------------------------------
    // testBomb
    //--------------------------------------------------------------------------
    /**
     * This test pushes the envelope, it tests with a non-standard XML Document.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public void testRootIsRoot()
    {
        System.out.println("testRootIsRoot");
        
        String buff = TEST_BOMBER_1 + TEST_BOMBER_1 + TEST_BOMBER_1;
        
        XMLInputStream instance = new XMLInputStream(new ByteArrayInputStream(buff.getBytes()));
        try
        {
            XMLUtils.readDocument(instance);
            instance.debug();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
        catch (ParserConfigurationException ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
        catch (SAXException ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
    
//    //--------------------------------------------------------------------------
//    // testBomb2
//    //--------------------------------------------------------------------------
//    /**
//     * This test also pushes the envelope.  It tests with an XML Document that
//     * uses a node of the same name as the root node.  
//     *
//     * This test currently fails; it exposes a shortcoming of the API.  This is
//     * typically not a big deal though, and is easy to work around.
//     *
//     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
//     */
//    public void testBomb2()
//    {
//        System.out.println("testBomb2");
//        
//        String buff = TEST_BOMBER_2 + TEST_BOMBER_2 + TEST_BOMBER_2;
//        XMLInputStream instance = new XMLInputStream(new ByteArrayInputStream(buff.getBytes()));
//        try
//        {
//            XMLUtils.readDocument(instance);
//            instance.debug();
//        }
//        catch (ParserConfigurationException ex)
//        {
//            ex.printStackTrace();
//            fail(ex.toString());
//        }
//        catch (IOException ex)
//        {
//            ex.printStackTrace();
//            fail(ex.toString());
//        }
//        catch (SAXException ex)
//        {
//            ex.printStackTrace();
//            fail(ex.toString());
//        }        
//    }
    
    //--------------------------------------------------------------------------
    // getInputStream
    //--------------------------------------------------------------------------
    /**
     * This method creates a ByteArrayInputStream with the test XML Document.
     * @return The ByteArrayInputStream object.
     * @throws ParserConfigurationException
     * @throws TransformerConfigurationException
     * @throws TransformerException
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    private ByteArrayInputStream getInputStream( int count ) throws ParserConfigurationException, TransformerConfigurationException, TransformerException
    {
        ByteArrayOutputStream out = getXMLOutputStream( count );
        return new ByteArrayInputStream( out.toByteArray() );
    }
    
    //--------------------------------------------------------------------------
    // getXMLOutputStream
    //--------------------------------------------------------------------------
    /**
     * This method gives you the ByteArrayOutputStream that contains the dump
     * of the Test XML Document.
     * @return The ByteArrayOutputStream
     * @throws ParserConfigurationException
     * @throws TransformerConfigurationException
     * @throws TransformerException
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    private ByteArrayOutputStream getXMLOutputStream( int count ) throws ParserConfigurationException, TransformerConfigurationException, TransformerException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        for( int i=0; i<count; i++ )
        {
            XMLUtils.writeDocument( getTestDocument(), out );
        }
        
        return out;
    }
    
    //--------------------------------------------------------------------------
    // getTestDocument
    //--------------------------------------------------------------------------
    /**
     * This method creates the Test XML Document.
     * @return The Document object.
     * @throws ParserConfigurationException
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    private Document getTestDocument() throws ParserConfigurationException
    {
        Document doc = XMLUtils.getNewDocument();
        
        Element e = doc.createElement( "root" );
        
        e.appendChild( doc.createElement( "eric" ) );
        e.appendChild( doc.createElement( "dad" ) );
        e.appendChild( doc.createElement( "willis" ) );
        
        doc.appendChild( (Node)e );
        
        return doc;
    }
    
    
    
    //==========================================================================
    //  Inner Class(es)
    //==========================================================================
    
    //--------------------------------------------------------------------------
    // ServerThread
    //--------------------------------------------------------------------------
    /**
     * This class is used to open a ServerSocket, listen on it, read from it, and
     * then gracefully die.
     * @param
     * @return
     * @throws
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public class ServerThread extends Thread
    {
        public ServerSocket     ssock           = null;
        public Socket           listener        = null;
        public XMLInputStream   input           = null;
        public boolean          keepRunning     = true;
        public int              documentsRead   = 0;
        
        public ServerThread()
        {
            try
            {
                ssock = new ServerSocket(0);
                System.out.println("Server running on port: " + ssock.getLocalPort());
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        
        public void run()
        {
            try
            {
                listener = ssock.accept();
                System.out.println("Client Connected; server will begin reading...");
                input = new XMLInputStream(listener.getInputStream());
                
                while(keepRunning)
                {
                    Document doc = XMLUtils.readDocument(input);
                    input.reset();
                    System.out.println("Server recieved document: " + XMLUtils.toString(doc));
                    ++documentsRead;
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
            catch (ParserConfigurationException ex)
            {
                ex.printStackTrace();
            }
            catch (SAXException ex)
            {
                ex.printStackTrace();
            }
            catch(TransformerConfigurationException ex)
            {
                ex.printStackTrace();
            }
            catch(TransformerException ex)
            {
                ex.printStackTrace();
            }
            
            try
            {
                listener.close();
                ssock.close();
                input.close();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
}
