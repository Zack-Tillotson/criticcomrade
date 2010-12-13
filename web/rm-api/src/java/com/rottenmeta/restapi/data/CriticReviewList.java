/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rottenmeta.restapi.data;

/**
 *
 * @author chloburr
 */
public class CriticReviewList extends MetadList<CriticReview>
{

    private int tot_c = -1, pos_c = -1, rec_c = -1;
    private int u_comrade_c = -1, u_agree_c = -1, u_intersect_c = -1;
    private double u_agg_score = -1;
    private int u_comrade_suggestion = 0;

    public void setTotalReviewCount(int tot_c) { this.tot_c = tot_c; }
    public void setPositiveReviewCount(int pos_c) { this.pos_c = pos_c; }
    public void setRecentReviewCount(int rec_c) { this.rec_c = rec_c; }
    public void setUserComradeCount(int u_intersect_c) { this.u_comrade_c = u_intersect_c; }
    public void setUserAgreeCount(int u_agree_c) { this.u_agree_c = u_agree_c; }
    public void setUserIntersectCount(int u_intersect_c) { this.u_intersect_c = u_intersect_c; }
    public void setUserAggregateScore(double u_agg_score)
    {
        this.u_agg_score = u_agg_score;
        if(u_agg_score >= .80) u_comrade_suggestion = 2;
        else if(u_agg_score >= .6) u_comrade_suggestion = 1;
        else if(u_agg_score <= .30) u_comrade_suggestion = -2;
        else u_comrade_suggestion = -1;
    }

    public int getTotalReviewCount() { return tot_c; }
    public int getPositiveReviewCount() { return pos_c; }
    public int getRecentReviewCount() { return rec_c; }
    public int getUserComradeCount() { return u_comrade_c; }
    public int getUserComradeSuggestion() { return u_comrade_suggestion; }
    public int getUserAgreeCount() { return u_agree_c; }
    public int getUserIntersectCount() { return u_intersect_c; }
    public double getUserAggregateScore() { return u_agg_score; }

    public void orderByComradery()
    {

        CriticReviewList list = this;

        // Bubble sort kthx
        for(int i = 0 ; i < list.size() - 1 ; i++)
            for(int j = i + 1 ; j < list.size() ; j++)
                if(list.get(i).getConfidence() < list.get(j).getConfidence())
                {
                    CriticReview tmp = list.get(i);
                    list.setElementAt(list.get(j), i);
                    list.setElementAt(tmp, j);
                }

    }
    
}

