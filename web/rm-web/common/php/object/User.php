<?php

    require_once('UserReviewList.php');
    require_once('CCObject.php');

    class User extends CCObject
    {

        var $url;

        // Accessors

        function getUserName() { return $this->xa['user']['_c']['username']['_v']; }
        function getCreateDate() { return $this->xa['user']['_c']['create-date']['_v']; }

        function getUserReviewList()
        {
            if($this->url == "") $this->url = new UserReviewList(get_children($this->xa['user']['_c']));
            return $this->url;
        }

    }

?>