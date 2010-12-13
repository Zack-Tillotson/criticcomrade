<?php

    require_once('CCObject.php');

    class PersonalUser extends CCObject
    {

        var $rl;

        // Accessors

        function getUserName() { return $this->xa['user']['_c']['username']['_v']; }
        function getCreateDate() { return $this->xa['user']['_c']['create-date']['_v']; }
        function getEmail() { return $this->xa['user']['_c']['email']['_v']; }
        function getSex() { return $this->xa['user']['_c']['sex']['_v']; }
        function getAge() { return $this->xa['user']['_c']['age']['_v']; }
        function getFirstName() { return $this->xa['user']['_c']['first-name']['_v']; }
        function getLastName() { return $this->xa['user']['_c']['last-name']['_v']; }
        function getReviewCount() { return (int)($this->xa['user']['_c']['user-review-list']['_a']['review-count']); }
        function getRecentReviewCount() { return (int)($this->xa['user']['_c']['user-review-list']['_a']['recent-review-count']); }
        function getPositiveReviewCount() { return (int)($this->xa['user']['_c']['user-review-list']['_a']['positive-review-count']); }
        function isLongSession() { return isset($this->xa['user']['_a']['long-session']) ? $this->xa['user']['_a']['long-session'] == "true" ? true : false : false; }

        function getReviews()
        {
            if($this->rl == "") $this->rl = new UserReviewList($this->xa['user']['_c']);
            return $this->rl;
        }

        
        function isGuest() { return (isset($this->xa['user']['_a']['temporary']) ? $this->xa['user']['_a']['temporary'] : "false" == "true") ? true : false; }
        function isTemporary()
        {
            if($this->isErrorObject())
                return false;
            else if($this->isGuest())
                return false;
            else if($this->getUserName() == "")
                return true;
            else
                return false;
        }

        function isReal()
        {
            if($this->isGuest())
                return false;
            else if($this->getUserName() == "")
                return false;
            else
                return true;
        }

    }

?>