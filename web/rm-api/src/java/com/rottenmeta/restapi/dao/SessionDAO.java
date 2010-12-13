package com.rottenmeta.restapi.dao;

import java.sql.*;
import com.rottenmeta.restapi.*;
import com.rottenmeta.restapi.data.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.apache.log4j.Logger;
import com.rottenmeta.restapi.RMInvalidIDException;
import java.text.ParseException;

/**
 * @author chloburr
 */
public class SessionDAO
{

    static Logger logger = Logger.getLogger(SessionDAO.class.getName());

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.0");
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

    private Connection conn;

    public static final int REAL_STATE = 3;
    public static final int TRIAL_STATE = 2;
    public static final int GUEST_STATE = 1;
    public static final int INVALID_STATE = 0;

    public SessionDAO() throws RMDBException
    {
        conn = DBUtility.getDBConnection();
    }

    private final String GET_IS_SESSION_VALID =
        "select 'true' is_valid from web_sessions where session_id = ? and ((expires = 1 and activity > subdate(now(), interval 15 minute)) or expires = 0)";

    private final String GET_USER_INFO_FROM_SID =
        " select case when s.user_id != 0 then 'true' else 'false' end is_user, u.user_id, u.user_name, u.create_date, u.first_name, u.last_name, u.email, u.sex, u.age, ur.rev_c, ur.rec_c, ur.pos_c, s.expires" +
        " from web_sessions s left join users u on s.user_id = u.user_id left join (select user_id, count(*) rev_c, sum(case when review_date > subdate(now(), interval 2 week) then 1 else 0 end) rec_c, sum(case when score > (select cutoff from review_pos_cutoffs where source = 'user') then 1 else 0 end) pos_c from u_reviews group by user_id) ur on s.user_id = ur.user_id" +
        " where s.session_id = ? and ((expires = 1 and activity > subdate(now(), interval 15 minute)) or expires = 0)";

    public User getUserInfoFromSID(String sid)  throws RMDBException, RMInvalidIDException
    {

        try
        {

            PreparedStatement stmt = conn.prepareStatement(GET_USER_INFO_FROM_SID);
            stmt.setString(1, sid);
            logger.trace("getting user info from sid: " + stmt.toString());

            ResultSet rs;
            rs = stmt.executeQuery();
            
            // Not a logged in SID
            if(!rs.next() || rs.isAfterLast() || rs.getString("is_user").equalsIgnoreCase("false"))
            {

                logger.trace("not a logged in session");

                // Check to see if the SID is valid at all
                stmt = conn.prepareStatement(GET_IS_SESSION_VALID);
                stmt.setString(1, sid);
                logger.trace("checking to see if the SID is valid: " + stmt.toString());

                rs = stmt.executeQuery();

                if(!rs.next()) throw new RMInvalidIDException(sid, "Session");
                else return null;
                
            }
            else
            {

                String joinDate = rs.getString("create_date");
                if(joinDate != null) joinDate = sdf.format(df.parse(joinDate));

                String is_user = rs.getString("is_user");
                String name = rs.getString("user_name");
                int userID = rs.getInt("user_id");
                String create = joinDate;
                String firstn = rs.getString("first_name");
                String lastn = rs.getString("last_name");
                String email = rs.getString("email");
                String sex = rs.getString("sex");
                String age = rs.getString("age");
                int revC = rs.getInt("rev_c");
                int recC = rs.getInt("rec_c");
                int posC = rs.getInt("pos_c");
                boolean longSession = (rs.getInt("expires") == 0);

                User u = new User();
                u.setUserID(userID);
                u.setUserName(name);
                u.setCreateDate(create);
                u.setFirstName(firstn);
                u.setLastName(lastn);
                u.setEmail(email);
                u.setSex(sex);
                u.setAge(age);
                u.reviews.setTotalReviewCount(revC);
                u.reviews.setRecentReviewCount(recC);
                u.reviews.setPositiveReviewCount(posC);
                u.setIsLongSession(longSession);

                logger.debug("user gotten from db is " + u);

                if(is_user.equalsIgnoreCase("false")) return null;
                else return u;

            }

        }
        catch(SQLException e)
        {
            logger.debug("caught exception: " + e);
            throw new RMDBException(e);
        }
        catch(ParseException e)
        {
            logger.debug("caught exception: " + e);
            throw new RMDBException(e);
        }

    }
    
    private final String GET_USER_ID_FROM_USERNAME_AND_PASS =
            "select user_id from users where user_name = ? and password = ?";

    private final String DELETE_OLD_SESSIONS =
            "delete from web_sessions where session_id = ? or (expires = 1 and activity <= subdate(now(), interval 15 minute))";

    private final String ADD_NEW_SESSION =
            "insert into web_sessions (session_id, user_id, expires) values (?, ?, ?)";

    public boolean logInUser(String sid, String username, String password, Boolean longSession) throws RMDBException, RMInvalidIDException
    {

        try
        {

            PreparedStatement pstmt = conn.prepareStatement(GET_USER_ID_FROM_USERNAME_AND_PASS);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            logger.trace("getting user id from un/pw: " + pstmt.toString());

            ResultSet rs = pstmt.executeQuery();

            int userID;
            try
            {
                rs.next();
                String userIDString = rs.getString("user_id");
                userID = Integer.parseInt(userIDString);
            }
            catch(Exception e)
            {
                logger.trace("not a valid id: " + e);
                throw new RMInvalidIDException(username, "Username and Password");
            }

            return startUserSession(sid, userID, longSession);

        }
        catch(SQLException e)
        {
            logger.debug(e);
            throw new RMDBException(e);
        }

    }

    private final String CREATE_NEW_TRIAL_USER =
            "insert into users () values ()";

    private final String GET_LAST_INSERT_ID =
            "select last_insert_id() id";

    public boolean logInTrialUser(String sid) throws RMDBException, RMInvalidIDException
    {

        try
        {

            PreparedStatement pstmt = conn.prepareStatement(CREATE_NEW_TRIAL_USER);
            logger.trace("creating new trial user: " + pstmt.toString());
            
            pstmt.execute();

            pstmt = conn.prepareStatement(GET_LAST_INSERT_ID);
            logger.trace("getting trial user id: " + pstmt.toString());
            
            ResultSet rs = pstmt.executeQuery();

            rs.next();
            String userIDString = rs.getString("id");
            int userID = userID = Integer.parseInt(userIDString);

            return startUserSession(sid, userID, false);

        }
        catch(SQLException e)
        {
            logger.debug(e);
            throw new RMDBException(e);
        }
   
    }

    private boolean startUserSession(String sid, int userID, Boolean longSession) throws RMDBException, RMInvalidIDException
    {

        try
        {

            PreparedStatement pstmt = conn.prepareStatement(DELETE_OLD_SESSIONS);
            pstmt.setString(1, sid);
            logger.trace("deleting old session: " + pstmt.toString());

            pstmt.execute();

            pstmt = conn.prepareStatement(ADD_NEW_SESSION);
            pstmt.setString(1, sid);
            pstmt.setInt(2, userID);
            pstmt.setInt(3, longSession ? 0 : 1);
            logger.trace("adding the new session: " + pstmt.toString());

            pstmt.execute();

            return true;
            
        }
        catch(SQLException e)
        {
            logger.debug(e);
            throw new RMDBException(e);
        }
        
    }

    public boolean logOutUser(String sid) throws RMDBException
    {

        try
        {

            PreparedStatement pstmt = conn.prepareStatement(DELETE_OLD_SESSIONS);
            pstmt.setString(1, sid);

            logger.trace("logging out user: " + pstmt.toString());

            return pstmt.execute();

        }
        catch(SQLException e)
        {
            logger.debug(e);
            throw new RMDBException(e);
        }
        
    }
    
    private final String GET_SESSION_STATE =
        "select case when user_id = 0 then 'guest' when (select user_name from users where user_id = s.user_id) is null then 'trial' else 'real' end state from web_sessions s where session_id = ? and ((expires = 1 and activity > subdate(now(), interval 15 minute)) or expires = 0)";

    public int getSessionState(String sid) throws RMDBException
    {

        try
        {

            PreparedStatement stmt = conn.prepareStatement(GET_SESSION_STATE);
            stmt.setString(1, sid);

            logger.trace("checking session state: " + stmt.toString());

            ResultSet rs;
            rs = stmt.executeQuery();

            try
            {
                rs.next();

                if(rs.getString("state").equalsIgnoreCase("guest")) return SessionDAO.GUEST_STATE;
                else if(rs.getString("state").equalsIgnoreCase("trial")) return SessionDAO.TRIAL_STATE;
                else if(rs.getString("state").equalsIgnoreCase("real")) return SessionDAO.REAL_STATE;
                else return SessionDAO.INVALID_STATE;

            }
            catch(SQLException e)
            {
                return SessionDAO.INVALID_STATE;       // Not a correct sid
            }

        }
        catch(SQLException e)
        {
            
            logger.debug(e);
            throw new RMDBException(e);
            
        }

    }
    
    public boolean startGuestSession(String sid) throws RMDBException
    {

        try
        {

            // Clean old sessions
            PreparedStatement stmt = conn.prepareStatement(DELETE_OLD_SESSIONS);
            stmt.setString(1, sid);

            logger.trace("cleaning web sesions: " + stmt.toString());

            stmt.execute();

            // Start this guest session
            stmt = conn.prepareStatement(ADD_NEW_SESSION);
            stmt.setString(1, sid);
            stmt.setInt(2, 0);
            stmt.setInt(3, 1);

            logger.trace("starting guest session: " + stmt.toString());

            return !stmt.execute();

        }
        catch(SQLException e)
        {
            logger.debug(e);
            throw new RMDBException(e);
        }
        
    }

    private final String REFRESH_SESSION_TIMER =
        "update web_sessions set activity = now() where session_id = ?";

    public boolean refreshSession(String sid) throws RMDBException
    {

        try
        {

            PreparedStatement stmt = conn.prepareStatement(REFRESH_SESSION_TIMER);
            stmt.setString(1, sid);

            logger.trace("refreshing timer: " + stmt.toString());

            return stmt.execute();

        }
        catch(SQLException e)
        {
            logger.debug(e);
            throw new RMDBException(e);
        }
        
    }
}