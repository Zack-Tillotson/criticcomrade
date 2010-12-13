package com.criticcomrade.ai.data;

import java.util.Vector;

public class ParameterList extends Vector<Parameter>
{

    public ParameterList()
    {
        super();
    }

    public void jiggle(double temp)
    {
        for(Parameter par : this)
            par.jiggle(temp);
    }

    public void saveJiggle()
    {
        for(Parameter par : this)
            par.saveJiggle();
    }

    @Override
    public String toString()
    {
        String ret = "";
        for(Parameter par : this)
            ret += ", " + par.getName() + " : " + par.getValue() + " [" + par.getMinValue() + "-" + par.getMaxValue() + "]";
        return this.size() > 0 ? ret.substring(2) : "";
    }

}
