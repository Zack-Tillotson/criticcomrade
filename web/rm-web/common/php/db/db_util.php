<?php

include_once $_SERVER['DOCUMENT_ROOT'] . '/common/php/session/session_util.php';

function db_session_in_good_standing()
{
    db_refresh_session();
    $t = $GLOBALS['CC_API']['SESSION_DITTY'];
    return !($t->isErrorObject());
    
}

function db_refresh_session()
{
    $GLOBALS['CC_API']['SESSION_DITTY'] = new PersonalUser(xml2ary(getRESTXML($GLOBALS['RM_API_LOC'] . '/sessions/id/' . session_id() . '/?type=ditty')));
}

function db_get_username()
{

    $ui = $GLOBALS['CC_API']['SESSION_DITTY'];
    return $ui->isReal() ? $ui->getUserName() : ($ui->isGuest() ? "Guest" : "Trial User");
    
}

function db_login_user($user, $pass, $long_sess = false)
{

    $url = $GLOBALS['RM_API_LOC'] . '/sessions/id/' . session_id() . '/';
    $data = array("session"=>htmlentities
                            (
                                "<?xml version='1.0'?>" .
                                "<user-session>" .
                                    "<username>" . xmlentities($user) . "</username>" .
                                    "<password>" . xmlentities($pass) . "</password>" .
                                    "<long-session-option>" . ($long_sess ? "true" : "false") . "</long-session-option>" .
                                "</user-session>"
                            )
                 );

    $result = trim(post_rest_xml($url, $data));

    //if($result != "true") return false;
    
    if($long_sess) set_long_session_cookie();

    return true;
    
}

// This function will setup a session to be valid, as a guest. real users login to assoc their user
// to the session. guest sessions will be automatically upgraded to trial session when they do something
// which requires being a user
function db_start_session()
{

    $url = $GLOBALS['RM_API_LOC'] . '/sessions/id/' . session_id() . '/';
    $data = array("session"=>htmlentities
                            (
                                "<?xml version='1.0'?>\n" .
                                "<user-session />"
                            )
                 );
    $result = new CCObject(post_rest_xml($url, $data));

    db_refresh_session();

    if($result->isErrorObject()) return false;

    return true;

}

function db_logout_session()
{

    $url = $GLOBALS['RM_API_LOC'] . '/sessions/id/' . session_id() . '/';
    $data = array("session"=>htmlentities
                            (
                                "<?xml version='1.0'?>\n" .
                                "<user-session />"
                            )
                 );
    $result = new CCObject(post_rest_xml($url, $data));
    
    if($result->isErrorObject()) return false;

    return true;
    
}

function db_session_remember_me($sid = "")
{
    $t = $GLOBALS['CC_API']['SESSION_DITTY'];
    return $t->isLongSession();
    
}

function db_new_full_user($user, $pass, $pass2, $email = "", $age = "", $sex = "", $firstname = "", $lastname = "")
{

    if($pass != $pass2) return false;

    $url = $GLOBALS['RM_API_LOC'] . '/users/id/' . htmlentities($user) . '/sid/' . session_id() . '/';
    $data = array("user"=>htmlentities
                            (
                                "<?xml version='1.0'?>\n" .
                                "<user>" .
                                    "<username>" . xmlentities($user) . "</username>" .
                                    "<password>" . xmlentities($pass) . "</password>" .
                                    "<email>" . xmlentities($email) . "</email>" .
                                    "<age>" . xmlentities($age) . "</age>" .
                                    "<sex>" . xmlentities($sex) . "</sex>" .
                                    "<first-name>" . xmlentities($firstname) . "</first-name>" .
                                    "<last-name>" . xmlentities($lastname) . "</last-name>" .
                                "</user>"
                            )
                 );

    $result = post_rest_xml($url, $data);

    return substr($result, 0, 3) > 300 ? false : true;

}

?>