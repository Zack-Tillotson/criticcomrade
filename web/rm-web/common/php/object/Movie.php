<?php

    require_once('CriticReviewList.php');
    require_once('UserReviewList.php');
    require_once('CCObject.php');

    class Movie extends CCObject
    {

        var $crl;   // CriticReviewList
        var $url;   // UserReviewList

        var $gl;    // Genre array
        var $dl;    // Director list
        var $wl;    // Writer list
        var $sl;    // Star list

        // Accessors

        function getMovieID() { return $this->xa['movie']['_c']['id']['_v']; }
        function getTitle() { return $this->xa['movie']['_c']['title']['_v']; }
        function getPublisher() { return $this->xa['movie']['_c']['publisher']['_v']; }
        function getSummary() { return $this->xa['movie']['_c']['summary']['_v']; }
        function getRating() { return $this->xa['movie']['_c']['rating']['_v']; }
        function getRatingReason() { return $this->xa['movie']['_c']['rating-reason']['_v']; }
        function getBoxOfficeRank() { return $this->xa['movie']['_c']['box-office-rank']['_v']; }
        function getReleaseDate() { return $this->xa['movie']['_c']['release-date']['_v']; }

        function getGenres()
        {
            if($this->gl == "")
            {
                $this->gl = array();
                foreach(get_children($this->xa['movie']['_c']['genre-list']['_c']['genre']) as $genre)
                    $this->gl[] = $genre['_v'];
            }
            return $this->gl;
        }

        function getWriters()
        {
            if($this->wl == "")
            {
                $this->wl = array();
                foreach(get_children($this->xa['movie']['_c']['writer-list']['_c']['writer']) as $v)
                    $this->wl[] = $v['_v'];
            }
            return $this->wl;
        }

        function getStars()
        {
            if($this->sl == "")
            {
                $this->sl = array();
                foreach(get_children($this->xa['movie']['_c']['star-list']['_c']['star']) as $v)
                    $this->sl[] = $v['_v'];
            }
            return $this->sl;
        }

        function getDirector()
        {
            if($this->dl == "")
            {
                $this->dl = array();
                foreach(get_children($this->xa['movie']['_c']['director-list']['_c']['director']) as $v)
                    $this->dl[] = $v['_v'];
            }
            return $this->dl;
        }

        function getCriticReviewList()
        {
            if($this->crl == "") $this->crl = new CriticReviewList($this->xa['movie']['_c']['reviews-list']['_c']);
            return $this->crl;
        }

        function getUserReviewList() 
        {
            if($this->url == "") $this->url = new UserReviewList($this->xa['movie']['_c']['reviews-list']['_c']);
            return $this->url;
        }

    }

?>