package com.criticcomrade.ai.vanilla;

import com.criticcomrade.ai.data.ReviewerGroups;
import java.util.HashMap;

public interface ComraderyFunction extends Parameterable
{

    public String getFunctionName();
    public HashMap<String, Double> calculateComraderies(ReviewerGroups peeps);
    public HashMap<String, Double> calculateCCCPSuggestions(ReviewerGroups peeps);

}
