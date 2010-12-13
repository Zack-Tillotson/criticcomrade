package com.criticcomrade.ai.vanilla;

import java.util.Vector;
import com.criticcomrade.ai.data.Parameter;

public interface Parameterable
{

    public Vector<Parameter> getActiveParameters();
    public Vector<Parameter> getAllParameters();
    public double getParameter(String name);

}
