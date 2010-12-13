<?php

    require_once('SearchResult.php');
    require_once('CCObject.php');

    class SearchResultList extends CCObject
    {

        // Accessors

        function getMaxSize() { return $this->xa['search-result-list']['_a']['max-size']; }
        function getCount() { return $this->xa['search-result-list']['_a']['count']; }
        function getOrderedBy() { return $this->xa['search-result-list']['_a']['ordered-by']; }
        function getSearchTerm() { return $this->xa['search-result-list']['_a']['search-term']; }
        function getOffset() { return $this->xa['search-result-list']['_a']['offset']; }

        function getSearchResults()
        {
            if(!isset($this->srlist))
                foreach(get_children($this->xa['search-result-list']['_c']['search-result']) as $sr)
                {
                    $t['search-result'] = $sr;
                    $this->srlist[] = new SearchResult($t);
                }
            return $this->srlist;
        }

    }

?>