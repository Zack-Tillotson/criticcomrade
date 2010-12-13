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

open(INFILE, $file) or die("Error: could not open input file - $file");

my $film_name;
my $film_release_date;
my $film_summary;

my $review_score;
my $review_publisher;
my $review_name;
my $review_summary;
my $review_link;

$atList = 0;
while($line = <INFILE>)
{

	chomp($line);
	
	if($atList == 0 && $line =~ /h1>(.*)<br/)
	{
		$film_name = $1;
		$atList = 1;
	}

	if($atList == 1 && $line =~ /subhead">(.*)</)
	{
		$atList = 2;
	}

	if($atList == 2 && $line =~ /<p>Starring/)
	{
		$atList = 3;
	}
	
	if($atList == 3.5 && $line =~ /<p>(.*)(<\/p>)?/)
	{
		$atList = 4;
		$film_summary = $1;
		$film_summary =~ s/&#039;/\\'/g;
	}
	
	if($atList == 3 && $line =~ /<p>/)
	{
		$atList = 3.5
	}

	if($atList == 4 && $line =~ /Theatrical: <b>(.*)</)
	{
		$atList = 5;
		$film_release_date = $1;
	}

	if($atList == 5 && $line =~ /All critic scores/)
	{
		$atList = 6;
		print $film_name . "\n";
		print $film_summary . "\n";
		print $film_release_date . "\n";
	}

	if($atList == 6 && $line =~ /criticscore">(.*)<\/div>/)
	{
		$atList = 7;
		$review_score = $1;
	}

	if($atList == 7 && $line =~ /publication">(.*)<\/span>/)
	{
		$atList = 8;
		$review_publisher = $1;
	}

	if($atList == 8 && $line =~ /criticname">(.*)<\/span>/)
	{
		$atList = 9;
		$review_name = $1;
	}

	if($atList == 9 && $line =~ /quote">(.*)$/)
	{
		$atList = 10;
		$review_summary = $1;
		$review_summary =~ s/'/\\'/g;
	}

	if($atList == 10 && $line =~ /href="(.*)" TARGET/)
	{
		$atList = 6;
		$review_link = $1;
		print $review_name . "\n";
		print $review_publisher . "\n";
		print $review_summary . "\n";
		print $review_link . "\n";
		print $review_score . "\n";
	}
	
}

close(INFILE);
