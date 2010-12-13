<?php

    require_once('CriticReviewList.php');
    require_once('CCObject.php');

    class Critic extends CCObject
    {

        var $crl;

        // Accessors

        function getCriticID() { return $this->xa['critic']['_c']['id']['_v']; }
        function getName() { return $this->xa['critic']['_c']['name']['_v']; }
        function getPublisher() { return $this->xa['critic']['_c']['publisher']['_v']; }
        function getComradery() { return isset($this->xa['critic']['_c']['comradery']['_v']) ? $this->xa['critic']['_c']['comradery']['_v'] : "-1"; }

        function getNameAndPublisher()
        {
            if($this->getName() != "")
                return $this->getName() . " | " . $this->getPublisher();
            else
                return $this->getPublisher();
        }
        
        function getReviewList()
        {
            if($this->crl == "") $this->crl = new CriticReviewList($this->xa['critic']['_c']);
            return $this->crl;
        }
        
    }

?>