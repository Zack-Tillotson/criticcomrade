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
import java.util.Vector;

public class AIRunner extends Thread
{

    private static Logger log = Logger.getLogger(AIRunner.class.toString());

    private final int DEFAULT_TIME_TO_RUN_MS = 1000 * 60 * 60 * 24 * 7 * 1;		// 1 Week

    private ReviewsDAO dao;
    private ComraderyFunction cf;

    public AIRunner(ComraderyFunction cf, ReviewsDAO dao)  throws SQLException, NamingException
    {

        this.dao = dao;
        this.cf = cf;

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
        
        long startTime = System.currentTimeMillis();
        boolean foundResult = false;

        log.info("Starting runs");

        while(System.currentTimeMillis() - startTime < DEFAULT_TIME_TO_RUN_MS && !foundResult)
        {

            log.info("Starting run");

            long runStartTime = System.currentTimeMillis();

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

            log.info("Getting new starting parameters");
            for(Parameter par : cf.getActiveParameters())
            {

                try
                {
                    double newRandomValue = logDAO.getRandomParameterValue(cf.getFunctionName(), par.getName());
                    par.setStartingValue(newRandomValue);
                }
                catch(Exception e)
                {
                    log.error("Could not get a new value for parameter [" + cf.getFunctionName() + " - " + par.getName() + "]");
                }                

            }                    

            optimizer = new SimulatedAnnealing(revGroup, cf);
            optimizer.doneFitness = .8;
            optimizer.doneRounds = 100;
            optimizer.startTemp = .2;
            optimizer.endTemp = .01;

            foundResult = optimizer.doOptimization();

            log.info("Saving run result");
            logDAO.saveRunResult(   optimizer.getFunctionName(),
                                    cf,
                                    optimizer.getBestVars(),
                                    optimizer.getBestFitness(),
                                    optimizer.getLocationsCalculated(),
                                    -1l,
                                    runStartTime,
                                    System.currentTimeMillis()
                                );

        }

        log.info("Done with runs");
        
    }

    public static void main(String[] args) throws Exception
    {

        ReviewsDAO dao = new ReviewsDAO();

        Vector<AIRunner> threads = new Vector<AIRunner>();

        for(int i = 0 ; i < 5 ; i++)
            threads.add(new AIRunner(new AlphaOneDotFiveTwo(), dao));
        for(int i = 0 ; i < 5 ; i++)
            threads.add(new AIRunner(new AlphaOneDotFive(), dao));
        
        for(AIRunner thread : threads)
            thread.start();

    }

}
