//Zack Tillotson
//Jan 20 2009

package com.rottenmeta.restapi;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Enumeration;
import org.apache.log4j.*;
import java.util.regex.Pattern;
import java.net.URLDecoder;

/**
 *
 * @author Zill
 */
public class CCRequestHandler extends HttpServlet
{

    static Logger logger = Logger.getLogger(CCRequestHandler.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        logger.debug("GET " + request.getRequestURI());
        long startTime = System.currentTimeMillis();
        processRequest(request, response);
        logger.debug("  ######### Request Time: " + (System.currentTimeMillis() - startTime) / 1000.);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        logger.debug("POST " + request.getRequestURI());
        long startTime = System.currentTimeMillis();
        processRequest(request, response);
        logger.debug("  ######### Request Time: " + (System.currentTimeMillis() - startTime) / 1000.);
    }

    @Override
    public String getServletInfo()
    {
        return "This servlet serves CriticComrade requests";
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {

        // Set some repsonse header info
        response.setContentType("text/xml");
        
        // Generate the response XML (or error message)
        try
        {

            String responseXML = getResponseBody(request);

            // Print the response body
            PrintWriter out = response.getWriter();
            try
            {
                out.println(responseXML);
            }
            catch (Exception e)
            {
                out.println("Error");
            }
            finally
            {
                out.close();
            }

        }
        catch(RMDBException e)
        {
            errBody(response, 500, e.toString());
        }
        catch(RMInvalidIDException e)
        {
            errBody(response, 404, e.toString());
        }
        catch(RMInvalidParamException e)
        {
            errBody(response, 400, e.toString());
        }
        catch(RMInvalidCredentialsException e)
        {
            errBody(response, 403, e.toString());
        }

    }

    // Serves get requests. The URI that is served with some code is on the
    // right in comments.
    private String getResponseBody(HttpServletRequest req) throws RMDBException, RMInvalidIDException, RMInvalidParamException, RMInvalidCredentialsException
    {

        logger.trace("Top of getResponseBody");
        
        //Initialize RM Util with the currect request's URL
        RMUtil.setURLBase("http://" + req.getServerName() + ((req.getServerPort() != 80)?":" + req.getServerPort():"") + "/cc-api/");

        String respBody = "";

        String realURI = URLDecoder.decode(req.getRequestURI()).replaceAll("\\+", " ");
        
        // Parse the URN
        String[] result = realURI.split("/");

        // Get rid of the /sid/.*/ at the end if it is there
        if(realURI.matches("^.*/sid/[^/]*/?$"))
        {
            Pattern p = Pattern.compile("/sid/[^/]*/?$");
            result = p.matcher(realURI).replaceAll("").split("/");
        }
        
        // Parse the parameters
        HashMap<String, String> params;
        params = parseReqParams(req);

        // ATM we only do XML
        if(!params.containsKey("representation")) params.put("representation", "xml");

        logger.trace("Request tokenized and parameters parsed, started url match");

        // If an error occurs when getting the result string, just print an error
        try
        {

            // /cc-api/
            if(result[1].equalsIgnoreCase("cc-api") || result.length <= 2)
            {

                // /cc-api/movies/
                // /cc-api/critics/
                // /cc-api/users/
                if(result[2].equalsIgnoreCase("movies"))
                {

                    // /cc-api/movies/
                    // /cc-api/movies/all/
                    // /cc-api/movies/in-theaters/
                    // /cc-api/movies/opening/
                    // /cc-api/movies/id/
                    if(result.length <= 3)                                               // /cc-api/movies/
                    {
                        respBody =
                            "movies";
                    }
                    else if(result[3].equalsIgnoreCase("all") && result.length == 4)    // /cc-api/movies/all/
                    {
                        if(!params.containsKey("type")) params.put("type", "ditty");
                        if(!params.containsKey("count")) params.put("count", "15");
                        respBody = RMUtil.getMovies(params);
                    }
                    else if(result[3].equalsIgnoreCase("in-theaters") && result.length == 4)  // /cc-api/movies/in-theaters/
                    {
                        if(!params.containsKey("type")) params.put("type", "summary");
                        params.put("filter", "in-theaters");
                        respBody = RMUtil.getMovies(params);
                    }
                    else if(result[3].equalsIgnoreCase("opening") && result.length == 4) // /cc-api/movies/opening/
                    {
                        if(!params.containsKey("type")) params.put("type", "summary");
                        params.put("filter", "opening");
                        respBody = RMUtil.getMovies(params);
                    }
                    else if(result[3].equalsIgnoreCase("top-box-office") && result.length == 4)  // /cc-api/movies/top-box-office/
                    {
                        if(!params.containsKey("type")) params.put("type", "summary");
                        if(!params.containsKey("count")) params.put("count", "10");
                        if(!params.containsKey("order-by")) params.put("order-by", "rank");
                        params.put("filter", "top-box-office");
                        respBody = RMUtil.getMovies(params);
                    }
                    else if(result[3].equalsIgnoreCase("coming-up") && result.length == 4)  // /cc-api/movies/coming-up/
                    {
                        if(!params.containsKey("type")) params.put("type", "summary");
                        if(!params.containsKey("count")) params.put("count", "10");
                        if(!params.containsKey("order-by")) params.put("order-by", "opening");
                        params.put("filter", "coming-up");
                        respBody = RMUtil.getMovies(params);
                    }
                    else if(result[3].equalsIgnoreCase("not-reviewed") && result.length == 4) // /cc-api/movies/not-reviewed/
                    {
                        if(!params.containsKey("type")) params.put("type", "ditty");
                        params.put("filter", "not-reviewed");
                        respBody = RMUtil.getMovies(params);
                    }
                    else if(result[3].equalsIgnoreCase("current") && result.length == 4) // /cc-api/movies/current/
                    {
                        if(!params.containsKey("type")) params.put("type", "ditty");
                        params.put("filter", "current");
                        respBody = RMUtil.getMovies(params);
                    }
                    else if(result[3].equalsIgnoreCase("id"))
                    {

                        // /cc-api/movies/id/
                        // /cc-api/movies/id/.*/
                        if(result.length <= 4)                                           // /cc-api/movies/id/
                        {
                            throw new RMInvalidIDException("", "Movie");
                        }
                        else
                        {
                            if(req.getMethod().equalsIgnoreCase("post"))
                            {
//                                respBody = RMUtil.postMovieFullOne(result[4]);
                            }
                            else
                            {
                                respBody = RMUtil.getMovie(result[4], params);
                            }
                        }

                    }

                }
                else if(result[2].equalsIgnoreCase("critics"))
                {

                    // /cc-api/critics/
                    // /cc-api/critics/all/
                    // /cc-api/critics/recent/
                    // /cc-api/critics/id/
                    if(result.length <= 3)                                              // /cc-api/critics/
                    {
                        
                        respBody =
                                "critics";
                        
                    }
                    else if(result[3].equalsIgnoreCase("all") && result.length == 4)    // /cc-api/critics/all/
                    {
                        if(!params.containsKey("type")) params.put("type", "ditty");
                        if(!params.containsKey("count")) params.put("count", "15");
                        respBody = RMUtil.getCritics(params);
                    }
                    else if(result[3].equalsIgnoreCase("id"))
                    {

                        if(result.length <= 4)                                          // /cc-api/critics/id/
                        {
                            throw new RMInvalidIDException("", "Critic");
                        }
                        else if(!result[4].equalsIgnoreCase(""))
                        {

                            if(result.length <= 5)                                      // /cc-api/critics/id/.*/
                            {
                                if(req.getMethod().equalsIgnoreCase("post"))
                                {
//                                    respBody = RMUtil.postCriticFullOne(result[4]);
                                }
                                else
                                {
                                    if(!params.containsKey("type")) params.put("type", "full");
                                    respBody = RMUtil.getCritic(result[4], params);
                                }
                            }
                            else if(result[5].equalsIgnoreCase("reviews"))
                            {

                                if(result.length <= 6)
                                {
                                    throw new RMInvalidIDException("", "Review");
                                }
                                else if(!result[6].equalsIgnoreCase("") && result.length <= 7)  // /cc-api/critics/id/.* /reviews/.*/
                                {
                                    if(req.getMethod().equalsIgnoreCase("post"))
                                    {
//                                        respBody = RMUtil.postReviewFromCritic(result[4], result[6]);
                                    }
                                    else
                                    {
                                        if(!params.containsKey("type")) params.put("type", "summary");
                                        respBody = RMUtil.getCriticReview(result[4], result[6], params);
                                    }
                                }
                                
                            }
                            
                        }

                    }

                }
                else if(result[2].equalsIgnoreCase("users"))
                {

                    // /cc-api/users/
                    // /cc-api/users/all/
                    // /cc-api/users/recent/
                    // /cc-api/users/id/
                    if(result.length <= 3)                                              // /cc-api/users/
                    {
                        respBody =
                                "users";
                    }
                    else if(result[3].equalsIgnoreCase("all") && result.length == 4)    // /cc-api/users/all/
                    {
                        if(!params.containsKey("type")) params.put("type", "ditty");
                        if(!params.containsKey("count")) params.put("count", "15");
                        respBody = RMUtil.getUsers(params);
                    }
                    else if(result[3].equalsIgnoreCase("id"))
                    {

                        if(result.length <= 4)                                          // /cc-api/users/id/
                        {
                            throw new RMInvalidIDException("", "User");
                        }
                        else if(!result[4].equalsIgnoreCase(""))
                        {

                            if(result.length <= 5)                                      // /cc-api/users/id/.*/
                            {
                                if(req.getMethod().equalsIgnoreCase("post"))
                                {
                                    respBody = RMUtil.postUser(DataUtil.parseUserFromRequest(req), params);
                                }
                                else
                                {
                                    if(!params.containsKey("type")) params.put("type", "full");
                                    respBody = RMUtil.getUser(result[4], params);
                                }
                            }
                            else if(result[5].equalsIgnoreCase("reviews"))
                            {
                                if(result.length <= 6)                                          // /cc-api/users/id/.*/reviews/
                                {
                                    if(req.getMethod().equalsIgnoreCase("post"))
                                        respBody = RMUtil.postUserReviews(DataUtil.parseUserFromRequest(req).reviews, params);

                                    else
                                        throw new RMInvalidIDException("", "Review");
                                }
                                else if(!result[6].equalsIgnoreCase("") && result.length <= 7)  // /cc-api/users/id/.*/reviews/.*/
                                {
                                    if(req.getMethod().equalsIgnoreCase("post"))
                                            respBody = RMUtil.postUserReview(DataUtil.parseReviewFromRequest(req), params);
                                    else
                                    {
                                        if(!params.containsKey("type")) params.put("type", "summary");
                                        respBody = RMUtil.getUserReview(result[4], result[6], params);
                                    }

                                }
                                
                            }
                            
                        }

                    }

                }
                else if(result[2].equalsIgnoreCase("sessions"))
                {

                    if(result.length <= 3)                                      // cc-api/sessions/
                    {
                        respBody = "sessions";
                    }
                    else if(result[3].equalsIgnoreCase("id"))
                    {
                        if(result.length <= 4)                                  // cc-api/sessions/id/
                        {
                            throw new RMInvalidIDException("", "Session");
                        }
                        else if(!result[4].equalsIgnoreCase(""))
                        {

                            if(result.length <= 5)                              // cc-api/sessions/id/.*/
                            {
                            
                                if(req.getMethod().equalsIgnoreCase("get") && !params.containsKey("post"))
                                {
                                    respBody = RMUtil.getSession(result[4], params);
                                }
                                else if(req.getMethod().equalsIgnoreCase("post") || params.containsKey("post"))
                                {
                                    respBody = RMUtil.postSession(result[4], DataUtil.parseSessionFromRequest(req), params);
                                }

                            }
                            else
                            {

                                if(result[5].equalsIgnoreCase("id"))
                                {

                                    if(result.length <= 6)                      // cc-api/sessions/id/.*/id/
                                    {
                                        
                                        if(req.getMethod().equalsIgnoreCase("get"))
                                        {
                                            throw new RMInvalidIDException("", "Movie");
                                        }
                                        else if(req.getMethod().equalsIgnoreCase("post"))
                                        {
                                            logger.debug("posting session reviews");
                                            respBody = RMUtil.postSessionReviews(result[4], DataUtil.parseUserFromRequest(req).reviews, params);
                                        }
                                        
                                    }
                                    else if(!result[6].equals(""))              // cc-api/sessions/id/.*/id/.*/
                                    {
                                        if(req.getMethod().equalsIgnoreCase("get"))
                                        {
                                            if(!params.containsKey("type")) params.put("type", "summary");
                                            respBody = RMUtil.getSessionReview(result[4], result[6], params);
                                        }
                                        else if(req.getMethod().equalsIgnoreCase("post"))
                                        {
                                            logger.debug("posting session reviews id");
                                            respBody = RMUtil.postSessionReview(result[4], result[6], DataUtil.parseReviewFromRequest(req), params);
                                        }
                                    }
                                    
                                }

                            }
                            
                        }

                    }
                
                }
                else if(result[2].equalsIgnoreCase("search"))
                {
                    logger.trace("URL matches with /cc-api/search/");
                    
                    if(result.length == 3)                                      //cc-api/search/
                    {
                        logger.debug("Give search explanation or something");
                    }
                    else if(!result[3].equals("") && result.length == 4)        //cc-api/search/.* /
                    {
                        logger.debug("Doing a search [" + result[3] + "]");
                        if(!params.containsKey("count")) params.put("count", "20");
                        if(!params.containsKey("search-movie-titles") &&
                           !params.containsKey("search-critic-names") &&
                           !params.containsKey("search-critic-pubs"))
                        {
                            params.put("search-movie-titles", "true");
                            params.put("search-critic-names", "true");
                            params.put("search-critic-pubs", "true");
                        }
                        respBody = RMUtil.getSearchResults(result[3], params);
                    }
                    
                }

            }

            if(respBody.equals("")) { throw new RMInvalidParamException("URL", req.getRequestURI()); };
            
        }
        catch(RMInvalidIDException e)
        {
            logger.debug(e.toString());
            throw e;
        }
        catch(RMDBException e)
        {
            logger.error(e.toString());
            throw e;
        }
        catch(RMInvalidCredentialsException e)
        {
            logger.debug(e.toString());
            throw e;
        }

        return respBody;

    }

    private HashMap parseReqParams(HttpServletRequest req) throws RMInvalidParamException
    {
        HashMap<String, String> ret = new HashMap();

        for(Enumeration i = req.getParameterNames(); i.hasMoreElements() ; )
        {
            String key = (String)(i.nextElement());
            String value = (String)(req.getParameter(key));
            ret.put(key, value);
        }

        logger.trace("Parsed parameters: " + ret.toString());

        // Check each parameter for validity
        for(String key: ret.keySet())
        {
            int val = validParameter(key, ret.get(key));
            if(val < 0) throw new RMInvalidParamException(key, ret.get(key));
            else if(val > 0) ret.remove(key);
        }

        // Add the SID to the parameters
        if(req.getRequestURI().matches("^.*/sid/[^/]*/?$"))
            ret.put("sid", req.getRequestURI().split("/")[req.getRequestURI().split("/").length-1]);

        return ret;
    }

    // a return value < 0 means a parameter which is an error (this is like an invalid ID)
    // a return value > 0 means we want to ignore this parameter (this is like a count that is negative or whatever)
    // a return value = 0 means it's a ok parameter
    private int validParameter(String key, String value)
    {

        if(key.equalsIgnoreCase("count"))
        {
            try
            {
                int tmp = Integer.parseInt(value);
                if(tmp <= 0)
                {
                    logger.debug("invalid parameter: " + key + " = " + value);
                    return 1;
                }
            }
            catch(NumberFormatException e)
            {
                logger.debug("invalid parameter: " + key + " = " + value);
                return 1;
            }
        }
        else if(key.equalsIgnoreCase("offset"))
        {
            try
            {
                int tmp = Integer.parseInt(value);
                if(tmp < 0)
                {
                    logger.debug("invalid parameter: " + key + " = " + value);
                    return 1;
                }
            }
            catch(NumberFormatException e)
            {
                logger.debug("invalid parameter: " + key + " = " + value);
                return 1;
            }
        }
        else if(key.equalsIgnoreCase("type"))
        {
            if(!value.equalsIgnoreCase("ditty") &&
               !value.equalsIgnoreCase("summary") &&
               !value.equalsIgnoreCase("full"))
            {
                logger.debug("invalid parameter: " + key + " = " + value);
                return -1;
            }
        }
        else if(key.equalsIgnoreCase("order-by"))
        {
            if(value.equalsIgnoreCase(""))
            {
                logger.debug("invalid parameter: " + key + " = " + value);
                return -1;
            }
        }
        else if(key.equalsIgnoreCase("filter"))
        {
            logger.debug("invalid parameter: " + key + " = " + value);
            return -1;
        }
        else if(key.equalsIgnoreCase("user-id"))
        {
            logger.debug("invalid parameter: " + key + " = " + value);
            return -1;
        }
        else if(key.equalsIgnoreCase("sid"))
        {
            logger.debug("invalid parameter: " + key + " = " + value);
            return -1;
        }
        
        return 0;
        
    }

    private void errBody(HttpServletResponse resp, int errCode, String errMsg)
    {

        resp.setStatus(errCode);

        PrintWriter out = null;
        
        try
        {

            out = resp.getWriter();
            out.println
            (
                "<?xml version='1.0' standalone='yes'?>"            + "\n" +
                "<cc-error>"                                        + "\n" +
                "   <error-code>" + errCode + "</error-code>"       + "\n" +
                "   <error-message>" + errMsg + "</error-message>"  + "\n" +
                "</cc-error>"                                          
            );
            
        }
        catch (Exception e)
        {
        }
        finally
        {
            if(out != null) out.close();
        }

    }

}