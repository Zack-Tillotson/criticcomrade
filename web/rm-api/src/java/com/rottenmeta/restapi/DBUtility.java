/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rottenmeta.restapi;

import java.sql.*;
import javax.naming.*;
import javax.sql.*;
import org.apache.log4j.Logger;

/**
 *
 * @author chloburr
 */
public class DBUtility
{

    static Logger logger = Logger.getLogger(DBUtility.class.toString());

    static Connection conn;

    private static void initConn() throws SQLException, NamingException
    {

        logger.debug("Getting DB Connection");

        logger.trace("getting context");
        Context initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup("java:comp/env");
        logger.trace("got context");

        logger.trace("getting datasource");
        DataSource ds = (DataSource) envCtx.lookup("jdbc/CCDB");
        logger.trace("got datasource");

        logger.trace("getting connection");
        conn = ds.getConnection();
        logger.trace("got connection");
        
    }
    
    public static Connection getDBConnection() throws RMDBException
    {

        try
        {
            if(conn == null || !conn.prepareCall("select 'true'").execute()) initConn();
        }
        catch(Exception e)
        {

            logger.error("Getting DB Connection failed: " + e.toString() + ", getting new connection.");
            try
            {
                initConn();
            }
            catch(SQLException e2)
            {
                logger.error("Getting DB connection failed again: " +e2.toString());
                throw new RMDBException(e2);
            }
            catch(NamingException e2)
            {
                logger.error("Getting DB connection failed again: " +e2.toString());
                throw new RMDBException(e2);
            }

        }
        catch(Error e)
        {
            logger.error("Getting DB Connection failed: " + e.toString() + ", getting new connection.");
            try
            {
                initConn();
            }
            catch(SQLException e2)
            {
                logger.error("Getting DB connection failed again: " +e2.toString());
                throw new RMDBException(e2);
            }
            catch(NamingException e2)
            {
                logger.error("Getting DB connection failed again: " +e2.toString());
                throw new RMDBException(e2);
            }
        }
        
        return conn;

    }

    public static void closeConnection(Connection conn) throws RMDBException
    {
        try
        {
            conn.close();
        }
        catch(SQLException e)
        {
            logger.debug("error closing connection");
            throw new RMDBException(e);
        }
    }

}

