/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rottenmeta.restapi.data;

/**
 *
 * @author chloburr
 */
public class UserReviewList extends MetadList<UserReview>
{

    private int tot_c, pos_c, rec_c;

    public void setTotalReviewCount(int tot_c) { this.tot_c = tot_c; }
    public void setPositiveReviewCount(int pos_c) { this.pos_c = pos_c; }
    public void setRecentReviewCount(int rec_c) { this.rec_c = rec_c; }

    public int getTotalReviewCount() { return tot_c; }
    public int getPositiveReviewCount() { return pos_c; }
    public int getRecentReviewCount() { return rec_c; }

    public void clearBadReviews()
    {
        for(int i = 0 ; i < this.size() ; i++)
            if(this.get(i).getAPIID() == null || this.get(i).getScore() < 0)
                this.remove(i--);
    }
    
}

