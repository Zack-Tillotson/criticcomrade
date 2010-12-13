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
	print "		file name = name of input file name which is like http://www.rottentomatoes.com/m/pray_the_devil_back_to_hell/?page=1&critic=columns&sortby=date&name_order=asc&view=text\n";
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
		$movie_title =~ s/'/\\'/g;
		$movie_title =~ s/\([0-9]{4}\)$//g;
		$movie_title =~ s/ \s*$//g;
		$movie_title =~ s/^The (.*)/$1, The/g;
	}

	if($atList == 1 && $line =~ /Theatrical Release:<\/span> ?<span class="content">(... ..?, ....).*<\/span>/)
	{
		$atList = 2;

		$movie_release_date = $1;
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

	if($atList == 2.75 || ($atList < 3 && $line =~ /ratingText/))
	{
		$atList = 3;
		
		$movie_summary =~ s/'/\\'/g;
		$movie_summary =~ s/&#039;/\\'/g;
		$movie_summary =~ s/&#034;/"/g;

		print $movie_title . "\n";
		print $movie_summary . "\n";
		print $movie_release_date . "\n";
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
