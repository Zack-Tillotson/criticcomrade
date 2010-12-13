<?php

    include_once $_SERVER['DOCUMENT_ROOT'] . '/common/php/db/db_util.php';
    include_once $_SERVER['DOCUMENT_ROOT'] . '/common/php/session/session_util.php';

    // Get the variables
    $id = $_POST['id'];
    $user = $_POST['user'];
    $reason = $_POST['reason'];
    $ref = $_POST['ref'];

    // Add the report on the review thing to the DB
    $went_well = true;

    if($went_well) redirect($ref);

?>