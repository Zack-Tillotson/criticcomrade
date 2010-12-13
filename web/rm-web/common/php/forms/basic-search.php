<?php

    $search_term = urlencode($_POST['search-box']);

    $redirect_string = "http://" . $_SERVER['SERVER_NAME'] . "/search/" . $search_term . "/";

    $has_par = false;

    if($_POST['search-movie-titles'] == "true")
    {

        if(!$has_par)
        {
            $has_par = true;
            $redirect_string .= "?";
        }
        else
        {
            $redirect_string .= "&";
        }

        $redirect_string .= "movie-titles=true";

    }

    if($_POST['search-critic-names'] == "true")
    {

        if(!$has_par)
        {
            $has_par = true;
            $redirect_string .= "?";
        }
        else
        {
            $redirect_string .= "&";
        }

        $redirect_string .= "critic-names=true";

    }

    if($_POST['search-critic-publishers'] == "true")
    {

        if(!$has_par)
        {
            $has_par = true;
            $redirect_string .= "?";
        }
        else
        {
            $redirect_string .= "&";
        }

        $redirect_string .= "critic-pubs=true";

    }

    redirect($redirect_string);
    exit;
?>
