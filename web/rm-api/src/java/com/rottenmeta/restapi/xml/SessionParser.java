package com.rottenmeta.restapi.xml;

import com.rottenmeta.restapi.data.*;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;

/**
 * @author Zill
 */
public class SessionParser extends DefaultHandler
{

    static Logger logger = Logger.getLogger(SessionParser.class.getName());

    private SAXParser parser;
    private String atElement;
    private Session session;

    public SessionParser(String xmlString)
    {

        // Parse the XML using a tmp file and a SAX parsing
        try
        {

            logger.debug("in SessionParser - xml = " + xmlString);
            
            File tmpFile = File.createTempFile("cc-api", ".xml.tmp");
            PrintStream out = new PrintStream(new FileOutputStream(tmpFile));
            out.println(xmlString);
            out.close();

            parser = new SAXParser();
            parser.setContentHandler(this);

            logger.debug("starting parse session");
            parser.parse(tmpFile.toURI().toString());
            logger.debug("ending parse");

        }
        catch(Exception e)
        {
            logger.debug("XML parse error - " + e.toString() + ", atElemnt = " + atElement);
        }
        
    }

    @Override
    public void startElement(String uri, String local, String qName, Attributes attrs)
    {

        logger.trace("found element: " + local);

        if(session == null && local.equalsIgnoreCase("user-session"))
        {
            session = new Session();
        }
        else
            atElement = local;

    }

    @Override
    public void characters(char[] text, int start, int length)
    {

        String content = new String(text, start, length);

        logger.trace("found info: [atElement] " + atElement + " [content] " + content);

        if(atElement == null) return;

        if(atElement.equalsIgnoreCase("username"))
            session.setUser(content);
        else if(atElement.equalsIgnoreCase("password"))
            session.setPassword(content);
        else if(atElement.equalsIgnoreCase("long-session-option"))
            session.setIsLongSession(content.equalsIgnoreCase("true"));
        
    }

    public Session getSession() { return session; }

}