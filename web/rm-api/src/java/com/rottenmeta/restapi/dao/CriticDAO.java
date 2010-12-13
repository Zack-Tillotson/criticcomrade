/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
public class CriticDAO
{

    static Logger logger = Logger.getLogger(CriticDAO.class.getName());

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.0");
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

    private Connection conn;    

    public CriticDAO() throws RMDBException
    {
        conn = DBUtility.getDBConnection();
    }

    private final String PERSONAL_USER_CRITIC_COMRADERY =
        UserDAO.PERSONAL_USER_CRITIC_COMRADERY;

    private final String CRITIC_LIST_CRITIC_INFO =
        " select" +
        "    c.api_id" +
        "    ,c.name" +
        "    ,c.publisher" +
        "    ,r.tot_rev_c" +
        "    ,r.pos_rev_c" +
        "    ,r.rec_rev_c" +
        "    ,ifnull(pc.comradery, -1) conf" +
        "    ,ifnull(pc.matching_c, -1) matching_c" +
        "    ,ifnull(pc.intersect_c, -1) intersect_c" +
        " from" +
        "    critics c left join" +
        "    (select critic_id, count(*) tot_rev_c, sum(case when review_date > subdate(now(), interval 2 week) then 1 else 0 end) rec_rev_c, sum(case when score >= (select cutoff from review_pos_cutoffs where source = cr.source) then 1 else 0 end) pos_rev_c from c_reviews cr group by critic_id) r on c.critic_id = r.critic_id left join" +
        "    (" + PERSONAL_USER_CRITIC_COMRADERY + ") pc on pc.cid = c.api_id";

    private final String CRITIC_LIST_REVIEWS =
        " select" +
        "    c.api_id" +
        "    ,m.api_id mid" +
        "    ,m.title" +
        "    ,case when score >= (select cutoff from review_pos_cutoffs where source = r.source) then 1 else 0 end score" +
        "    ,r.summary" +
        "    ,r.review_date" +
        "    ,r.link" +
        " from" +
        "    critics c" +
        "    ,movies m" +
        "    ,c_reviews r" +
        " where" +
        "    c.critic_id = r.critic_id and" +
        "    m.movie_id = r.movie_id";

    private final String WHERE_ONE =
        "api_id = ?";

    private final String WHERE_SEARCH_NAMES =
        "lower(name) like ?";

    private final String WHERE_SEARCH_PUBLISHERS =
        "lower(publisher) like ?";

    private final String ORDER_DEFAULT =
        "name";

    private final String ORDER_RECENT_ACTIVE =
        "rec_rev_c desc";

    private final String ORDER_ALLTIME_ACTIVE =
        "tot_rev_c desc";

    private final String ORDER_BY_INTERSECT =
        "intersect_c desc, name";

    private final String ORDER_REVIEWS_BY_DATE =
        "review_date desc";

    private final String ORDER_REVIEWS_BY_TITLE =
        "title";

    private final String ORDER_COMRADE =
        "conf desc, intersect_c desc, matching_c desc, tot_rev_c desc";

    private final String ORDER_COMRADE_DESC =
        "case when conf = -1 then 10000 else conf end, matching_c asc, intersect_c desc, tot_rev_c desc";

    public MetadList<Critic> getCriticList() throws RMDBException { return getCriticList(null); }
    public MetadList<Critic> getCriticList(HashMap<String, String> filters) throws RMDBException
    {

        logger.debug("starting to getCriticList");
        
        if(filters == null) filters = new HashMap<String, String>();
        if(!filters.containsKey("type")) filters.put("type", "ditty");
        if(!filters.containsKey("count")) filters.put("count", "20");
        if(!filters.containsKey("offset")) filters.put("offset", "0");

        // Get basic critic info list - ditty, summary, and full
        String query;
        String sizeQuery;
        if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("one"))
        {
            query = "select * from (" + CRITIC_LIST_CRITIC_INFO + ") aa where " + WHERE_ONE;
            sizeQuery = "select count(*) size from (" + CRITIC_LIST_CRITIC_INFO + ") aa where " + WHERE_ONE;
        }
        else if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("search-critic-names"))
        {
            query = "select * from (" + CRITIC_LIST_CRITIC_INFO + ") aa where " + WHERE_SEARCH_NAMES;
            sizeQuery = "select count(*) size from (" + CRITIC_LIST_CRITIC_INFO + ") aa where " + WHERE_SEARCH_NAMES;
        }
        else if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("search-critic-publishers"))
        {
            query = "select * from (" + CRITIC_LIST_CRITIC_INFO + ") aa where " + WHERE_SEARCH_PUBLISHERS;
            sizeQuery = "select count(*) size from (" + CRITIC_LIST_CRITIC_INFO + ") aa where " + WHERE_SEARCH_PUBLISHERS;
        }
        else
        {
            query = CRITIC_LIST_CRITIC_INFO + " order by " + "<<ORDER-BY>>" + " limit " + filters.get("offset") + ", " + filters.get("count");
            sizeQuery = "select count(*) size from (" + CRITIC_LIST_CRITIC_INFO + ") aa";
        }

        MetadList<Critic> v = new MetadList<Critic>();
        v.setURL("critics/all/");

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
            else if(filters.containsKey("order-by") && filters.get("order-by").equalsIgnoreCase("review-intersect-count"))
            {
                query = query.replace("<<ORDER-BY>>", ORDER_BY_INTERSECT);
                v.setOrderBy(filters.get("order-by"));
            }
            else if(filters.containsKey("order-by") && filters.get("order-by").equalsIgnoreCase("comrade"))
            {
                query = query.replace("<<ORDER-BY>>", ORDER_COMRADE);
                v.setOrderBy(filters.get("order-by"));
            }
            else if(filters.containsKey("order-by") && filters.get("order-by").equalsIgnoreCase("comrade-desc"))
            {
                query = query.replace("<<ORDER-BY>>", ORDER_COMRADE_DESC);
                v.setOrderBy(filters.get("order-by"));
            }
            else
            {
                query = query.replace("<<ORDER-BY>>", ORDER_DEFAULT);
                v.setOrderBy("name");
            }
        }

        v.setOffset(Integer.parseInt(filters.get("offset")));

        try
        {

            PreparedStatement stmt = conn.prepareCall(query);

            stmt.setInt(1, filters.containsKey("user-id") ? Integer.parseInt(filters.get("user-id")) : 0);
            if(filters.containsKey("cid") && filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("one"))
                stmt.setString(2, filters.get("cid"));
            if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("search-critic-names"))
                stmt.setString(2, filters.get("search-term"));
            if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("search-critic-publishers"))
                stmt.setString(2, filters.get("search-term"));
            

            logger.trace("getting critic info list : " + stmt.toString());

            stmt.execute();

            ResultSet rs = stmt.getResultSet();

            while(rs.next())
            {

                Critic ret = new Critic();

                ret.setAPIID(rs.getString("api_id"));
                ret.setName(rs.getString("name"));
                ret.setPublisher(rs.getString("publisher"));
                ret.reviews.setPositiveReviewCount(rs.getInt("pos_rev_c"));
                ret.reviews.setTotalReviewCount(rs.getInt("tot_rev_c"));
                ret.reviews.setRecentReviewCount(rs.getInt("rec_rev_c"));
                ret.setConfidence(rs.getDouble("conf"));
                ret.reviews.setUserAgreeCount(rs.getInt("matching_c"));
                ret.reviews.setUserIntersectCount(rs.getInt("intersect_c"));
                
                v.add(ret);

            }

            // Get the possible size of the result set
            stmt = conn.prepareCall(sizeQuery);

            stmt.setInt(1, filters.containsKey("user-id") ? Integer.parseInt(filters.get("user-id")) : 0);
            if(filters.containsKey("cid") && filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("one"))
                stmt.setString(2, filters.get("cid"));
            if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("search-critic-names"))
                stmt.setString(2, filters.get("search-term"));
            if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("search-critic-publishers"))
                stmt.setString(2, filters.get("search-term"));

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

        // Return if it's an empty result set
        if(v.size() == 0) return v;

        String apiList = "";
        for(Iterator<Critic> i = v.iterator() ; i.hasNext() ; )
            apiList = apiList + ", '" + i.next().getAPIID() + "'";
        apiList = apiList.substring(2);

        // Reviews - summary and full
        if(filters.get("type").equalsIgnoreCase("summary") || filters.get("type").equalsIgnoreCase("full"))
        {

            logger.debug("adding reviews for these api's: " + apiList);

            try
            {

                if(filters.containsKey("reviews-order-by") && filters.get("reviews-order-by").equalsIgnoreCase("title"))
                    query = "select * from (" + CRITIC_LIST_REVIEWS + ") aaa where api_id in (" + apiList + ") order by " + ORDER_REVIEWS_BY_TITLE;
                else
                    query = "select * from (" + CRITIC_LIST_REVIEWS + ") aaa where api_id in (" + apiList + ") order by " + ORDER_REVIEWS_BY_DATE;

                PreparedStatement stmt = conn.prepareCall(query);

                logger.trace("getting critic review list : " + stmt.toString());

                stmt.execute();

                ResultSet rs = stmt.getResultSet();

                logger.trace("rs gotten, ");
                while(rs.next())
                {

                    // Find the critic which matches this api_id
                    String apiId = rs.getString("api_id");
                    String mid = rs.getString("mid");
                    String title = rs.getString("title");
                    int score = rs.getInt("score");
                    String summary = rs.getString("summary");
                    String link = rs.getString("link");
                    String reviewDate = sdf.format(df.parse(rs.getString("review_date")));

                    Critic c = null;
                    
                    for(Iterator<Critic> i  = v.iterator(); i.hasNext() ; )
                    {
                        Critic ic = i.next();
                        if(ic.getAPIID().equals(apiId))
                        {
                            c = ic;
                            break;
                        }
                    }

                    // If we didn't find a critic for this api_id (should never happen...) then throw an error
                    if(c == null) throw new RMDBException("Found orphan movie review - " + mid);

                    // Always bump up the max review size for this critic
                    c.reviews.setMaxSize(c.reviews.getMaxSize() + 1);

                    // If it is a summary we only want to add 5 reviews
                    if(filters.get("type").equalsIgnoreCase("full") ||
                      (filters.get("type").equalsIgnoreCase("summary") && c.reviews.size() < 5))
                    {

                        // Now just add this review to the review list
                        CriticReview r = new CriticReview();
                        r.setCriticID(c.getAPIID());
                        r.setName(c.getName());
                        r.setPublisher(c.getPublisher());
                        r.setAPIID(mid);
                        r.setTitle(title);
                        r.setScore(score);
                        r.setSummary(summary);
                        r.setOutsideLink(link);
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
        
        return v;
    
    }        

}