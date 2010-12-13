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
public class MovieDAO
{

    static Logger logger = Logger.getLogger(MovieDAO.class.getName());

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.0");
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

    private Connection conn;

    public MovieDAO() throws RMDBException
    {
        conn = DBUtility.getDBConnection();
    }

    private final String MOVIE_LIST_CRITIC_REVIEWS =
        "select " +
        "   m.api_id, " +
        "   c.api_id cid, " +
        "   c.name, " +
        "   c.publisher, " +
        "   case when r.score >= (select cutoff from review_pos_cutoffs where source = r.source) then 1 else 0 end score, " +
        "   r.summary, " +
        "   r.link, " +
        "   r.review_date " +
        "from " +
        "   movies m, " +
        "   c_reviews r, " +
        "   critics c " +
        "where " +
        "   m.movie_id = r.movie_id and " +
        "   c.critic_id = r.critic_id ";

    private final String MOVIE_LIST_USER_REVIEWS =
        "select " +
        "   m.api_id, " +
        "   u.user_name, " +
        "   r.score, " +
        "   r.summary, " +
        "   r.review_date " +
        "from " +
        "   movies m," +
        "   u_reviews r, " +
        "   users u " +
        "where " +
        "   m.movie_id = r.movie_id and " +
        "   u.user_id = r.user_id ";

    // Gets a list of personal movie reviews for a user
    private final String PERSONAL_USER_MOVIE_REVIEWS =
        " select" +
        "   cr.api_id mid, " +
        "   cr.cid, " +
        "   cr.name, " +
        "   cr.publisher, " +
        "   cr.score, " +
        "   cr.summary, " +
        "   cr.link, " +
        "   cr.review_date, " +
        "   cl.comradery confidence" +
        " from " +
        "   (" + MOVIE_LIST_CRITIC_REVIEWS + ") cr, " +
        "   (select * from user_critic_comradery where user_id = ?) cl " +
        " where " +
        "   cr.cid = cl.cid";

    private final String MOVIE_LIST_MOVIE_INFO =
        "select  " +
        "  m.title,  " +
        "  m.api_id,  " +
        "  m.release_date,  " +
        "  m.studio,  " +
        "  m.rating,  " +
        "  m.rating_reason, " +
        "  ifnull(br.rank, 0) rank, " +
        "  ifnull(sc.pos_c_count, 0) pos_c_count, " +
        "  ifnull(sc.tot_c_count, 0) tot_c_count, " +
        "  ifnull(sc.recent_c_count, 0) recent_c_count, " +
        "  ifnull(su.pos_u_count, 0) pos_u_count, " +
        "  ifnull(su.tot_u_count, 0) tot_u_count, " +
        "  ifnull(su.recent_u_count, 0) recent_u_count, " +
        "  ifnull(pm.comrade_score, -1) agg_score, " +
        "  ifnull(pm.comrade_c, -1) comerade_count " +
        "from  " +
        "  movies m left join " +
        "  (select movie_id, sum(case when score >= (select cutoff from review_pos_cutoffs where source = r.source) then 1 else 0 end) pos_c_count, sum(case when review_date > subdate(now(), interval 2 week) then 1 else 0 end) recent_c_count, count(*) tot_c_count from c_reviews r group by movie_id) sc on sc.movie_id = m.movie_id left join " +
        "  (select movie_id, sum(case when score >= (select cutoff from review_pos_cutoffs where source = 'user') then 1 else 0 end) pos_u_count, sum(case when review_date > subdate(now(), interval 2 week) then 1 else 0 end) recent_u_count, count(*) tot_u_count from u_reviews r group by movie_id) su on m.movie_id = su.movie_id left join " +
        "  (select * from user_critic_movie_comrade_score where user_id = ?) pm on m.movie_id = pm.movie_id left join " +
        "  (select r.movie_id, r.rank from movie_rankings r order by year desc, week desc, rank asc limit 10) br on m.movie_id = br.movie_id";

    private final String MOVIE_LIST_PEOPLE = 
        "select " +
        "   m.api_id, " +
        "   p.name, " +
        "   a.role " +
        "from " +
        "   movie_people p, " +
        "   movie_people_assoc a, " +
        "   movies m " +
        "where " +
        "   p.mp_id = a.mp_id and " +
        "   a.movie_id = m.movie_id";
    
    private final String MOVIE_LIST_SUMMARIES =
        "select " +
        "   m.api_id, " +
        "   min(s.summary) summary " +
        "from " +
        "   movie_summaries s, " +
        "   movies m " +
        "where " +
        "   m.movie_id = s.movie_id " +
        "group by " +
        "   s.movie_id " +
        "order by " +
        "   source = 'metacritic' desc, " +
        "   source = 'rottentomatoes-full' desc, " +
        "   source = 'rottentomatoes-blurb' desc ";

    private final String MOVIE_LIST_GENRES =
        "select " +
        "   m.api_id, " +
        "   g.genre " +
        "from " +
        "   movies m, " +
        "   movie_genres g " +
        "where " +
        "   m.movie_id = g.movie_id";

    private final String WHERE_OPENING =
        "release_date > subdate(makedate(year(now()), 7*weekofyear(now())), interval 12 day) and " +
        "release_date <= subdate(makedate(year(now()), 7*weekofyear(now())), interval 5 day)";

    private final String WHERE_IN_THEATERS =
        "release_date >= subdate(makedate(year(now()), 7*weekofyear(now())), interval 33 day) and " +
        "release_date <= subdate(makedate(year(now()), 7*weekofyear(now())), interval 12 day)";

    private final String WHERE_NOT_REVIEWED =
        "aa.api_id not in (select api_id from movies mm, u_reviews urr where urr.movie_id = mm.movie_id and user_id = ?)";

    private final String WHERE_CURRENT =
        "release_date >= subdate(makedate(year(now()), 7*weekofyear(now())), interval 33 day) and " +
        "release_date <= subdate(makedate(year(now()), 7*weekofyear(now())), interval 5 day)";

    private final String WHERE_TOP_BOX_OFFICE =
        "rank != 0";

    private final String WHERE_COMING_UP =
        "release_date > subdate(makedate(year(now()), 7*weekofyear(now())), interval 5 day)";

    private final String WHERE_ONE =
        "api_id = ?";

    private final String WHERE_SEARCH_MOVIE_TITLES =
        "lower(title) like ?";

    private final HashMap<String, String> ORDER_BY;
    {
        ORDER_BY = new HashMap();
        ORDER_BY.put("in-theaters", "release_date desc, title asc");
        ORDER_BY.put("opening", "release_date asc, title asc");
        ORDER_BY.put("default", "title asc");
        ORDER_BY.put("comrade", "agg_score is null, agg_score desc");
        ORDER_BY.put("title", "title asc");
        ORDER_BY.put("rank", "rank asc");
        ORDER_BY.put("publisher", "studio asc");
        ORDER_BY.put("rating", "rating is null, rating = 'G' desc, rating = 'PG' desc, rating = 'PG-13' desc, rating = 'R' desc");
        ORDER_BY.put("positive-critic-reviews", "pos_c_count desc");
        ORDER_BY.put("total-critic-reviews", "tot_c_count desc");
        ORDER_BY.put("recent-critic-reviews", "recent_c_count desc");
        ORDER_BY.put("positive-user-reviews", "pos_u_count desc");
        ORDER_BY.put("total-user-reviews", "tot_u_count desc");
        ORDER_BY.put("recent-user-reviews", "recent_u_count desc");
        ORDER_BY.put("comrade-count", "comerade_count desc");
    }

    public MetadList<Movie> getMovieList() throws RMDBException { return getMovieList(null); }
    public MetadList<Movie> getMovieList(HashMap<String, String> filters) throws RMDBException
    {

        logger.debug("starting to getMovieList");
        long startTime = System.currentTimeMillis();

        if(filters == null) filters = new HashMap<String, String>();
        if(!filters.containsKey("type")) filters.put("type", "ditty");
        if(!filters.containsKey("count")) filters.put("count", "20");
        if(!filters.containsKey("offset")) filters.put("offset", "0");
        if(!filters.containsKey("order-by")) filters.put("order-by", "title");

        logger.trace("params: " + filters);

        // Get basic movie info list - ditty, summary, and full
        String query;
        String sizeQuery;
        if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("in-theaters"))
        {
            logger.trace("filtering to in theater movies");
            query = "select * from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_IN_THEATERS + " order by <<ORDER-BY>> limit " + filters.get("offset") + ", " + filters.get("count");
            sizeQuery = "select count(*) size from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_IN_THEATERS;
            if(!filters.containsKey("order-by")) filters.put("order-by", "in-theaters");
        }

        else if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("opening"))
        {
            logger.trace("filtering to opening movies");
            query = "select * from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_OPENING + " order by <<ORDER-BY>> limit " + filters.get("offset") + ", " + filters.get("count");
            sizeQuery = "select count(*) size from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_OPENING;
            if(!filters.containsKey("order-by")) filters.put("order-by", "opening");
        }
        else if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("not-reviewed"))
        {
            logger.trace("filtering to not reviewed movies");
            query = "select * from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_NOT_REVIEWED + " order by <<ORDER-BY>> limit " + filters.get("offset") + ", " + filters.get("count");
            sizeQuery = "select count(*) size from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_NOT_REVIEWED;
            if(!filters.containsKey("order-by")) filters.put("order-by", "total-critic-reviews");
        }
        else if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("current"))
        {
            logger.trace("filtering to current movies");
            query = "select * from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_CURRENT + " order by <<ORDER-BY>> limit " + filters.get("offset") + ", " + filters.get("count");
            sizeQuery = "select count(*) size from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_CURRENT;
            if(!filters.containsKey("order-by")) filters.put("order-by", "total-critic-reviews");
        }
        else if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("one"))
        {
            logger.trace("filtering to one movie");
            query = "select * from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_ONE;
            sizeQuery = "select count(*) size from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_ONE;
        }
        else if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("top-box-office"))
        {
            logger.trace("filtering to top box office movies");
            query = "select * from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_TOP_BOX_OFFICE + " order by <<ORDER-BY>> limit " + filters.get("offset") + ", " + filters.get("count");
            sizeQuery = "select count(*) size from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_TOP_BOX_OFFICE;
        }
        else if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("coming-up"))
        {
            logger.trace("filtering to coming up movies");
            query = "select * from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_COMING_UP + " order by <<ORDER-BY>> limit " + filters.get("offset") + ", " + filters.get("count");
            sizeQuery = "select count(*) size from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_COMING_UP;
        }
        else if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("search-movie-titles"))
        {
            logger.trace("filtering to search movie titles");
            query = "select * from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_SEARCH_MOVIE_TITLES;
            sizeQuery = "select count(*) size from (" + MOVIE_LIST_MOVIE_INFO + ") aa where " + WHERE_SEARCH_MOVIE_TITLES;
        }
        else
        {
            query = MOVIE_LIST_MOVIE_INFO + " order by " + "<<ORDER-BY>>" + " limit " + filters.get("offset") + ", " + filters.get("count");
            sizeQuery = "select count(*) size from (" + MOVIE_LIST_MOVIE_INFO + ") aa";
        }

        MetadList<Movie> vm = new MetadList<Movie>();
        vm.setURL("movies/all/");

        if(query.contains("<<ORDER-BY>>"))
        {

            // Get the string to order by from the filters
            if(!filters.containsKey("order-by")) filters.put("order-by", "default");                
            String filterOrderBy = filters.get("order-by");

            // See if it needs to be switched around
            Boolean doSwitch = false;
            if(filterOrderBy.matches("^.*-desc$"))
            {
                filterOrderBy = filterOrderBy.substring(0, filterOrderBy.length() - 5);
                doSwitch = true;
            }

            // Make sure the filters.get("order-by") contains a valid order by value
            if(!ORDER_BY.containsKey(filterOrderBy))
            {
                filterOrderBy = "default";
                filters.put("order-by", "default");
            }
                
            // Give it the old switcharoo if needed
            String order = ORDER_BY.get(filterOrderBy);
            if(doSwitch)
            {
                order = order.replaceAll("asc", "XXX");
                order = order.replaceAll("desc", "asc");
                order = order.replaceAll("XXX", "desc");
            }

            // Do the replacing and put in meta info for the list
            query = query.replace("<<ORDER-BY>>", order);
            vm.setOrderBy(filters.get("order-by").equalsIgnoreCase("default")?ORDER_BY.get("default"):filters.get("order-by"));
            
        }

        vm.setOffset(Integer.parseInt(filters.get("offset")));

        try
        {

            PreparedStatement stmt = conn.prepareCall(query);

            int userID = (filters.containsKey("user-id")) ? Integer.parseInt(filters.get("user-id")) : 0;
            stmt.setInt(1, userID);

            if(filters.containsKey("mid") && filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("one"))
                stmt.setString(2, filters.get("mid"));

            if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("not-reviewed"))
                stmt.setInt(2, Integer.parseInt(filters.get("user-id")));

            if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("search-movie-titles"))
                stmt.setString(2, filters.get("search-term"));

            logger.trace("getting movie info list : " + stmt.toString());
            stmt.execute();
            logger.trace("      ---- time so far: " + (System.currentTimeMillis() - startTime) / 1000. + " sec");

            ResultSet rs = stmt.getResultSet();

            while(rs.next())
            {

                Movie ret = new Movie();

                ret.setTitle(rs.getString("title"));
                ret.setMovieID(rs.getString("api_id"));
                ret.setReleaseDate(sdf.format(df.parse(rs.getString("release_date"))));
                ret.setRating(rs.getString("rating"));
                ret.setRatingReason(rs.getString("rating_reason"));
                ret.setStudio(rs.getString("studio"));
                ret.setRanking(rs.getInt("rank"));
                ret.critic_reviews.setTotalReviewCount(rs.getInt("tot_c_count"));
                ret.critic_reviews.setRecentReviewCount(rs.getInt("recent_c_count"));
                ret.critic_reviews.setPositiveReviewCount(rs.getInt("pos_c_count"));
                ret.user_reviews.setTotalReviewCount(rs.getInt("tot_u_count"));
                ret.user_reviews.setPositiveReviewCount(rs.getInt("pos_u_count"));
                ret.user_reviews.setRecentReviewCount(rs.getInt("recent_u_count"));
                ret.critic_reviews.setUserAggregateScore(rs.getDouble("agg_score"));
                ret.critic_reviews.setUserComradeCount(rs.getInt("comerade_count"));
                
                vm.add(ret);

            }

            // Get the possible size of the result set
            stmt = conn.prepareCall(sizeQuery);
            stmt.setInt(1, userID);

            if(filters.containsKey("mid") && filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("one"))
                stmt.setString(2, filters.get("mid"));

            if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("not-reviewed"))
                stmt.setInt(2, Integer.parseInt(filters.get("user-id")));

            if(filters.containsKey("filter") && filters.get("filter").equalsIgnoreCase("search-movie-titles"))
                stmt.setString(2, filters.get("search-term"));

            logger.trace("size query: " + stmt);
            
            stmt.execute();
            logger.trace("      ---- time so far: " + (System.currentTimeMillis() - startTime) / 1000. + " sec");

            rs = stmt.getResultSet();

            while(rs.next())
            {
                vm.setMaxSize(rs.getInt("size"));
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
        if(vm.size() == 0) return vm;

        String apiList = "";
        for(Iterator<Movie> i = vm.iterator() ; i.hasNext() ; )
            apiList = apiList + ", '" + i.next().getMovieID() + "'";
        apiList = apiList.substring(2);

        // People, Summaries, Genres - summary and full
        if(filters.get("type").equalsIgnoreCase("summary") || filters.get("type").equalsIgnoreCase("full"))
        {

            logger.trace("adding people, summaries, and genres for these movie api's: " + apiList);

            // People
            try
            {

                query = "select * from (" + MOVIE_LIST_PEOPLE + ") aaa where api_id in (" + apiList + ")";

                PreparedStatement stmt = conn.prepareCall(query);

                logger.trace("getting movie people list : " + stmt.toString());
                stmt.execute();
                logger.trace("      ---- time so far: " + (System.currentTimeMillis() - startTime) / 1000. + " sec");

                ResultSet rs = stmt.getResultSet();

                while(rs.next())
                {

                    // Find the movie which matches this api_id
                    String apiId = rs.getString("api_id");
                    String name = rs.getString("name");
                    String role = rs.getString("role");

                    Movie m = null;
                    
                    for(Iterator<Movie> i  = vm.iterator(); i.hasNext() ; )
                    {
                        Movie im = i.next();
                        if(im.getMovieID().equals(apiId)) m = im;
                    }

                    // If we didn't find a movie for this api_id (should never happen...) then throw an error
                    if(m == null) throw new RMDBException("Found orphan movie person - " + apiId + ", [" + role + "] " + name);

                    // Now just add this person to the appropriate people list
                    if(role.equalsIgnoreCase("s"))
                        m.vstar.add(name);
                    else if(role.equalsIgnoreCase("d"))
                        m.vdirector.add(name);
                    else if(role.equalsIgnoreCase("w"))
                        m.vwriter.add(name);
                    else throw new RMDBException("Non writer/director/star person - " + apiId + ", [" + role + "] " + name);

                }


            }
            catch(SQLException e)
            {
                logger.debug("exception caught: " + e.toString());
                throw new RMDBException(e);
            }

            // Summaries
            try
            {

                query = "select * from (" + MOVIE_LIST_SUMMARIES + ") aaa where api_id in (" + apiList + ")";

                PreparedStatement stmt = conn.prepareCall(query);

                logger.trace("getting movie summary list : " + stmt.toString());
                stmt.execute();
                logger.trace("      ---- time so far: " + (System.currentTimeMillis() - startTime) / 1000. + " sec");

                ResultSet rs = stmt.getResultSet();

                while(rs.next())
                {

                    // Find the movie which matches this api_id
                    String apiId = rs.getString("api_id");
                    String summary = rs.getString("summary");

                    Movie m = null;

                    for(Iterator<Movie> i  = vm.iterator(); i.hasNext() ; )
                    {
                        Movie im = i.next();
                        if(im.getMovieID().equals(apiId)) m = im;
                    }

                    // If we didn't find a movie for this api_id (should never happen...) then throw an error
                    if(m == null) throw new RMDBException("Found orphan movie summary - " + apiId + ", " + summary);

                    // Now just add this person to the appropriate people list
                    m.setSummary(summary);
                    
                }


            }
            catch(SQLException e)
            {
                logger.debug("exception caught: " + e.toString());
                throw new RMDBException(e);
            }

            // Genres
            try
            {

                query = "select * from (" + MOVIE_LIST_GENRES + ") aaa where api_id in (" + apiList + ")";

                PreparedStatement stmt = conn.prepareCall(query);

                logger.trace("getting movie genre list : " + stmt.toString());
                stmt.execute();
                logger.trace("      ---- time so far: " + (System.currentTimeMillis() - startTime) / 1000. + " sec");

                ResultSet rs = stmt.getResultSet();

                while(rs.next())
                {

                    // Find the movie which matches this api_id
                    String apiId = rs.getString("api_id");
                    String genre = rs.getString("genre");

                    Movie m = null;

                    for(Iterator<Movie> i  = vm.iterator(); i.hasNext() ; )
                    {
                        Movie im = i.next();
                        if(im.getMovieID().equals(apiId)) m = im;
                    }

                    // If we didn't find a movie for this api_id (should never happen...) then throw an error
                    if(m == null) throw new RMDBException("Found orphan movie genre - " + apiId + ", " + genre);

                    // Now just add this person to the appropriate people list
                    m.vgenre.add(genre);

                }


            }
            catch(SQLException e)
            {
                logger.debug("exception caught: " + e.toString());
                throw new RMDBException(e);
            }
            
        }

        // Reviews - full
        if(filters.get("type").equalsIgnoreCase("full"))
        {

            logger.trace("adding user and critic reviews for these movie api's: " + apiList);

            // User Reviews
            try
            {

                query = "select * from (" + MOVIE_LIST_USER_REVIEWS + ") aaa where api_id in (" + apiList + ")";

                PreparedStatement stmt = conn.prepareCall(query);

                logger.trace("getting user review list : " + stmt.toString());
                stmt.execute();
                logger.trace("      ---- time so far: " + (System.currentTimeMillis() - startTime) / 1000. + " sec");

                ResultSet rs = stmt.getResultSet();

                while(rs.next())
                {

                    // Find the movie which matches this api_id
                    String apiId = rs.getString("api_id");
                    String user = rs.getString("user_name");
                    int score = rs.getInt("score");
                    String summary = rs.getString("summary");
                    String review_date = sdf.format(df.parse(rs.getString("review_date")));

                    Movie m = null;

                    for(Iterator<Movie> i  = vm.iterator(); i.hasNext() ; )
                    {
                        Movie im = i.next();
                        if(im.getMovieID().equals(apiId)) m = im;
                    }

                    // If we didn't find a movie for this api_id (should never happen...) then throw an error
                    if(m == null) throw new RMDBException("Found orphan user review - " + apiId + ", " + user);

                    // Now just add this user review
                    UserReview r = new UserReview();

                    r.setAPIID(m.getMovieID());
                    r.setTitle(m.getTitle());
                    r.setUserName(user);
                    r.setScore(score);
                    r.setSummary(summary);
                    r.setReviewDate(review_date);
                    
                    m.user_reviews.add(r);

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

            // Critic Reviews
            try
            {

                query = "select * from (" + MOVIE_LIST_CRITIC_REVIEWS + ") aaa where api_id in (" + apiList + ")";

                PreparedStatement stmt = conn.prepareStatement(query);

                logger.trace("getting critic review list : " + stmt.toString());
                stmt.execute();
                logger.trace("      ---- time so far: " + (System.currentTimeMillis() - startTime) / 1000. + " sec");

                ResultSet rs = stmt.getResultSet();

                while(rs.next())
                {

                    // Find the movie which matches this api_id
                    String apiId = rs.getString("api_id");
                    String cid = rs.getString("cid");
                    String name = rs.getString("name");
                    String publisher = rs.getString("publisher");
                    String link = rs.getString("link");
                    int score = rs.getInt("score");
                    String summary = rs.getString("summary");
                    String review_date = sdf.format(df.parse(rs.getString("review_date")));

                    Movie m = null;

                    for(Iterator<Movie> i  = vm.iterator(); i.hasNext() ; )
                    {
                        Movie im = i.next();
                        if(im.getMovieID().equals(apiId)) m = im;
                    }

                    // If we didn't find a movie for this api_id (should never happen...) then throw an error
                    if(m == null) throw new RMDBException("Found orphan critic review - " + apiId + ", " + cid);

                    // Now just add this user review
                    CriticReview r = new CriticReview();

                    r.setAPIID(m.getMovieID());
                    r.setTitle(m.getTitle());
                    r.setCriticID(cid);
                    r.setName(name);
                    r.setPublisher(publisher);
                    r.setScore(score);
                    r.setSummary(summary);
                    r.setReviewDate(review_date);
                    r.setOutsideLink(link);

                    m.critic_reviews.add(r);

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

            // Set max critic size for the reviews
            for(Iterator<Movie> i = vm.listIterator(); i.hasNext(); )
            {
                Movie m = i.next();
                m.user_reviews.setMaxSize(m.user_reviews.size());
                m.critic_reviews.setMaxSize(m.critic_reviews.size());
            }
            
        }
        
        // Personal critic reviews - per critic
        if(filters.get("type").equalsIgnoreCase("full") && filters.containsKey("user-id"))
        {

            try
            {

                query = "select * from (" + PERSONAL_USER_MOVIE_REVIEWS + ") aa where mid in (" + apiList + ")";

                PreparedStatement stmt = conn.prepareCall(query);
                stmt.setInt(1, Integer.parseInt(filters.get("user-id")));

                logger.trace("getting personal review list : " + stmt.toString());
                stmt.execute();
                logger.trace("      ---- time so far: " + (System.currentTimeMillis() - startTime) / 1000. + " sec");

                ResultSet rs = stmt.getResultSet();

                while(rs.next())
                {

                    // Find the movie which matches this api_id
                    String apiId = rs.getString("mid");
                    String cid = rs.getString("cid");
                    Double conf = rs.getDouble("confidence");

                    Boolean found = false;
                    for(Movie m : vm)
                        if(m.getMovieID().equals(apiId))
                        {
                            for(CriticReview mr : m.critic_reviews)
                                if(mr.getCriticID().equalsIgnoreCase(cid))
                                {
                                    found = true;
                                    mr.setConfidence(conf);
                                }
                            m.critic_reviews.orderByComradery();
                        }

                    // If we didn't find a movie for this api_id (should never happen...) then throw an error
                    if(!found) throw new RMDBException("Found orphan critic confidence - " + apiId + ", " + cid);

                }


            }
            catch(SQLException e)
            {
                logger.debug("exception caught: " + e.toString());
                throw new RMDBException(e);
            }

        }

        return vm;
    
    }

}