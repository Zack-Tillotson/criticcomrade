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
public class UserParser extends DefaultHandler
{

    static Logger logger = Logger.getLogger(UserParser.class.getName());

    private SAXParser parser;
    private String atElement;
    private User user;

    public UserParser(String xmlString)
    {

        // Parse the XML using a tmp file and a SAX parsing
        try
        {

            logger.debug("in UserParser - xml=" + xmlString);
            
            File tmpFile = File.createTempFile("cc-api", ".xml.tmp");
            PrintStream out = new PrintStream(new FileOutputStream(tmpFile));
            out.println(xmlString);
            out.close();

            parser = new SAXParser();
            parser.setContentHandler(this);

            logger.debug("starting parse user");
            parser.parse(tmpFile.toURI().toString());
            logger.debug("ending parse");

        }
        catch(Exception e)
        {
            e.printStackTrace();
            logger.debug("XML parse error - " + e.toString() + ", atElemnt = " + atElement);
        }
        
    }

    @Override
    public void startElement(String uri, String local, String qName, Attributes attrs)
    {

        logger.debug("Element: " + local);

        if(user == null && local.equalsIgnoreCase("user"))
        {
            user = new User();
        }
        else if(user != null && local.equalsIgnoreCase("review"))
        {
            user.reviews.add(new UserReview());
        }
        else
            atElement = local;

    }

    @Override
    public void characters(char[] text, int start, int length)
    {

        String content = new String(text, start, length);

        logger.debug("Data: [atElement] " + atElement + " [content] " + content);

        if(atElement == null) return;

        if(atElement.equalsIgnoreCase("username"))
            user.setUserName(content);
        else if(atElement.equalsIgnoreCase("password"))
            user.setPassword(content);
        else if(atElement.equalsIgnoreCase("email"))
            user.setEmail(content);
        else if(atElement.equalsIgnoreCase("age"))
            user.setAge(content);
        else if(atElement.equalsIgnoreCase("sex"))
            user.setSex(content);
        else if(atElement.equalsIgnoreCase("first-name"))
            user.setFirstName(content);
        else if(atElement.equalsIgnoreCase("last-name"))
            user.setLastName(content);
        else if(atElement.equalsIgnoreCase("join-date"))
            user.setCreateDate(content);
        else if(atElement.equalsIgnoreCase("number-reviews"))
            user.reviews.setTotalReviewCount(Integer.parseInt(content));
        else if(atElement.equalsIgnoreCase("number-positive-reviews"))
            user.reviews.setPositiveReviewCount(Integer.parseInt(content));
        else if(atElement.equalsIgnoreCase("id"))
            user.reviews.lastElement().setAPIID(content);
        else if(atElement.equalsIgnoreCase("score"))
            user.reviews.lastElement().setScore(Integer.parseInt(content));
        else if(atElement.equalsIgnoreCase("summary"))
            user.reviews.lastElement().setSummary(content);
        else if(atElement.equalsIgnoreCase("review-date"))
            user.reviews.lastElement().setReviewDate(content);

    }

    public User getUser() 
    {

        // Clear out the empty reviews before we give back the user
        this.user.reviews.clearBadReviews();
        return user;
    }


}