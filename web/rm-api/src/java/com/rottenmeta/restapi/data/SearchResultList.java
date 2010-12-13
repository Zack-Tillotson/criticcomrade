package com.rottenmeta.restapi.data;

import java.util.Vector;
import java.util.LinkedHashSet;

/**
 *
 * @author chloburr
 */
public class SearchResultList extends MetadList<SearchResult>
{

    private String searchTerm;

    public SearchResultList(String searchTerm)
    {
        this.searchTerm = searchTerm.replaceAll("\\%", "*");
    }

    public String getSearchTerm() { return this.searchTerm; }

    public void addCritics(Vector<Critic> criticList, String searchItem)
    {
        for(Critic critic : criticList)
            this.add(new SearchResult("Critic", critic, searchItem.equalsIgnoreCase("name") ? critic.getName().length() : searchItem.equalsIgnoreCase("publisher") ? critic.getPublisher().length() : 0));
        this.setMaxSize(this.getMaxSize() + criticList.size());
    }

    public void addMovies(Vector<Movie> movieList, String searchItem)
    {
        for(Movie movie : movieList)
            this.add(new SearchResult("Movie", movie, searchItem.equalsIgnoreCase("title") ? movie.getTitle().length() : 0));
        this.setMaxSize(this.getMaxSize() + movieList.size());
    }

    @Override
    public String getURL()
    {
        return "search/" + this.getSearchTerm() + "/";
    }
    
}