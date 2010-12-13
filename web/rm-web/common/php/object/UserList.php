<?php

    require_once('User.php');
    require_once('CCObject.php');

    class UserList extends CCObject
    {

        var $olist;

        // Accessors

        function getMaxSize() { return $this->xa['user-list']['_a']['max-size']; }
        function getCount() { return $this->xa['user-list']['_a']['count']; }
        function getOrderedBy() { return $this->xa['user-list']['_a']['ordered-by']; }
        function getType() { return $this->xa['user-list']['_a']['type']; }
        function getOffset() { return $this->xa['user-list']['_a']['offset']; }
        function getFilter() { return $this->xa['user-list']['_a']['filter']; }
        
        function getUsers()
        {
            if($this->olist == "")
                foreach(get_children($this->xa['user-list']['_c']['user']) as $m)
                {
                    $t['user'] = $m;
                    $this->olist[] = new User($t);
                }
            return $this->olist;
        }

    }

?>