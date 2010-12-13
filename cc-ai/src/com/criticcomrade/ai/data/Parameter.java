package com.criticcomrade.ai.data;

import com.criticcomrade.ai.vanilla.RandomUtil;

public class Parameter
{

    private String name;
    
    private double value;
    private double jiggledValue;

    private double granularity;
    private double min;
    private double max;

    private boolean isActive;

    public Parameter(String name, double initial, double min, double max, double granularity, boolean isActive)
    {

        this.name = name;
        this.value = initial;
        this.min = min;
        this.max = max;
        this.granularity = granularity;
        this.isActive = isActive;

        this.jiggledValue = this.value;
        
    }

    public Parameter(Parameter source)
    {
        this.name = source.name;
        this.value = source.value;
        this.min = source.min;
        this.max = source.max;
        this.granularity = source.granularity;
        this.isActive = source.isActive;
        this.jiggledValue = source.jiggledValue;
    }

    public String getName() { return this.name; }
    public double getValue() { return this.jiggledValue; }
    public double getMaxValue() { return this.max; }
    public double getMinValue() { return this.min; }
    public double getGranularity() { return this.granularity; }
    public boolean getIsActive() { return this.isActive; }

    public void setStartingValue(double newVal)
    {
        this.value = newVal;
        this.jiggledValue = newVal;
    }

    public void jiggle(double jiggleFraction)
    {

        jiggledValue = value + (max - min) * jiggleFraction * (RandomUtil.rand.nextDouble() * 2 - 1.0);
        if(jiggledValue > max) jiggledValue = max;
        if(jiggledValue < min) jiggledValue = min;
        
        // If not at a granularity level
        if(jiggledValue % granularity != 0)
        {

            boolean shouldGoUp = (granularity - jiggledValue % granularity <= jiggledValue % granularity) ? true : false;
            jiggledValue = jiggledValue - jiggledValue % granularity + (shouldGoUp ? granularity : 0);

        }

    }

    public void saveJiggle()
    {
        value = jiggledValue;
    }

    @Override
    public Parameter clone()
    {
        return new Parameter(this);
    }

}
