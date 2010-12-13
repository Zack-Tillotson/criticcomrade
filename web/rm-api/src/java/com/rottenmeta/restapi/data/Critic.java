/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rottenmeta.restapi.data;

/**
 *
 * @author chloe
 */
public class Critic extends RMObject
{

    private String critic_id, name, publisher;
    private double u_confidence;
    public CriticReviewList reviews;

    public Critic()
    {
        reviews = new CriticReviewList();
    }

    public void setAPIID(String c) {critic_id = c;}
    public String getAPIID() { return critic_id; }

    public void setName(String n) {name = n;}
    public String getName() { return name; }

    public void setPublisher(String c) {publisher = c;}
    public String getPublisher() { return publisher; }

    public void setConfidence(Double conf) { this.u_confidence = conf; }
    public double getConfidence() { return u_confidence; }

    @Override
    public String toString()
    {
        return "[Critic] " + critic_id;
    }

    @Override
    public String getURL()
    {
        return "critics/id/" + critic_id + "/";
    }
        
}