<?php

    require_once('CriticReview.php');
    require_once('CCObject.php');

    class CriticReviewList extends CCObject
    {

        var $vo;

        // Accessors

        function getReviewCount() { return $this->xa['critic-review-list']['_a']['review-count']; }
        function getPositiveReviewCount() { return $this->xa['critic-review-list']['_a']['positive-review-count']; }
        function getRecentReviewCount() { return $this->xa['critic-review-list']['_a']['recent-review-count']; }
        function getComradeCount() { return isset($this->xa['critic-review-list']['_a']['comrade-count']) ? $this->xa['critic-review-list']['_a']['comrade-count'] : 0; }
        function getComradeScore() { return isset($this->xa['critic-review-list']['_a']['comrade-score']) ? $this->xa['critic-review-list']['_a']['comrade-score'] : "-"; }
        function getComradeSuggestion() { return isset($this->xa['critic-review-list']['_a']['comrade-suggestion']) ? $this->xa['critic-review-list']['_a']['comrade-suggestion'] : "-"; }
        function getComradeIntersectCount() { return $this->xa['critic-review-list']['_a']['review-intersect-count']; }
        function getComradeAgreeCount() { return $this->xa['critic-review-list']['_a']['matching-review-count']; }
        
        function getReviews() 
        {
            if($this->vo == "")
                foreach(get_children($this->xa['critic-review-list']['_c']['critic-review']) as $cr)
                    $this->vo[] = new CriticReview(array('critic-review'=>$cr));
            return $this->vo;
        }

    }

?>