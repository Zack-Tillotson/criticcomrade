<?php

require_once 'rm_config.php';
require_once 'pieces_util.php';
require_once 'session/session_util.php';
require_once 'rest/do_rest_call.php';
require_once 'object/MovieList.php';
require_once 'object/UserList.php';
require_once 'object/CriticList.php';
require_once 'object/PersonalUser.php';
require_once 'object/SearchResultList.php';

function init_rottenmeta()
{

    //Set the global include path
    if(substr($_SERVER['DOCUMENT_ROOT'], -1) == "/")
        $_SERVER['DOCUMENT_ROOT'] = substr($_SERVER['DOCUMENT_ROOT'], 0, strlen($_SERVER['DOCUMENT_ROOT'])-1);

    //Get our custom parsed request object
    $GLOBALS['RM_REQ'] = parse_request();

    // Ensure the session has a session id, unless it's a robot
    if(!is_robot_request()) ensure_session_started();

    // The random elements of the page will change every hour
    srand(date('G'));
  
}

function include_htm($file_name = "")
{

    if(file_exists($_SERVER['DOCUMENT_ROOT'] . "/common/htm/" . $file_name))
    {
        $rval = include $_SERVER['DOCUMENT_ROOT'] . "/common/htm/" . $file_name;
        if($rval)
        {
            echo "\n";
            return true;
        }
    }

    return false;
    
}

function include_piece($file_name = "")
{
    if(file_exists($_SERVER['DOCUMENT_ROOT'] . "/common/htm/pieces/" . $file_name))
    {
        $rval = include $_SERVER['DOCUMENT_ROOT'] . "/common/htm/pieces/" . $file_name;
        if($rval)
        {
            echo "\n";
            return true;
        }
    }

    return false;

}

function include_form($file_name = "")
{

    if(file_exists($_SERVER['DOCUMENT_ROOT'] . "/common/htm/form/" . $file_name))
    {
        $rval = include $_SERVER['DOCUMENT_ROOT'] . "/common/htm/form/" . $file_name;
        if($rval)
        {
            echo "\n";
            return true;
        }
    }
    return false;
    
}

function include_css()
{
    $file_name = $GLOBALS['RM_REQ']['URI'][3];
    if(file_exists($_SERVER['DOCUMENT_ROOT'] . "/common/css/" . $file_name))
        include $_SERVER['DOCUMENT_ROOT'] . "/common/css/" . $file_name;
}

function include_image()
{

    $file_name = "";
    for($i = 3 ; $i < sizeof($GLOBALS['RM_REQ']['URI']) ; $i++)
            $file_name .= "/" . $GLOBALS['RM_REQ']['URI'][$i];
    if(file_exists($_SERVER['DOCUMENT_ROOT'] . "/common/img/" . $file_name))
        include $_SERVER['DOCUMENT_ROOT'] . "/common/img/" . $file_name;

}

function image_exists($file_name = "")
{

    if($file_name == "")
        for($i = 3 ; $i < sizeof($GLOBALS['RM_REQ']['URI']) ; $i++)
            $file_name .= "/" . $GLOBALS['RM_REQ']['URI'][$i];

    return file_exists($_SERVER['DOCUMENT_ROOT'] . "/common/img" . $file_name);

}

function handle_form($file_name)
{

    if(file_exists($_SERVER['DOCUMENT_ROOT'] . "/common/php/forms/" . $file_name))
        include$_SERVER['DOCUMENT_ROOT'] . "/common/php/forms/" . $file_name;

}

function parse_request()
{

    //URI
    $uri = substr($_SERVER['REQUEST_URI'], 0, strpos($_SERVER['REQUEST_URI'], '?'));
    if(strlen($uri) == 0) $uri = $_SERVER['REQUEST_URI'];

    $uri_array = explode('/', $uri);
    if($uri_array[0] == "") array_shift($uri_array);    //Always a / at the start, can ignore
    array_unshift($uri_array, $uri);                    // Add the full uri to the start of the array
    
    //Parameters
    $pars = array();
    foreach(explode("&", $_SERVER['QUERY_STRING']) as $par)
    {
        if($par != "")
        {
            $parpair = explode("=", $par);
            $pars[strtolower($parpair[0])] = strtolower($parpair[1]);
        }
    }

    //Build return object
    $req = array();
    $req['METHOD'] = $_SERVER['REQUEST_METHOD'];
    $req['URI'] = $uri_array;
    $req['PARAMS'] = $pars;

    return $req;
    
}

function is_robot_request()
{

    if($GLOBALS['RM_REQ']['URI'][1] == "robots.txt") $_SESSION['IS_ROBOT'] = true;

    $is_robot = false;
    if(isset($_SESSION['IS_ROBOT'])) $is_robot = $_SESSION['IS_ROBOT'];

    return $is_robot;

}

function redirect($loc = "NOT VALID")
{

    if($loc == "NOT VALID") $loc = "http://" . $_SERVER['SERVER_NAME'];

    header("Location: " . $loc);

    exit(); //Just redirecting, don't print anything else out
    
}

function getRESTXML($url)
{
    return get_rest_xml($url);
}

?>