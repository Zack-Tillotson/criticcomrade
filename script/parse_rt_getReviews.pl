#!/usr/bin/perl
# This program is called with 1 input - the input file
# this input file is like http://www.rottentomatoes.com/m/pray_the_devil_back_to_hell/?page=1&critic=columns&sortby=date&name_order=asc&view=text#contentReviews
# the output is like this:
# 	Movie title
# 	Movie Summary
# 	Movie release date
# 	Critic name
# 	Critic publisher
# 	Critic review summary
# 	Critic review link
# 	Critic review score
# 	<repeat critic info until end>


if(@ARGV != 1)
{
	usage();
	exit();
}

my $file = $ARGV[0]; 

sub usage
{
	print "Usage: 	perl parse_rt_getReviews.pl <file name>\n";
	print "		file name = name of input file name which is like http://www.rottentomatoes.com/m/pray_the_devil_back_to_hell/".
	      "?page=1&critic=columns&sortby=date&name_order=asc&view=text\n";
}

open(INFILE, $file) or die("Error: could not open input file - $file");

$atList = 0;
while($line = <INFILE>)
{

	chomp($line);

	if($atList == 0 && $line =~ /movie_title clearfix">(.*)<\/h1>/)
	{
		$atList = 1;

		$movie_title = $1;
		$movie_title =~ s/&#039;/\\'/g;
		$movie_title =~ s/&amp;/&/g;
		$movie_title =~ s/'/\\'/g;
		$movie_title =~ s/\([0-9]{4}\)$//g;
		$movie_title =~ s/ \s*$//g;
#		$movie_title =~ s/^The (.*)/$1, The/g;
	}

	if($atList == 1 && $line =~ /id="movie_stats"/)
	{
		$atList = 2;
	}

	if($atList == 2 && $line =~/Rated/)
	{
		$atList = 2.1;
	}

	if($atList == 2.1 && $line =~ /class="content">(.*) <a.*movie_rating_reason/)
	{
		$atList = 2.2;
		$movie_rating = $1;
	}

	if($atList == 2.1 && $line =~ /class="content">(.*) *<\/span>.*$/)
	{
		$atList = 2.2;
		$movie_rating = $1;
	}

	if($atList == 2.2 && $line =~ /movie_rating_reason.*display: none"> (.*)<\/span>.*<\/span>/)
	{
		$atList = 2;
		$movie_rating_reason = $1;
	}

	if($atList == 2.2 && $line =~ /class="label"/)
	{
		$atList = 2;
	}


	if($atList == 2 && $line =~ /Theatrical Release:<\/span> ?<span class="content">(... ..?, ....).*<\/span>/)
	{
		$movie_release_date = $1;
	}

	if($atList == 2 && $line =~ /Theatrical Release:<\/span> ?<span class="content">([0-9]{4})\s*<\/span>/)
	{
		$movie_release_date = "Jan 1, " .$1;
	}

	if($atList == 2 && $line =~ /class="label">Genre:<\/span> <span class="content">(.*)<\/span><\/p>/)
	{
		$movie_genres = $1;
		$movie_genres =~ s/<a href="\/movie\/browser\.php\?genre=[0-9]*">//g;
		$movie_genres =~ s/<\/a>//g;
		$movie_genres =~ s/\//, /g;
		$movie_genres =~ s/&/\\&/g;
	}

	if($atList == 2 && $line =~ /id="movie_synopsis_blurb" style="display: inline;"> (.*)<\/span>/)
	{
		$movie_summary_short = $1;
	}

	if($atList == 2 && $line =~ /id="movie_synopsis_all" style="display: none;">(.*)(<\/span>)?/)
	{
		$atList = 2.5;

		$movie_summary = $1;

		if($movie_summary =~ /.*(<\/span>)/)
		{
			$atList = 2.75;

			$movie_summary =~ s/<\/span>$//;
		}

	}

	if($atList == 2.5 && $line !~ /id="movie_synopsis_all" style="display: none;">(.*)(<\/span>)?/)
	{

		if($line =~ /(.*)<\/span>/)
		{

			$atList = 2.75;

			$movie_summary = $movie_summary . " " . $1;

		}
		else
		{
			$movie_summary = $movie_summary . " " . $line;
		}

	}

	if($atList <= 2.75 && ($line =~ /movie_cast_all/ || $line =~ /movie_crew_all/))
	{

		$movie_director = "";
		$movie_stars = "";
		$movie_writers = "";
		$movie_studio = "";

		if($line =~ /<p class="movie_cast_all" style="display: none;"><span class="label">Starring:<\/span>(.*)/)
		{

			$movie_stars = $1;

			$movie_stars =~ s/<a href="[^ ]*">//g;
			$movie_stars =~ s/<\/a>//g;
			$movie_stars =~ s/<\/p>.*//g;
			$movie_stars =~ s/'/\\'/g;
			$movie_stars =~ s/&#039;/\\'/g;
		}

		if($line =~ /<p class="movie_crew_all".*<span class="label">Director:<\/span>(.*)/)
		{

			$movie_director = $1;

			$movie_director =~ s/<a href="[^ ]*">//g;
			$movie_director =~ s/<\/a>//g;
			$movie_director =~ s/<\/p>.*//g;
			$movie_director =~ s/<br ?\/?>.*//g;
			$movie_director =~ s/<\/span ?>.*//g;
			$movie_director =~ s/'/\\'/g;
			$movie_director =~ s/&#039;/\\'/g;
		}

		if($line =~ /<p class="movie_crew_all".*<span class="label">Screenwriter:<\/span>(.*)/)
		{

			$movie_writers = $1;

			$movie_writers =~ s/<a href="[^ ]*">//g;
			$movie_writers =~ s/<\/a>//g;
			$movie_writers =~ s/<\/p>.*//g;
			$movie_writers =~ s/<br ?\/?>.*//g;
			$movie_writers =~ s/<\/span ?>.*//g;
			$movie_writers =~ s/&#039;/\\'/g;
			$movie_writers =~ s/'/\\'/g;
			$movie_writers =~ s/&#039;/\\'/g;
		}
		
		if($line =~ /<p class="movie_crew_all".*<span class="label">Studio:<\/span>(.*)/)
		{

			$movie_studio = $1;

			$movie_studio =~ s/<a href="[^ ]*">//g;
			$movie_studio =~ s/<\/a>//g;
			$movie_studio =~ s/<\/p>.*//g;
			$movie_studio =~ s/<br ?\/?>.*//g;
			$movie_studio =~ s/<\/span ?>.*//g;
			$movie_studio =~ s/'/\\'/g;
			$movie_studio=~ s/&#039;/\\'/g;

		}
		
	}

	if($atList >= 2 && $atList < 2.99 && $line =~ /text\/javascript/)
	{
		$atList = 2.99;
	}

	if($atList == 2.99 || ($atList < 3 && $line =~ /ratingText/))
	{
		$atList = 3;
		
		$movie_summary =~ s/'/\\'/g;
		$movie_summary =~ s/&#039;/\\'/g;
		$movie_summary =~ s/&#034;/"/g;
		$movie_summary =~ s/&amp;/&/g;

		$movie_summary_short =~ s/'/\\'/g;
		$movie_summary_short =~ s/&#039;/\\'/g;
		$movie_summary_short =~ s/&#034;/"/g;
		$movie_summary_short =~ s/&amp;/&/g;

		if(length($movie_summary_short) == 0 && length($movie_summary) > 0)
		{
			$movie_summary_short = substr($movie_summary, 0, 300);
			$movie_summary_short =~ s/ [^ ]$/.../;
		}

		print $movie_title . "\n";
		print $movie_summary_short . "\n";
		print $movie_summary . "\n";
		print $movie_release_date . "\n";
		print $movie_studio . "\n";
		print $movie_rating . "\n";
		print $movie_rating_reason . "\n";
		print $movie_stars . "\n";
		print $movie_genres . "\n";
		print $movie_writers . "\n";
		print $movie_director . "\n";
	}

	if($atList == 3 && $line =~ /ratingText/)
	{
		$atList = 4;
	}

	if($atList == 4 && $line =~ /http:\/\/images.rottentomatoes.com\/images\/tomato\/(.*).gif"/)
	{
		$atList = 5;

		if($1 eq "fresh")
		{
			$review_score = 1;
		}
		else
		{
			$review_score = 0;
		}
	}

	if($atList == 5 && $line =~ /author\/author.*">(.*)<\/a>/)
	{
		$atList = 6;

		$review_name = $1;
		$review_name =~ s/'/\\'/g;
		$review_name =~ s/&#039;/\\'/g;
	}

	if($atList == 6 && $line =~ /<td>$/)
	{
		$atList = 7;
	}

	if($atList == 7 && $line =~ /<p>(.*)<\/p>/)
	{
		$atList = 8;

		$review_summary = $1;
		$review_summary =~ s/'/\\'/g;
		$review_summary =~ s/&#039;/\\'/g;
	}

	if($atList == 8 && $line =~ /<a rel="nofollow" target="_blank" href="(.*)" rel="nofollow">Full Review/)
	{
		$atList = 9;

		$review_link = $1;
	}

	if($atList == 9 && $line =~ /<span class="date">(...\., .. ....) ..:.. ..<\/span>/)
	{
		$atList = 10;

		$review_date = $1;
		$review_date =~ s/,//;
		$review_date =~ s/\.//;
	}

	if($atList == 10 && $line =~ /<td class="category"><p><a href="\/source.*\/">(.*)<\/a>/)
	{

		$atList = 3;

		$review_publisher = $1;
		$review_publisher =~ s/'/\\'/g;
		$review_publisher =~ s/&#039;/\\'/g;

		print $review_name . "\n";
		print $review_publisher . "\n";
		print $review_summary . "\n";
		print $review_link . "\n";
		print $review_score . "\n";
		print $review_date . "\n";

	}
}

close(INFILE);
