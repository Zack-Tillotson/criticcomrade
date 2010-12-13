package com.rottenmeta.restapi;

import com.rottenmeta.restapi.xml.XMLGenerator;
import com.rottenmeta.restapi.dao.*;
import com.rottenmeta.restapi.data.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author chloburr
 */
public class RMUtil
{

    static Logger logger = Logger.getLogger(RMUtil.class.getName());

    public static void setURLBase(String url_base)
    {
        XMLGenerator.setURLBase(url_base);
    }

    public static String getMovies() throws RMInvalidIDException, RMDBException { return getMovie(null); }
    public static String getMovies(HashMap<String, String> params) throws RMInvalidIDException, RMDBException
    {

        logger.debug("starting to get movie list");

        if(params == null) params = new HashMap<String, String>();
        if(!params.containsKey("type")) params.put("type", "ditty");
        if(!params.containsKey("representation")) params.put("representation", "xml");
        if(params.containsKey("sid"))
        {
            SessionDAO sDAO = new SessionDAO();
            if(sDAO.getSessionState(params.get("sid")) == SessionDAO.INVALID_STATE) throw new RMInvalidIDException(params.get("sid"), "Session");
            User user = sDAO.getUserInfoFromSID(params.get("sid"));
            if(user == null) params.remove("sid");
            else params.put("user-id", "" + user.getUserID());
        }
        if(params.containsKey("filter") && params.get("filter").equalsIgnoreCase("not-reviewed"))
        {
            if(!params.containsKey("user-id")) params.remove("filter");
            if(!params.containsKey("order-by")) params.put("order-by", "total-critic-reviews");
        }

        MovieDAO mDAO = new MovieDAO();
        MetadList<Movie> mList = mDAO.getMovieList(params);

        String returnString = "";

        if(params.get("representation").equalsIgnoreCase("xml"))
            returnString = XMLGenerator.generateMovieList(params.get("type"), mList);

        return returnString;

    }
    
    public static String getMovie(String apiId) throws RMInvalidIDException, RMDBException { return getMovie(apiId, null); }
    public static String getMovie(String apiId, HashMap<String, String> params) throws RMInvalidIDException, RMDBException
    {

        logger.debug("starting to get a movie");

        if(params == null) params = new HashMap<String, String>();
        if(!params.containsKey("type")) params.put("type", "full");
        if(!params.containsKey("representation")) params.put("representation", "xml");
        if(params.containsKey("sid"))
        {
            SessionDAO sDAO = new SessionDAO();            
            User user = sDAO.getUserInfoFromSID(params.get("sid"));
            if(user == null) params.remove("sid");
            else params.put("user-id", "" + user.getUserID());
        }

        params.put("filter", "one");
        params.put("mid", apiId);
        
        MovieDAO mDAO = new MovieDAO();
        
        Movie m;

        try
        {
            m = mDAO.getMovieList(params).firstElement();
        }
        catch(NoSuchElementException e)
        {
            throw new RMInvalidIDException(apiId, "Movie");
        }
        logger.debug("got movie via dao: " + m.toString());

        String returnString = "";

        if(params.get("representation").equalsIgnoreCase("xml"))
            returnString = XMLGenerator.generateMovie(params.get("type"), m);

        return returnString;
        
    }

    public static String getCritics() throws RMInvalidIDException, RMDBException { return getCritics(null); }
    public static String getCritics(HashMap<String, String> params) throws RMInvalidIDException, RMDBException
    {

        logger.debug("starting to get critic list");

        if(params == null) params = new HashMap<String, String>();
        if(!params.containsKey("type")) params.put("type", "ditty");
        if(!params.containsKey("representation")) params.put("representation", "xml");
        if(params.containsKey("sid"))
        {
            SessionDAO sDAO = new SessionDAO();            
            User user = sDAO.getUserInfoFromSID(params.get("sid"));
            if(user == null) params.remove("sid");
            else params.put("user-id", "" + user.getUserID());
        }

        logger.trace("params: " + params.toString());

        CriticDAO dao = new CriticDAO();
        MetadList<Critic> cList = dao.getCriticList(params);

        String returnString = "";

        if(params.get("representation").equalsIgnoreCase("xml"))
            returnString = XMLGenerator.generateCriticList(params.get("type"), cList);

        return returnString;

    }

    public static String getCritic(String apiId) throws RMInvalidIDException, RMDBException { return getCritic(apiId, null); }
    public static String getCritic(String apiId, HashMap<String, String> params) throws RMInvalidIDException, RMDBException
    {

        logger.debug("starting to get a critic");

        if(params == null) params = new HashMap<String, String>();
        if(!params.containsKey("type")) params.put("type", "full");
        if(!params.containsKey("representation")) params.put("representation", "xml");
        if(params.containsKey("sid"))
        {
            SessionDAO sDAO = new SessionDAO();            
            User user = sDAO.getUserInfoFromSID(params.get("sid"));
            if(user == null) params.remove("sid");
            else params.put("user-id", "" + user.getUserID());
        }

        params.put("filter", "one");
        params.put("cid", apiId);

        CriticDAO dao = new CriticDAO();

        Critic c;

        try
        {
            c = dao.getCriticList(params).firstElement();
        }
        catch(NoSuchElementException e)
        {
            throw new RMInvalidIDException(apiId, "Critic");
        }

        String returnString = "";

        if(params.get("representation").equalsIgnoreCase("xml"))
            returnString = XMLGenerator.generateCritic(params.get("type"), c);

        return returnString;

    }

    public static String getCriticReview(String criticApiId, String movieApiId) throws RMInvalidIDException, RMDBException { return getCriticReview(criticApiId, movieApiId, null); }
    public static String getCriticReview(String criticApiId, String movieApiId, HashMap<String, String> params) throws RMInvalidIDException, RMDBException
    {

        logger.debug("starting to get a critic review");

        if(params == null) params = new HashMap<String, String>();
        if(!params.containsKey("representation")) params.put("representation", "xml");
        if(params.containsKey("sid"))
        {
            SessionDAO sDAO = new SessionDAO();            
            User user = sDAO.getUserInfoFromSID(params.get("sid"));
            if(user == null) params.remove("sid");
            else params.put("user-id", "" + user.getUserID());
        }

        params.put("type", "full");
        params.put("filter", "one");
        params.put("cid", criticApiId);

        CriticDAO dao = new CriticDAO();

        Critic c;
        
        try
        {
            c = dao.getCriticList(params).firstElement();
        }
        catch(NoSuchElementException e)
        {
            throw new RMInvalidIDException(criticApiId, "Critic");
        }

        CriticReview ret = null;
        for(Iterator<CriticReview> i = c.reviews.listIterator(); i.hasNext(); )
        {
            CriticReview r = i.next();
            if(r.getAPIID().equalsIgnoreCase(movieApiId))
                ret = r;
        }

        if(ret == null) throw new RMInvalidIDException(movieApiId, "Review");

        ret.setConfidence(c.getConfidence());
        logger.debug("set conf: " + ret.getConfidence());

        String returnString = "";

        if(params.get("representation").equalsIgnoreCase("xml"))
            returnString = XMLGenerator.generateCriticReview(ret);

        return returnString;
        
    }


    public static String getUsers() throws RMInvalidIDException, RMDBException { return getUsers(null); }
    public static String getUsers(HashMap<String, String> params) throws RMInvalidIDException, RMDBException
    {

        logger.debug("starting to get user list");

        if(params == null) params = new HashMap<String, String>();
        if(!params.containsKey("type")) params.put("type", "ditty");
        if(!params.containsKey("representation")) params.put("representation", "xml");
        if(params.containsKey("sid"))
        {
            SessionDAO sDAO = new SessionDAO();            
            User user = sDAO.getUserInfoFromSID(params.get("sid"));
            if(user == null) params.remove("sid");
            else params.put("user-id", "" + user.getUserID());
        }

        UserDAO dao = new UserDAO();
        MetadList<User> uList = dao.getUserList(params);

        String returnString = "";

        if(params.get("representation").equalsIgnoreCase("xml"))
            returnString = XMLGenerator.generateUserList(params.get("type"), uList);

        return returnString;
        
    }

    public static String getUser(String apiId) throws RMInvalidIDException, RMDBException { return getUser(apiId, null); }
    public static String getUser(String apiId, HashMap<String, String> params) throws RMInvalidIDException, RMDBException
    {

        logger.debug("starting to get a user");

        UserDAO dao = new UserDAO();

        if(params == null) params = new HashMap<String, String>();
        if(!params.containsKey("type")) params.put("type", "full");
        if(!params.containsKey("representation")) params.put("representation", "xml");

        params.put("filter", "one");
        params.put("user_id", "" + dao.getUserIDFromUserName(apiId));

        User c;

        try
        {
            c = dao.getUserList(params).firstElement();
        }
        catch(NoSuchElementException e)
        {
            throw new RMInvalidIDException(apiId, "User");
        }

        String returnString = "";

        if(params.get("representation").equalsIgnoreCase("xml"))
            returnString = XMLGenerator.generateUser(params.get("type"), c);

        return returnString;
        
    }

    public static String getUserReview(String userApiId, String movieApiId) throws RMInvalidIDException, RMDBException { return getUserReview(userApiId, movieApiId, null); }
    public static String getUserReview(String userApiId, String movieApiId, HashMap<String, String> params) throws RMInvalidIDException, RMDBException
    {

        logger.debug("starting to get a user review");

        if(params == null) params = new HashMap<String, String>();
        if(!params.containsKey("representation")) params.put("representation", "xml");

        UserDAO dao = new UserDAO();
        
        params.put("type", "full");
        params.put("filter", "one");
        params.put("user_id", "" + dao.getUserIDFromUserName(userApiId));

        User c;

        try
        {
            c = dao.getUserList(params).firstElement();
        }
        catch(NoSuchElementException e)
        {
            throw new RMInvalidIDException(userApiId, "User");
        }

        UserReview ret = null;
        for(Iterator<UserReview> i = c.reviews.listIterator(); i.hasNext(); )
        {
            UserReview r = i.next();
            if(r.getAPIID().equalsIgnoreCase(movieApiId))
                ret = r;
        }

        if(ret == null) throw new RMInvalidIDException(movieApiId, "Review");

        String returnString = "";

        if(params.get("representation").equalsIgnoreCase("xml"))
            returnString = XMLGenerator.generateUserReview(ret);

        return returnString;
        
    }
    
    public static String postMovie(String id) 
    {
        // For the future
        return "";
    }

    public static String postCritic(String id)
    {
        // For the future
        return null;
    }

    public static String postCriticReview(String cid, String mid)
    {
        // For the future
        return null;
    }

    public static String postUser(User u, HashMap<String, String> params) throws RMDBException, RMInvalidIDException
    {

        logger.debug("postUser(User u, HashMap params) called");
        logger.debug("u: " + u);
        logger.debug("params: " + params);

        if(params == null) params = new HashMap<String, String>();
        if(!params.containsKey("representation")) params.put("representation", "xml");

        // SID is required
        if(!params.containsKey("sid")) throw new RMInvalidIDException("", "Session");

        UserDAO uDAO = new UserDAO();
        SessionDAO sDAO = new SessionDAO();

        int nameUID = -1;
        Boolean userExists = true;
        try
        {
            nameUID = uDAO.getUserIDFromUserName(u.getUserName());
        }
        catch(RMInvalidIDException e)
        {
            userExists = false;
        }

        // If the user already exists
        if(userExists)
        {

            logger.trace("User exists, updating");

            // Ensure authorized
            User sidU = sDAO.getUserInfoFromSID(params.get("sid"));
            int sessUserID = sidU == null ? -1 : sidU.getUserID();

            if(nameUID == sessUserID)
                uDAO.updateUser(sessUserID, u);                     // Update the user
            else
                throw new RMInvalidIDException(params.get("sid"), "Session");

        }
        else
        {

            logger.trace("User is new, creating");

            // If the sid already has a user
            User sidU = sDAO.getUserInfoFromSID(params.get("sid"));

            // Guest = make trial and update to real
            // Trial = update to real
            // Real  = if they are the same, update
            if(sidU == null || sidU.getUserID() <= 0)
            {

                logger.trace("in user is a new user, session's user is guest");
                sDAO.logInTrialUser(params.get("sid"));
                uDAO.updateUser(sDAO.getUserInfoFromSID(params.get("sid")).getUserID(), u);

            }
            else if(sidU.getUserName() == null || sidU.getUserName().equals(""))
            {
                logger.trace("in user is a new user, session is trial");
                uDAO.updateUser(sDAO.getUserInfoFromSID(params.get("sid")).getUserID(), u);
            }
            else
            {
                logger.trace("in user is a new user, session is real");
                uDAO.updateUser(sDAO.getUserInfoFromSID(params.get("sid")).getUserID(), u);
            }

            
            

        }

        return "<?xml version='1.0'?><post-success>true</post-success>";
        
    }
    
    public static String postUserReview(UserReview u, HashMap<String, String> params) throws RMDBException, RMInvalidIDException
    {

         Vector<UserReview> v = new Vector<UserReview>();
         v.add(u);

        return postUserReviews(v, params);

    }

    public static String postUserReviews(Vector<UserReview> revs, HashMap<String, String> params) throws RMDBException, RMInvalidIDException
    {

        logger.debug("starting to post user reviews (pl)");
        logger.debug("  params: " + params);
        logger.debug("  Review Count: " + revs.size());

        if(params == null) params = new HashMap<String, String>();
        if(!params.containsKey("representation")) params.put("representation", "xml");

        if(!params.containsKey("sid")) throw new RMInvalidIDException("", "Session");
        String sid = params.get("sid");

        SessionDAO sDAO = new SessionDAO();

        if(sDAO.getSessionState(params.get("sid")) == SessionDAO.INVALID_STATE)
        {
            logger.debug("did not pass authentic test");
            throw new RMInvalidIDException(sid, "Session");
        }

        User ui = sDAO.getUserInfoFromSID(sid);
        if(ui == null)
        {
            logger.debug("did not pass authentic test");
            throw new RMInvalidIDException(sid, "Session");
        }

        UserDAO uDAO = new UserDAO();

        for(UserReview rev : revs)
        {

            logger.debug("adding review - " + rev);
            uDAO.addUserReview(ui.getUserID(), rev);

        }

        // Refresh the comradery scores for this user
        uDAO.refreshComradery(ui.getUserID());

        logger.debug("successfully added reviews (pl) to db");

        return "<?xml version='1.0'?><post-success>true</post-success>";

    }

    public static String getSession(String sid, HashMap<String, String> params) throws RMDBException, RMInvalidIDException
    {

        logger.debug("getSession(sid, params)");
        logger.debug("sid: " + sid);
        logger.debug("params: " + params);

        if(params == null) params = new HashMap<String, String>();
        if(!params.containsKey("representation")) params.put("representation", "xml");
        if(!params.containsKey("type")) params.put("type", "summary");

        SessionDAO sDAO = new SessionDAO();

        if(sDAO.getSessionState(sid) == SessionDAO.INVALID_STATE) throw new RMInvalidIDException(sid, "Session");

        // Get the basic user information
        User u = sDAO.getUserInfoFromSID(sid);
        sDAO.refreshSession(sid);

        if(u != null)
        {

            // Get the user reviews
            UserDAO uDAO = new UserDAO();

            HashMap<String, String> upars = new HashMap<String, String>();
            upars.put("filter", "one");
            upars.put("user_id", "" + u.getUserID());
            upars.put("type", params.get("type"));

            User ur = uDAO.getUserList(upars).firstElement();

            // Add the user reviews to the user information
            u.reviews.setTotalReviewCount(ur.reviews.getTotalReviewCount());
            u.reviews.setRecentReviewCount(ur.reviews.getRecentReviewCount());
            u.reviews.setPositiveReviewCount(ur.reviews.getPositiveReviewCount());
            for(UserReview r : ur.reviews)
                u.reviews.add(r);

        }

        String returnString = "";

        if(params.get("representation").equalsIgnoreCase("xml"))
            returnString = XMLGenerator.generateSession(u, params.get("type"));

        return returnString;
        
    }

    public static String getSessionReview(String sid, String mid) throws RMInvalidIDException, RMDBException { return getSessionReview(sid, null); }
    public static String getSessionReview(String sid, String mid, HashMap<String, String> params) throws RMInvalidIDException, RMDBException
    {

        logger.debug("getSessionReview(sid, params)");
        logger.debug("sid: " + sid);
        logger.debug("mid: " + mid);
        logger.debug("params: " + params);

        if(params == null) params = new HashMap<String, String>();
        if(!params.containsKey("representation")) params.put("representation", "xml");

        SessionDAO sDAO = new SessionDAO();

        if(sDAO.getSessionState(sid) == SessionDAO.INVALID_STATE) throw new RMInvalidIDException(sid, "Session");

        // Refresh the session
        sDAO.refreshSession(sid);

        // Get the basic user information
        User u = sDAO.getUserInfoFromSID(sid);

        // If the session does not have a user, just return nothing
        if(u == null)
        {
            throw new RMInvalidIDException(mid, "Movie");
        }
        

        // Get the user reviews
        UserDAO uDAO = new UserDAO();

        HashMap<String, String> upars = new HashMap<String, String>();
        upars.put("filter", "one");
        upars.put("user_id", "" + u.getUserID());
        upars.put("type", "full");

        User ur = uDAO.getUserList(upars).firstElement();

        UserReview ret = null;
        for(Iterator<UserReview> i = ur.reviews.listIterator(); i.hasNext(); )
        {
            UserReview r = i.next();
            if(r.getAPIID().equalsIgnoreCase(mid))
                ret = r;
        }

        if(ret == null) throw new RMInvalidIDException(mid, "Review");

        String returnString = "";

        if(params.get("representation").equalsIgnoreCase("xml"))
            returnString = XMLGenerator.generateUserReview(ret);

        return returnString;
        
    }

    //                          --post on real session
    //                          --      with un/pw:     log the user out, attempt to login user for input un/pw and assoc with sid, incorrect un/pw means session is no longer valid
    //                          --      without un/pw:  log the user out, sid is no longer valid
    //                          --post on trial session
    //                          --      with un/pw:     attempt to login user and assoc with sid (merge trial user with real user), incorrect un/pw will not do anything
    //                          --      without un/pw:  log the user out, sid is no longer valid
    //                          --post on guest/invalid session
    //                          --      with un/pw:     attempt to login user and assoc with sid, incorrect un/pw will initialize session as guest
    //                          --      without un/pw:  initialize session as guest
    public static String postSession(String sid, Session s, HashMap<String, String> params) throws RMDBException, RMInvalidIDException, RMInvalidCredentialsException
    {

        logger.debug("postSession(sid, params)");
        logger.debug("sid: " + sid);
        logger.debug("params: " + params);

        SessionDAO sDAO = new SessionDAO();

        // Get current state
        int state = sDAO.getSessionState(sid);

        boolean loggedIn = false;

        // If un/pw supplied
        if(!s.getUser().equals("") && !s.getPassword().equals(""))
        {

            // Try to log user in, if it fails then
                // If real, session is not valid
                // If trial, no change
                // If guest, destroy session
            if(!sDAO.logInUser(sid, s.getUser(), s.getPassword(), s.getIsLongSession()))
            {

                if(state == SessionDAO.REAL_STATE)
                    sDAO.logOutUser(sid);
                else if(state == SessionDAO.TRIAL_STATE)
                    {}
                else if(state == SessionDAO.GUEST_STATE)
                    sDAO.logOutUser(sid);
                
                throw new RMInvalidCredentialsException();
                
            }

            loggedIn = true;

        }
        else
        {
        
            if(state == SessionDAO.GUEST_STATE || state == SessionDAO.INVALID_STATE)
            {
                logger.trace("guest or invalid state + no un/pw, starting guest session");
                if(sDAO.startGuestSession(sid))
                    loggedIn = true;
                else
                    loggedIn = false;
                logger.trace("are we logged in? " + (loggedIn ? "yes" : "no"));
            }
            else
            {
                sDAO.logOutUser(sid);
                loggedIn = false;
            }

        }

        // If the have supplied a username and password that is valid, log them in
        String returnString = "";
        
        // For all other situations, log this session out
        if(loggedIn)
        {
            if(params.get("representation").equalsIgnoreCase("xml"))
                returnString = XMLGenerator.generateLogin(sid);
        }
        else
        {
           if(params.get("representation").equalsIgnoreCase("xml"))
                returnString = XMLGenerator.generateLogoff(sid);

        }

        return returnString;
        
    }

    public static String postSessionReview(String sid, String mid, UserReview u, HashMap<String, String> params) throws RMDBException, RMInvalidIDException
    {

        u.setAPIID(mid);

         Vector<UserReview> v = new Vector<UserReview>();
         v.add(u);

        return postSessionReviews(sid, v, params);

    }

    public static String postSessionReviews(String sid, Vector<UserReview> revs, HashMap<String, String> params) throws RMDBException, RMInvalidIDException
    {

        logger.debug("starting to post session reviews");
        logger.debug("  session: " + sid);
        logger.debug("  params: " + params);
        logger.debug("  Review Count: " + revs.size());

        if(params == null) params = new HashMap<String, String>();
        if(!params.containsKey("representation")) params.put("representation", "xml");

        SessionDAO sDAO = new SessionDAO();

        if(sDAO.getSessionState(sid) == SessionDAO.INVALID_STATE || sDAO.getSessionState(sid) == SessionDAO.GUEST_STATE)
        {
            logger.debug("non-user session, upgrading it to trial state");
            if(!sDAO.logInTrialUser(sid)) throw new RMDBException("Error logging in session as trial");
        }

        User ui = sDAO.getUserInfoFromSID(sid);
        if(ui == null)
        {
            logger.debug("did not pass authentic test");
            throw new RMInvalidIDException(sid, "Session");
        }

        UserDAO uDAO = new UserDAO();

        for(UserReview rev : revs)
        {

            logger.debug("adding review - " + rev);
            uDAO.addUserReview(ui.getUserID(), rev);

        }

        // Refresh the comradery scores for this user
        uDAO.refreshComradery(ui.getUserID());

        logger.debug("successfully added reviews to db");

        return "<?xml version='1.0'?><post-success>true</post-success>";

    }

    public static String getSearchResults(String searchTerm) throws RMDBException, RMInvalidIDException { return getSearchResults(searchTerm, null); }
    public static String getSearchResults(String searchTerm, HashMap<String, String> params) throws RMDBException, RMInvalidIDException
    {

        if(params == null) params = new HashMap<String, String>();
        if(!params.containsKey("count")) params.put("count", "20");
        if(!params.containsKey("search-movie-titles") &&
           !params.containsKey("search-critic-names") &&
           !params.containsKey("search-critic-pubs"))
        {
            params.put("search-movie-titles", "true");
            params.put("search-critic-names", "true");
            params.put("search-critic-pubs", "true");
        }        
        if(params.containsKey("sid"))
        {
            SessionDAO sDAO = new SessionDAO();
            if(sDAO.getSessionState(params.get("sid")) == SessionDAO.INVALID_STATE) throw new RMInvalidIDException(params.get("sid"), "Session");
            User user = sDAO.getUserInfoFromSID(params.get("sid"));
            if(user == null) params.remove("sid");
            else params.put("user-id", "" + user.getUserID());
        }
        if(!params.containsKey("order-by")) params.put("order-by", "shortest-term");
        if(!params.containsKey("offset")) params.put("offset", "0");

        searchTerm = searchTerm.toLowerCase();
        if(!searchTerm.contains("*")) searchTerm = "*" + searchTerm + "*";
        searchTerm = searchTerm.replaceAll("\\*", "%");
        params.put("search-term", searchTerm);

        SearchResultList results = new SearchResultList(searchTerm);
        results.setOffset(Integer.parseInt(params.get("offset")));

        if(params.containsKey("search-movie-titles") && params.get("search-movie-titles").equalsIgnoreCase("true"))
        {
            
            MovieDAO mDAO = new MovieDAO();
            params.put("filter", "search-movie-titles");
            results.addMovies(mDAO.getMovieList(params), "title");
            
        }

        CriticDAO cDAO = null;

        if(params.containsKey("search-critic-names") && params.get("search-critic-names").equalsIgnoreCase("true"))           
        {
            cDAO = new CriticDAO();
            params.put("filter", "search-critic-names");
            results.addCritics(cDAO.getCriticList(params), "name");
        }
        
        if(params.containsKey("search-critic-publishers") && params.get("search-critic-publishers").equalsIgnoreCase("true"))
        {
            if(cDAO == null) cDAO = new CriticDAO();
            params.put("filter", "search-critic-publishers");
            results.addCritics(cDAO.getCriticList(params), "publisher");
        }

        // Remove any duplicates
        for(int a = 0 ; a < results.size() - 1 ; a++)
            for(int b = a + 1 ; b < results.size() ; b++)
                if(results.get(a).getResultType().equals(results.get(b).getResultType()))
                {
                    boolean theSame = false;
                    if(results.get(a).getResultType().equals("Movie") && results.get(a).getResultMovie().getMovieID().equals(results.get(b).getResultMovie().getMovieID()))
                        theSame = true;
                    else if(results.get(a).getResultType().equals("Critic") && results.get(a).getResultCritic().getAPIID().equals(results.get(b).getResultCritic().getAPIID()))
                        theSame = true;
                    if(theSame)
                    {
                        results.remove(b);
                        b--;
                    }
                        
                }
        
        // Shortest term ordering is a special case of ordering
        if(params.containsKey("order-by") && params.get("order-by").equals("shortest-term"))
        {
            
            results.setOrderBy(params.get("order-by"));
            Collections.sort(results, new SearchResultComparator());

        }

        if(params.containsKey("count") &&  results.size() > Integer.parseInt(params.get("count")))
        {
            
            int offset = 0;
            int count = Integer.parseInt(params.get("count"));
            if(params.containsKey("offset")) offset = Integer.parseInt(params.get("offset"));

            if(offset > results.size()) offset = results.size();
            if(offset + count > results.size()) count = results.size() - offset;

            logger.trace("Getting search results sublist [" + offset + " - " + (count + offset) + "]");
            
            results.subList(0, offset).clear();
            results.subList(count, results.size()).clear();

        }

        String returnString = "";
        
        if(params.get("representation").equalsIgnoreCase("xml"))
                returnString = XMLGenerator.generateSearchResults(results);

        return returnString;
        
    }
}
