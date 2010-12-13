<?php

    class CCObject
    {

        var $xa;    // The XML Array

        function __construct($inxa) { $this->xa = $inxa; }

        function isErrorObject() { return (isset($this->xa['cc-error']) && sizeof($this->xa['cc-error']) != 0); }
        function getErrorCode() { return $this->xa['cc-error']['_c']['error-code']['_v']; }
        function getErrorMessage() { return $this->xa['cc-error']['_c']['error-message']['_v']; }


    }

?>