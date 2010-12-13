package com.criticcomrade.ai.vanilla;

import java.util.Vector;
import com.criticcomrade.ai.data.Parameter;

public interface OptimizerFunction
{

    public String getFunctionName();
    public Double getBestFitness();
    public Vector<Parameter> getBestVars();
    public int getLocationsCalculated();

}
