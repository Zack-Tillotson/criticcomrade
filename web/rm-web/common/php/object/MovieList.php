<?php

    require_once('Movie.php');
    require_once('CCObject.php');

    class MovieList extends CCObject
    {

        // Accessors

        function getMaxSize() { return $this->xa['movie-list']['_a']['max-size']; }
        function getCount() { return $this->xa['movie-list']['_a']['count']; }
        function getOrderedBy() { return $this->xa['movie-list']['_a']['ordered-by']; }
        function getType() { return $this->xa['movie-list']['_a']['type']; }
        function getOffset() { return $this->xa['movie-list']['_a']['offset']; }
        function getFilter() { return $this->xa['movie-list']['_a']['filter']; }
        
        function getMovies()
        {
            if(!isset($this->molist))
                foreach(get_children($this->xa['movie-list']['_c']['movie']) as $m)
                {
                    $t['movie'] = $m;
                    $this->molist[] = new Movie($t);
                }
            return $this->molist;
        }

    }

?>