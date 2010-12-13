package com.criticcomrade.ai.data;

import java.util.Vector;

public class Reviewer
{

    public static int BASE_PERIOD = 1;
    public static int TEST_PERIOD = 2;
    public static int ALL_PERIOD  = 3;

    // Attributes
    private String reviewerID;
    private Vector<Review> reviews;
    private int testPeriod;

    public Reviewer(String reviewerID)
    {
        this.reviewerID = reviewerID;
        reviews = new Vector<Review>();
        testPeriod = 21;
    }

    public void addReview(Review rev)
    {
        reviews.add(rev);
    }

    public String  getID()
    {
        return reviewerID;
    }

    public void setTestPeriod(int testPeriod)
    {
        this.testPeriod = testPeriod;
    }

    public Vector<Review> getReviews(int period)
    {

        Vector<Review> activeReviews = new Vector<Review>();

        for(Review rev : reviews)
            if( (period == Reviewer.TEST_PERIOD && rev.daysAgo <= testPeriod) ||
                (period == Reviewer.BASE_PERIOD && rev.daysAgo > testPeriod)  ||
                (period == Reviewer.ALL_PERIOD)
              )
                activeReviews.add(rev);

        return activeReviews;
        
    }

    public boolean hasReview(int mid)
    {
        for(Review revs : reviews)
            if(revs.movieID == mid) return true;
        return false;
    }

}
