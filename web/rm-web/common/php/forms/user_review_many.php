<?php

    include_once $_SERVER['DOCUMENT_ROOT'] . '/common/php/db/db_util.php';
    include_once $_SERVER['DOCUMENT_ROOT'] . '/common/php/session/session_util.php';
    include_once $_SERVER['DOCUMENT_ROOT'] . '/common/php/rest/xml2array.php';

    // Get the variables
    $number_reviews = $_POST['num-movies'];
    $ref = $_POST['ref'];
    $sid = session_id();
    $date = Date('M j, Y');

    $revs = array();
    for($i = 1; $i <= $number_reviews; $i++)
    {

        $id = $_POST["id-$i"];
        $score = $_POST["score-$i"];

        if($score != "-")
        {
            $revs[] = array("id"=>$id, "score"=>$score);
        }

    }

    $xml_data =
        "<?xml version='1.0'?>" .
        "<user>" .
            "<review-list>";
    foreach($revs as $rev)
    {
        $xml_data .=
            "<review>" .                
                "<id>" . $rev['id'] . "</id>" .
                "<score>" . $rev['score'] . "</score>" .
                "<review-date>$date</review-date>" .
            "</review>";
    }
    $xml_data .=
            "</review-list>" .
        "</user>";

    // Add the report on the review thing to the DB
    $went_well = true;

    $url = $GLOBALS['RM_API_LOC'] . '/sessions/id/' . $sid . '/id/';
    $data = array("user"=>htmlentities($xml_data));

    $result = post_rest_xml($url, $data);

    redirect($ref);

?>