/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rottenmeta.restapi.data;

/**
 *
 * @author chloburr
 */
public class PersonalCriticReview extends CriticReview
{

    String sid;

    public void setSessionID(String sid) { this.sid = sid; }
    public String getSessionID() { return sid; }

    @Override
    public String getURL()
    {
        return "critics/id/" + this.getCriticID() + "/reviews/" + this.getAPIID() + "/sid/" + this.getSessionID() + "/";
    }

}

