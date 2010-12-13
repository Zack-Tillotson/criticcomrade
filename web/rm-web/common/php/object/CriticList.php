<?php

    require_once('Critic.php');
    require_once('CCObject.php');

    class CriticList extends CCObject
    {

        var $olist;

        // Accessors

        function getMaxSize() { return $this->xa['critic-list']['_a']['max-size']; }
        function getCount() { return $this->xa['critic-list']['_a']['count']; }
        function getOrderedBy() { return $this->xa['critic-list']['_a']['ordered-by']; }
        function getType() { return $this->xa['critic-list']['_a']['type']; }
        function getOffset() { return $this->xa['critic-list']['_a']['offset']; }
        function getFilter() { return $this->xa['critic-list']['_a']['filter']; }

        function getCritics()
        {
            if($this->olist == "")
                foreach(get_children($this->xa['critic-list']['_c']['critic']) as $m)
                {
                    $t['critic'] = $m;
                    $this->olist[] = new Critic($t);
                }
            return $this->olist;
        }

    }

?>