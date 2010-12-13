#!/usr/bin/perl
# This program is called with 1 input - the input file
# this input file is http://www.rottentomatoes.com/movies/in_theaters.php?sortby=title&mode=simple&order=ASC&
# the output is a list of html links for all of the pages that have movie links on them


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
	print "Usage: 	perl parse_rottentomatoes_in_theaters.pl <file name>\n";
	print "		file name = name of input file name which is http://www.rottentomatoes.com/movies/in_theaters.php?sortby=title&mode=simple&order=ASC&\n";
}

open(INFILE, $file) or die("Error: could not open input file - $file");

$atList = 0;
while($line = <INFILE>)
{

	chomp($line);
	
	if($atList == 0 && $line =~ /\?sortby\=title\&mode\=simple\&order\=ASC&\=\&page\=(.?.?)&?\" style\=\"display\:none\;\"\>\&gt\;\|/)
	{
		$atList = 1;
		for($i = 1; $i <= $1 ; $i++)
		{
			print "http://www.rottentomatoes.com/movies/in_theaters.php?sortby=title&mode=simple&order=ASC&=&page=" . $i . "\n";
		}
	}

}

close(INFILE);
