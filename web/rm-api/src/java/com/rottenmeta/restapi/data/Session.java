/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rottenmeta.restapi.data;

/**
 *
 * @author chloe
 */
public class Session extends RMObject
{
    
    private String user, pass, sid;
    private boolean longSession;

    public Session()
    {
        user = "";
        pass = "";
        sid = "";
        longSession = false;
    }

    public void setUser(String user) { this.user = user; }
    public String getUser() { return user; }

    public void setPassword(String pass) { this.pass = pass; }
    public String getPassword() { return pass; }

    public void setSessionID(String sid) { this.sid = sid; }
    public String getSessionID() { return sid; }

    public void setIsLongSession(boolean longSession) { this.longSession = longSession; }
    public boolean getIsLongSession() { return longSession; }

    @Override
    public String toString()
    {
        return "[Session] " + sid;
    }

    @Override
    public String getURL()
    {
        return "sessions/id/" + sid + "/";
    }
    
}