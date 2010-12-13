/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rottenmeta.restapi.data;

/**
 *
 * @author chloe
 */
public class User extends RMObject
{
    
    private String user_name, create_date, first_name, last_name, email, sex, 
                   age, password;
    private int user_id;
    private boolean longSession = false;
    public UserReviewList reviews;

    public User()
    {
        reviews = new UserReviewList();
        user_id = -1;
    }

    public void setUserID(int u) { user_id = u; }
    public int getUserID() { return user_id; }
    
    public void setUserName(String u) {user_name = u;}
    public String getUserName() { return user_name; }

    public void setPassword(String u) {password = u;}
    public String getPassword() { return password; }

    public void setCreateDate(String c) {create_date = c;}
    public String getCreateDate() { return create_date; }

    public void setFirstName(String c) { first_name = c;}
    public String getFirstName() { return first_name; }

    public void setLastName(String c) {last_name = c;}
    public String getLastName() { return last_name; }

    public void setEmail(String c) {email = c;}
    public String getEmail() { return email; }

    public void setSex(String c) {sex = c;}
    public String getSex() { return sex; }

    public void setAge(String c) {age = c;}
    public String getAge() { return age; }

    public void setIsLongSession(boolean isLong) { this.longSession = isLong;}
    public boolean getIsLongSession() { return this.longSession; }

    @Override
    public String toString()
    {
        return "[User] " + user_name;
    }

    @Override
    public String getURL()
    {
        return "users/id/" + user_name + "/";
    }
    
}