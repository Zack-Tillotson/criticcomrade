#!/usr/bin/perl
# This program is called with 1 input - the input file
# this input file is http://www.rottentomatoes.com/movies/in_theaters.php?sortby=title&mode=simple&order=ASC&=&page=<number>
# the output is a list of movie links


if(@ARGV == 0)
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
	print "Usage: 	perl parse_rottentomatoes_movies.pl <file name>\n";
	print "		file name = name of input file name which is http://www.rottentomatoes.com/movies/in_theaters.php?sortby=title&mode=simple&order=ASC&=&page=<number>\n";
}

open(INFILE, $file) or die("Error: could not open input file - $file");

$atList = 0;
while($line = <INFILE>)
{

	chomp($line);
	
	if($atList == 0 && $line =~ /<h1 class="movie_title" style="font-size:14pt;margin:0px;"><a href='(.*)' >.*<\/a> <\/h1>/)
	{

		print "http://www.rottentomatoes.com" . $1 . "?page=1&critic=columns&sortby=date&name_order=asc&view=text#contentReviews\n";

	}
	
}

close(INFILE);
