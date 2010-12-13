<?php

include_once $_SERVER['DOCUMENT_ROOT'] . '/common/php/db/db_util.php';

function ensure_session_started()
{

    // Make sure the user has a session cookie
    session_set_cookie_params(60*15);   //Session cookies last 15 minutes
    session_start();

    // Check DB's session table for this session. If it present and recent then
    // refresh its experation timestamp. Otherwise, register a new session in
    // the db. Users will only be created if the user does a user based action
    // (eg reviews a movie).
    if(db_session_in_good_standing())
    {

        // We only need to refresh sessions that expire
        if(!db_session_remember_me())
        {
            refresh_session_cookie();
        }
        
    }
    else
    {
        db_start_session(session_id());
    }

}

function refresh_session_cookie()
{

    setcookie
        (
            ini_get("session.name"),
            session_id(),
            time() + ini_get("session.cookie_lifetime"),
            ini_get("session.cookie_path"),
            ini_get("session.cookie_domain"),
            ini_get("session.cookie_secure"),
            ini_get("session.cookie_httponly")
        );
}

function set_long_session_cookie()
{

        setcookie
        (
            ini_get("session.name"),
            session_id(),
            time() + 60 * 60 * 24 * 365 * 25,  // 25 years
            ini_get("session.cookie_path"),
            ini_get("session.cookie_domain"),
            ini_get("session.cookie_secure"),
            ini_get("session.cookie_httponly")
        );

}

function delete_session_cookie()
{
    setcookie
        (
            ini_get("session.name"),
            session_id(),
            time()-3600,
            ini_get("session.cookie_path"),
            ini_get("session.cookie_domain"),
            ini_get("session.cookie_secure"),
            ini_get("session.cookie_httponly")
        );
}

?>