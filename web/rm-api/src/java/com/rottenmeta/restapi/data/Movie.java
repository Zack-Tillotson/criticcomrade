package com.rottenmeta.restapi.data;

import java.util.Vector;

/**
 *
 * @author chloburr
 */

public class Movie extends RMObject
{

    private String title, id,  release_date, studio, rating, rating_reason,
                   summary;
    private int ranking;
    public Vector<String> vgenre, vprole, vdirector, vwriter, vstar;
    public CriticReviewList critic_reviews;
    public UserReviewList user_reviews;

    public Movie()
    {
        vgenre = new Vector<String>();
        vprole = new Vector<String>();
        vdirector = new Vector<String>();
        vwriter = new Vector<String>();
        vstar = new Vector<String>();
        critic_reviews= new CriticReviewList();
        user_reviews = new UserReviewList();
        ranking = 0;
    }

    public void setTitle(String t) {title = t;}
    public String getTitle() { return title; }
    
    public void setMovieID(String i) {id = i;}
    public String getMovieID() { return id; }
    
    public void setSummary(String s) {summary = s;}
    public String getSummary() { return summary; }
    
    public void setReleaseDate(String r) {release_date = r;}
    public String getReleaseDate() { return release_date; }
    
    public void setStudio(String st) {studio = st;}
    public String getStudio() { return studio; }
    
    public void setRating(String rat) {rating  = rat;}
    public String getRating() { return rating; }
    
    public void setRatingReason(String rat_reason) { rating_reason = rat_reason; }
    public String getRatingReason() { return rating_reason; }

    public void setRanking(int rank) { ranking = rank; }
    public int getRanking() { return ranking; }

    @Override
    public String toString()
    {
        return "[movie] " + title;
    }

    @Override
    public String getURL()
    {
        return "movies/id/" + id + "/";
    }

}
