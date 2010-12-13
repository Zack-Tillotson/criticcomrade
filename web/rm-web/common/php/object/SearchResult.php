<?php

require_once('Movie.php');
require_once('Critic.php');
require_once('CCObject.php');

class SearchResult extends CCObject
{

    var $result;

    // Accessors
    function getResultType() { return $this->xa['search-result']['_a']['type']; }
    function getResult()
    {
        if(!isset($this->result))
        {
            if($this->getResultType() == "Movie")
                $result = new Movie($this->xa['search-result']['_c']);
            elseif($this->getResultType() == "Critic")
                $result = new Critic($this->xa['search-result']['_c']);
        }
        return $result;
    }

}

?>