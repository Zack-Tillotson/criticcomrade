<?php

include_once 'HTTP/Client.php';
include_once $_SERVER['DOCUMENT_ROOT'] . '/common/php/object/Movie.php';

function get_rest_xml($url)
{

    $c = new HTTP_Client();

    $start_time = microtime(true);
    
    try
    {
        
        $rc = $c->get($url);
        $raw_xml = $c->_responses[0]['body'];

    }
    catch(Exception $e)
    {
        $raw_xml = "";
        echo "REST GET error"; exit;
    }

    $end_time = microtime(true);
    
    $GLOBALS['API_CALLS'][] = "<br>GET [" . ($end_time - $start_time) . " sec] $url";

    return $raw_xml;

}

function post_rest_xml($url, $data)
{

    $c = new HTTP_Client();

    $start_time = microtime(true);

    try
    {
        
        $rc = $c->post($url, $data);
        $raw_xml = $c->_responses[0]['body'];

    }
    catch(Exception $e)
    {
        $raw_xml = "";
        echo "REST POST error"; exit;
    }

    $end_time = microtime(true);

    $GLOBALS['API_CALLS'][] = "<br>POST [" . ($end_time - $start_time) . " sec] $url";

    return $raw_xml;
    
}

function put_rest_xml($url, $data)
{

    $c = new HTTP_Client();

    try
    {

        $rc = $c->put($url, $data);
        $raw_xml = $c->_responses[0]['body'];

    }
    catch(Exception $e)
    {
        $raw_xml = "";
        echo "REST POST error"; exit;
    }

    return $raw_xml;

}

function delete_rest_xml($url)
{

    $c = new HTTP_Client();

    try
    {

        $rc = $c->delete($url);
        $raw_xml = $c->_responses[0]['body'];

    }
    catch(Exception $e)
    {
        $raw_xml = "";
        echo "REST POST error"; exit;
    }

    return $raw_xml;

}

?>