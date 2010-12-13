//Zack Tillotson

package com.rottenmeta.restapi;

public class RMInvalidIDException extends Exception
{

    private String message;

    public RMInvalidIDException(String id, String objectType)
    {

        message = "[" + objectType + "]: '" + id + "'";

    }
    
    public String getIDMessage()
    {
        return message;
    }
    
    @Override
    public String toString()
    {
        return "Invalid ID " + message;
    }

}
