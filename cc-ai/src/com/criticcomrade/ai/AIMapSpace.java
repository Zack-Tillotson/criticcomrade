package com.criticcomrade.ai;

import com.criticcomrade.ai.data.Parameter;
import com.criticcomrade.ai.vanilla.functions.*;
import com.criticcomrade.ai.vanilla.SimulatedAnnealing;
import com.criticcomrade.ai.data.ReviewerGroups;
import com.criticcomrade.ai.dao.ReviewsDAO;
import com.criticcomrade.ai.dao.RunLogDAO;
import com.criticcomrade.ai.vanilla.ComraderyFunction;
import java.sql.SQLException;
import javax.naming.NamingException;
import org.apache.log4j.Logger;

public class AIMapSpace extends Thread
{

    private static Logger log = Logger.getLogger(AIMapSpace.class.toString());

    private ReviewsDAO dao;
    private ComraderyFunction cf;
    private String parToMap;

    public AIMapSpace(ComraderyFunction cf, String varToMap)  throws SQLException, NamingException
    {

        dao = new ReviewsDAO();
        this.cf = cf;
        this.parToMap = varToMap;

    }

    @Override
    public void run()
    {

        SimulatedAnnealing optimizer;
        RunLogDAO logDAO = null;

        try
        {
            logDAO = new RunLogDAO();
        }
        catch(SQLException e)
        {
            log.error("Error creating DAO - " + e.toString());
        }
        catch(NamingException e)
        {
            log.error("Error creating DAO - " + e.toString());
        }
        
        log.info("Starting single variable map");

        ReviewerGroups revGroup = null;

        try
        {

            revGroup = dao.getReviewerGroups();

        }
        catch(SQLException e)
        {
            log.error("SQL Exception...");
            System.exit(1);
        }
        catch(NamingException e)
        {
            log.error("Naming Exception...");
            System.exit(2);
        }
        
        log.info("Getting variable bounds [" + parToMap + "]");

        Parameter par = null;
        try
        {
            par = logDAO.getParameter(cf.getFunctionName(), parToMap);
            log.error("parameter gotten from db: " + par.getName() + " [" + par.getMinValue() + "-" + par.getMaxValue() + "; " + par.getGranularity() + "]");
        }
        catch(NamingException e)
        {
            log.error("Could not get a new value for parameter [" + cf.getFunctionName() + " - " + parToMap + "] " + e.toString());
        }
        catch(SQLException e)
        {
            log.error("Could not get a new value for parameter [" + cf.getFunctionName() + " - " + parToMap + "] " + e.toString());
        }

        for(double loc = par.getMinValue() ; loc < par.getMaxValue() ; loc += par.getGranularity())
        {

            long runStartTime = System.currentTimeMillis();

            log.info("Progress: " + loc + " [" + par.getMinValue() + "-" + par.getMaxValue() + "] = " + (int)(100*(loc-par.getMinValue())/(par.getMaxValue()-par.getMinValue())) + "%");

            // Set the starting value of the parameter we are mapping
            for(Parameter allPar : revGroup.getAllParameters())
                if(allPar.getName().equalsIgnoreCase(par.getName()))
                    allPar.setStartingValue(loc);
            for(Parameter allPar : cf.getAllParameters())
                if(allPar.getName().equalsIgnoreCase(par.getName()))
                    allPar.setStartingValue(loc);
            
            optimizer = new SimulatedAnnealing(revGroup, cf);
            optimizer.doneRounds = 0;

            optimizer.doOptimization();

            logDAO.saveRunResult("Variable Mapping [" + parToMap + "]",
                                 cf,
                                 optimizer.getBestVars(),
                                 optimizer.getBestFitness(),
                                 optimizer.getLocationsCalculated(),
                                 -1l,
                                 runStartTime,
                                 System.currentTimeMillis()
                                );
            
        }

        log.info("Done with run");
    
    }

    public static void main(String[] args) throws Exception
    {
        (new AIMapSpace(new AlphaOneDotFive(), "suggestion-cutoff")).start();
    }

}
