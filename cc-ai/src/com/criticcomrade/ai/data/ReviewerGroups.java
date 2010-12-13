package com.criticcomrade.ai.data;

import com.criticcomrade.ai.vanilla.Parameterable;
import com.criticcomrade.ai.vanilla.functions.RottenTomatoes;
import java.util.Vector;
import java.util.HashMap;
import org.apache.log4j.Logger;

public class ReviewerGroups implements Parameterable
{

    private Logger log = Logger.getLogger(ReviewerGroups.class.toString());

    // Parameters
    private HashMap<String, Parameter> params;

    // Attributes
    private Vector<Reviewer> critics;
    public HashMap<String, Integer> reviewerStats;
    
    public HashMap<String, Double> comradery;
    public HashMap<String, Double> cccpSuggestions;
    
    public HashMap<String, Double> baselineSuggestions;
    
    public ReviewerGroups()
    {

        critics = new Vector<Reviewer>();

        comradery = new HashMap<String, Double>();
        cccpSuggestions = new HashMap<String, Double>();
        reviewerStats = new HashMap<String, Integer>();

        params = new HashMap<String, Parameter>();

        Parameter tmp;
        tmp = new Parameter("test-period-days", 49, 0, 0, 1, false); params.put(tmp.getName(), tmp);
        tmp = new Parameter("test-critic-count", 150, 0, 0, 1, false); params.put(tmp.getName(), tmp);
        tmp = new Parameter("required-critic-reviews", 15, 0, 0, 1, false); params.put(tmp.getName(), tmp);
        tmp = new Parameter("required-user-reviews", 15, 0, 0, 1, false); params.put(tmp.getName(), tmp);

    }

    public ReviewerGroups(ReviewerGroups parent)
    {

        critics = parent.critics;
        reviewerStats = parent.reviewerStats;

        comradery = new HashMap<String, Double>();
        cccpSuggestions = new HashMap<String, Double>();        

        params = new HashMap<String, Parameter>(parent.params);
        
    }

    public Vector<Reviewer> getActiveReviewers()
    {

        Vector<Reviewer> active = new Vector<Reviewer>();

        for(Reviewer rever : critics)
            if(rever.getReviews(Reviewer.BASE_PERIOD).size() >= params.get("required-critic-reviews").getValue() &&
               rever.getReviews(Reviewer.TEST_PERIOD).size() > 0)
                active.add(rever);

        return active;
        
    }

    public Vector<Reviewer> getTestReviewers()
    {
        
        int sizeToReturn = this.params.get("test-critic-count").getValue() > this.getActiveReviewers().size() ?
                                this.getActiveReviewers().size() :
                                (int)(this.params.get("test-critic-count").getValue());

        Vector<Reviewer> testGroup = new Vector<Reviewer>();

        for(Reviewer rever : this.getActiveReviewers())
            if(rever.getID().startsWith("U") && testGroup.size() < sizeToReturn)
                testGroup.add(rever);

        return testGroup;
    }

    public void addReview(String id, Review cRev)
    {

        boolean found = false;
        
        for(Reviewer rever : critics)
            if(rever.getID().equals(id))
            {
                rever.addReview(cRev);
                found = true;
            }

        if(!found)
        {
            Reviewer newRever = new Reviewer(id);
            newRever.addReview(cRev);
            newRever.setTestPeriod((int)params.get("test-period-days").getValue());
            critics.add(newRever);
        }

    }

    public double getParameter(String name)
    {
        return params.get(name).getValue();
    }

    public Vector<Parameter> getAllParameters()
    {
        Vector<Parameter> allParams = new Vector<Parameter>();
        for(String key : params.keySet())
            allParams.add(params.get(key));
        return allParams;
    }

    public Vector<Parameter> getActiveParameters()
    {
        return new Vector<Parameter>();        
    }

    public void clean()
    {

        comradery = new HashMap<String, Double>();
        cccpSuggestions = new HashMap<String, Double>();

    }

    public void calculateStats()
    {

        log.trace("Calculating Stats for Reviewers");

        int trace = 0;

        for(Reviewer user : this.getTestReviewers())
        {
            trace++;
            for(Reviewer critic : this.getActiveReviewers())
            {

                if(user.getID().equals(critic.getID())) continue;

                // Calculate the agree count and intersect count
                int S = 0;
                int I = 0;
                for(Review uRev : user.getReviews(Reviewer.BASE_PERIOD))
                    for(Review cRev : critic.getReviews(Reviewer.BASE_PERIOD))
                        if(uRev.movieID == cRev.movieID)
                        {
                            I++;
                            if(uRev.score == cRev.score) S++;
                            break;
                        }

                 reviewerStats.put(user.getID() + "-" + critic.getID() + "-I", I);
                 reviewerStats.put(critic.getID() + "-" + user.getID() + "-I", I);

                 reviewerStats.put(user.getID() + "-" + critic.getID() + "-S", S);
                 reviewerStats.put(critic.getID() + "-" + user.getID() + "-S", S);

/*
                 if(trace == 1)
                 {
                     log.trace(user.getID() + "-" + critic.getID() + " : S/I = " + S + "/" + I + "(" + (int)(100.*S/I) + "%)");
                 }
*/

            }

        }

        log.trace("Done with stat calculations");

    }

    public double getTestAgreeFraction()
    {

        if(baselineSuggestions == null)
        {
            log.debug("   Calculating old style of suggestions");
            baselineSuggestions = (new RottenTomatoes()).calculateCCCPSuggestions(new ReviewerGroups(this));
        }

        int agree = 0;
        int intersect = 0;

        log.debug("    Calculating correctness of suggestions");

        int traceRever = 0;

        int differentCount = 0;
        int suggestionChangeGood = 0;

        for(Reviewer rever : this.getTestReviewers())
        {

            int userAgree = 0;
            int userIntersect = 0;

            traceRever++;

            if(rever.getID().equals("U33")) traceRever = 1;
            else traceRever = 0;

            if(traceRever == 1)
            {
                log.trace("        " + rever.getID());
            }

            for(Review r : rever.getReviews(Reviewer.TEST_PERIOD))
            {

                if(cccpSuggestions.containsKey(rever.getID() + "-" + r.movieID))
                {

                    userIntersect++;

                    boolean suggestion = cccpSuggestions.get(rever.getID() + "-" + r.movieID) == 1;
                    boolean userLiked = r.score == 1;
                    boolean baselineSuggestion = baselineSuggestions.get(rever.getID() + "-" + r.movieID) == 1;

                    if(suggestion == userLiked) userAgree++;

                    if(traceRever == 1)
                    {
                        log.trace("            " + (suggestion == userLiked ? " " : "-") + " [" + r.movieID + "] Real: " + r.score + ", Suggestion: " + cccpSuggestions.get(rever.getID() + "-" + r.movieID) + (baselineSuggestion != suggestion ? " [Baseline difference! " + (baselineSuggestion == userLiked ? "worse" : "better") + "]" : ""));
                    }

                    if(baselineSuggestion != suggestion)
                    {

                        differentCount++;
                        if(suggestion == userLiked) suggestionChangeGood++;

                    }
                      
                }
                
            }

            if(traceRever == 1)
            {
                log.trace("                " + userAgree + " / " + userIntersect + " = " + 1.0 * userAgree / userIntersect);
            }

            agree += userAgree;
            intersect += userIntersect;
            
        }

        log.debug("        Correct : " + (int)(1000. * agree / intersect) / 10. + "% [" + agree + "/" + intersect + "] [Different: " + suggestionChangeGood + "/" + differentCount + "]");

        return 1.0 * agree / intersect;
        
    }

}