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

public class AITestOneLocation extends Thread
{

    private static Logger log = Logger.getLogger(AITestOneLocation.class.toString());

    private ReviewsDAO dao;
    private ComraderyFunction cf;
    private int runID;

    public AITestOneLocation(ComraderyFunction cf, int runID)  throws SQLException, NamingException
    {

        dao = new ReviewsDAO();
        this.cf = cf;
        this.runID = runID;

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
        
        log.info("Starting single location test");

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
                double oldValue = logDAO.getPreviousRunParameterValue(cf.getFunctionName(), par.getName(), runID);
                log.error("parameter gotten from db: " + par.getName() + " = " + oldValue);
                par.setStartingValue(oldValue);
            }
            catch(NamingException e)
            {
                log.error("Could not get a new value for parameter [" + cf.getFunctionName() + " - " + par.getName() + " - " + runID + "] " + e.toString());
            }
            catch(SQLException e)
            {
                log.error("Could not get a new value for parameter [" + cf.getFunctionName() + " - " + par.getName() + " - " + runID + "] " + e.toString());
            }

        }

        optimizer = new SimulatedAnnealing(revGroup, cf);
        optimizer.doneRounds = 0;

        optimizer.doOptimization();

        log.info("Done with run");
    
    }

    public static void main(String[] args) throws Exception
    {
        int runID = 1327;
        (new AITestOneLocation(new AlphaOneDotFiveTwo(), runID)).start();
    }

}
