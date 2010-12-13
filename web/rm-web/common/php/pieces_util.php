<?php

    function print_movie_list($ml, $type)
    {
        $GLOBALS['ml'] = $ml;
        include_piece("movie-$type-list.htm");
    }

    function print_movie($m, $type)
    {
        $GLOBALS['m'] = $m;
        include_piece("movie-$type.htm");
    }

    function print_critic_reviews($m, $type)
    {
        $GLOBALS['m'] = $m;
        include_piece("critic-review-list-$type.htm");
    }

    function print_critic_list($cl, $type)
    {
        $GLOBALS['cl'] = $cl;
        include_piece("critic-$type-list.htm");
    }

    function print_critic($c, $type)
    {
        $GLOBALS['c'] = $c;
        include_piece("critic-$type.htm");
    }

    function print_critic_review($cr, $type)
    {
        $GLOBALS['cr'] = $cr;
        $GLOBALS['type'] = $type;
        include_piece("critic-review.htm");
    }

    function print_critic_movies($c, $type)
    {
        $GLOBALS['c'] = $c;
        include_piece("critic-movies-list-$type.htm");
    }

    function print_user_review_list($url, $type)
    {
        $GLOBALS['url'] = $url;
        include_piece("user-review-list-$type.htm");
    }

    function print_user_review($ur, $type)
    {
        $GLOBALS['ur'] = $ur;
        $GLOBALS['type'] = $type;
        include_piece("user-review.htm");
    }

    function print_movie_stats($m)
    {
        $GLOBALS['m'] = $m;
        include_piece("movie-stats.htm");
    }

?>