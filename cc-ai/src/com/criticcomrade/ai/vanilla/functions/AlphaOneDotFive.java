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

public class AlphaOneDotFive implements ComraderyFunction, Parameterable
{

    private Logger log = Logger.getLogger(AlphaOneDotFive.class.toString());
    private final int version = 1;

    private HashMap<String, Parameter> params;

    public AlphaOneDotFive()
    {

        params = new HashMap<String, Parameter>();

        Parameter tmp;
        tmp = new Parameter("size-cutoff", 24, 10, 50, .1, true); params.put(tmp.getName(), tmp);
        tmp = new Parameter("large-percent-compound", 3, 1, 15, .1, true); params.put(tmp.getName(), tmp);
        tmp = new Parameter("large-percent-weight", 10, 5, 25, .1, true); params.put(tmp.getName(), tmp);
        tmp = new Parameter("large-min", 8, 0, 15, .1, true); params.put(tmp.getName(), tmp);
        tmp = new Parameter("total-compound", 9, 1, 15, .1, true); params.put(tmp.getName(), tmp);
        tmp = new Parameter("suggestion-cutoff", 49, 35, 90, .1, true); params.put(tmp.getName(), tmp);

    }

    public String getFunctionName()
    {
        return AlphaOneDotFive.class.getSimpleName() + "-" + this.version;
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
        activeParams.add(params.get("large-percent-compound"));
        activeParams.add(params.get("large-percent-weight"));
        activeParams.add(params.get("large-min"));
        activeParams.add(params.get("total-compound"));
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

        int trace = 0;

        // For each user
        for(Reviewer user : peeps.getTestReviewers())
        {

            trace++;

            if(trace == 1)
            {
                log.trace("                " + user.getID());
            }

            double minCom = Double.MAX_VALUE;

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
                double P_LPC = params.get("large-percent-compound").getValue();
                double P_LPW = params.get("large-percent-weight").getValue();
                double P_LM = params.get("large-min").getValue();
                double P_TC = params.get("total-compound").getValue();

                double C = Math.pow( (I < P_SCO) ? 0 : Math.pow( 1.0 * S / I, P_LPC) * P_LPW + P_LM, P_TC);

                if(C < minCom && C > 0) minCom = C;

                peeps.comradery.put(user.getID() + "-" + critic.getID(), C);

            }

            // Adjust all of the comraderies to be a lot smaller t.t
            for(Reviewer critic : peeps.getActiveReviewers())
            {

                if(!peeps.comradery.containsKey(user.getID() + "-" + critic.getID())) continue;

                peeps.comradery.put(user.getID() + "-" + critic.getID(), peeps.comradery.get(user.getID() + "-" + critic.getID())/minCom);

                int S = peeps.reviewerStats.containsKey(user.getID() + "-" + critic.getID() + "-S") ? peeps.reviewerStats.get(user.getID() + "-" + critic.getID() + "-S") : 0;
                int I = peeps.reviewerStats.containsKey(user.getID() + "-" + critic.getID() + "-I") ? peeps.reviewerStats.get(user.getID() + "-" + critic.getID() + "-I") : 0;

                if(trace == 1)
                {
                    log.trace("                    [" + critic.getID() + "] " + S + "/" + I + " (" + (int)(100.*S/I) + "%) = " +
                                                   (int)(100*peeps.comradery.get(user.getID() + "-" + critic.getID()))/100.);
                }

            }

        }

        log.debug("        Count: " + peeps.comradery.size());

        return null;
        
    }
    
    public HashMap<String, Double> calculateCCCPSuggestions(ReviewerGroups peeps)
    {

        log.info("    Calculating CCCP Score");

        int traceSwitch = 0;

        int sugPassCount = 0;

        // For each user
        for(Reviewer user : peeps.getTestReviewers())
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
                int criticsWhoHaveReviewedMovieCount = 0;

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

                                criticsWhoHaveReviewedMovieCount++;
                                break;

                            }

                    }

                }

                // Save the suggestion
                if(totComradery != 0)
                {

                    if(traceSwitch == 1)
                    {
                        log.trace("                [" + uRev.movieID + "] Count: "+ sugInputCount + " - " + totScorePts + " / " + totComradery + " = " + totScorePts / totComradery);
                    }

                    double P_SC = params.get("suggestion-cutoff").getValue();
                    peeps.cccpSuggestions.put(user.getID() + "-" + uRev.movieID, totScorePts / totComradery >= P_SC / 100. ? 1. : 0.);

                }
                else
                {
                    if(traceSwitch == 1)
                        log.trace("                    [" + uRev.movieID + "] No suggestion - number of critics contributing: " + criticsWhoHaveReviewedMovieCount);
                    sugPassCount++;
                }

            }

        }

        log.debug("        Count: " + peeps.cccpSuggestions.size() + " [Passed: " + sugPassCount + "]");

        return null;

    }

}