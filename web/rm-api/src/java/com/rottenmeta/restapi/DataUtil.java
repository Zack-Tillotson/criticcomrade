package com.rottenmeta.restapi;

import com.rottenmeta.restapi.data.*;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringEscapeUtils;
import com.rottenmeta.restapi.xml.*;

/**
 * @author Zill
 */
public class DataUtil
{

    static Logger logger = Logger.getLogger(DataUtil.class.getName());

    // All user contins is the username and one review
    public static UserReview parseReviewFromRequest(HttpServletRequest req)
    {

        String reviewXML = req.getParameter("review");
        ReviewParser rp = new ReviewParser(StringEscapeUtils.unescapeHtml(reviewXML));
        return rp.getReview();
        
    }

    public static User parseUserFromRequest(HttpServletRequest req)
    {

        String reviewXML = req.getParameter("user");
        UserParser rp = new UserParser(StringEscapeUtils.unescapeHtml(reviewXML));
        return rp.getUser();
        
    }

    public static Session parseSessionFromRequest(HttpServletRequest req)
    {
        String sessionXML = req.getParameter("session");
        SessionParser s = new SessionParser(StringEscapeUtils.unescapeHtml(sessionXML));
        return s.getSession();
    }
 
}