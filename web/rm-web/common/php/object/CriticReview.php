<?php

    require_once('CCObject.php');

    class CriticReview extends CCObject
    {

        var $v;

        // Accessors

        function getCriticID() { return $this->xa['critic-review']['_c']['id']['_v']; }
        function getName() { return $this->xa['critic-review']['_c']['name']['_v']; }
        function getPublisher() { return $this->xa['critic-review']['_c']['publisher']['_v']; }
        function getComradery() { return $this->xa['critic-review']['_c']['comradery']['_v']; }
        function getMovieID() { return $this->xa['critic-review']['_c']['movie-id']['_v']; }
        function getTitle() { return $this->xa['critic-review']['_c']['title']['_v']; }
        function getScore() { return $this->xa['critic-review']['_c']['score']['_v']; }
        function getSummary() { return $this->xa['critic-review']['_c']['summary']['_v']; }
        function getReviewDate() { return $this->xa['critic-review']['_c']['review-date']['_v']; }
        function getLink() { return $this->xa['critic-review']['_c']['link']['_v']; }

    }

?>