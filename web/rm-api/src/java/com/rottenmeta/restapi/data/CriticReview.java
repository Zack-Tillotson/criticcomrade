/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rottenmeta.restapi.data;

/**
 *
 * @author chloburr
 */
public class CriticReview extends Review
{

    private String cid, name, publisher;
    private double conf = -1.;

    public void setCriticID(String a) {cid = a;}
    public String getCriticID() { return cid; }

    public void setName(String a) {name = a;}
    public String getName() { return name; }

    public void setPublisher(String a) {publisher = a;}
    public String getPublisher() { return publisher; }

    public void setConfidence(double conf) { this.conf = conf; }
    public double getConfidence() { return conf; }

    @Override
    public String getURL()
    {
        return "critics/id/" + this.cid + "/reviews/" + this.getAPIID() + "/";
    }

}

