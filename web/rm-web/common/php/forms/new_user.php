<?php

    include_once $_SERVER['DOCUMENT_ROOT'] . '/common/php/db/db_util.php';

    // Get the variables
    $user = $_POST["username"];
    $pass = $_POST["password"];
    $pass2 = $_POST["password2"];
    $email = $_POST["email"];
    $age = $_POST["age"];
    $sex = $_POST["sex"];
    $firstname = $_POST["firstname"];
    $lastname = $_POST["lastname"];
    $rememberme = $_POST["rememberme"];
    $ref = $_POST['ref'];

    $result = db_new_full_user($user, $pass, $pass2, $email, $age, $sex, $firstname, $lastname);

    //If the variables are valid make a new user
    if($result)
    {
        
        if(isset($ref)) redirect($ref);
        else redirect("http://" . $_SERVER['SERVER_NAME'] . "/");
        
    }
    else
    {

        redirect("http://" . $_SERVER['SERVER_NAME'] . "/user/newuser/?ref=" . htmlentities($ref));

    }

?>