<?php

    include_once $_SERVER['DOCUMENT_ROOT'] . '/common/php/db/db_util.php';

    // Get the variables
    $user = $_POST["username"];
    $pass = $_POST["password"];
    $ref = $_POST['ref'];
    $rememberme = isset($_POST['rememberme'])?$_POST['rememberme']:"";

    // Check the variables for validity
    $is_valid = array();

    if(!db_login_user($user, $pass, ($rememberme == "on")))
        $is_valid['userpass'] = 1;

    // If the user is logged in
    if(sizeof($is_valid) == 0)
    {

        if($ref != "")
            redirect($ref);
        else
            redirect("http://" . $_SERVER['SERVER_NAME'] . "/");

    }
    else
    {

        //There has been an error with the form, go back to the calling form
        //and let it know what problem cropped up

        $error_string = "?";

        foreach($is_valid as $name=>$val)
            $error_string .= "$name=$val&";

        if($user != "") $error_string .= "inuser=$user&";
        if($ref != "") $error_string .= "inref=$ref&";

       // redirect("http://" . $_SERVER['SERVER_NAME'] . "/users/login/" . $error_string);

    }
exit;

?>
