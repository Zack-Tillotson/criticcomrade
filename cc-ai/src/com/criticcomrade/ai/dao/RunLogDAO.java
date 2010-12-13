package com.criticcomrade.ai.dao;

import com.criticcomrade.ai.vanilla.ComraderyFunction;
import com.criticcomrade.ai.vanilla.RandomUtil;
import com.criticcomrade.ai.data.Parameter;
import java.util.Vector;
import java.sql.*;
import javax.naming.*;
import org.apache.log4j.Logger;

public class RunLogDAO
{

    private Logger log = Logger.getLogger(RunLogDAO.class.toString());

    private final String user = "critic_review";
    private final String pass = "critic_review_pwd";
    private final String url = "jdbc:mysql://notatrick.com/critic_review_new";

    private Connection conn;

    public RunLogDAO() throws NamingException, SQLException
    {

        try
        {
            Class.forName ("com.mysql.jdbc.Driver").newInstance();
        }
        catch(ClassNotFoundException e)
        {
            throw new SQLException();
        }
        catch(InstantiationException e)
        {
            throw new SQLException();
        }
        catch(IllegalAccessException e)
        {
            throw new SQLException();
        }
        conn = DriverManager.getConnection (url, user, pass);
        
    }

    private final String GET_AI_VERSION_ID =
        "select ai_version_id from ai_versions where name like ?";

    private final String ADD_AI_VERSION =
        "insert into ai_versions (name) values (?)";

    private final String ADD_AI_VERSION_VAR_LIST =
        "insert into ai_vars (ai_version_id, var_name, max_value, min_value, initial_max_value, initial_min_value, granularity, active_flg) values (?, ?, ?, ?, ?, ?, ?, ?)";

    private final String ADD_AI_VERSIONS_VAR_ANOTHER =
        ", (?, ?, ?, ?, ?, ?, ?, ?)";

    private final String ADD_RUN_STATS =
        "insert into ai_log (search_technique, ai_version_id, random_seed, start_date, end_date, fitness_calculation_count, fitness) values (?, ?, ?, (select from_unixtime(?)), (select from_unixtime(?)), ?, ?)";

    private final String GET_RUN_ID =
        "select last_insert_id() row_id";

    private final String ADD_RUN_VARIABLE_INSTANCE_LIST =
        "insert into ai_var_instances (ai_var_id, run_id, value) values ((select ai_var_id from ai_vars where ai_version_id = ? and var_name = ?), ?, ?)";

    private final String ADD_VARIABLE_INSTANCE_ANOTHER =
        ", ((select ai_var_id from ai_vars where ai_version_id = ? and var_name = ?), ?, ?)";

    public void saveRunResult(String aiType, ComraderyFunction cf, Vector<Parameter> pars, double fitness, int rounds, long randomSeed, long startTime, long endTime)
    {

        log.info("Saving run result");

        // Ensure the ai version is in the db
        int versionID = -1;
        try
        {
            versionID = this.getVersionID(cf.getFunctionName());
        }
        catch(Exception e)
        {
            log.error("Error getting version ID - " + e.toString());
        }

        if(versionID == -1) 
        {
            try
            {
                log.debug("Saving new AI version");
                versionID = this.newAIVersion(cf.getFunctionName(), cf.getAllParameters());
            }
            catch(Exception e)
            {
                log.error("Error creating new version - " + e.toString());
            }
        }

        log.debug("AI version ID found [" + versionID + "]");

        // Save the stats to the log
        try
        {

            log.debug("Saving run to log");

            PreparedStatement stmt = conn.prepareCall(ADD_RUN_STATS);

            stmt.setString(1, aiType);      // search_technique, ai_version_id, random_seed, start_date, end_date, fitness_calculation_count, fitness
            stmt.setInt(2, versionID);
            stmt.setLong(3, randomSeed);
            stmt.setLong(4, startTime/1000);
            stmt.setLong(5, endTime/1000);
            stmt.setInt(6, rounds);
            stmt.setDouble(7, fitness);

            stmt.execute();

            log.debug("Run saved, getting the Run ID");

            // Get the run id
            stmt = conn.prepareCall(GET_RUN_ID);
            ResultSet rs = stmt.executeQuery();

            if(rs.next())
            {

                int runID = rs.getInt(1);

                log.debug("RunID Found [" + runID + "]");

                // Save the variables
                this.saveRunParameters(runID, versionID, pars);

            }
            else
            {
                log.error("Error saving run [" + aiType + ", " + cf.getFunctionName() + ", " + versionID + ", " + fitness);
            }
        }
        catch(Exception e)
        {
            log.error("Error saving run after gotten version - " + e.toString());
        }

        log.debug("Done saving run result");

    }

    private int getVersionID(String cfName) throws SQLException, NamingException
    {

        PreparedStatement stmt = conn.prepareCall(GET_AI_VERSION_ID);
        stmt.setString(1, cfName);

        ResultSet rs = stmt.executeQuery();
        if(rs.next())
        {
            int versionID = rs.getInt(1);
            if(versionID != 0) return versionID;
        }

        return -1;
        
    }

    private int newAIVersion(String cfName, Vector<Parameter> pl) throws SQLException, NamingException
    {

        log.debug("Saving a new AI version");

        // Add the base
        PreparedStatement stmt = conn.prepareCall(ADD_AI_VERSION);
        stmt.setString(1, cfName);
        stmt.execute();

        log.debug("Done saving AI version, getting ID");

        int versionID = this.getVersionID(cfName);

        log.debug("Done getting id version ID [" + versionID + "] - now on to parameters [size " + pl.size() + "]");

        // Add the parameters
        if(pl.size() != 0 && versionID != -1)
        {

            String query = ADD_AI_VERSION_VAR_LIST;
            for(int i = 1 ; i < pl.size() ; i++)
                query += ADD_AI_VERSIONS_VAR_ANOTHER;

            stmt = conn.prepareCall(query);
            for(int i = 0 ; i < pl.size() ; i++)
            {

                Parameter par = pl.get(i);
                
                // ai_version_id, var_name, initial_max_value, initial_min_value, granularity, active_flg
                stmt.setInt(i * 8 + 1, versionID);
                stmt.setString(i * 8 + 2, par.getName());
                stmt.setDouble(i * 8 + 3, par.getMaxValue());
                stmt.setDouble(i * 8 + 4, par.getMinValue());
                stmt.setDouble(i * 8 + 5, par.getMaxValue());
                stmt.setDouble(i * 8 + 6, par.getMinValue());
                stmt.setDouble(i * 8 + 7, par.getGranularity());
                stmt.setBoolean(i * 8 + 8, par.getIsActive());

            }

            stmt.execute();

            log.debug("Done saving parameters");

        }

        return versionID;
        
    }

    private void saveRunParameters(int runID, int versionID, Vector<Parameter> pars) throws SQLException, NamingException
    {

        log.debug("Saving run parameters [size " + pars.size() + "]");

        if(pars.size() == 0) return;    // There must be some parameters to save

        String query = ADD_RUN_VARIABLE_INSTANCE_LIST;
        for(int i = 1 ; i < pars.size() ; i++)
            query += ADD_VARIABLE_INSTANCE_ANOTHER;

        PreparedStatement stmt = conn.prepareCall(query);

        for(int i = 0 ; i < pars.size() ; i++)
        {

            Parameter par = pars.get(i);

            // ai_var_id, run_id, value
            stmt.setInt(i * 4 + 1, versionID);
            stmt.setString(i * 4 + 2, par.getName());
            stmt.setInt(i * 4 + 3, runID);
            stmt.setDouble(i * 4 + 4, par.getValue());
            
        }

        stmt.execute();

        log.debug("Done saving run parameters");

    }

    private final String GET_PARAMETER_RANGE =
        "select min_value, max_value, granularity from ai_vars where ai_version_id = (select ai_version_id from ai_versions where name = ?) and var_name = ?";

    public double getRandomParameterValue(String cfName, String parName) throws SQLException, NamingException
    {

        PreparedStatement stmt = conn.prepareCall(GET_PARAMETER_RANGE);
        stmt.setString(1, cfName);
        stmt.setString(2, parName);

        ResultSet rs = stmt.executeQuery();
        if(rs.next())
        {

            double min = rs.getDouble(1);
            double max = rs.getDouble(2);
            double granularity = rs.getDouble(3);

            double newVal = RandomUtil.rand.nextDouble() * (max - min) + min;

            // If not at a granularity level
            if(newVal % granularity != 0)
            {

                boolean shouldGoUp = (granularity - newVal % granularity <= newVal % granularity) ? true : false;
                newVal = newVal - newVal % granularity + (shouldGoUp ? granularity : 0);

            }

            return newVal;
            
        }
        else
            throw new SQLException();

    }

    private final String GET_PARAMETER =
        "select min_value, max_value, granularity from ai_vars where ai_version_id = (select ai_version_id from ai_versions where name = ?) and var_name = ?";

    public Parameter getParameter(String cfName, String parName) throws SQLException, NamingException
    {

        PreparedStatement stmt = conn.prepareCall(GET_PARAMETER);
        stmt.setString(1, cfName);
        stmt.setString(2, parName);

        ResultSet rs = stmt.executeQuery();
        if(rs.next())
        {

            double min = rs.getDouble(1);
            double max = rs.getDouble(2);
            double granularity = rs.getDouble(3);

            return new Parameter(parName, 0, min, max, granularity, false);

        }
        else
            throw new SQLException();

    }

    private final String GET_PAST_PARAMETER =
        "select value from ai_var_instances where ai_var_id = (select ai_var_id from ai_vars where var_name = ? and ai_version_id = (select ai_version_id from ai_versions where name like ?)) and run_id = ?";
    
    public double getPreviousRunParameterValue(String cfName, String parName, int runID) throws SQLException, NamingException
    {

        PreparedStatement stmt = conn.prepareCall(GET_PAST_PARAMETER);
        stmt.setString(1, parName);
        stmt.setString(2, cfName);
        stmt.setInt(3, runID);

        ResultSet rs = stmt.executeQuery();
        if(rs.next())
        {

            return rs.getDouble(1);

        }
        else
            throw new SQLException();

    }

}