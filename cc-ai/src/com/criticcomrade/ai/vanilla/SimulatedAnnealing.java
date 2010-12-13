package com.criticcomrade.ai.vanilla;

import com.criticcomrade.ai.data.ParameterList;
import com.criticcomrade.ai.data.ReviewerGroups;
import com.criticcomrade.ai.data.Parameter;
import java.util.Vector;
import org.apache.log4j.Logger;

public class SimulatedAnnealing implements OptimizerFunction
{

    private Logger log = Logger.getLogger(SimulatedAnnealing.class.toString());

    private ParameterList params;
    private ReviewerGroups peeps;
    private ComraderyFunction comFunc;

    // For jiggling parameters
    public double startTemp = .35;           // Fractional distance to jiggle each variable each round
    public double endTemp = .01;

    // Finish conditions
    public double doneFitness = .85;        // The fitness level which means the optimization is over
    public int doneRounds = 200;           // The maximum number of rounds to attempt to find needed fitness level

    // Status variables
    double bestFitness;
    Vector<Parameter> bestVars;
    int round;

    public SimulatedAnnealing(ReviewerGroups revGroup, ComraderyFunction comFunc)
    {

        this.comFunc = comFunc;

        // Get the reviews from the DB
        peeps = revGroup;

        params = new ParameterList();
        params.addAll(peeps.getActiveParameters());
        params.addAll(comFunc.getActiveParameters());
        
    }

    public String getFunctionName()
    {
        return "Simulated Annealing";
    }

    public Vector<Parameter> getBestVars()
    {
        return this.bestVars;
    }

    public Double getBestFitness()
    {
        return this.bestFitness;
    }

    public int getLocationsCalculated()
    {
        return this.round;
    }

    public boolean doOptimization()
    {

        // Get the initial fitness
        log.warn("Intial");
        double fitness = calculateFitness(peeps, comFunc);
        double temp = calculateTemp(0);
        log.warn("    Fitness: " + fitness);

        bestFitness = fitness;
        bestVars = (Vector<Parameter>)comFunc.getAllParameters().clone();
        bestVars.addAll((Vector<Parameter>)peeps.getAllParameters().clone());

        // Optimize until we're done
        log.info("Starting optimization");
        for(round = 1 ; round <= doneRounds && fitness < doneFitness ; round++)
        {

            log.info("Round " + round);
            log.info("        Fitness     : " + fitness);
            log.info("        Temperature : " + temp);

            double jiggledFitness = -1;

            // Move
            int moveAttempts = 0;
            do
            {
            
                // Jiggle the parameters
                log.info("    Jiggling");
                params.jiggle(temp);

                // Calculate the possible new fitness level
                jiggledFitness = calculateFitness(peeps, comFunc);

                if(jiggledFitness > bestFitness)
                {
                    log.warn("New Best Fitness Found");
                    log.warn("Fitness: " + jiggledFitness);
                    log.warn("    Parameters: " + params.toString());
                    bestFitness = jiggledFitness;
                    bestVars = new Vector<Parameter>();
                    for(Parameter par : comFunc.getAllParameters())
                        bestVars.add(par.clone());
                    for(Parameter par : peeps.getAllParameters())
                        bestVars.add(par.clone());
                }
                else
                {
                    log.debug("         Fitness     : " + fitness);
                    log.debug("         Temperature : " + temp);
                    log.debug("         Parameters: " + params.toString());
                }

            }
            while(!shouldFollowJiggle(fitness, jiggledFitness) && moveAttempts++ < 10);

            log.debug("        Following...");
            
            fitness = jiggledFitness;
            temp = calculateTemp(round);
            params.saveJiggle();

        }

        if(fitness >= doneFitness) return true;
        else                       return false;
        
    }

    // This calculates the fitness level for a parameter set, in fractional form 0.0-1.0
    // To calculate the fitness level:
    //      1. Calculate the User/Critic comradery levels
    //      2. Calculate the User/Critic/Movie CCCP Suggestions for the test movies
    //      3. Calculate the fractional correctness of the suggestions
    private double calculateFitness(ReviewerGroups peeps, ComraderyFunction comFunc)
    {

        comFunc.calculateComraderies(peeps);
        comFunc.calculateCCCPSuggestions(peeps);

        return peeps.getTestAgreeFraction();

    }

    // Temperature is the fractional fitness amount from the start temp to the end temp
    // So a fitness of 0 means the temp will be the start temp, and a fitness of 1 will be
    // the end temp.
    private double calculateTemp(int round)
    {
        return (endTemp - startTemp) * (1.0 * round / doneRounds) + startTemp;
    }

    // We will follow the jiggle based on how much better its fitness is than
    // the current fitness, with a random element thrown in.
    private boolean shouldFollowJiggle(double fitness, double jiggledFitness)
    {

        // Equation breaks if both fitnesses are 0
        if(fitness == 0 && jiggledFitness == 0)
            return false;

        double J = jiggledFitness;
        double F = fitness;
        double D = doneFitness;
        double r = .05;
        double R = .4;

        double followOdds = .5 + (((J - F) / D + r) / (2 * r)) * 2 * R - R;

        log.info("            Follow odds: " + (int)(followOdds * 10000)/100. + "% [J: " + (int)(jiggledFitness * 10000)/10000. + ", F: " + (int)(fitness * 10000)/10000. + "]");

        if(RandomUtil.rand.nextDouble() < followOdds)
            return true;
        else
            return false;
        
    }

}