#!/usr/bin/perl
# This program is called with 1 input - the input file
# this input file is like http://www.imdb.com/title/<movie code>/
# the output is the movie information in this format:
# 	Movie title
# 	Movie Summary
# 	Movie release date
# 	Movie rating
# 	Movie rating reason
# 	Movie genres (comma seperated)
# 	Movie writers (")
# 	Movie director (")
# 	Movie promo poster link


if(@ARGV == 0 || @ARGV > 1)
{
	usage();
	exit();
}

my $i = 0;
my $file = "";
foreach my $parm (@ARGV)
{
	$file = $parm;
	$i++;
}

if($i > 1)
{
	usage();
	exit();
}

sub usage
{
	print "Usage: 	perl parse_imdb_getMovie.pl <file name>\n";
	print "		file name = name of input file name which is http://www.imdb.com/title/<movie code>/"
}

open(INFILE, $file) or die("Error: could not open input file - $file");

my $film_name;
my $film_release_date;
my $film_summary;
my $film_rating;
my $film_rating_explanation;
my $film_starring;
my $film_genres;
my $film_writers;
my $film_director;
my $film_poster_link;

$atList = 0;
while($line = <INFILE>)
{

	chomp($line);

	if($line =~ /<h5>Directors?/) { $atList = 5; }
	if($line =~ /<h5>Writers?/) { $atList = 10; }
	if($line =~ /<h5>Release Date/) { $atList = 15; }
	if($line =~ /<h5>Genre/) { $atList = 20; }
	if($line =~ /<h5>Plot:/) { $atList = 25; }
	if($line =~ /<h5><a href="\/mpaa">MPAA<\/a>:<\/h5>/) { $atList = 30; }
	if($line =~ /<h3>Fun Stuff<\/h3>/) { last; }

	# Poster 
	if( $line =~ /name="poster" .* src="(.*)" ?\/>/)
	{
		$film_poster_link = $1;
	}

	# Title
	if( $line =~ /h1>(.*) <span>/)
	{
		$film_name = $1;
		$film_name =~ s/&#039;/\\'/g;
		$film_name =~ s/&#034;/"/g;
		$film_name =~ s/&amp;/&/g;
	}
	
	# Directed By
	if($atList == 5)
	{
		my $done = 0;
		while($done == 0)
		{
			$line = <INFILE>;
			$line =~ s/^\s*//;
			$line =~ s/\s*$//;
			if($line =~ /<\/div>/) 
			{ 
				$done = 1; 
			}
			elsif($line =~ /<a href=".*">(.*)<\/a>/)
			{
				$film_director .= $1 . ", ";
			}
		}
		$atList = 6;
	}
	
	# Written By
	if($atList == 10)
	{
		my $done = 0;
		while(!$done)
		{
			$line = <INFILE>;
			$line =~ s/^\s*//;
			$line =~ s/\s*$//;
			if($line =~ /<\/div>/) { $done = 1; }
			elsif($line =~ /<a href=".*">(.*)<\/a>/)
			{
				$film_writers .= $1 . ", ";
			}
		}
		$atList = 11;
	}

	# Release Date
	if($atList == 15 && $line =~ /(.*) \(USA\)/)
	{
		$film_release_date = $1;
	}
	
	# Genre	
	if($atList == 20 && $line =~ /href/)
	{
		$line =~ s/<a class.*$//;
		$line =~ s/<a[^>]*>//g;
		$line =~ s/<\/a>/|/g;
		@genres = split(/\|/, $line);
		foreach(@genres)
		{
			if($_ !~ /^ *$/) 
			{ 
				$_ =~ s/^ *<a.*>//;
				$_ =~ s/<\/a> *$//;
				$film_genres .= $_ . ", ";
			}
		}
		$atList = 21;
	}
	
	# Summary
	if($atList == 25 && $line !~ /<h5>/)
	{
		$film_summary = $line;
		$film_summary =~ s/\|.*$//;
		$film_summary =~ s/<a[^>]*>full summary<\/a> *$//;
		$atList = 26;
	}

	# Rating and rating explination
	if($atList == 30 && $line =~ /Rated ([^ ]*) (.*)/)
	{

		$film_rating = $1;
		$film_rating_explanation = $2;
		
		$film_rating_explanation =~ s/. *$//;

		if($film_rating eq "Not" && $film_rating_explanation eq "Rated")
		{
			$film_rating = "Not Rated";
			$film_rating_explanation = "";
		}
		$atList = 31;

	}

}

close(INFILE);

$film_name =~ s/'/\\'/g;
$film_name =~ s/&#x26;/&/g;
$film_name =~ s/&#x27;/\\'/g;
$film_name =~ s/&#xFC;/u/g;
$film_name =~ s/&#039;/\\'/g;
$film_name =~ s/&#034;/"/g;

$film_summary =~ s/'/\\'/g;
$film_summary =~ s/&#x26;/&/g;
$film_summary =~ s/&#xFC;/u/g;
$film_summary =~ s/&#039;/\\'/g;
$film_summary =~ s/&#034;/"/g;
$film_summary =~ s/&#x27;/\\'/g;

$film_director =~ s/\s+/ /g;
$film_director =~ s/^\s*[ ,]//;
$film_director =~ s/,\s*,/,/;
$film_director =~ s/,\s*,/,/;
$film_director =~ s/,\s*,/,/;
$film_director =~ s/,\s*,/,/;
$film_director =~ s/,\s*,/,/;
$film_director =~ s/\s*[ ,]*$//;
$film_director =~ s/'/\\'/g;
$film_director =~ s/&#039;/\\'/g;
$film_director =~ s/&#034;/"/g;
$film_director =~ s/^\s*//g;

$film_writers =~ s/,\s*,/,/;
$film_writers =~ s/,\s*,/,/;
$film_writers =~ s/,\s*,/,/;
$film_writers =~ s/,\s*,/,/;
$film_writers =~ s/\s+/ /g;
$film_writers =~ s/^\s*[ ,]//;
$film_writers =~ s/\s*[ ,]*$//;
$film_writers =~ s/'/\\'/g;
$film_writers =~ s/&#039;/\\'/g;
$film_writers =~ s/&#034;/"/g;
$film_writers =~ s/^\s*//g;

$film_genres =~ s/\s+/ /g;
$film_genres =~ s/'/\\'/g;
$film_genres =~ s/&/\\&/g;
$film_genres =~ s/,\s*,/,/;
$film_genres =~ s/,\s*,/,/;
$film_genres =~ s/,\s*,/,/;
$film_genres =~ s/,\s*,/,/;
$film_genres =~ s/^\s*//g;
$film_genres =~ s/\s*[ ,]*$//g;
$film_genres =~ s/&#039;/\\'/g;
$film_genres =~ s/&#034;/"/g;

print $film_name . "\n";
print $film_summary . "\n";
print $film_release_date . "\n";
print $film_rating . "\n";
print $film_rating_explanation . "\n";
print $film_genres . "\n";
print $film_writers . "\n";
print $film_director . "\n";
print $film_poster_link . "\n";
