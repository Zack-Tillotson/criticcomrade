package com.rottenmeta.restapi.dao;

import java.sql.*;
import com.rottenmeta.restapi.*;
import com.rottenmeta.restapi.data.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.log4j.Logger;
import java.text.ParseException;

/**
 * @author chloburr
 */
public class UserDAO
{

    static Logger logger = Logger.getLogger(UserDAO.class.getName());

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.0");
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

    private Connection conn;    

    public UserDAO() throws RMDBException
    {
        conn = DBUtility.getDBConnection();
    }

    private final String USER_LIST_USER_INFO =
        " select" +
        "    u.user_name" +
        "    ,u.user_id" +
        "    ,u.create_date" +
        "    ,ifnull(r.tot_rev_c, 0) tot_rev_c" +
        "    ,ifnull(r.pos_rev_c, 0) pos_rev_c" +
        "    ,ifnull(r.rec_rev_c, 0) rec_rev_c" +
        " from" +
        "    users u left join" +
        "    (select user_id, count(*) tot_rev_c, sum(case when review_date > subdate(now(), interval 2 week) then 1 else 0 end) rec_rev_c, sum(case when score >= (select cutoff from review_pos_cutoffs where source = 'user') then 1 else 0 end) pos_rev_c from u_reviews cr group by user_id) r on u.user_id = r.user_id";

    private final String USER_LIST_REVIEWS =
        " select" +
        "    u.user_id" +
        "    ,m.api_id mid" +
        "    ,m.title" +
        "    ,r.score" +
        "    ,r.summary" +
        "    ,r.review_date" +
        " from" +
        "    users u" +
        "    ,movies m" +
        "    ,u_reviews r" +
        " where" +
        "    u.user_id = r.user_id and" +
        "    m.movie_id = r.movie_id";

    private final String WHERE_ONE =
        "user_id = ?";

    private final String WHERE_DEFAULT =
        "user_name is not null";

    private final String ORDER_DEFAULT =
        "user_name";

    private final String ORDER_RECENT_ACTIVE =
        "rec_rev_c desc";

    private final String ORDER_ALLTIME_ACTIVE =
        "tot_rev_c desc";

    private final String ORDER_REVIEWS_BY_DATE =
        "review_date desc";

    private final String ORDER_REVIEWS_BY_TITLE =
        "title";

    public static final String PERSONAL_USER_CRITIC_COMRADERY =
        "select * from user_critic_comradery where user_id = ?";

    public MetadList<User> getUserList() throws RMDBException { return getUserList(null); }
    public MetadList<User> getUserList(HashMap<String, String> filters) throws RMDBException
    {

        logger.debug("starting to getUserList");
        
        if(filters == null) filters = new HashMap<String, String>();
        if(!filters.containsKey("type")) filters.put("type", "ditty");
        if(!filters.containsKey("count")) filters.put("count", "20");
        if(!filters.containsKey("offset")) filters.put("offset", "0");
        if(!filters.containsKey("order-by")) filters.put("order-by", "user_name");

        // Get basic user info list - ditty, summary, and full
        String query;
        String sizeQuery;
        if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("one"))
        {
            query = "select * from (" + USER_LIST_USER_INFO + ") aa where " + WHERE_ONE;
            sizeQuery = "select count(*) size from (" + USER_LIST_USER_INFO + ") aa where " + WHERE_ONE;
        }
        else
        {
            query = "select * from (" + USER_LIST_USER_INFO + ") aa where " + WHERE_DEFAULT + " order by " + "<<ORDER-BY>>" + " limit " + filters.get("offset") + ", " + filters.get("count");
            sizeQuery = "select count(*) size from (" + "select * from (" + USER_LIST_USER_INFO + ") aa where " + WHERE_DEFAULT + ") aa";
        }

        MetadList<User> v = new MetadList<User>();
        v.setURL("users/all/");

        if(query.contains("<<ORDER-BY>>"))
        {

            if(filters.containsKey("order-by") && filters.get("order-by").equalsIgnoreCase("review-count"))
            {
                query = query.replace("<<ORDER-BY>>", ORDER_ALLTIME_ACTIVE);
                v.setOrderBy(filters.get("order-by"));
            }
            else if(filters.containsKey("order-by") && filters.get("order-by").equalsIgnoreCase("recent-review-count"))
            {
                query = query.replace("<<ORDER-BY>>", ORDER_RECENT_ACTIVE);
                v.setOrderBy(filters.get("order-by"));
            }
            else
            {
                query = query.replace("<<ORDER-BY>>", ORDER_DEFAULT);
                v.setOrderBy("user-name");
            }
        }

        v.setOffset(Integer.parseInt(filters.get("offset")));

        try
        {

            PreparedStatement stmt = conn.prepareCall(query);

            if(filters.containsKey("user_id") && filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("one"))
                stmt.setInt(1, Integer.parseInt(filters.get("user_id")));

            logger.trace("getting user name info list : " + stmt.toString());
            stmt.execute();

            ResultSet rs = stmt.getResultSet();

            while(rs.next())
            {

                User ret = new User();

                ret.setUserName(rs.getString("user_name"));
                ret.setUserID(rs.getInt("user_id"));
                ret.setCreateDate(sdf.format(df.parse(rs.getString("create_date"))));
                ret.reviews.setPositiveReviewCount(rs.getInt("pos_rev_c"));
                ret.reviews.setTotalReviewCount(rs.getInt("tot_rev_c"));
                ret.reviews.setRecentReviewCount(rs.getInt("rec_rev_c"));
                
                v.add(ret);

            }

            // Get the possible size of the result set
            stmt = conn.prepareCall(sizeQuery);

            if(filters.containsKey("user_id") && filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("one"))
                stmt.setInt(1, Integer.parseInt(filters.get("user_id")));

            logger.trace("doing max size query");
            stmt.execute();

            rs = stmt.getResultSet();

            while(rs.next())
            {
                v.setMaxSize(rs.getInt("size"));
            }

        }
        catch(SQLException e)
        {
            throw new RMDBException(e);
        }
        catch(ParseException e)
        {
            throw new RMDBException(e);
        }

        // Return if it's an empty result set
        if(v.size() == 0) return v;

        String apiList = "";
        for(Iterator<User> i = v.iterator() ; i.hasNext() ; )
            apiList = apiList + ", " + i.next().getUserID() + "";
        apiList = apiList.substring(2);

        // Reviews - summary and full
        if(filters.get("type").equalsIgnoreCase("summary") || filters.get("type").equalsIgnoreCase("full"))
        {

            logger.debug("adding reviews for these user names's: " + apiList);

            try
            {

                if(filters.containsKey("reviews-order-by") && filters.get("reviews-order-by").equalsIgnoreCase("title"))
                    query = "select * from (" + USER_LIST_REVIEWS + ") aaa where user_id in (" + apiList + ") order by " + ORDER_REVIEWS_BY_TITLE;
                else
                    query = "select * from (" + USER_LIST_REVIEWS + ") aaa where user_id in (" + apiList + ") order by " + ORDER_REVIEWS_BY_DATE;

                PreparedStatement stmt = conn.prepareCall(query);

                logger.trace("getting user review list : " + stmt.toString());
                stmt.execute();

                ResultSet rs = stmt.getResultSet();

                while(rs.next())
                {

                    // Find the user which matches this user name
                    int uid = rs.getInt("user_id");
                    String mid = rs.getString("mid");
                    String title = rs.getString("title");
                    int score = rs.getInt("score");
                    String summary = rs.getString("summary");
                    String reviewDate = sdf.format(df.parse(rs.getString("review_date")));

                    User c = null;
                    
                    for(Iterator<User> i  = v.iterator(); i.hasNext() ; )
                    {
                        User ic = i.next();
                        if(ic.getUserID() == uid) c = ic;
                    }

                    // If we didn't find a user for this user name (should never happen...) then throw an error
                    if(c == null) throw new RMDBException("Found orphan movie review - " + mid);

                    // Always bump up the max review size for this user
                    c.reviews.setMaxSize(c.reviews.getMaxSize() + 1);

                    // If it is a summary we only want to add 5 reviews
                    if(filters.get("type").equalsIgnoreCase("full") ||
                      (filters.get("type").equalsIgnoreCase("summary") && c.reviews.size() < 5))
                    {

                        // Now just add this review to the review list
                        UserReview r = new UserReview();
                        
                        r.setUserName(c.getUserName());
                        r.setAPIID(mid);
                        r.setTitle(title);
                        r.setScore(score);
                        r.setSummary(summary);
                        r.setReviewDate(reviewDate);

                        c.reviews.add(r);

                    }

                }

            }
            catch(SQLException e)
            {
                logger.debug("exception caught: " + e.toString());
                throw new RMDBException(e);
            }
            catch(ParseException e)
            {
                logger.debug("exception caught: " + e.toString());
                throw new RMDBException(e);
            }

        }

        logger.trace("review size in dao : " + v.lastElement().reviews.size());
        
        return v;
    
    }

    private final String CHECK_FOR_ID_IN_USERS =
        "select 'true' \"exists\" from users where user_id = ?";

    private final String UPDATE_USER_IN_USERS =
        "update users set password = ?, first_name = ?, last_name = ?, email = ?, sex = ?, age = ?, user_name = ? where user_id = ?";

    public boolean updateUser(int userID, User u) throws RMDBException, RMInvalidIDException
    {

        logger.debug("starting to add user");

        // Ensure the user id is valid
        try
        {

            PreparedStatement stmt = conn.prepareStatement(CHECK_FOR_ID_IN_USERS);
            stmt.setInt(1, userID);

            logger.trace("check for query: " + stmt.toString());
            ResultSet rs = stmt.executeQuery();

            if(rs.next())       // User exists already
            {

                logger.debug("user already exists");

                //Update the user
                stmt = conn.prepareStatement(UPDATE_USER_IN_USERS);

                stmt.setString(1, u.getPassword());

                if(u.getFirstName() == null) stmt.setNull(2, Types.VARCHAR);
                else stmt.setString(2, u.getFirstName());

                if(u.getLastName() == null) stmt.setNull(3, Types.VARCHAR);
                else stmt.setString(3, u.getLastName());

                if(u.getEmail() == null) stmt.setNull(4, Types.VARCHAR);
                else stmt.setString(4, u.getEmail());

                if(u.getSex() == null) stmt.setNull(5, Types.VARCHAR);
                else stmt.setString(5, u.getSex());

                if(u.getAge() == null) stmt.setNull(6, Types.INTEGER);
                else stmt.setInt(6, Integer.parseInt(u.getAge()));

                stmt.setString(7, u.getUserName());

                stmt.setInt(8, userID);

                logger.debug("update query: " + stmt.toString());
                stmt.execute();

            }
            else
                throw new RMInvalidIDException("--", "User ID");

        }
        catch(SQLException e)
        {
            logger.debug("error updating user - " + e);
            throw new RMDBException(e);
        }

        return true;

    }

    private final String CHECK_FOR_REVIEW_IN_U_REVIEWS =
        "select 'true' from u_reviews where user_id = ? and movie_id = (select movie_id from movies where api_id = ?)";

    private final String UPDATE_REVIEW_IN_U_REVIEWS =
        "update u_reviews set score = ?, summary = ?, review_date = now() where user_id = ? and movie_id = (select movie_id from movies where api_id = ?)";

    private final String ADD_USER_REVIEW_TO_U_REVIEWS =
        "insert into u_reviews (user_id, movie_id, score, summary) values (?, (select movie_id from movies where api_id = ?), ?, ?)";

    public void addUserReview(int userID, UserReview r) throws RMInvalidIDException, RMDBException
    {

        logger.debug("starting to add user review");
        logger.debug("User ID: " + userID);
        logger.debug("Review: " + r);

        if(r.getAPIID() == null ||
           r.getScore() == -1)
        {
            logger.debug("invalid review");
            throw new RMInvalidIDException(r.getAPIID(), "Movie Review");
        }

        try
        {

            PreparedStatement stmt = conn.prepareStatement(CHECK_FOR_REVIEW_IN_U_REVIEWS);
            stmt.setInt(1, userID);
            stmt.setString(2, r.getAPIID());

            logger.debug("check for review already existing");
            logger.trace("stmt: " + stmt.toString());
            ResultSet rs = stmt.executeQuery();

            if(rs.next())       // Review already exists
            {

                logger.debug("review already exists, updating");

                // So update it
                stmt = conn.prepareStatement(UPDATE_REVIEW_IN_U_REVIEWS);
                stmt.setInt(1, r.getScore());
                if(r.getSummary() == null || r.getSummary().equals(""))
                    stmt.setNull(2, Types.LONGVARCHAR);
                else
                    stmt.setString(2, r.getSummary());
                stmt.setInt(3, userID);
                stmt.setString(4, r.getAPIID());

                logger.trace("stmt: " + stmt.toString());
                stmt.execute();

            }
            else
            {

                logger.debug("review is new, inserting");
                logger.debug("review = " + r);

                // So insert it
                stmt = conn.prepareStatement(ADD_USER_REVIEW_TO_U_REVIEWS);
                stmt.setInt(1, userID);
                stmt.setString(2, r.getAPIID());
                stmt.setInt(3, r.getScore());
                if(r.getSummary() == null || r.getSummary().equals(""))
                    stmt.setNull(4, Types.LONGVARCHAR);
                else
                    stmt.setString(4, r.getSummary());

                logger.trace("stmt: " + stmt.toString());
                stmt.execute();

            }

        }
        catch(SQLException e)
        {
            logger.debug("error adding review: " + e.toString());
            throw new RMDBException(e);
        }

    }

    private final String REMOVE_CRITIC_USER_COMRADERY =
            "delete from user_critic_comradery where user_id = ?";

    private final String ADD_CRITIC_USER_COMRADERY =
            "insert into user_critic_comradery select    ur.user_id, c.api_id critic_id    ,pow(case when I > 10.9 then pow(S/I, 2.5)*20.2+6.1 else 0.1 end, 9.8)/10000000 comeradery ,I intersect_c, S matching_c     from  critics c    ,(select critic_id, count(*) N from c_reviews cr group by critic_id) cr    ,(select critic_id, iur.user_id, count(*) I, sum(case when case when icr.score >= (select cutoff from review_pos_cutoffs where source = icr.source) then 1 else 0 end = case when iur.score >= (select cutoff from review_pos_cutoffs where source = 'user') then 1 else 0 end then 1 else 0 end) S from c_reviews icr, u_reviews iur where iur.user_id = ? and icr.movie_id = iur.movie_id group by critic_id, user_id) ur    ,(select user_id, count(*) R from u_reviews group by user_id) urc where        c.critic_id = cr.critic_id    and c.critic_id = ur.critic_id    and cr.N > 15 group by    ur.user_id, c.api_id";

    private final String REMOVE_CRITIC_USER_MOVIE_COMRADE_SCORES =
            "delete from user_critic_movie_comrade_score where user_id = ?";

    private final String ADD_CRITIC_USER_MOVIE_COMRADE_SCORES =
            "insert into user_critic_movie_comrade_score select cl.user_id, cr.movie_id, format(sum(cr.score * cl.comradery) / sum(cl.comradery), 4) comrade_score, count(cr.score) comrade_c from (select   m.movie_id, c.api_id cid,    case when r.score >= (select cutoff from review_pos_cutoffs where source = r.source) then 1 else 0 end score from    movies m,   c_reviews r,    critics c where    m.movie_id = r.movie_id and   c.critic_id = r.critic_id) cr, user_critic_comradery cl   where    cr.cid = cl.cid and cl.user_id = ? group by   cl.user_id, cr.movie_id";

    public void refreshComradery(int userID) throws RMDBException
    {

        try
        {

            PreparedStatement stmt;

            // Refresh the critic-user comradery scores
            stmt = conn.prepareStatement(REMOVE_CRITIC_USER_COMRADERY);
            stmt.setInt(1, userID);

            logger.trace("clearing comradery: " + stmt.toString());

            stmt.execute();

            stmt = conn.prepareStatement(ADD_CRITIC_USER_COMRADERY);
            stmt.setInt(1, userID);

            logger.trace("adding comradery: " + stmt.toString());

            stmt.execute();

            // Refresh the critic-user-movie comrade scores
            stmt = conn.prepareStatement(REMOVE_CRITIC_USER_MOVIE_COMRADE_SCORES);
            stmt.setInt(1, userID);

            logger.trace("clearing comrade scores: " + stmt.toString());

            stmt.execute();

            stmt = conn.prepareStatement(ADD_CRITIC_USER_MOVIE_COMRADE_SCORES);
            stmt.setInt(1, userID);

            logger.trace("adding comrade scores: " + stmt.toString());

            stmt.execute();

        }
        catch(SQLException e)
        {
            logger.debug(e.toString());
            throw new RMDBException(e);
        }
        
    }

    private final String GET_USER_ID_FROM_USER_NAME =
            "select user_id from users where user_name = ?";

    public int getUserIDFromUserName(String username) throws RMDBException, RMInvalidIDException
    {

        logger.debug("getting user id from user name");
        logger.trace("username: " + username);

        try
        {

            PreparedStatement stmt = conn.prepareStatement(GET_USER_ID_FROM_USER_NAME);
            stmt.setString(1, username);

            logger.trace("doing query to get user id from user_name");
            ResultSet rs = stmt.executeQuery();

            if(!rs.next())
                throw new RMInvalidIDException(username, "User");

            return rs.getInt("user_id");

        }
        catch(SQLException e)
        {
            logger.debug(e);
            throw new RMDBException(e);
        }

    }

}