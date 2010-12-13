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
public class ReviewParser extends DefaultHandler
{

    static Logger logger = Logger.getLogger(ReviewParser.class.getName());

    private SAXParser parser;
    private String atElement;
    private UserReview review;

    public ReviewParser(String xmlString)
    {

        // Parse the XML using a tmp file and a SAX parsing
        try
        {
            
            File tmpFile = File.createTempFile("cc-api", ".xml.tmp");
            PrintStream out = new PrintStream(new FileOutputStream(tmpFile));
            out.println(xmlString);
            out.close();

            parser = new SAXParser();
            parser.setContentHandler(this);

            logger.debug("starting parse");
            parser.parse(tmpFile.toURI().toString());
            logger.debug("ending parse");

        }
        catch(Exception e)
        {
            e.printStackTrace();;
            logger.debug("XML parse error - " + e.toString());
        }
        
    }

    @Override
    public void startElement(String uri, String local, String qName, Attributes attrs)
    {

        logger.debug("found element: " + local);

        if(local.equalsIgnoreCase("one-review"))
        {
            review = new UserReview();
        }
        else if(review != null && local.equalsIgnoreCase("user"))
            atElement = "user";
        else if(review != null && local.equalsIgnoreCase("id"))
            atElement = "id";
        else if(review != null && local.equalsIgnoreCase("score"))
            atElement = "score";
        else if(review != null && local.equalsIgnoreCase("summary"))
            atElement = "summary";
        else if(review != null && local.equalsIgnoreCase("review-date"))
            atElement = "review-date";

    }

    @Override
    public void characters(char[] text, int start, int length)
    {

        String content = new String(text, start, length);

        logger.trace("Content: [" + atElement + "] " + content);

        if(atElement != null && atElement.equalsIgnoreCase("user"))
            review.setUserName(content);
        else if(atElement != null && atElement.equalsIgnoreCase("id"))
            review.setAPIID(content);
        else if(atElement != null && atElement.equalsIgnoreCase("score"))
            review.setScore(Integer.parseInt(content));
        else if(atElement != null && atElement.equalsIgnoreCase("summary"))
            review.setSummary(content);
        else if(atElement != null && atElement.equalsIgnoreCase("review-date"))
            review.setReviewDate(content);

    }

    public UserReview getReview() { return review; }

}