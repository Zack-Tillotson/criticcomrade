package com.criticcomrade.ai.data;

public class Review
{

    // Attributes
    public int movieID;
    public int score;
    public int daysAgo;

    public Review(int movieID, int score, int daysAgo)
    {
        this.movieID = movieID;
        this.score = score;
        this.daysAgo = daysAgo;
    }

}
