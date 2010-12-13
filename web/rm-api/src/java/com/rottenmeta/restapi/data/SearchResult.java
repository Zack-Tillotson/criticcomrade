/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rottenmeta.restapi.data;

/**
 *
 * @author chloe
 */
public class SearchResult extends RMObject
{

    private Movie movieResult;
    private Critic criticResult;
    private String resultType;
    private int resultLength;

    public SearchResult(String resultType, Object result, int resultLength)
    {
        this.resultType = resultType;
        this.movieResult = resultType.equals("Movie") ? (Movie)result : null;
        this.criticResult = resultType.equals("Critic") ? (Critic)result : null;
        this.resultLength = resultLength;
    }

    public String getResultType() { return this.resultType; }

    public Movie getResultMovie() { return this.movieResult; }
    public Critic getResultCritic() { return this.criticResult; }

    public int getResultLength() { return this.resultLength; }

    @Override
    public String toString()
    {
        return "[SearchResult] (" + resultType + ") " + (movieResult == null ? criticResult == null ? "-" : criticResult : movieResult);
    }

    @Override
    public String getURL()
    {
        return (movieResult == null ? criticResult == null ? "search/" : criticResult.getURL() : movieResult.getURL());
    }
        
}