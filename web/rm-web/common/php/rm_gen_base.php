<?php

include_once "rm_util.php";
require_once "rest/xml2array.php";

////////////////////////////////////////////////////////////////////////////////
// gen_page
//used to generate the response to all querries. passes off to do_get or do_post
function gen_page()
{

    // Initialzes session if needed, parses request, etc
    init_rottenmeta();

    // Generate the header
    //      could be 200, 404, or 302/303
    header_and_non_http();

    // Build the result page
    if($GLOBALS['RM_REQ']['METHOD'] == "GET") do_get();
    elseif ($GLOBALS['RM_REQ']['METHOD'] == "POST") do_post();

}
// gen_page
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// do_header
//  sends the header for the response
//  if the request is not valid it will send a 404
//  if the requested page has moved it will send a 301/302
//  otherwise, it sends a 200
function header_and_non_http()
{

    if(is_bad_request())            // Filter out bad requests
    {
        error_header();
    }
    elseif(is_redirected_page())    // Redirect redirect pages
    {
        redirect_header();
        exit(0);
    }
    elseif(is_image_request())      // Image request
    {
        image_header();
        include_image();
        exit(0);
    }
    elseif(is_css_request())        // CSS request
    {
        css_header();
        include_css();
        exit(0);
    }
    else                            //For normal pages
    {
        normal_header();
    }

}
// do_header
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// is_redirected_page
function is_redirected_page()
{

    return false;

}
// is_redirected_page
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// is_bad_request
//  checks request to ensure it is valid. valid doesn't mean that the request
//  will get a good page, just that it won't break anything and doesn't deserve
//  an error page.
//      eg. GET www.rottenmeta.com/movie/MySuperMovie/ will be valid, even if
//      there is no movie called MySuperMovie
function is_bad_request()
{

    // Ensure the request method is either PUT or GET
    if($GLOBALS['RM_REQ']['METHOD'] != "GET" && $GLOBALS['RM_REQ']['METHOD'] != "PUT")
        return 1;

    // Ensure the page requested is in the site map
    $found = false;
    
    switch($GLOBALS['RM_REQ']['URI'][1])
    {
        case "index.html":
        case "index.php":
        case "robots.txt":
        case "about":
        case "faq":
        case "":
            if($GLOBALS['RM_REQ']['URI'][2] == "")
                $found = true;
            else
                $found = false;
            break;
        case "movies":
            $found = true;
            break;
        case "critics":
            $found = true;
            break;
        case "common":
            if($GLOBALS['RM_REQ']['URI'][2] == "img" &&
               $GLOBALS['RM_REQ']['URI'][3] != "")
            {
                $found = true;
            }
            else if($GLOBALS['RM_REQ']['URI'][2] == "css" &&
                    $GLOBALS['RM_REQ']['URI'][3] != "")
            {
                $found = true;
            }
            else
            {
                $found = false;
            }
            break;
        case "users":
            $found = true;
            break;
        case "form":
            if($GLOBALS['RM_REQ']['URI'][3] == "")
                $found = true;
            else
                $found = false;
            break;
        case "search":
            if(sizeof($GLOBALS['RM_REQ']['URI']) > 3)
                $found = false;
            else
                $found = true;
        default:
            $found = false;
            break;
    }

    if($found == false)
        return 2;

    return false;
    
}
// is_bad_request
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// is_image_request
function is_image_request()
{

    $image_path = "";

    if($GLOBALS['RM_REQ']['URI'][1] == "common" &&
       $GLOBALS['RM_REQ']['URI'][2] == "img" &&
       $GLOBALS['RM_REQ']['URI'][3] != "" &&
       image_exists())
    {
        return true;
    }
    else
    {
        return false;
    }
    
}
// is_image_request
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// is_css_request
function is_css_request()
{

    if($GLOBALS['RM_REQ']['URI'][1] == "common" &&
       $GLOBALS['RM_REQ']['URI'][2] == "css" &&
       $GLOBALS['RM_REQ']['URI'][3] != "" &&
       sizeof($GLOBALS['RM_REQ']['URI']) == 4)

    {
        return true;
    }
    else
    {
        return false;
    }

}
// is_css_request
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// error_header
//  sends an error header
function error_header()
{

    header("HTTP/1.0 404 File Not Found");
    header("X-Powered-By: Bad page requests, thanks.");

}
// error_header
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// redirect_header
//  sends a redirected header
function redirect_header()
{

    redirect();

}
// redirect_header
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// normal_header
//  sends a regular header
function normal_header()
{

    header("X-Powered-By: Cute monkies, red hats, and loud cymbals");

}
// normal_header
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// image header
//  sends an image header
function image_header()
{

    $type = explode('.', $GLOBALS['RM_REQ']['URI'][sizeof($GLOBALS['RM_REQ']['URI']) - 1]);
    $type = $type[sizeof($type)-1];

    header("X-Powered-By: Hypno-toad");
    header("Content-type: img/" . $type);

}
// image header
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// css header
//  sends an css header
function css_header()
{

    header("X-Powered-By: Jack Bauer. Yes, we're that good.");
    header("Content-type: text/css");

}
// css header
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// do_get
//  handles get requests
//  calls:
//      get_header()
//      get_title()
//      get_left_sidebar()
//      get_main_section()
//      get_right_sidebar()
//      get_footer()
function do_get()
{

    if($GLOBALS['RM_REQ']['URI'][0] == "/robots.txt")   // The robots.txt file
        include_htm("robots.txt");
    else
        include_htm("get_template.htm");

}
// do_get
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// do_post
//  handles post requests
function do_post()
{

    // If it's a post that doesn't start with '/form/', redirect to error page
    if($GLOBALS['RM_REQ']['URI'][1] != "form")
        redirect("http://" . $_SERVER['SERVER_NAME'] . "/error/");

    // It should only have 2 words
    if($GLOBALS['RM_REQ']['URI'][3] != "")
        redirect("http://" . $_SERVER['SERVER_NAME'] . "/error/");

    switch($GLOBALS['RM_REQ']['URI'][2])
    {

        case "new_user":
            handle_form("new_user.php");
            break;

        case "login":
            handle_form("login.php");
            break;

        case "logout":
            handle_form("logout.php");
            break;

        case "report_user_review":
            handle_form("report_user_review.php");
            break;

        case "user_review":
            handle_form("user_review.php");
            break;

        case "user_quick_rate":
            handle_form("user_review_many.php");
            break;

        case "basic_search":
            handle_form("basic-search.php");
            break;
        
        default:
            redirect("http://" . $_SERVER['SERVER_NAME'] . "/error/");
            break;

    }

    redirect(); // Will only be reached if form didn't redirect

}
// do_post
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// Generate common pieces of the site //////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

function gen_header()
{
    include_htm("head.htm");
}

function gen_title()
{
    include_htm("title.htm");
}

function gen_left_sidebar()
{
    include_htm("left_sidebar.htm");
}

function gen_main_section()
{

    $went_well = true;
 
    switch($GLOBALS['RM_REQ']['URI'][1])
    {
        case "":
        case "index.html":
        case "index.php":
            $went_well = gen_main_home();
            break;
        case "movies":
            $went_well = gen_main_movie();
            break;
        case "critics":
            $went_well = gen_main_critic();
            break;
        case "users":
            $went_well = gen_main_user();
            break;
        case "about":
            $went_well = gen_about();
            break;
        case "faq":
            $went_well = gen_faq();
            break;
        case "search":
            $went_well = gen_search();
            break;
        default:
            $went_well = false;
            break;
    }

    if(!$went_well)
    {
        error_header();
        gen_main_error();   // If there was an error displaying a page, show an error
    }

}

function gen_right_sidebar()
{

    include_htm("right_sidebar.htm");

}

function gen_footer()
{

    include_htm("footer.htm");
    
}

////////////////////////////////////////////////////////////////////////////////
// Generate main sections //////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// gen_about
//      just the about page
function gen_about()
{
    if($GLOBALS['RM_REQ']['URI'][2] != "") return 0;
    else return include_htm("main/about.htm");
}
// gen_about
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// gen_faq
//      just the faq page
function gen_faq()
{
    if($GLOBALS['RM_REQ']['URI'][2] != "") return 0;
    else return include_htm("main/faq.htm");
}
// gen_faq
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// gen_main_error
//      handles error requests
function gen_main_error()
{
    
    include_htm("main/error.htm");

}
// gen_main_error
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// gen_main_home
//      the main page
function gen_main_home()
{

    if($GLOBALS['RM_REQ']['URI'][2] != "") return 0;
    else return include_htm("main/index.htm");

}
// gen_main_home
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// gen_main_movie
//      the main movie section
//      map:
//          /movie/                     movie section
//          /movie/id/<movie>/          a movie's page
//          /movie/id/<movie>/reviews/  all the reviews for a movie
function gen_main_movie()
{

    $htm = "main/error.htm";

    if($GLOBALS['RM_REQ']['URI'][2] == "all")
        $htm = "main/movie/all.htm";                        // Requesting all the movies
    elseif($GLOBALS['RM_REQ']['URI'][2] == "id")
        if($GLOBALS['RM_REQ']['URI'][3] == "")
            $htm = "main/movie/search.htm";                 // Requesting a certain movie but no id
        else
            if($GLOBALS['RM_REQ']['URI'][4] == "")
                $htm = "main/movie/one/index.htm";          // Requesting a certain movie
            elseif($GLOBALS['RM_REQ']['URI'][4] == "reviews")
                $htm = "main/movie/one/reviews.htm";        // Requesting a certain movie's reviews
            else $htm = "main/error.htm";
    elseif($GLOBALS['RM_REQ']['URI'][2] == "opening")
        $htm = "main/movie/opening.htm";                    // Opening movies
    elseif($GLOBALS['RM_REQ']['URI'][2] == "in-theaters")
        $htm = "main/movie/in-theaters.htm";                // In Theaters movies
    elseif($GLOBALS['RM_REQ']['URI'][2] == "coming-up")
        $htm = "main/movie/coming-up.htm";                // Coming up movies
    elseif($GLOBALS['RM_REQ']['URI'][2] == "")
        $htm = "main/movie/index.htm";                      // General movie section

    return include_htm($htm);

}
// gen_main_movie
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// gen_main_critic
//      the main critic section
//      map:
//          /critic/                    critic section
//          /critic/active/             active critics
//          /critic/<critic>/           a critic's page
//          /critic/<critic>/<movie>/   a critic's review of a movie
function gen_main_critic()
{

    $htm = "main/error.htm";

    if($GLOBALS['RM_REQ']['URI'][2] == "recent")
        $htm = "main/critic/recent.htm";                // Latest reviews
    elseif ($GLOBALS['RM_REQ']['URI'][2] == "id")
        if($GLOBALS['RM_REQ']['URI'][3] == "")
            $htm = "main/critic/search.htm";            // Requesting a critic but not id
        else
            if($GLOBALS['RM_REQ']['URI'][3] != "")
                if($GLOBALS['RM_REQ']['URI'][4] == "")
                    $htm = "main/critic/one/index.htm";           // A critic
                elseif($GLOBALS['RM_REQ']['URI'][4] == "reviews")
                    if(sizeof($GLOBALS['RM_REQ']['URI']) == 7 && $GLOBALS['RM_REQ']['URI'][5] != "")
                        $htm = "main/critic/one/one-review.htm";   // A critic's reviews
                    else
                        $htm = "main/error.htm";
                else
                    $htm = "main/error.htm";
            else
                $htm = "main/error.htm";
    else
        $htm = "main/critic/index.htm";                 // Critic section

    return include_htm($htm);

}
// gen_main_critic
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// gen_main_user()
//      shows the form which allows a user to sign up
function gen_main_user()
{

    $htm = "main/error.htm";

    if($GLOBALS['RM_REQ']['URI'][2] == "")
        $htm = "main/error.htm";
    elseif($GLOBALS['RM_REQ']['URI'][2] == "new-user")
        $htm = "form/new_user.htm";
    elseif($GLOBALS['RM_REQ']['URI'][2] == "login")
        $htm = "form/login.htm";
    elseif($GLOBALS['RM_REQ']['URI'][2] == "tutorial")
        if($GLOBALS['RM_REQ']['URI'][3] == "step-1" &&
           sizeof($GLOBALS['RM_REQ']['URI']) == 5)
            $htm = "main/user/tutorial/step1.htm";
        elseif($GLOBALS['RM_REQ']['URI'][3] == "step-2" &&
           sizeof($GLOBALS['RM_REQ']['URI']) == 5)
            $htm = "main/user/tutorial/step2.htm";
        elseif($GLOBALS['RM_REQ']['URI'][3] == "step-3" &&
           sizeof($GLOBALS['RM_REQ']['URI']) == 5)
            $htm = "main/user/tutorial/step3.htm";
        else
            $htm = "main/error.htm";
    elseif($GLOBALS['RM_REQ']['URI'][2] == "my")
        if($GLOBALS['RM_REQ']['URI'][3] == "")
            $htm = "main/user/home/index.htm";
        elseif($GLOBALS['RM_REQ']['URI'][3] == "reviews")
            if($GLOBALS['RM_REQ']['URI'][4] == "")
                $htm = "main/user/home/reviews.htm";
            elseif($GLOBALS['RM_REQ']['URI'][4] == "id" &&
                   $GLOBALS['RM_REQ']['URI'][5] != "" &&
                   $GLOBALS['RM_REQ']['URI'][6] == "edit")
                $htm = "main/user/home/review_edit.htm";
            elseif($GLOBALS['RM_REQ']['URI'][4] == "quick-rate" &&
                   sizeof($GLOBALS['RM_REQ']['URI']) == 6)
                $htm = "main/user/home/quick_rate.htm";
            else
                $htm = "main/error.htm";
        elseif($GLOBALS['RM_REQ']['URI'][3] == "critics")
            $htm = "main/user/home/critics.htm";

    return include_htm($htm);
    
}
// gen_main_user()
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// gen_search()
function gen_search()
{

    return include_htm("main/search/index.htm");

}
// gen_search()
////////////////////////////////////////////////////////////////////////////////
?>