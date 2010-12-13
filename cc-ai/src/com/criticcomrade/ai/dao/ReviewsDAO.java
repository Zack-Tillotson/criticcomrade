package com.criticcomrade.ai.dao;

import com.criticcomrade.ai.data.*;
import java.sql.*;
import javax.naming.*;
import org.apache.log4j.Logger;

public class ReviewsDAO
{

    private Logger log = Logger.getLogger(ReviewsDAO.class.toString());

    private final String user = "critic_review";
    private final String pass = "critic_review_pwd";
    private final String url = "jdbc:mysql://notatrick.com/critic_review_new";

    private Connection conn;

    private final long DEFAULT_TIME_TO_SAVE_REVIEWER_GROUP = 1000 * 60 * 60 * 4;

    private ReviewerGroups savedRevGroup = null;
    private long revGroupSaveDate = -1;

    public ReviewsDAO() throws NamingException, SQLException
    {

        try
        {
            Class.forName ("com.mysql.jdbc.Driver").newInstance();
        }
        catch(ClassNotFoundException e)
        {
            throw new SQLException();
        }
        catch(InstantiationException e)
        {
            throw new SQLException();
        }
        catch(IllegalAccessException e)
        {
            throw new SQLException();
        }
        conn = DriverManager.getConnection (url, user, pass);
        
    }

    private final String GET_CRITIC_REVIEWS =
        "select critic_id, r.movie_id, case when score >= (select cutoff from review_pos_cutoffs where source = r.source) then 1 else 0 end score, datediff(now(), m.release_date) days_ago from c_reviews r, movies m where r.movie_id = m.movie_id order by 1 asc";

    private static final String GET_USER_REVIEWS =
        "select user_id, r.movie_id, case when score >= (select cutoff from review_pos_cutoffs where source = 'user') then 1 else 0 end score, datediff(now(), m.release_date) days_ago from u_reviews r, movies m where r.movie_id = m.movie_id order by 1 asc";

    public synchronized ReviewerGroups getReviewerGroups() throws SQLException, NamingException
    {

        if(revGroupSaveDate != -1 && System.currentTimeMillis() - revGroupSaveDate < DEFAULT_TIME_TO_SAVE_REVIEWER_GROUP && savedRevGroup != null)
        {
            log.debug("Using saved reviews");
            return new ReviewerGroups(savedRevGroup);
        }

        log.debug("Getting DB reviews");

        ReviewerGroups revGroup = new ReviewerGroups();

        // Critics
        PreparedStatement stmt = conn.prepareCall(GET_CRITIC_REVIEWS);
        //log.trace("Critic query: " + stmt.toString());

        int reviewC = 0;

        stmt.execute();
        
        ResultSet rs = stmt.getResultSet();
        while(rs.next())
        {

            int cid = rs.getInt(1);
            int mid = rs.getInt(2);
            int score = rs.getInt(3);
            int daysAgo = rs.getInt(4);

            reviewC++;

            Review rev = new Review(mid, score, daysAgo);

            revGroup.addReview("C" + cid, rev);

        }

        int criticC = revGroup.getActiveReviewers().size();

        log.debug("Active critics : " + criticC + " (from " + reviewC + " reviews)");

        // Users
        stmt = conn.prepareCall(GET_USER_REVIEWS);
        //log.trace("User query: " + stmt.toString());
        
        stmt.execute();
        
        reviewC = 0;

        rs = stmt.getResultSet();
        while(rs.next())
        {

            int uid = rs.getInt(1);
            int mid = rs.getInt(2);
            int score = rs.getInt(3);
            int daysAgo = rs.getInt(4);

            reviewC++;

            Review rev = new Review(mid, score, daysAgo);

            revGroup.addReview("U" + uid, rev);

        }

        log.debug("Active users : " + (revGroup.getActiveReviewers().size() - criticC) + " (from " + reviewC + " reviews)");

        // Calculate all the stats for the reviewers
        revGroup.calculateStats();

        // Save this rev group for future use
        revGroupSaveDate = System.currentTimeMillis();
        savedRevGroup = revGroup;
        savedRevGroup.clean();

        log.debug("Done getting DB reviews review");

        return revGroup;

    }

}