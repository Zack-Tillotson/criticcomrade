//Zack Tillotson

package com.rottenmeta.restapi;

public class RMInvalidParamException extends Exception
{

    private String message;

    public RMInvalidParamException(String param, String value)
    {

        message = "[" + param + "]: " + value;

    }
    
    public String getIDMessage()
    {
        return message;
    }
    
    @Override
    public String toString()
    {
        return "Invalid Parameter " + message;
    }

}
