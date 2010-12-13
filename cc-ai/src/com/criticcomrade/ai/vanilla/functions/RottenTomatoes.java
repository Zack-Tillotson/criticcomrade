package com.criticcomrade.ai.vanilla.functions;

import java.util.Vector;
import java.util.HashMap;
import com.criticcomrade.ai.vanilla.ComraderyFunction;
import com.criticcomrade.ai.vanilla.Parameterable;
import com.criticcomrade.ai.data.Parameter;
import com.criticcomrade.ai.data.Reviewer;
import com.criticcomrade.ai.data.Review;
import com.criticcomrade.ai.data.ReviewerGroups;
import org.apache.log4j.Logger;

public class RottenTomatoes implements ComraderyFunction, Parameterable
{

    private Logger log = Logger.getLogger(RottenTomatoes.class.toString());
    private final int version = 1;

    private HashMap<String, Parameter> params;

    public RottenTomatoes()
    {

        params = new HashMap<String, Parameter>();
        Parameter tmp = new Parameter("suggestion-cutoff", 49, 0, 100, 1, true); params.put(tmp.getName(), tmp);

    }

    public String getFunctionName()
    {
        return RottenTomatoes.class.getSimpleName() + "-" + this.version;
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

        Vector<Parameter> activeParams = new Vector<Parameter>();
        for(String key : params.keySet())
            if(params.get(key).getIsActive())
                activeParams.add(params.get(key));
        return activeParams;

    }

    public double getParameter(String name)
    {
        return params.get(name).getValue();
    }
    
    public HashMap<String, Double> calculateComraderies(ReviewerGroups peeps)
    {

        log.info("    Calculating Comraderies");
        return null;

    }
    
    public HashMap<String, Double> calculateCCCPSuggestions(ReviewerGroups peeps)
    {

        log.info("    Calculating CCCP Score");

        int traceSwitch = 0;

        // For each user
        for(Reviewer user : peeps.getTestReviewers())
        {

            traceSwitch++;

            if(traceSwitch == 1)
            {
                log.trace("        " + user.getID());
            }

            // For each movie the users have a review for
            for(Review uRev : user.getReviews(Reviewer.TEST_PERIOD))
            {

                // Sum comradery and comradery * score for each critic
                double totComradery = 0;
                double totScorePts = 0;

                int sugInputCount = 0;

                for(Reviewer critic : peeps.getActiveReviewers())
                {

                    // Make sure the 2 reviewers aren't the same
                    if(user.getID().equals(critic.getID())) continue;

                    // All comraderies are 1 in RottenTomatoes
                    double ucComradery = 1.;

                    for(Review cRev : critic.getReviews(Reviewer.ALL_PERIOD))
                        if(cRev.movieID == uRev.movieID)
                        {

                            totComradery += ucComradery;
                            totScorePts += ucComradery * cRev.score;

                            sugInputCount++;

                            break;

                        }
            
                }

                if(traceSwitch == 1)
                {
                    log.trace("                [" + uRev.movieID + "] Count: "+ sugInputCount + " - " + totScorePts + " / " + totComradery + " = " + totScorePts / totComradery);
                }

                // Save the suggestion
                if(totComradery != 0)
                {

                    double P_SC = params.get("suggestion-cutoff").getValue();
                    
                    peeps.cccpSuggestions.put(user.getID() + "-" + uRev.movieID, totScorePts / totComradery >= P_SC / 100. ? 1. : 0.);

                }

            }

        }

        log.debug("        Count: " + peeps.cccpSuggestions.size());

        return (HashMap<String, Double>)peeps.cccpSuggestions.clone();

    }

}