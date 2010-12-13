<?php

    require_once('UserReview.php');
    require_once('CCObject.php');

    class UserReviewList extends CCObject
    {

        var $vo;

        // Accessors

        function getReviewCount() { return $this->xa['user-review-list']['_a']['review-count']; }
        function getPositiveReviewCount() { return $this->xa['user-review-list']['_a']['positive-review-count']; }
        function getRecentReviewCount() { return $this->xa['user-review-list']['_a']['recent-review-count']; }

        function getReviews()
        {
            if($this->vo == "")
                foreach(get_children($this->xa['user-review-list']['_c']['user-review']) as $cr)
                    $this->vo[] = new UserReview(array("user-review"=>$cr));
            return $this->vo;
        }

    }

?>