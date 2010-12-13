<?php

    require_once('CCObject.php');

    class UserReview extends CCObject
    {

        // Accessors

        function getUserName() { return $this->xa['user-review']['_c']['user-name']['_v']; }
        function getMovieID() { return $this->xa['user-review']['_c']['movie-id']['_v']; }
        function getTitle() { return $this->xa['user-review']['_c']['title']['_v']; }
        function getScore() { return isset($this->xa['user-review']['_c']['score']['_v']) ? $this->xa['user-review']['_c']['score']['_v'] : "-1"; }
        function getSummary() { return $this->xa['user-review']['_c']['summary']['_v']; }
        function getReviewDate() { return $this->xa['user-review']['_c']['review-date']['_v']; }

    }

?>