//Zack Tillotson

package com.rottenmeta.restapi;

public class RMInvalidCredentialsException extends Exception
{

    private String message;

    public RMInvalidCredentialsException()
    {
        message = "Invalid username and/or password";
    }
    
    public String getIDMessage()
    {
        return message;
    }
    
    @Override
    public String toString()
    {
        return "Invalid Credentials: " + message;
    }

}
