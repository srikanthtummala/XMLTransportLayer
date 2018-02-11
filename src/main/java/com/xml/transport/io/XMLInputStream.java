/*
 * FILE:        XMLInputStream.java
 *
 * CREATED:     November 6, 2005, 5:26 AM
 *
 * NOTES:       
 *
 * COPYRIGHT:   LGPL
 *
 */

package com.xml.transport.io;

import java.io.IOException;
import java.io.InputStream;

//--------------------------------------------------------------------------
// CLASS XMLInputStream
//--------------------------------------------------------------------------
/**
 * This class is used to Fool the XML APIs into allowing multiple XML Documents
 * to be sent over a Single I/O Stream.  Obviously it will be up to you to ensure
 * that the Documents are Well Formed.  This API is very dumb, it does not allow
 * you to reuse the root node name anywhere else in the document.  <br />
 * <strong>For instance the following document is invalid:</strong><br/>
 * &lt;xml&gt;&lt;data&gt;&lt;xml&gt;blah&lt;/xml&gt;&lt;/data&gt;&lt;/xml&gt;
 * <br/><br/>
 * <strong>The following document is valid:</strong><br/>
 * &lt;xml&gt;&lt;data&gt;blah&lt;/data&gt;&lt;/xml&gt;
 * <br/><br/>
 * <b>Sample Usage</b>:<br/>
 * <pre>
 * XMLInputStream xin = new XMLInputStream(in);     // in is a regular InputStream (of any type).
 * Document doc = XMLUtils.readDocument(xin);       // Get an XML Document.
 * xin.reset();                                     // now Reset the XML InputStream - we can now read again...
 * Document doc2 = XMLUtils.readDocument(xin);      // Read another XML Document.
 * </pre>
 *
 * @version 0.1.5
 * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
 */
public class XMLInputStream extends InputStream
{
    //==========================================================================
    // VARIABLE(S)
    //==========================================================================
    private InputStream         in                              = null;
    private boolean             closed                          = false;
    private boolean             readingXML                      = true;
    private ParserState         state                           = new ParserState();
    private int                 index                           = 0;
    private String              rootTag                         = null;
    private String              buffer                          = null;
    
    private static final String HEADER_START    = "<?xml";
    private static final String HEADER_END      = "?>";
    
    /**
     * Creates a new instance of XMLInputStream.
     * @param in The InpuStream to initialize this class with.
     */
    public XMLInputStream( InputStream in )
    {
        this.in = in;
    }
    
    /**
     * This method returns -1 if the socket is closed; otherwise it just grabs
     * the next byte from the actual InputStream; processes the Byte, and then
     * returns the Byte to the calling method.
     * @return the Byte that was just read (as an integer).
     * @throws IOException
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public int read() throws IOException
    {
        int read = -1;
        char cRead = '\0';
        
        if( isReadingXML() )
        {
            if( !isClosed() )
            {
                if( buffer == null )
                {
                    buffer = new String();
                }
                read = in.read();
                
                cRead = (char)read;
                
                buffer += "" + cRead;
                processRead( read );
            }
        }
        else
        {
            read = in.read();
        }
        
        return read;
    }
    
    /**
     * This method is used to debug the InputStream.  This will System.out.println all of the
     * output of the buffer.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public void debug()
    {
        if( buffer != null )
        {
            System.out.println( "Read: " + buffer );
        }
    }
    
    /**
     * This method is responsible for processing the byte that was just read.
     * @param read The Byte that was just read.
     * @throws IOException
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    protected void processRead( int read ) throws IOException
    {
        String working = getSearchString();
        
        switch( state.getState() )
        {
            case ParserStateTypes.MODE_FIND_HEADER_START:
            {
                if( index == 1 )
                {
                    if( (char)read != '?' )
                    {
                        state.setState(ParserStateTypes.MODE_FIND_DOC_ROOT_TAG);
                        processRead( read );
                        return;
                    }
                }
            }
            
            case ParserStateTypes.MODE_FIND_HEADER_END:
            case ParserStateTypes.MODE_FIND_DOC_ROOT_START:
            case ParserStateTypes.MODE_FIND_DOC_END:
            {
                if( working.charAt( index ) == (char)read )
                {
                    ++index;
                    if( index == working.length() )
                    {
                        nextMode();
                    }
                }
                else
                {
                    index = 0;
                }
            }
            break;
            
            case ParserStateTypes.MODE_FIND_DOC_ROOT_TAG:
            {
                if( rootTag == null )
                {
                    rootTag = new String();
                }
                
                if( (char)read != '>' )
                {
                    rootTag += "" + (char)read;
                }
                else if( rootTag.trim().charAt( rootTag.length() - 1 ) != '/' )
                {
                    nextMode();
                }
                else
                {
                    close();
                }
            }
            break;
        }
        
    }
    
    /**
     * This method is responsible for moving to the next mode.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    protected void nextMode()
    {
        state.nextState();
        index = 0;
        
        if( state.getState() == ParserStateTypes.MODE_READ_DOC_CLOSING )
        {
            try
            {
                close();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * This method will tell you what our "Search String" is; based on what
     * the current mode is.
     * @return The current search String.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    protected String getSearchString()
    {
        String str = null;
        
        switch(state.getState())
        {
            case ParserStateTypes.MODE_FIND_HEADER_START:
            {
                str = HEADER_START;
            }
            break;
            
            case ParserStateTypes.MODE_FIND_HEADER_END:
            {
                str = HEADER_END;
            }
            break;
            
            case ParserStateTypes.MODE_FIND_DOC_ROOT_START:
            {
                str = "<";
            }
            break;
            
            case ParserStateTypes.MODE_FIND_DOC_ROOT_TAG:
            {
                str = ">";
            }
            break;
            
            case ParserStateTypes.MODE_FIND_DOC_END:
            {
                str = "</" + rootTag + ">";
            }
            break;
            
            case ParserStateTypes.MODE_READ_DOC_CLOSING:
            {
                str = null;
            }
            break;
        }
        
        return str;
    }
    
    /**
     * This method closes the socket (actually simulates closing the socket; does not actually close it).
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public void close() throws IOException
    {
        closed = true;
    }
    
    /**
     * This method rests everything; and gets ready for reading again.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public void reset()
    {
        closed = false;
        state.setState(ParserStateTypes.MODE_FIND_HEADER_START);
        index = 0;
        rootTag = null;
        buffer = null;
    }
    
    //==========================================================================
    //  GETTER(S) & SETTER(S)
    //==========================================================================
    
    /**
     * This method tells you if the this InputStream "Closed" flag is set to
     * true or not.  This flag governs wether or not the read method will return
     * -1 or actually read from the InputStream.  Note: This is only the case when
     * the reader is reading XML.
     * @return True if the XML Document has been read.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public boolean isClosed()
    {
        return closed;
    }
    
    /**
     * This method tells you if the mode is "isReadingXML".
     * @return True if the mode is currently set to Read XML, false otherwise.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public boolean isReadingXML()
    {
        return readingXML;
    }
    
    /**
     * This method sets the status of "reading XML" to what you provide.
     * @param readingXML wether or not you're reading XML.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public void setReadingXML(boolean readingXML)
    {
        this.readingXML = readingXML;
    }
    
    /**
     * Getter for the InputStream.
     * @return The InputStream object.
     *
     * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
     */
    public InputStream getInputStream()
    {
        return in;
    }
   
    //==========================================================================
    //  Inner Class(es)
    //==========================================================================

    public class ParserState
    {
        private int state = XMLInputStream.ParserStateTypes.MODE_FIND_HEADER_START;
        
        //==========================================================================
        //  GETTER(S) & SETTER(S)
        //==========================================================================

        public int getState()
        {
            return state;
        }

        public void setState(int state)
        {
            this.state = state;
        }

        private void nextState()
        {
            ++state;
        }
        
        
    }
    
    public interface ParserStateTypes
    {
        // The different states that the reader can be in.
        public final int    MODE_FIND_HEADER_START          = 0;
        public final int    MODE_FIND_HEADER_END            = 1;
        public final int    MODE_FIND_DOC_ROOT_START        = 2;    //start buffering bytes (handle "/" character too; this means an empty document).
        public final int    MODE_FIND_DOC_ROOT_TAG          = 3;
        public final int    MODE_FIND_DOC_END               = 4;
        public final int    MODE_READ_DOC_CLOSING           = 5;
    }
    
}
