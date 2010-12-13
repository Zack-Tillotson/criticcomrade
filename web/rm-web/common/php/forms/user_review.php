<?php

    include_once $_SERVER['DOCUMENT_ROOT'] . '/common/php/db/db_util.php';
    include_once $_SERVER['DOCUMENT_ROOT'] . '/common/php/session/session_util.php';
    include_once $_SERVER['DOCUMENT_ROOT'] . '/common/php/rest/xml2array.php';

    // Get the variables
    $mid = $_POST['mid'];
    $sid = session_id();
    $score = $_POST['score'];
    $words = $_POST['words'];
    $date = Date('M j, Y');
    
    $ref = $_POST['ref'];

    // Add the report on the review thing to the DB
    $went_well = true;

    $url = $GLOBALS['RM_API_LOC'] . '/sessions/id/' . $sid . '/id/' . $mid . '/';
    $data = array("review"=>htmlentities
                            (
                                "<?xml version='1.0'?>\n" .
                                "<one-review>" .
                                    "<score>$score</score>" .
                                    "<summary>" . xmlentities($words) . "</summary>" .
                                    "<review-date>$date</review-date>" .
                                "</one-review>"
                            )
                 );

    $result = post_rest_xml($url, $data);

    $went_well = true;
    if(substr($result, 0, 3) > 1) $went_well = false;

    if($went_well) redirect($ref);

?>