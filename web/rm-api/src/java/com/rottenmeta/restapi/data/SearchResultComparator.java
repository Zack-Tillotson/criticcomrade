package com.rottenmeta.restapi.data;

import java.util.Comparator;


public class SearchResultComparator implements Comparator<SearchResult>
{

    @Override
    public int compare(SearchResult a, SearchResult b)
    {
        return a.getResultLength() - b.getResultLength();
    }

}
