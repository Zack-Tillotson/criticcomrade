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

public class AlphaOneDotFiveTwo implements ComraderyFunction, Parameterable
{

    private Logger log = Logger.getLogger(AlphaOneDotFiveTwo.class.toString());
    private final int version = 2;

    private HashMap<String, Parameter> params;

    public AlphaOneDotFiveTwo()
    {

        params = new HashMap<String, Parameter>();

        Parameter tmp;
        tmp = new Parameter("suggestion-cutoff", .5, 0, 1, .01, true); params.put(tmp.getName(), tmp);
        tmp = new Parameter("percent-compound", 1, 0, 25, .1, true); params.put(tmp.getName(), tmp);
        tmp = new Parameter("ideal-min-comradery", 0, 0, 0, 0, false); params.put(tmp.getName(), tmp);
        tmp = new Parameter("ideal-max-comradery", 1, 0, 0, 0, false); params.put(tmp.getName(), tmp);

    }

    public String getFunctionName()
    {
        return AlphaOneDotFiveTwo.class.getSimpleName() + "-" + this.version;
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

        // Clean out any old comraderies and suggestions
        peeps.clean();

        int trace = 0;

        // For each user
        for(Reviewer user : peeps.getTestReviewers())
        {

            trace++;

            if(user.getID().equals("U33")) trace = 1;
            else trace = 0;

            if(trace == 1)
            {
                log.trace("                " + user.getID());
            }

            double minCom = Double.MAX_VALUE;
            double maxCom = Double.MIN_VALUE;

            double idealMinCom = this.getParameter("ideal-min-comradery");
            double idealMaxCom = this.getParameter("ideal-max-comradery");

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
                double P_PC = params.get("percent-compound").getValue();

                double C = Math.pow(1.0*S/I, P_PC) * 100;

                if(C < minCom) minCom = C;
                if(C > maxCom) maxCom = C;

                peeps.comradery.put(user.getID() + "-" + critic.getID(), C);

            }

            // Adjust all of the comraderies to be normallized
            for(Reviewer critic : peeps.getActiveReviewers())
            {

                if(!peeps.comradery.containsKey(user.getID() + "-" + critic.getID())) continue;

                peeps.comradery.put(user.getID() + "-" + critic.getID(), (peeps.comradery.get(user.getID() + "-" + critic.getID())-minCom)/(maxCom-minCom)*(idealMaxCom - idealMinCom) + idealMinCom);

                int S = peeps.reviewerStats.containsKey(user.getID() + "-" + critic.getID() + "-S") ? peeps.reviewerStats.get(user.getID() + "-" + critic.getID() + "-S") : 0;
                int I = peeps.reviewerStats.containsKey(user.getID() + "-" + critic.getID() + "-I") ? peeps.reviewerStats.get(user.getID() + "-" + critic.getID() + "-I") : 0;

                if(trace == 1)
                {
                    log.trace("                    [" + critic.getID() + "] " + S + "/" + I + " (" + (int)(100.*S/I) + "%) = " +
                                                   (int)(100000*peeps.comradery.get(user.getID() + "-" + critic.getID()))/100000.);
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
        for(Reviewer user : peeps.getActiveReviewers())
        {

            traceSwitch++;

            if(user.getID().equals("U33")) traceSwitch = 1;
            else traceSwitch = 0;

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
                    peeps.cccpSuggestions.put(user.getID() + "-" + uRev.movieID, totScorePts / totComradery >= P_SC ? 1. : 0.);

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
