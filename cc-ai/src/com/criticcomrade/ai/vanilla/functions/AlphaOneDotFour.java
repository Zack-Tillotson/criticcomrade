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

public class AlphaOneDotFour implements ComraderyFunction, Parameterable
{

    private Logger log = Logger.getLogger(AlphaOneDotFour.class.toString());
    private final int version = 1;

    private HashMap<String, Parameter> params;

    public AlphaOneDotFour()
    {

        params = new HashMap<String, Parameter>();

        Parameter tmp;
        tmp = new Parameter("small-weight", 0, 0, 5, 1, true); params.put(tmp.getName(), tmp);
        tmp = new Parameter("size-cutoff", 25, 10, 50, 1, true); params.put(tmp.getName(), tmp);
        tmp = new Parameter("large-percent-compound", 7, 1, 8, 1, true); params.put(tmp.getName(), tmp);
        tmp = new Parameter("large-percent-weight", 5, 1, 15, 1, true); params.put(tmp.getName(), tmp);
        tmp = new Parameter("large-min", 4, 0, 10, 1, true); params.put(tmp.getName(), tmp);
        tmp = new Parameter("total-compound", 6, 1, 8, 1, true); params.put(tmp.getName(), tmp);
        tmp = new Parameter("total-divide", 29, 10, 300, 1, true); params.put(tmp.getName(), tmp);
        tmp = new Parameter("comraderies-to-keep", 10, 0, 0, 1, true); params.put(tmp.getName(), tmp);
        tmp = new Parameter("suggestion-cutoff", 49, 35, 90, 1, true); params.put(tmp.getName(), tmp);

    }

    public String getFunctionName()
    {
        return AlphaOneDotFour.class.getSimpleName() + "-" + this.version;
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
        activeParams.add(params.get("size-cutoff"));
        activeParams.add(params.get("small-weight"));
        activeParams.add(params.get("large-percent-compound"));
        activeParams.add(params.get("large-percent-weight"));
        activeParams.add(params.get("large-min"));
        activeParams.add(params.get("total-compound"));
        activeParams.add(params.get("total-divide"));
        activeParams.add(params.get("suggestion-cutoff"));
        return activeParams;

    }

    public double getParameter(String name)
    {
        return params.get(name).getValue();
    }
    
    public HashMap<String, Double> calculateComraderies(ReviewerGroups peeps)
    {

        log.info("    Calculating Comraderies");

        // Clean out any old comraderies and suggestions
        peeps.clean();

        // For each user
        for(Reviewer user : peeps.getTestReviewers())
        {

            // For each critic
            for(Reviewer critic : peeps.getActiveReviewers())
            {

                // Make sure the 2 reviewers aren't the same
                if(user.getID().equals(critic.getID())) continue;

                // Calculate the agree count and intersect count
                int S = peeps.reviewerStats.containsKey(user.getID() + "-" + critic.getID() + "-S") ? peeps.reviewerStats.get(user.getID() + "-" + critic.getID() + "-S") : 0;
                int I = peeps.reviewerStats.containsKey(user.getID() + "-" + critic.getID() + "-I") ? peeps.reviewerStats.get(user.getID() + "-" + critic.getID() + "-I") : 0;
                
                if(I == 0) continue;

                // Calculate the comradery for this user + critic
                double P_SCO = params.get("size-cutoff").getValue();
                double P_SW = params.get("small-weight").getValue();
                double P_LPC = params.get("large-percent-compound").getValue();
                double P_LPW = params.get("large-percent-weight").getValue();
                double P_LM = params.get("large-min").getValue();
                double P_TC = params.get("total-compound").getValue();
                double P_TD = params.get("total-divide").getValue();

                peeps.comradery.put(user.getID() + "-" + critic.getID(),
                        Math.pow( (I <= P_SCO) ? 1.0 * S / I * P_SW : Math.pow( 1.0 * S / I, P_LPC) * P_LPW + P_LM, P_TC) / P_TD
                             );

            }

        }

        log.debug("        Count: " + peeps.comradery.size());

        return null;
        
    }
    
    public HashMap<String, Double> calculateCCCPSuggestions(ReviewerGroups peeps)
    {

        log.info("    Calculating CCCP Score");

        int traceSwitch = 0;

        // For each user
        for(Reviewer user : peeps.getActiveReviewers())
        {

            traceSwitch++;

            if(traceSwitch == 1)
            {
                log.trace("            " + user.getID());
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

                    // Make sure we have comradery for this user + critic
                    String comKey = user.getID() + "-" + critic.getID();
                    if(peeps.comradery.containsKey(comKey))
                    {

                        double ucComradery = peeps.comradery.get(comKey);

                        for(Review cRev : critic.getReviews(Reviewer.ALL_PERIOD))
                            if(cRev.movieID == uRev.movieID)
                            {

                                totComradery += ucComradery;
                                totScorePts += ucComradery * cRev.score;

                                sugInputCount++;

                                if(traceSwitch == 1)
                                {
                                    //log.trace("                " + critic.getID() + "-" + ucComradery + "=" + cRev.score);
                                }

                                break;

                            }

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

        return null;

    }

}