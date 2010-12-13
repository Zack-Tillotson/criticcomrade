package com.rottenmeta.restapi.xml;

import com.rottenmeta.restapi.data.*;
import org.apache.ecs.xml.*;
import java.util.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author chloe
 */
public class XMLGenerator
{

    static Logger logger = Logger.getLogger(XMLGenerator.class.getName());

    private static String URL_BASE = "http://localhost:8080/cc-api/";

    public static void setURLBase(String url_base)
    {
        URL_BASE=url_base;
    }

    public static String generateMovieList(String type, MetadList<Movie> vm)
    {
        
        XMLDocument doc = (XMLDocument) new XMLDocument();

        XML ml = new XML("movie-list");
        doc.addElement(ml);
        ml.addAttribute("type", type);
        ml.addAttribute("count", vm.size());
        ml.addAttribute("offset", vm.getOffset());
        ml.addAttribute("order-by", vm.getOrderBy());
        ml.addAttribute("max-size", vm.getMaxSize());
        ml.addAttribute("url", XMLGenerator.URL_BASE + vm.getURL());

        for(Iterator i = vm.listIterator(); i.hasNext();)
        {
            XML mXml = generateMovieXML(type, (Movie)i.next());
            mXml.removeAttribute("type");
            ml.addElement(mXml);
        }
        
        return doc.toString();
    }

    public static String generateMovie(String type, Movie m){

        XMLDocument doc = (XMLDocument) new XMLDocument();

        XML mx = generateMovieXML(type, m);
        mx.addAttribute("type", type);
        
        doc.addElement(mx);

        return doc.toString();
    }

    private static XML generateMovieXML(String type, Movie m)
    {

        XML md = new XML("movie");
        md.addAttribute("url", XMLGenerator.URL_BASE + m.getURL());

        XML title = new XML("title");
        md.addElement(title);
        title.addElement(StringEscapeUtils.escapeXml(m.getTitle()));

        XML id = new XML("id");
        md.addElement(id);
        id.addElement(StringEscapeUtils.escapeXml(m.getMovieID()));

        XML release_date = new XML("release-date");
        md.addElement(release_date);
        release_date.addElement(StringEscapeUtils.escapeXml(m.getReleaseDate()));

        if(m.getRanking() != 0)
        {
            md.addElement(new XML("box-office-rank").addElement("" + m.getRanking()));
        }

        if(type.equals("summary") || type.equals("full"))
        {
            XML publisher = new XML("publisher");
            md.addElement(publisher);
            publisher.addElement(StringEscapeUtils.escapeXml(m.getStudio()));

            XML summary = new XML("summary");
            md.addElement(summary);
            summary.addElement(StringEscapeUtils.escapeXml(m.getSummary()));

            XML rating = new XML("rating");
            md.addElement(rating);
            rating.addElement(StringEscapeUtils.escapeXml(m.getRating()));

            XML ratingreason = new XML("rating-reason");
            md.addElement(ratingreason);
            ratingreason.addElement(StringEscapeUtils.escapeXml(m.getRatingReason()));

            XML genrelist = new XML("genre-list");
            md.addElement(genrelist);

            for(Iterator<String> i = m.vgenre.listIterator(); i.hasNext(); )
            {
                String g = i.next();
                XML genre = new XML("genre");
                genrelist.addElement(genre);
                genre.addElement(StringEscapeUtils.escapeXml(g));
            }

            XML directorlist = new XML("director-list");
            md.addElement(directorlist);

            String d = new String();
            for(Iterator<String> i = m.vdirector.listIterator(); i.hasNext(); )
            {
                d = i.next();
                XML director = new XML("director");
                directorlist.addElement(director);
                director.addElement(StringEscapeUtils.escapeXml(d));
            }
            XML writerlist = new XML("writer-list");
            md.addElement(writerlist);

            String w = new String();
            for(Iterator<String> i = m.vwriter.listIterator(); i.hasNext(); )
            {
                w = i.next();
                XML writer = new XML("writer");
                writerlist.addElement(writer);
                writer.addElement(StringEscapeUtils.escapeXml(w));
            }


            XML starlist = new XML("star-list");
            md.addElement(starlist);

            String s = new String();
            for(Iterator<String> i = m.vstar.listIterator(); i.hasNext(); )
            {
                s = i.next();
                XML star = new XML("star");
                starlist.addElement(star);
                star.addElement(StringEscapeUtils.escapeXml(s));
            }

        }

        XML reviewslist = new XML("reviews-list");
        md.addElement(reviewslist);

        reviewslist.addElement(generateCriticReviewListXML(type.equalsIgnoreCase("full")?"summary":"ditty", m.critic_reviews));
        reviewslist.addElement(generateUserReviewListXML(type.equalsIgnoreCase("full")?"summary":"ditty", m.user_reviews));

        return md;
        
    }

    private static XML generateCriticReviewListXML(String type, CriticReviewList crl)
    {

        XML criticreviews = new XML("critic-review-list");

        criticreviews.addAttribute("review-count", "" + crl.getTotalReviewCount());
        criticreviews.addAttribute("positive-review-count", "" + crl.getPositiveReviewCount());
        criticreviews.addAttribute("recent-review-count", "" + crl.getRecentReviewCount());
        criticreviews.addAttribute("count", "" + crl.size());


        if(crl.getUserAggregateScore() != -1) criticreviews.addAttribute("comrade-score", "" + crl.getUserAggregateScore());
        if(crl.getUserComradeCount() != -1) criticreviews.addAttribute("comrade-count", "" + crl.getUserComradeCount());
        if(crl.getUserComradeCount() > 0) criticreviews.addAttribute("comrade-suggestion", "" + crl.getUserComradeSuggestion());

        if(crl.getUserAgreeCount() != -1) criticreviews.addAttribute("matching-review-count", "" + crl.getUserAgreeCount());
        if(crl.getUserIntersectCount() != -1) criticreviews.addAttribute("review-intersect-count", "" + crl.getUserIntersectCount());
        
        if(type.equals("full") || type.equals("summary"))
            for(Iterator<CriticReview> i = crl.listIterator(); i.hasNext(); )
                criticreviews.addElement(generateCriticReviewXML("full", i.next()));

        return criticreviews;

    }

    private static XML generateUserReviewListXML(String type, UserReviewList url)
    {

        XML userreviews = new XML("user-review-list");

        userreviews.addAttribute("review-count", "" + url.getTotalReviewCount());
        userreviews.addAttribute("positive-review-count", "" + url.getPositiveReviewCount());
        userreviews.addAttribute("recent-review-count", "" + url.getRecentReviewCount());

        userreviews.addAttribute("count", "" + url.size());

        if(type.equalsIgnoreCase("full") || type.equalsIgnoreCase("summary"))
            for(Iterator<UserReview> i = url.listIterator(); i.hasNext(); )
                    userreviews.addElement(generateUserReviewXML("full", i.next()));

        return userreviews;
        
    }

    public static String generateCriticList(String type, MetadList<Critic> vc){

        XMLDocument doc = (XMLDocument) new XMLDocument();

        XML cl = new XML("critic-list");
        doc.addElement(cl);
        cl.addAttribute("type", type);
        cl.addAttribute("count", vc.size());
        cl.addAttribute("offset", vc.getOffset());
        cl.addAttribute("max-size", vc.getMaxSize());
        cl.addAttribute("order-by", vc.getOrderBy());
        cl.addAttribute("url", XMLGenerator.URL_BASE + vc.getURL());

        for(Iterator i = vc.listIterator(); i.hasNext();)
        {
            XML c = generateCriticXML(type, (Critic)i.next());
            c.removeAttribute("type");
            cl.addElement(c);
        }

        return doc.toString();
    }
    public static String generateCritic(String type, Critic c)
    {

        XMLDocument doc = (XMLDocument) new XMLDocument();

        XML cl = generateCriticXML(type, c);
        cl.addAttribute("type", type);

        doc.addElement(cl);

        return doc.toString();

    }
    
    private static XML generateCriticXML(String type, Critic c)
    {

        XML cd = new XML("critic");
        cd.addAttribute("url", XMLGenerator.URL_BASE + c.getURL());

        XML name = new XML("name");
        cd.addElement(name);
        name.addElement(StringEscapeUtils.escapeXml(c.getName()));

        XML criticid = new XML("id");
        cd.addElement(criticid);
        criticid.addElement(StringEscapeUtils.escapeXml(c.getAPIID()));

        XML publisher = new XML("publisher");
        cd.addElement(publisher);
        publisher.addElement(StringEscapeUtils.escapeXml(c.getPublisher()));

        if(c.getConfidence() != -1)
            cd.addElement((new XML("comradery").addElement("" + ((int)(100. * c.getConfidence()))/100.)));

        cd.addElement(generateCriticReviewListXML(type.equalsIgnoreCase("full")?"full":"summary", c.reviews));
        
        return cd;
        
    }

    public static String generateCriticReview(CriticReview c)
    {

        XMLDocument doc = (XMLDocument) new XMLDocument();

        doc.addElement(generateCriticReviewXML("full", c));

        return doc.toString();
    }

    private static XML generateCriticReviewXML(String type, CriticReview r)
    {

        XML rd = new XML("critic-review");
        rd.addAttribute("url", XMLGenerator.URL_BASE + r.getURL());

        if(r.getCriticID() != null && type.equalsIgnoreCase("full"))
        {

            XML id = new XML("id");
            XML name = new XML("name");
            XML pub = new XML("publisher");

            id.addElement(r.getCriticID());
            name.addElement(StringEscapeUtils.escapeXml(r.getName()));
            pub.addElement(StringEscapeUtils.escapeXml(r.getPublisher()));

            rd.addElement(id);
            rd.addElement(name);
            rd.addElement(pub);

            if(r.getConfidence() != -1)
                rd.addElement((new XML("comradery")).addElement("" + ((int)(100. * r.getConfidence()))/100.));

        }
        
        if(r.getAPIID() != null)
        {

            XML midxml = new XML("movie-id");
            midxml.addElement(r.getAPIID());
            rd.addElement(midxml);

            XML title = new XML("title");
            title.addElement(StringEscapeUtils.escapeXml(r.getTitle()));
            rd.addElement(title);

        }

        XML score = new XML("score");
        score.addElement("" + r.getScore());
        rd.addElement(score);

        XML summary = new XML("summary");
        summary.addElement(StringEscapeUtils.escapeXml(r.getSummary()));
        rd.addElement(summary);

        XML outsidelink = new XML("link");
        outsidelink.addElement(StringEscapeUtils.escapeXml(r.getOutsideLink()));
        rd.addElement(outsidelink);

        XML reviewdate = new XML("review-date");
        reviewdate.addElement(r.getReviewDate());
        rd.addElement(reviewdate);

        return rd;    
    }

    public static String generateUserList(String type, MetadList<User> vu)
    {
        XMLDocument doc = new XMLDocument();

        XML ul = new XML("user-list");
        doc.addElement(ul);
        ul.addAttribute("type", type);
        ul.addAttribute("count", vu.size());
        ul.addAttribute("offset", vu.getOffset());
        ul.addAttribute("max-size", vu.getMaxSize());
        ul.addAttribute("order-by", vu.getOrderBy());
        ul.addAttribute("url", XMLGenerator.URL_BASE + vu.getURL());

        for(Iterator i = vu.listIterator(); i.hasNext();)
        {
            ul.addElement(generateUserXML(type, (User)i.next()));
        }

        return doc.toString();
    }

    public static String generateUser(String type, User u)
    {
        XMLDocument doc = (XMLDocument) new XMLDocument();

        XML ux = generateUserXML(type, u);
        ux.addAttribute("type", type);

        doc.addElement(ux);

        return doc.toString();
    }

    private static XML generateUserXML(String type, User u)
    {

        XML user = new XML("user");
        user.addAttribute("url", XMLGenerator.URL_BASE + u.getURL());

        XML username = new XML("username");
        user.addElement(username);
        username.addElement(StringEscapeUtils.escapeXml(u.getUserName()));

        XML createdate = new XML("create-date");
        user.addElement(createdate);
        createdate.addElement(StringEscapeUtils.escapeXml(u.getCreateDate()));

        user.addElement(generateUserReviewListXML(type.equalsIgnoreCase("full")?"full":"summary", u.reviews));

        return user;

    }

    public static String generateUserReview(UserReview u)
    {

        XMLDocument doc = (XMLDocument) new XMLDocument();

        doc.addElement(generateUserReviewXML("full", u));

        return doc.toString();

    }

    private static XML generateUserReviewXML(String type, UserReview r)
    {

        XML review = new XML("user-review");
        review.addAttribute("url", XMLGenerator.URL_BASE + r.getURL());

        if(r.getUserName() != null && type.equalsIgnoreCase("full"))
        {
            XML id = new XML("user-name");
            id.addElement(r.getUserName());
            review.addElement(id);
        }

        if(r.getAPIID() != null)
        {
            XML midxml = new XML("movie-id");
            midxml.addElement(r.getAPIID());
            review.addElement(midxml);

            XML title = new XML("title");
            title.addElement(StringEscapeUtils.escapeXml(r.getTitle()));
            review.addElement(title);
        }

        XML score = new XML("score");
        score.addElement("" + r.getScore());
        review.addElement(score);

        XML summary = new XML("summary");
        summary.addElement(StringEscapeUtils.escapeXml(r.getSummary()));
        review.addElement(summary);

        XML reviewdate = new XML("review-date");
        reviewdate.addElement(r.getReviewDate());
        review.addElement(reviewdate);

        return review;

    }

    private static XML generatePersonalUserXML(User u, String type)
    {

        XML user = new XML("user");

        user.addAttribute("url", XMLGenerator.URL_BASE + u.getURL());
        user.addAttribute("type", type);
        user.addAttribute("long-session", u.getIsLongSession() ? "true" : "false");
        
        user.addElement(new XML("username").addElement(u.getUserName()));
        user.addElement(new XML("create-date").addElement(u.getCreateDate()));
        user.addElement(new XML("first-name").addElement(u.getFirstName()));
        user.addElement(new XML("last-name").addElement(u.getLastName()));
        user.addElement(new XML("email").addElement(u.getEmail()));
        user.addElement(new XML("age").addElement(u.getAge()));
        user.addElement(new XML("sex").addElement(u.getSex()));

        user.addElement(generateUserReviewListXML(type, u.reviews));

        return user;

    }

    public static String generateSession(User u, String type)
    {

        XMLDocument doc = new XMLDocument();

        if(u != null)
            doc.addElement(generatePersonalUserXML(u, type));
        else
        {
            XML tempUser = (new XML("user"));
            tempUser.addAttribute("temporary", "true");
            doc.addElement(tempUser);
        }

        return doc.toString();
        
    }

    public static String generateLogin(String message)
    {
        return (new XMLDocument().addElement(new XML("session-status").addElement(new XML("login").addElement("true")))).toString();
    }

    public static String generateLogoff(String message)
    {
        return (new XMLDocument().addElement(new XML("session-status").addElement(new XML("logoff").addElement("true")))).toString();
    }

    public static String generateSearchResults(SearchResultList list)
    {

        XMLDocument doc = (XMLDocument) new XMLDocument();

        XML listXML = new XML("search-result-list");
        listXML.addAttribute("search-term", list.getSearchTerm());
        listXML.addAttribute("count", list.size());
        listXML.addAttribute("offset", list.getOffset());
        listXML.addAttribute("max-size", list.getMaxSize());
        listXML.addAttribute("url", list.getURL());
        listXML.addAttribute("order-by", list.getOrderBy());
        doc.addElement(listXML);

        for(SearchResult result : list)
            listXML.addElement(generateSearchResultXML(result));

        return doc.toString();
        
    }

    private static XML generateSearchResultXML(SearchResult result)
    {

        XML resultXML = new XML("search-result");

        resultXML.addAttribute("type", result.getResultType());

        if(result.getResultType().equals("Movie")) resultXML.addElement(generateMovieXML("ditty", result.getResultMovie()));
        if(result.getResultType().equals("Critic")) resultXML.addElement(generateCriticXML("ditty", result.getResultCritic()));

        return resultXML;
        
    }

}