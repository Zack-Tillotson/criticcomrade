/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rottenmeta.restapi.data;

/**
 *
 * @author chloburr
 */

public class PersonalMovie extends RMObject
{

    String sid;
    String mid;
    Double score = 0.;
    MetadList<PersonalCriticReview> revs;


    public PersonalMovie()
    {
        revs = new MetadList<PersonalCriticReview>();
    }

    @Override
    public String toString()
    {
        return "[PersonalMovie] " + mid + " - " + sid;
    }

    public void setSessionID(String sid) { this.sid = sid; }
    public String getSessionID() { return sid; }

    public void setMovieID(String mid) { this.mid = mid; }
    public String getMovieID() { return mid; }

    public String getAggregateScore()
    {
        return ""+(int)(1000*score);
    }

    public MetadList<PersonalCriticReview> getCriticReviews()
    {
        return revs;
    }

    public void addCriticReview(PersonalCriticReview c)
    {
        if(c != null && c.getConfidence() > Double.MAX_VALUE * -1 && (c.getScore() == 0 || c.getScore() == 1))
        {
            score = (score * revs.size() + c.getScore() * c.getConfidence())/(revs.size() + 1);
            revs.add(c);

        }
    }

    @Override
    public String getURL()
    {
        return "movies/id/" + mid + "/sid/" + sid + "/";
    }

    public static MetadList<PersonalMovie> parsePersonalMovieList(MetadList<Movie> movs, String sid)
    {

        MetadList<PersonalMovie> pmovs = new MetadList<PersonalMovie>();
        pmovs.setMaxSize(movs.getMaxSize());
        pmovs.setOffset(movs.getOffset());
        pmovs.setOrderBy(movs.getOrderBy());
        pmovs.setURL("movies/all/sid/" + sid + "/");
        
        for(Movie mov : movs)
        {
            PersonalMovie pm = new PersonalMovie();
            pm.setMovieID(mov.getMovieID());
            pm.setSessionID(sid);
            pmovs.add(pm);
        }

        return pmovs;
    }

}
