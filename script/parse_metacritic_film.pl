#!/usr/bin/perl
# This program is called with 1 input - the input file
# this input file is like http://www.metacritic.com/film/titles/<movie name>
# the output is the movie information in this format:
# 	Movie title
# 	Movie Summary
# 	Movie release date
# 	Critic name
# 	Critic publisher
# 	Critic review summary
# 	Critic review link
# 	Critic review score
# 	<repeat critic info until end>


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
	print "Usage: 	perl parse_metacritic_film.pl <file name>\n";
	print "		file name = name of input file name which is http://www.metacritic.com/film/titles/<movie name>\n";
}

sub commentSectionLike
{
	my $comment = $_[0];
	my $section = $_[1];
	return $comment =~ /^.*<!-- $section.*-->.*$/;
}

open(INFILE, $file) or die("Error: could not open input file - $file");

my $film_name;
my $film_release_date;
my $film_summary;
my $film_prod_co;
my $film_rating;
my $film_rating_explanation;
my $film_starring;
my $film_genres;
my $film_writers;
my $film_director;

my $review_score;
my $review_publisher;
my $review_name;
my $review_summary;
my $review_link;

$atList = 0;
while($line = <INFILE>)
{


	chomp($line);

	if(commentSectionLike($line, "title and studio")) { $atList = 5; }
	if(commentSectionLike($line, "info left column")) { $atList = 15; }
	if(commentSectionLike($line, "theatrical")) { $atList = 17; }
	if(commentSectionLike($line, "info right column")) { $atList = 20; }
	if(commentSectionLike($line, "mpaa rating")) { $atList = 25; }
	if(commentSectionLike($line, "actors")) { $atList = 30; }
	if(commentSectionLike($line, "plot summary")) { $atList = 35; }
	if(commentSectionLike($line, "critic reviews")) { $atList = 40; }
	if(commentSectionLike($line, "critic score")) { $atList = 45; }
	if(commentSectionLike($line, "publication")) { $atList = 50; }
	if(commentSectionLike($line, "quote")) { $atList = 55; }
	if(commentSectionLike($line, "link")) { $atList = 60; }

	# Title
	if( $atList == 5 && $line =~ /h1>(.*)<\/h1/)
	{
		$film_name = $1;
		$film_name =~ s/&#039;/\\'/g;
		$film_name =~ s/&#034;/"/g;
		$film_name =~ s/&amp;/&/g;
		$film_name =~ s/^(.*), A$/A $1/g;
		$film_name =~ s/^(.*), The$/The $1/g;
		$film_name =~ s/^\s*//g;
	}
	
	# Production Company
	if($atList == 5 && $line =~ /PRINT<\/a>(.*)<\/p>/)
	{
		$film_prod_co = $1;
		$film_prod_co =~ s/ \(.*\).*$//;
	}

	# Genre	
	if($atList == 15 && $line =~ /Genre/)
	{
		my $done = 0;
		while($done == 0)
		{
			$line = <INFILE>;
			$line =~ s/^\s*//;
			$line =~ s/\s*$//;
			if($line =~ /<\/p>/) 
			{ 
				$done = 1; 
			}
			elsif($line !~ /nbsp;/)
			{
				$film_genres .= $line . ", ";
			}
		}
	}

	# Written By
	if($atList == 15 && $line =~ /Written by/)
	{
		my $done = 0;
		while(!$done)
		{
			$line = <INFILE>;
			$line =~ s/^\s*//;
			$line =~ s/\s*$//;
			if($line =~ /<\/p>/) { $done = 1; }
			elsif($line !~ /<br \/>/ && $line !~ /\(.*\)/)
			{
				$film_writers .= $line . ", ";
			}
		}
	}

	
	# Directed By
	if($atList == 15 && $line =~ /Directed by/)
	{
		my $done = 0;
		while(!$done)
		{
			$line = <INFILE>;
			$line =~ s/^\s*//;
			$line =~ s/\s*$//;
			if($line =~ /<\/p>/) { $done = 1; }
			elsif($line !~ /<br \/>/)
			{
				$film_director .= $line . ", ";
			}
		}
	}
	
	# Release Date
	if($atList == 17 && $line =~ /Theatrical: (.*)/)
	{
		$film_release_date = $1;
	}
	
	# Rating and rating explination
	if($atList == 25 && $line =~ /RATING:<\/strong> ([^ ]*)(.*)<\/p>/)
	{

		$film_rating = $1;
		$film_rating_explanation = $2;

		$film_rating_explanation =~ s/^ *//;

		if($film_rating eq "Not" && $film_rating_explanation eq "Rated")
		{
			$film_rating = "Not Rated";
			$film_rating_explanation = "";
		}

	}

	# Stars	
	if($atList == 30)
	{
		my $done = 0;
		while(!$done)
		{
			$line = <INFILE>;
			$line =~ s/^\s*//;
			$line =~ s/\s*$//;
			if($line =~ /<\/p>/) { $done = 1; }
			elsif($line !~ /<p>Starring/ && $line !~ /and/ && $line !~ /^$/)
			{
				$film_starring .= $line . " ";

			}
		}
		$atList = 31;
	}

	# Summary
	if($atList == 35 && $line =~ /summarytext">(.*)<\/p>/)
	{
		$film_summary = $1;
	}

	# We're done getting the movie info	
	if($atList == 40)
	{

		$film_starring =~ s/'/\\'/g;
		$film_starring =~ s/&#039;/\\'/g;
		$film_starring =~ s/&#034;/"/g;

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
		print $film_prod_co . "\n";
		print $film_rating . "\n";
		print $film_rating_explanation . "\n";
		print $film_starring . "\n";
		print $film_genres . "\n";
		print $film_writers . "\n";
		print $film_director . "\n";

		$atList = 41;

		$review_score = "";
		$review_publisher = "";
		$review_name = "";
		$review_summary = "";
		$review_link = "";

	}

	if($atList == 45 && $line =~ /<div class=".*">(.*)<\/div>/)
	{
		$review_score = $1;
	}

	if($atList == 50 && $line =~ /publication">(.*)/)
	{
		$review_publisher = $1;
		$review_publisher =~ s/'/\\'/g;
		$review_publisher =~ s/&#039;/\\'/g;
		$review_publisher =~ s/&#034;/"/g;
	}

	if($atList == 50 && $line =~ /criticname">(.*)<\/span>/)
	{
		$review_name = $1;
		$review_name =~ s/'/\\'/g;
		$review_name =~ s/&#039;/\\'/g;
		$review_name =~ s/&#034;/"/g;
		if($review_name =~ /Staff \(Not credited\)/) { $review_name = "" }
	}

	if($atList == 55 && $line =~ /quote">(.*)<\/p>/)
	{
		$review_summary = $1;
		$review_summary =~ s/'/\\'/g;
		$review_summary =~ s/&#039;/\\'/g;
		$review_summary =~ s/&#034;/"/g;
	}

	if($atList == 60 && $line =~ /href="(.*)" target/)
	{

		$review_link = $1;

		print $review_name . "\n";
		print $review_publisher . "\n";
		print $review_summary . "\n";
		print $review_link . "\n";
		print $review_score . "\n";

	}
	
}

close(INFILE);
