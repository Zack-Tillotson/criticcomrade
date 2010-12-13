/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rottenmeta.restapi.data;

/**
 *
 * @author chloburr
 */
public class UserReview extends Review
{

    private String username;

    public void setUserName(String a) {username = a;}
    public String getUserName() { return username; }

    @Override
    public String getURL()
    {
        return "users/id/" + this.username + "/reviews/" + this.getAPIID() + "/";
    }

}

