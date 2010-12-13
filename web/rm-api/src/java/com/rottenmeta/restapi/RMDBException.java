//Zack Tillotson

package com.rottenmeta.restapi;

import javax.naming.NamingException;
import java.sql.SQLException;
import java.text.ParseException;

public class RMDBException extends Exception
{

    private String message;

    public RMDBException(String m)
    {
        message = m;
    }

    public RMDBException(SQLException e)
    {

        message = "SQLException: " + e.toString();

    }
    
    public RMDBException(NamingException e)
    {
        message = "NamingException: " + e.toString();
    }

    public RMDBException(ParseException e)
    {
        message = "ParseException: " + e.toString();
    }

    @Override
    public String toString()
    {
        return "RMDBException: " + message;
    }

}
