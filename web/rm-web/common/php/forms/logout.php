<?php

    include_once $_SERVER['DOCUMENT_ROOT'] . '/common/php/db/db_util.php';
    include_once $_SERVER['DOCUMENT_ROOT'] . '/common/php/session/session_util.php';

    // Get the variables
    $ref = $_POST['ref'];

    db_logout_session();            // Get rid of the SID in the DB
    session_regenerate_id(true);    // Get a new SID

    redirect($ref);
    
?>