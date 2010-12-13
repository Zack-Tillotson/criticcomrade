#!/usr/bin/perl
# This program is called with 1 input - the input file
# this input file is http://www.imdb.com/chart/
# the output is the movie rankings in this format:
# 	Chart date
# 	Movie title
# 	Movie release year
# 	Movie ranking
# 	Movie link
# 	etc...


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
	print "Usage: 	perl parse_imdb_top10.pl <file name>\n";
	print "		file name = name of input file name which is http://www.imdb.com/chart/"
}

open(INFILE, $file) or die("Error: could not open input file - $file");

my $chart_date;
my $film_name;
my $film_release_year;
my $film_ranking;
my $film_link;

$atList = 0;
while($line = <INFILE>)
{


	chomp($line);

	if($line =~ /<div id="boxoffice" class="chart_meta">/) { $atList = 5; }
	if($line =~ /<td class="chart_(even|odd)_row" style="text-align: right">/) { $atList = 10; }
	if($atList == 11 && $line =~ /<\/table>/) { last; }

	# The week this is for
	if( $atList == 5 && $line =~ />week of ([^<]*)<\/span>/)
	{

		$chart_date = $1;
		print $chart_date . "\n";

		$line = 6;

	}

	# Title
	if( $atList == 10 )
	{

		# We're at a row of the chart, the format is
		# 	rank
		# 	...
		# 	...
		# 	...
		# 	...
		# 	...
		# 	title (year)
		$line = <INFILE>; 
		$line =~ s/(^\s*|\s*$)//g;

		$film_ranking = $line;
		$film_ranking =~ s/<\/?b>//g;

		for($i = 0 ; $i < 6 ; $i++) { $line = <INFILE>; }
		$line =~ s/(^\s*|\s*$)//g;
		
		$line =~ /<a href="([^"]*)">([^<]*)<\/a> \((....)\)/;

		$film_link = "http://www.imdb.com" . $1;
		$film_name = $2;
		$film_name =~ s/&#039;/\\'/g;
		$film_name =~ s/&#034;/"/g;
		$film_name =~ s/&#xFC;/u/g;
		$film_name =~ s/&#x26;/&/g;
		$film_name =~ s/&#x27;/'/g;
		$film_name =~ s/&amp;/&/g;
		
		$film_release_year = $3;
		if($film_release_year =~ /^ *$/) 
		{
			($second, $minute, $hour, $dayOfMonth, $month, $yearOffset, $dayOfWeek, $dayOfYear, $daylightSavings) = localtime();
			$film_release_year = 1900+$yearOffset;
		}

		
		print $film_name . "\n";
		print $film_release_year . "\n";
		print $film_link . "\n";
		print $film_ranking . "\n";

		$atList = 11;
		
		for($i = 0 ; $i < 9 ; $i++) { $line = <INFILE>; }

	}

}

close(INFILE);
