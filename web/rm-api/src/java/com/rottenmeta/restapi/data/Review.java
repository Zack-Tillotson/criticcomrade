/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rottenmeta.restapi.data;

/**
 *
 * @author chloburr
 */
public class Review extends RMObject
{

    private String title, api_id, summary, review_date, outside_link;
    private int score = -1;

    public void setSummary(String sum) {summary = sum;}
    public String getSummary() { return summary; }

    public void setScore(int s) {score = s;}
    public int getScore() { return score; }
    
    public void setReviewDate(String s) {review_date = s;}
    public String getReviewDate() { return review_date; }
    
    public void setOutsideLink(String s) {outside_link = s;}
    public String getOutsideLink() { return outside_link; }

    public void setAPIID(String s) {api_id = s;}
    public String getAPIID() { return api_id; }

    public void setTitle(String s) {title = s;}
    public String getTitle() { return title; }

    @Override
    public String toString()
    {
        return "[review] " + api_id;
    }

}

